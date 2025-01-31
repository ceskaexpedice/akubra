package org.ceskaexpedice.akubra.core;

import org.ceskaexpedice.hazelcast.HazelcastConfiguration;

public class RepositoryConfiguration {
    private final String processingIndexHost;
    private final String objectStorePath;
    private final String objectStorePattern;
    private final String datastreamStorePath;
    private final String datastreamStorePattern;
    private final int cacheTimeToLiveExpiration;
    private final HazelcastConfiguration hazelcastConfiguration;

    private RepositoryConfiguration(RepositoryConfiguration.Builder builder) {
        this.processingIndexHost = builder.processingIndexHost;
        this.objectStorePath = builder.objectStorePath;
        this.objectStorePattern = builder.objectStorePattern;
        this.datastreamStorePath = builder.datastreamStorePath;
        this.datastreamStorePattern = builder.datastreamStorePattern;
        this.cacheTimeToLiveExpiration = builder.cacheTimeToLiveExpiration;
        this.hazelcastConfiguration = builder.hazelcastConfiguration;
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

    public HazelcastConfiguration getHazelcastConfiguration() {
        return hazelcastConfiguration;
    }

    public static class Builder {
        private String processingIndexHost;
        private String objectStorePath;
        private String objectStorePattern;
        private String datastreamStorePath;
        private String datastreamStorePattern;
        private int cacheTimeToLiveExpiration;
        private HazelcastConfiguration hazelcastConfiguration;

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

        public RepositoryConfiguration.Builder hazelcastConfiguration(HazelcastConfiguration hazelcastConfiguration) {
            this.hazelcastConfiguration = hazelcastConfiguration;
            return this;
        }

        public RepositoryConfiguration build() {
            return new RepositoryConfiguration(this);
        }
    }
}
