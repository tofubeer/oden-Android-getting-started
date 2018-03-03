package net.opendatadev.odensample;


import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class PublicArt
{
    private String type;
    private Feature[] features;

    public String getType()
    {
        return type;
    }

    public Feature[] getFeatures()
    {
        return features;
    }

    public static class Feature
    {
        private String type;
        private Geometry geometry;
        private Properties properties;

        public String getType()
        {
            return type;
        }

        public Geometry getGeometry()
        {
            return geometry;
        }

        public Properties getProperties()
        {
            return properties;
        }

        public static class Geometry
        {
            private String type;
            private double[] coordinates;

            public String getType()
            {
                return type;
            }

            public double[] getCoordinates()
            {
                return coordinates;
            }
        }

        public static class Properties
        {
            private String name;
            private String summary;
            private String shortDescription;
            private String address;
            private String website;
            private String access;
            private Integer year;
            private String medium;
            private String material;
            private Image[] images;
            private Artist artist;

            public String getName()
            {
                return name;
            }

            public String getSummary()
            {
                return summary;
            }

            public String getShortDescription()
            {
                return shortDescription;
            }

            public String getAddress()
            {
                return address;
            }

            public String getWebsite()
            {
                return website;
            }

            public String getAccess()
            {
                return access;
            }

            public Integer getYear()
            {
                return year;
            }

            public String getMedium()
            {
                return medium;
            }

            public String getMaterial()
            {
                return material;
            }

            public Image[] getImages()
            {
                return images;
            }

            public Artist getArtist()
            {
                return artist;
            }

            public static class Image
            {
                public String image;
                public String credit;
            }

            public static class Artist
            {
                private String name;
                private String country;
                private String website;
                private String biography;
                private String image;
                private String imageCredit;

                public String getName()
                {
                    return name;
                }

                public String getCountry()
                {
                    return country;
                }

                public String getWebsite()
                {
                    return website;
                }

                public String getBiography()
                {
                    return biography;
                }

                public String getImage()
                {
                    return image;
                }

                public String getImageCredit()
                {
                    return imageCredit;
                }
            }
        }
    }

    public static PublicArt getPublicArt(@NonNull final File file)
        throws
        IOException
    {
        final Gson gson;

        gson = new Gson();

        try(final Reader reader = new InputStreamReader(new FileInputStream(file)))
        {
            final PublicArt object;

            object = gson.fromJson(reader,
                                   PublicArt.class);

            return (object);
        }
    }
}
