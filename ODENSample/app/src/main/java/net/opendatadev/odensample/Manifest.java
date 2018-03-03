package net.opendatadev.odensample;


import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;


public final class Manifest
{
    private Manifest()
    {
    }

    public static ManifestEntry[] getManifestEntries(final int resourceId, @NonNull final Resources resources)
        throws IOException
    {
        final Gson gson;
        final InputStream resourceStream;

        gson = new Gson();
        resourceStream = resources.openRawResource(resourceId);

        try(final Reader reader = new InputStreamReader(resourceStream))
        {
            final ManifestEntry[] entries;

            entries = gson.fromJson(reader,
                                    ManifestEntry[].class);

            return (entries);
        }
    }

    public static ManifestEntry getManifestEntry(@NonNull final String id, @NonNull final String fileName)
    {
        throw new IllegalStateException("getManifestEntry");
    }

    @NonNull
    public static File getLocalFolderFor(@NonNull final ManifestEntry entry, @NonNull final File rootFolder)
    {
        // Public Art/CA
        File folder;
        final String datasetName;
        final String countryName;

        datasetName = entry.getDatasetName();
        countryName = entry.getCountry();

        folder = new File(rootFolder,
                          datasetName);

        if(countryName != null)
        {
            final String province;

            folder = new File(folder,
                              countryName);
            province = entry.getProvince();

            if(province != null)
            {
                final String region;
                final String city;

                // Public Art/CA/BC
                folder = new File(folder,
                                  province);
                region = entry.getRegion();

                if(region != null)
                {
                    // Public Art/CA/BC/Metro Vancouver
                    folder = new File(folder,
                                      region);
                }

                city = entry.getCity();

                // a city doesn't have to be in a region
                if(city != null)
                {
                    // Public Art/CA/BC/Metro Vancouver/New Westminster
                    // or Public Art/CA/BC/Lund
                    folder = new File(folder,
                                      city);
                }
            }
        }

        return folder;
    }


    @NonNull
    public static File getLocalDatasetFileFor(@NonNull final ManifestEntry entry, @NonNull final File rootFolder)
    {
        final File localFolderFolder;
        final File providerFile;
        final String provider;

        localFolderFolder = getLocalFolderFor(entry, rootFolder);
        provider = entry.getProvider();
        providerFile     = new File(localFolderFolder, provider + ".json");

        return (providerFile);
    }

    @NonNull
    public static File[] getLocalDatasetFilesFor(@NonNull final ManifestEntry[] entries, @NonNull final File rootFolder)
    {
        final List<File> datasetFilesList;
        final int fileCount;

        datasetFilesList = new ArrayList<>();

        for(final ManifestEntry entry : entries)
        {
            final File providerDatasetFile;

            providerDatasetFile = getLocalDatasetFileFor(entry, rootFolder);
            datasetFilesList.add(providerDatasetFile);
        }

        fileCount = datasetFilesList.size();

        return datasetFilesList.toArray(new File[fileCount]);
    }
}
