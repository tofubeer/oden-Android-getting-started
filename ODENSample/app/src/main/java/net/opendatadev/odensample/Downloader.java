package net.opendatadev.odensample;


import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import java9.util.function.Consumer;

public final class Downloader
    extends AsyncTask<String, Void, Throwable>
{
    @NonNull
    private final File destinationFile;

    @NonNull
    private final Consumer<Throwable> callback;

    private Downloader(@NonNull final File destination,
                       @NonNull final Consumer<Throwable> closure)
    {
        destinationFile = destination;
        callback = closure;
    }

    public static void download(@NonNull final String url,
                                @NonNull final File destination,
                                @NonNull final Consumer<Throwable> closure)
    {
        final Downloader downloader;
        final String path;

        downloader = new Downloader(destination,
                                    closure);
        path = destination.getAbsolutePath();
        downloader.execute(url,
                           path);
    }

    protected Throwable doInBackground(@NonNull final String... urls)
    {
        try
        {
            final URL url;
            final URLConnection connection;

            url = new URL(urls[0]);
            connection = url.openConnection();

            try(final InputStream inputStream = connection.getInputStream())
            {
                FileUtils.copyFile(inputStream,
                                   destinationFile);
            }
        }
        catch(final IOException ex)
        {
            return ex;
        }

        return null;
    }

    protected void onPostExecute(@NonNull final Throwable ex)
    {
        callback.accept(ex);
    }
}