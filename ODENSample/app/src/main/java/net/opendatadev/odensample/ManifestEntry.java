package net.opendatadev.odensample;


public class ManifestEntry
{
    private String datasetName;
    private String country;
    private String province;
    private String region;
    private String city;
    private String provider;
    private String schema;
    private String converter;
    private String id;
    private Download[] downloads;

    public String getDatasetName()
    {
        return datasetName;
    }

    public String getCountry()
    {
        return country;
    }

    public String getProvince()
    {
        return province;
    }

    public String getRegion()
    {
        return region;
    }

    public String getCity()
    {
        return city;
    }

    public String getProvider()
    {
        return provider;
    }

    public String getSchema()
    {
        return schema;
    }

    public String getConverter()
    {
        return converter;
    }

    public String getId()
    {
        return id;
    }

    public Download[] getDownloads()
    {
        return downloads;
    }

    public static class Download
    {
        private String src;
        private String encoding;
        private Extract[] extract;

        public String getSrc()
        {
            return src;
        }

        public String getEncoding()
        {
            return encoding;
        }

        public Extract[] getExtract()
        {
            return extract;
        }

        public static class Extract
        {
            private String src;
            private String dst;
            private String encoding;

            public String getSrc()
            {
                return src;
            }

            public String getDst()
            {
                return dst;
            }

            public String getEncoding()
            {
                return encoding;
            }
        }
    }
}
