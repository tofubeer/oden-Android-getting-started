package net.opendatadev.odensample;


import android.content.ContextWrapper;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.opendatadev.odensample.PublicArt.Feature;
import static net.opendatadev.odensample.PublicArt.Feature.Geometry;
import static net.opendatadev.odensample.PublicArt.Feature.Properties;


public class MainActivity
    extends FragmentActivity
    implements ManifestDownloaderListener,
    OnMapReadyCallback
{
    private static final String TAG = MainActivity.class.getName();
    private GoogleMap map;
    private File rootFolder;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        final ContextWrapper contextWrapper;
        final Resources resources;
        final ManifestDownloader downloader;
        final FragmentManager fragmentManager;
        final SupportMapFragment mapFragment;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resources = getResources();
        contextWrapper = new ContextWrapper(this);
        rootFolder = contextWrapper.getFilesDir();
        downloader = new ManifestDownloader(rootFolder);
        downloader.addManifestDownloaderListener(this);

        try
        {
            downloader.download(R.raw.oden_manifest,
                                resources,
                                true);
        }
        catch(final IOException ex)
        {
            Log.e(TAG,
                  "Problem downloading manifest",
                  ex);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        fragmentManager = getSupportFragmentManager();
        mapFragment = (SupportMapFragment)fragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @MainThread
    @Override
    public void converted(@NonNull final ManifestEntry entry,
                          @NonNull final File localFile)
    {
        if(BuildConfig.DEBUG)
        {
            final String city;

            city = entry.getCity();
            Log.i(TAG,
                  "Converted " + city);
        }
    }

    @MainThread
    @Override
    public void conversionCompleted(@NonNull final ManifestEntry[] entries)
    {
        final LatLngBounds.Builder builder;
        final LatLngBounds bounds;
        final File[] convertedDatasetFiles;
        final List<Marker> markers;
        final CameraUpdate cameraUpdate;

        if(BuildConfig.DEBUG)
        {
            Log.i(TAG,
                  "Converted " + entries.length + " entries");
        }

        convertedDatasetFiles = Manifest.getLocalDatasetFilesFor(entries,
                                                                 rootFolder);
        markers = new ArrayList<>();

        for(final File convertedDatasetFile : convertedDatasetFiles)
        {
            addMapAnnotation(convertedDatasetFile,
                             markers);
        }

        builder = new LatLngBounds.Builder();

        for(final Marker marker : markers)
        {
            builder.include(marker.getPosition());
        }

        bounds = builder.build();
        cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 5);
        map.moveCamera(cameraUpdate);
    }

    private void addMapAnnotation(@NonNull final File convertedDatasetFile,
                                  @NonNull final List<Marker> markers)
    {
        try
        {
            final PublicArt object;

            object = PublicArt.getPublicArt(convertedDatasetFile);

            for(final Feature feature : object.getFeatures())
            {
                final LatLng coordinates;
                final Geometry geometry;
                final double[] longLat;
                final MarkerOptions markerOptions;
                final Properties properties;
                final String name;
                final Marker marker;

                geometry = feature.getGeometry();
                longLat = geometry.getCoordinates();
                coordinates = new LatLng(longLat[1],
                                         longLat[0]);
                markerOptions = new MarkerOptions();
                markerOptions.position(coordinates);
                properties = feature.getProperties();
                name = properties.getName();
                markerOptions.title(name);
                marker = map.addMarker(markerOptions);
                markers.add(marker);
            }
        }
        catch(final IOException ex)
        {
            final String path;

            path = convertedDatasetFile.getAbsolutePath();
            Log.e(TAG,
                  "Error getting data: " + path,
                  ex);
        }
    }

    @MainThread
    @Override
    public void downloadError(@NonNull final ManifestEntry entry,
                              @NonNull final Throwable error)
    {
        if(BuildConfig.DEBUG)
        {
            final String provider;
            final String message;

            provider = entry.getProvider();
            message = error.getLocalizedMessage();
            Log.i(TAG,
                  "download error " + provider + " " + message,
                  error);
        }
    }

    @MainThread
    @Override
    public void downloadError(@NonNull final ManifestEntry entry,
                              @NonNull final String url,
                              @NonNull final Throwable error)
    {
        if(BuildConfig.DEBUG)
        {
            final String provider;
            final String message;

            provider = entry.getProvider();
            message = error.getLocalizedMessage();
            Log.i(TAG,
                  "download error " + provider + " " + message,
                  error);
        }
    }

    @MainThread
    @Override
    public void unarchiveError(@NonNull final ManifestEntry entry,
                               @NonNull final File localFile,
                               @NonNull final Throwable error)
    {
        if(BuildConfig.DEBUG)
        {
            final String provider;
            final String message;

            provider = entry.getProvider();
            message = error.getLocalizedMessage();
            Log.i(TAG,
                  "unarchive error " + provider + " " + message);
        }
    }

    @MainThread
    @Override
    public void conversionError(@NonNull final ManifestEntry entry,
                                @NonNull final File localFile,
                                @NonNull final Throwable error)
    {
        if(BuildConfig.DEBUG)
        {
            final String provider;
            final String message;

            provider = entry.getProvider();
            message = error.getLocalizedMessage();
            Log.i(TAG,
                  "conversion error " + " " + provider + " " + message);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(@NonNull final GoogleMap googleMap)
    {
        map = googleMap;
    }
}
