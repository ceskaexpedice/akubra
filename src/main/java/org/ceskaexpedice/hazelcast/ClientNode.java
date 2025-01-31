package org.ceskaexpedice.hazelcast;

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
    private static HazelcastInstance hzInstance;

    public static synchronized void ensureHazelcastNode(HazelcastConfiguration configuration) {
        if (hzInstance != null) {
            return;
        }
        ClientConfig config = createHazelcastConfig(configuration);
        hzInstance = HazelcastClient.newHazelcastClient(config);
    }

    private static ClientConfig createHazelcastConfig(HazelcastConfiguration configuration) {
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
    public static HazelcastInstance getHzInstance() {
        return hzInstance;
    }

    public static void shutdown() {
        if (hzInstance != null) {
            hzInstance.shutdown();
        }
    }
}
