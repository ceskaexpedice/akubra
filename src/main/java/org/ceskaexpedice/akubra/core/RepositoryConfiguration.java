package org.ceskaexpedice.akubra.core;

public class RepositoryConfiguration {
    private final String processingIndexHost;
    private final String objectStorePath;
    private final String objectStorePattern;
    private final String datastreamStorePath;
    private final String datastreamStorePattern;
    private final int cacheTimeToLiveExpiration;
    private final String hazelcastConfigFile;
    private final String hazelcastClientConfigFile;
    private final String hazelcastInstance;
    private final String hazelcastUser;

    private RepositoryConfiguration(RepositoryConfiguration.Builder builder) {
        this.processingIndexHost = builder.processingIndexHost;
        this.objectStorePath = builder.objectStorePath;
        this.objectStorePattern = builder.objectStorePattern;
        this.datastreamStorePath = builder.datastreamStorePath;
        this.datastreamStorePattern = builder.datastreamStorePattern;
        this.cacheTimeToLiveExpiration = builder.cacheTimeToLiveExpiration;
        this.hazelcastConfigFile = builder.hazelcastConfigFile;
        this.hazelcastClientConfigFile = builder.hazelcastClientConfigFile;
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

    public String getHazelcastConfigFile() {
        return hazelcastConfigFile;
    }

    public String getHazelcastClientConfigFile() {
        return hazelcastClientConfigFile;
    }

    public static class Builder {
        private String processingIndexHost;
        private String objectStorePath;
        private String objectStorePattern;
        private String datastreamStorePath;
        private String datastreamStorePattern;
        private int cacheTimeToLiveExpiration;
        private String hazelcastConfigFile;
        private String hazelcastClientConfigFile;
        private String hazelcastInstance;
        private String hazelcastUser;

        public RepositoryConfiguration.Builder processingIndexHost(String processingIndexHost) {
            this.processingIndexHost = processingIndexHost;
            return this;
        }

        public RepositoryConfiguration.Builder objectStorePath(String objectStorePath) {
            this.objectStorePath = objectStorePath;
            return this;
        }

        public RepositoryConfiguration.Builder objectStorePattern(String objectStorePattern) {
            this.objectStorePattern = objectStorePattern;
            return this;
        }

        public RepositoryConfiguration.Builder datastreamStorePath(String datastreamStorePath) {
            this.datastreamStorePath = datastreamStorePath;
            return this;
        }

        public RepositoryConfiguration.Builder datastreamStorePattern(String datastreamStorePattern) {
            this.datastreamStorePattern = datastreamStorePattern;
            return this;
        }

        public RepositoryConfiguration.Builder cacheTimeToLiveExpiration(int cacheTimeToLiveExpiration) {
            this.cacheTimeToLiveExpiration = cacheTimeToLiveExpiration;
            return this;
        }

        public RepositoryConfiguration.Builder hazelcastConfigFile(String hazelcastConfigFile) {
            this.hazelcastConfigFile = hazelcastConfigFile;
            return this;
        }

        public RepositoryConfiguration.Builder hazelcastClientConfigFile(String hazelcastClientConfigFile) {
            this.hazelcastClientConfigFile = hazelcastClientConfigFile;
            return this;
        }

        public RepositoryConfiguration.Builder hazelcastInstance(String hazelcastInstance) {
            this.hazelcastInstance = hazelcastInstance;
            return this;
        }

        public RepositoryConfiguration.Builder hazelcastUser(String hazelcastUser) {
            this.hazelcastUser = hazelcastUser;
            return this;
        }

        public RepositoryConfiguration build() {
            return new RepositoryConfiguration(this);
        }
    }
}
