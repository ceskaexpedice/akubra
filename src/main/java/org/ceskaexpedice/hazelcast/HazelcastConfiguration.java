package org.ceskaexpedice.hazelcast;

public class HazelcastConfiguration {
    private final String hazelcastConfigFile;
    private final String hazelcastClientConfigFile;
    private final String hazelcastInstance;
    private final String hazelcastUser;

    private HazelcastConfiguration(HazelcastConfiguration.Builder builder) {
        this.hazelcastConfigFile = builder.hazelcastConfigFile;
        this.hazelcastClientConfigFile = builder.hazelcastClientConfigFile;
        this.hazelcastInstance = builder.hazelcastInstance;
        this.hazelcastUser = builder.hazelcastUser;
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
        private String hazelcastConfigFile;
        private String hazelcastClientConfigFile;
        private String hazelcastInstance;
        private String hazelcastUser;

        public HazelcastConfiguration.Builder hazelcastConfigFile(String hazelcastConfigFile) {
            this.hazelcastConfigFile = hazelcastConfigFile;
            return this;
        }

        public HazelcastConfiguration.Builder hazelcastClientConfigFile(String hazelcastClientConfigFile) {
            this.hazelcastClientConfigFile = hazelcastClientConfigFile;
            return this;
        }

        public HazelcastConfiguration.Builder hazelcastInstance(String hazelcastInstance) {
            this.hazelcastInstance = hazelcastInstance;
            return this;
        }

        public HazelcastConfiguration.Builder hazelcastUser(String hazelcastUser) {
            this.hazelcastUser = hazelcastUser;
            return this;
        }

        public HazelcastConfiguration build() {
            return new HazelcastConfiguration(this);
        }
    }
}
