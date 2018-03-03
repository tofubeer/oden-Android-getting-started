package net.opendatadev.odensample;


import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.liquidplayer.javascript.JSContext;
import org.liquidplayer.javascript.JSException;
import org.liquidplayer.javascript.JSValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static net.opendatadev.odensample.ManifestEntry.Download;
import static net.opendatadev.odensample.ManifestEntry.Download.Extract;

public class ManifestDownloader
{
    private final Set<ManifestDownloaderListener> listeners;
    private final File rootFolder;
    private int toDownloadCount;
    private AtomicInteger downloadedCount;
    private boolean allFilesAdded;

    {
        listeners = new CopyOnWriteArraySet<>();
    }

    public ManifestDownloader(@NonNull final File folder)
    {
        rootFolder = folder;
    }

    public void addManifestDownloaderListener(@NonNull final ManifestDownloaderListener listener)
    {
        listeners.add(listener);
    }

    public void removeManifestDownloaderListener(@NonNull final ManifestDownloaderListener listener)
    {
        listeners.remove(listener);
    }

    public void download(final int resId,
                         @NonNull final Resources resources,
                         final boolean overwrite)
        throws
        IOException
    {
        final ManifestEntry[] entries;

        entries = Manifest.getManifestEntries(resId,
                                              resources);
        download(entries,
                 overwrite);
    }

    public void download(@NonNull final ManifestEntry entry,
                         final boolean overwrite)
    {
        download(new ManifestEntry[]{entry},
                 overwrite);
    }

    public void download(@NonNull final ManifestEntry[] entries,
                         final boolean overwrite)
    {
        toDownloadCount = 0;
        downloadedCount = new AtomicInteger(0);
        allFilesAdded = false;

        for(@NonNull final ManifestEntry entry : entries)
        {
            try
            {
                downloadEntry(entry,
                              overwrite,
                              () -> convert(entries));
            }
            catch(final IOException ex)
            {
                for(final ManifestDownloaderListener listener : listeners)
                {
                    listener.downloadError(entry,
                                           ex);
                }
            }
        }

        allFilesAdded = true;

        // nothing downloaded
        if(toDownloadCount == 0)
        {
            for(final ManifestDownloaderListener listener : listeners)
            {
                listener.conversionCompleted(entries);
            }
        }
    }

    private void downloadEntry(@NonNull final ManifestEntry entry,
                               final boolean overwrite,
                               @NonNull final Runnable callback)
        throws
        IOException
    {
        final File localityFolder;

        localityFolder = Manifest.getLocalFolderFor(entry,
                                                    rootFolder);

        if(overwrite)
        {
            FileUtils.deleteFolder(localityFolder);
        }

        FileUtils.createFolder(localityFolder);
        downloadProvider(overwrite,
                         localityFolder,
                         entry,
                         callback);
    }

    private void downloadProvider(final boolean overwrite,
                                  @NonNull final File localityFolder,
                                  @NonNull final ManifestEntry entry,
                                  @NonNull final Runnable callback)
        throws
        IOException
    {
        final File providerFolder;
        final String provider;
        final Download[] downloads;

        provider = entry.getProvider();
        providerFolder = new File(localityFolder,
                                  provider);
        FileUtils.createFolder(providerFolder);

        if(entry.getConverter() != null)
        {
            final String converter;

            converter = entry.getConverter();
            downloadConverter(converter,
                              entry,
                              providerFolder,
                              overwrite,
                              callback);
        }

        downloads = entry.getDownloads();

        for(int i = 0; i < downloads.length; i++)
        {
            final File downloadFolder;
            final Download download;
            final String path;
            final String src;
            final Extract[] extract;

            path = Integer.toString(i);
            downloadFolder = new File(providerFolder,
                                      path);
            FileUtils.createFolder(downloadFolder);
            download = entry.getDownloads()[i];
            src = download.getSrc();
            extract = download.getExtract();
            downloadDataset(src,
                            entry,
                            downloadFolder,
                            overwrite,
                            extract,
                            callback);
        }
    }

    private void downloadConverter(@NonNull final String url,
                                   @NonNull final ManifestEntry entry,
                                   @NonNull final File toFolder,
                                   final boolean overwrite,
                                   @NonNull final Runnable callback)
    {
        final File localConverterFile;

        localConverterFile = new File(toFolder,
                                      "converter.js");
        download(url,
                 entry,
                 localConverterFile,
                 overwrite,
                 callback);
    }

    private void downloadDataset(@NonNull final String url,
                                 @NonNull final ManifestEntry entry,
                                 @NonNull final File localFolder,
                                 final boolean overwrite,
                                 @Nullable final Extract[] extract,
                                 @NonNull final Runnable callback)
    {
        final File localConverterFile;

        localConverterFile = new File(localFolder,
                                      "dataset");

        if(extract == null)
        {
            download(url,
                     entry,
                     localConverterFile,
                     overwrite,
                     callback);
        }
        else
        {
            downloadAndExtract(url,
                               entry,
                               localConverterFile,
                               overwrite,
                               localFolder,
                               extract,
                               callback);
        }
    }

    private void download(@NonNull final String url,
                          @NonNull final ManifestEntry entry,
                          @NonNull final File localFile,
                          final boolean overwrite,
                          @NonNull final Runnable downloadComplete)
    {
        if(shouldDownload(localFile,
                          overwrite))
        {
            addFile();

            Downloader.download(url,
                                localFile,
                                (error) ->
                                {
                                    if(error != null)
                                    {
                                        for(final ManifestDownloaderListener listener : listeners)
                                        {
                                            listener.downloadError(entry,
                                                                   url,
                                                                   error);
                                        }
                                    }

                                    fileCompleted(downloadComplete);
                                });
        }
    }

    private void downloadAndExtract(@NonNull final String url,
                                    @NonNull final ManifestEntry entry,
                                    @NonNull final File localFile,
                                    final boolean overwrite,
                                    @NonNull final File localFolder,
                                    @NonNull final Extract[] extract,
                                    @NonNull final Runnable downloadComplete)
    {
        if(shouldDownload(localFile,
                          overwrite))
        {
            addFile();

            Downloader.download(url,
                                localFile,
                                (error) ->
                                {
                                    if(error != null)
                                    {
                                        for(final ManifestDownloaderListener listener : listeners)
                                        {
                                            listener.downloadError(entry,
                                                                   url,
                                                                   error);
                                        }
                                    }
                                    else
                                    {
                                        try
                                        {
                                            extractEntries(extract,
                                                           localFile,
                                                           localFolder);
                                        }
                                        catch(final IOException ex)
                                        {
                                            for(final ManifestDownloaderListener listener : listeners)
                                            {
                                                listener.unarchiveError(entry,
                                                                        localFile,
                                                                        ex);
                                            }
                                        }
                                    }

                                    fileCompleted(downloadComplete);
                                });
        }
    }

    private void extractEntries(@NonNull final Extract[] extractEntries,
                                @NonNull final File archiveFile,
                                @NonNull final File localFolder)
        throws
        IOException
    {
        try(final ZipInputStream zipStream = new ZipInputStream(new FileInputStream(archiveFile)))
        {
            ZipEntry zipEntry;

            while((zipEntry = zipStream.getNextEntry()) != null)
            {
                final String zipEntryName;

                zipEntryName = zipEntry.getName();

                for(final Extract extractEntry : extractEntries)
                {
                    final String src;

                    src = extractEntry.getSrc();

                    if(src.equals(zipEntryName))
                    {
                        final File destinationFile;
                        final String path;

                        path = extractEntry.getDst();
                        destinationFile = new File(localFolder,
                                                   path);
                        FileUtils.copyFile(zipStream,
                                           destinationFile);
                    }
                }
            }
        }
    }

    private void convert(final ManifestEntry[] entries)
    {
        for(final ManifestEntry entry : entries)
        {
            convert(entry);
        }

        for(final ManifestDownloaderListener listener : listeners)
        {
            listener.conversionCompleted(entries);
        }
    }

    private void convert(final ManifestEntry entry)
    {
        final File cityFolder;
        final File providerFolder;
        final File converterFile;
        final File convertedFile;
        final String provider;

        cityFolder = Manifest.getLocalFolderFor(entry,
                                                rootFolder);
        provider = entry.getProvider();
        providerFolder = new File(cityFolder,
                                  provider);
        converterFile = new File(providerFolder,
                                 "converter.js");
        convertedFile = new File(cityFolder,
                                 provider + ".json");

        try
        {
            // no schema - no way to convert
            if(entry.getSchema() == null)
            {
                // how do we deal with this?
            }
            else
            {
                // yes schema, no converter - need to convert, proper format already
                if(entry.getConverter() == null)
                {
                    // for this to be the case we can only have a single data file, so it must be in 0
                    final File datasetFile = new File(providerFolder,
                                                      "0/dataset");

                    FileUtils.copyFile(datasetFile,
                                       convertedFile);
                }
                else
                {
                    runConversion(converterFile,
                                  convertedFile,
                                  entry,
                                  providerFolder);
                }
            }

            for(final ManifestDownloaderListener listener : listeners)
            {
                listener.converted(entry,
                                   convertedFile);
            }
        }
        catch(final IOException | JSException ex)
        {
            for(final ManifestDownloaderListener listener : listeners)
            {
                listener.conversionError(entry,
                                         convertedFile,
                                         ex);
            }
        }
    }

    private void runConversion(@NonNull final File converterFile,
                               @NonNull final File convertedFile,
                               @NonNull final ManifestEntry entry,
                               @NonNull final File providerFolder)
        throws
        IOException
    {
        final String converterJS;
        final List<String> datasets;
        final JSContext context;
        final JSValue result;
        final StringBuilder builder;
        final Download[] downloads;

        converterJS = FileUtils.readTextFile(converterFile);
        datasets = new ArrayList<>();
        downloads = entry.getDownloads();

        for(int i = 0; i < downloads.length; i++)
        {
            final Download download;
            final File downloadFolder;
            final String path;

            download = entry.getDownloads()[i];
            path = Integer.toString(i);
            downloadFolder = new File(providerFolder,
                                      path);

            if(download.getExtract() == null)
            {
                final File datasetFile;
                final String dataset;

                datasetFile = new File(downloadFolder,
                                       "dataset");
                dataset = FileUtils.readTextFile(datasetFile);
                datasets.add(dataset);
            }
            else
            {
                final Extract[] extract;

                extract = download.getExtract();

                for(int j = 0; j < extract.length; j++)
                {
                    final Extract extractEntry;
                    final File datasetFile;
                    final String dataset;
                    final String dstPath;

                    extractEntry = download.getExtract()[j];
                    dstPath = extractEntry.getDst();
                    datasetFile = new File(downloadFolder,
                                           dstPath);
                    dataset = FileUtils.readTextFile(datasetFile);
                    datasets.add(dataset);
                }
            }
        }

        builder = new StringBuilder();

        for(final String dataset : datasets)
        {
            builder.append(dataset);
            builder.append(",");
        }

        builder.setLength(builder.length() - 1);

        context = new JSContext();
        context.evaluateScript("var module = {}");
        context.evaluateScript(converterJS);
        context.evaluateScript("var result = convert(" + builder.toString() + ");");
        result = context.property("result");
        FileUtils.writeTextFile(result.toString(),
                                convertedFile);
    }

    private void addFile()
    {
        toDownloadCount++;
    }

    private void fileCompleted(@NonNull final Runnable downloadComplete)
    {
        final int count;

        count = downloadedCount.incrementAndGet();

        if(allFilesAdded)
        {
            if(count == toDownloadCount)
            {
                downloadComplete.run();
            }
        }
    }

    private boolean shouldDownload(@NonNull final File file,
                                   final boolean overwrite)
    {
        final boolean retVal;

        if(overwrite)
        {
            retVal = true;
        }
        else if(file.exists())
        {
            retVal = false;
        }
        else
        {
            retVal = true;
        }

        return retVal;
    }
}
