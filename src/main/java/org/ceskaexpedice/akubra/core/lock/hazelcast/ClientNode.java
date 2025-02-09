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

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ClientNode {

    private static final ILogger LOGGER = Logger.getLogger(ClientNode.class);
    private HazelcastInstance hzInstance;

    public void ensureHazelcastNode(HazelcastConfiguration configuration) {
        if (hzInstance != null) {
            return;
        }
        ClientConfig config = createHazelcastConfig(configuration);
        hzInstance = HazelcastClient.newHazelcastClient(config);
    }

    private ClientConfig createHazelcastConfig(HazelcastConfiguration configuration) {
        ClientConfig config = null;
        File configFile = configuration.getHazelcastClientConfigFile() == null ? null : new File(configuration.getHazelcastClientConfigFile());
        if (configFile != null) {
            try (FileInputStream configStream = new FileInputStream(configFile)) {
                config = new XmlClientConfigBuilder(configStream).build();
            } catch (IOException ex) {
                LOGGER.warning("Could not load Hazelcast config file " + configFile, ex);
            }
        }else{
            config = new ClientConfig();
            config.setInstanceName(configuration.getHazelcastInstance());
            GroupConfig groupConfig = config.getGroupConfig();
            groupConfig.setName(configuration.getHazelcastUser());
        }
        return config;
    }
    public HazelcastInstance getHzInstance() {
        return hzInstance;
    }

    public void shutdown() {
        if (hzInstance != null) {
            hzInstance.shutdown();
        }
    }
}
