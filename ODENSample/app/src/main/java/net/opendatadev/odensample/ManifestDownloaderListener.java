package net.opendatadev.odensample;


import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import java.io.File;

public interface ManifestDownloaderListener
{
    @NonNull
    void converted(@NonNull ManifestEntry entry, @NonNull File localFile);

    @MainThread
    void conversionCompleted(@NonNull ManifestEntry[] entries);

    @MainThread
    void downloadError(@NonNull ManifestEntry entry, @NonNull Throwable error);

    @MainThread
    void downloadError(@NonNull ManifestEntry entry, @NonNull String url, @NonNull Throwable error);

    @MainThread
    void unarchiveError(@NonNull ManifestEntry entry, @NonNull File localFile, @NonNull Throwable error);

    @MainThread
    void conversionError(@NonNull ManifestEntry entry, @NonNull File localFile, @NonNull Throwable error);
}
