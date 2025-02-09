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

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ServerNode {

    private static final ILogger LOGGER = Logger.getLogger(ServerNode.class);
    private static HazelcastInstance hzInstance;

    public static synchronized void ensureHazelcastNode(HazelcastConfiguration configuration) {
        if (hzInstance != null) {
            return;
        }
        Config config = createHazelcastConfig(configuration);
        hzInstance = Hazelcast.getOrCreateHazelcastInstance(config);
    }

    private static Config createHazelcastConfig(HazelcastConfiguration configuration) {
        Config config = null;
        File configFile = configuration.getHazelcastConfigFile() == null ? null : new File(configuration.getHazelcastConfigFile());
        if (configFile != null) {
            try (FileInputStream configStream = new FileInputStream(configFile)) {
                config = new XmlConfigBuilder(configStream).build();
            } catch (IOException ex) {
                LOGGER.warning("Could not load Hazelcast config file " + configFile, ex);
            }
        }else{
            config = new Config(configuration.getHazelcastInstance());
            GroupConfig groupConfig = config.getGroupConfig();
            groupConfig.setName(configuration.getHazelcastUser());
        }
        return config;
    }

    public static void shutdown() {
        if (hzInstance != null) {
            hzInstance.shutdown();
        }
    }
}
