/*
 * Copyright (C) 2025 Inovatika
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ceskaexpedice.akubra;

import org.ceskaexpedice.akubra.core.lock.hazelcast.HazelcastConfiguration;

/**
 * Represents the configuration for a repository, containing various paths, patterns,
 * cache expiration settings, and Hazelcast configuration for distributed locking.
 */
public class RepositoryConfiguration {

    private final String processingIndexHost;
    private final String objectStorePath;
    private final String objectStorePattern;
    private final String datastreamStorePath;
    private final String datastreamStorePattern;
    private final int cacheTimeToLiveExpiration;
    private final HazelcastConfiguration hazelcastConfiguration;

    /**
     * Constructor for RepositoryConfiguration using the Builder pattern.
     *
     * @param builder The builder used to construct this configuration.
     */
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

    /**
     * Builder class for constructing a {@link RepositoryConfiguration}.
     */
    public static class Builder {

        private String processingIndexHost;
        private String objectStorePath;
        private String objectStorePattern;
        private String datastreamStorePath;
        private String datastreamStorePattern;
        private int cacheTimeToLiveExpiration;
        private HazelcastConfiguration hazelcastConfiguration;

        /**
         * Sets the host for the processing index.
         *
         * @param processingIndexHost The host of the processing index.
         * @return The Builder instance.
         */
        public RepositoryConfiguration.Builder processingIndexHost(String processingIndexHost) {
            this.processingIndexHost = processingIndexHost;
            return this;
        }

        /**
         * Sets the path for the object store.
         *
         * @param objectStorePath The path for the object store.
         * @return The Builder instance.
         */
        public RepositoryConfiguration.Builder objectStorePath(String objectStorePath) {
            this.objectStorePath = objectStorePath;
            return this;
        }

        /**
         * Sets the pattern for the object store.
         *
         * @param objectStorePattern The pattern for the object store.
         * @return The Builder instance.
         */
        public RepositoryConfiguration.Builder objectStorePattern(String objectStorePattern) {
            this.objectStorePattern = objectStorePattern;
            return this;
        }

        /**
         * Sets the path for the datastream store.
         *
         * @param datastreamStorePath The path for the datastream store.
         * @return The Builder instance.
         */
        public RepositoryConfiguration.Builder datastreamStorePath(String datastreamStorePath) {
            this.datastreamStorePath = datastreamStorePath;
            return this;
        }

        /**
         * Sets the pattern for the datastream store.
         *
         * @param datastreamStorePattern The pattern for the datastream store.
         * @return The Builder instance.
         */
        public RepositoryConfiguration.Builder datastreamStorePattern(String datastreamStorePattern) {
            this.datastreamStorePattern = datastreamStorePattern;
            return this;
        }

        /**
         * Sets the cache time-to-live expiration value.
         *
         * @param cacheTimeToLiveExpiration The cache TTL expiration time in seconds.
         * @return The Builder instance.
         */
        public RepositoryConfiguration.Builder cacheTimeToLiveExpiration(int cacheTimeToLiveExpiration) {
            this.cacheTimeToLiveExpiration = cacheTimeToLiveExpiration;
            return this;
        }

        /**
         * Sets the Hazelcast configuration for distributed locking.
         *
         * @param hazelcastConfiguration The Hazelcast configuration object.
         * @return The Builder instance.
         */
        public RepositoryConfiguration.Builder hazelcastConfiguration(HazelcastConfiguration hazelcastConfiguration) {
            this.hazelcastConfiguration = hazelcastConfiguration;
            return this;
        }

        /**
         * Builds the {@link RepositoryConfiguration} instance.
         *
         * @return A fully constructed {@link RepositoryConfiguration}.
         */
        public RepositoryConfiguration build() {
            return new RepositoryConfiguration(this);
        }
    }
}

