package net.opendatadev.odensample;

import android.os.Build;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileUtils
{
    private FileUtils()
    {
    }

    public static void copyFile(@NonNull final File inputFile,
                                @NonNull final File destinationFile)
        throws
        FileNotFoundException,
        IOException
    {
        try(final InputStream inputStream = new FileInputStream(inputFile))
        {
            copyFile(inputStream,
                     destinationFile);
        }
    }

    public static void copyFile(@NonNull final InputStream inputStream,
                                @NonNull final File destinationFile)
        throws
        IOException
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            final Path path;

            path = destinationFile.toPath();
            Files.copy(inputStream,
                       path);
        }
        else
        {
            try(final OutputStream outputStream = new FileOutputStream(destinationFile))
            {
                final byte[] buffer;
                int length;

                buffer = new byte[1024];

                while((length = inputStream.read(buffer)) > 0)
                {
                    outputStream.write(buffer,
                                       0,
                                       length);
                }
            }
        }
    }

    public static String readTextFile(@NonNull final File inputFile)
        throws
        IOException
    {
        final String retVal;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            final byte[] bytes;
            final Path path;

            path = inputFile.toPath();
            bytes = Files.readAllBytes(path);

            retVal = new String(bytes,
                                StandardCharsets.UTF_8);
        }
        else
        {
            final StringBuilder builder;

            builder = new StringBuilder();

            try(final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile))))
            {
                String line;

                while((line = reader.readLine()) != null)
                {
                    builder.append(line);
                }
            }

            retVal = builder.toString();
        }

        return (retVal);
    }

    public static void writeTextFile(@NonNull final String text,
                                     @NonNull final File outputFile)
        throws
        IOException
    {
        try(final FileWriter writer = new FileWriter(outputFile))
        {
            writer.write(text);
        }
    }

    public static void deleteFolder(@NonNull final File folder)
        throws
        IOException
    {
        if(folder.exists())
        {
            if(!(folder.isDirectory()))
            {
                final String path;

                path = folder.getAbsolutePath();
                throw new IOException(path + " is not a directory");
            }

            deleteFolderRecursively(folder);
        }
    }

    private static void deleteFolderRecursively(@NonNull final File folder)
        throws
        IOException
    {
        final File[] entries;
        boolean deleted;

        entries = folder.listFiles();

        if(entries != null)
        {
            for(final File entry : entries)
            {
                if(entry.isDirectory())
                {
                    deleteFolderRecursively(entry);
                }
                else
                {
                    deleted = entry.delete();

                    if(!(deleted))
                    {
                        final String path;

                        path = folder.getAbsolutePath();
                        throw new IOException("Could not delete: " + path);
                    }
                }
            }
        }

        deleted = folder.delete();

        if(!(deleted))
        {
            final String path;

            path = folder.getAbsolutePath();
            throw new IOException("Could not delete: " + path);
        }
    }

    public static void createFolder(@NonNull final File folder)
        throws
        IOException
    {
        if(!(folder.exists()))
        {
            final boolean created;

            created = folder.mkdirs();

            if(!(created))
            {
                final String path;

                path = folder.getAbsolutePath();
                throw new IOException("Could not create: " + path);
            }
        }
    }
}
