package org.ceskaexpedice.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import org.ceskaexpedice.akubra.core.RepositoryConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ServerNode {

    private static final ILogger LOGGER = Logger.getLogger(ServerNode.class);
    private static HazelcastInstance hzInstance;

    public static synchronized void ensureHazelcastNode(RepositoryConfiguration configuration) {
        if (hzInstance != null) {
            return;
        }
        Config config = createHazelcastConfig(configuration);
        hzInstance = Hazelcast.getOrCreateHazelcastInstance(config);
    }

    private static Config createHazelcastConfig(RepositoryConfiguration configuration) {
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
