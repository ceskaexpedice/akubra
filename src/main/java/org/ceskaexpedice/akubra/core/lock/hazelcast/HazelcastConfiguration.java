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
package org.ceskaexpedice.akubra.core.lock.hazelcast;

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
