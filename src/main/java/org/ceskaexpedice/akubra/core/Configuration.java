package org.ceskaexpedice.akubra.core;

public class Configuration {
    private final String processingIndexHost;
    private final String objectStorePath;
    private final String objectStorePattern;
    private final String datastreamStorePath;
    private final String datastreamStorePattern;
    private final int cacheTimeToLiveExpiration;
    private final String hazelcastInstance;
    private final String hazelcastUser;

    private Configuration(Configuration.Builder builder) {
        this.processingIndexHost = builder.processingIndexHost;
        this.objectStorePath = builder.objectStorePath;
        this.objectStorePattern = builder.objectStorePattern;
        this.datastreamStorePath = builder.datastreamStorePath;
        this.datastreamStorePattern = builder.datastreamStorePattern;
        this.cacheTimeToLiveExpiration = builder.cacheTimeToLiveExpiration;
        this.hazelcastInstance = builder.hazelcastInstance;
        this.hazelcastUser = builder.hazelcastUser;
    }

    public String getProcessingIndexHost() {
        return processingIndexHost;
    }

    public String getObjectStorePath() {
        return objectStorePath;
    }

    public String getObjectStorePattern() {
        return objectStorePattern;
    }

    public String getDatastreamStorePath() {
        return datastreamStorePath;
    }

    public String getDatastreamStorePattern() {
        return datastreamStorePattern;
    }

    public int getCacheTimeToLiveExpiration() {
        return cacheTimeToLiveExpiration;
    }

    public String getHazelcastInstance() {
        return hazelcastInstance;
    }

    public String getHazelcastUser() {
        return hazelcastUser;
    }

    public static class Builder {
        private String processingIndexHost;
        private String objectStorePath;
        private String objectStorePattern;
        private String datastreamStorePath;
        private String datastreamStorePattern;
        private int cacheTimeToLiveExpiration;
        private String hazelcastInstance;
        private String hazelcastUser;

        public Configuration.Builder processingIndexHost(String processingIndexHost) {
            this.processingIndexHost = processingIndexHost;
            return this;
        }

        public Configuration.Builder objectStorePath(String objectStorePath) {
            this.objectStorePath = objectStorePath;
            return this;
        }

        public Configuration.Builder objectStorePattern(String objectStorePattern) {
            this.objectStorePattern = objectStorePattern;
            return this;
        }

        public Configuration.Builder datastreamStorePath(String datastreamStorePath) {
            this.datastreamStorePath = datastreamStorePath;
            return this;
        }

        public Configuration.Builder datastreamStorePattern(String datastreamStorePattern) {
            this.datastreamStorePattern = datastreamStorePattern;
            return this;
        }

        public Configuration.Builder cacheTimeToLiveExpiration(int cacheTimeToLiveExpiration) {
            this.cacheTimeToLiveExpiration = cacheTimeToLiveExpiration;
            return this;
        }

        public Configuration.Builder hazelcastInstance(String hazelcastInstance) {
            this.hazelcastInstance = hazelcastInstance;
            return this;
        }

        public Configuration.Builder hazelcastUser(String hazelcastUser) {
            this.hazelcastUser = hazelcastUser;
            return this;
        }

        public Configuration build() {
            return new Configuration(this);
        }
    }
}
