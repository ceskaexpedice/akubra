package org.ceskaexpedice.akubra.locks;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import org.ceskaexpedice.akubra.core.Configuration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class HazelcastServerNode implements ServletContextListener {

    private static final ILogger LOGGER = Logger.getLogger(HazelcastServerNode.class);
    private static HazelcastInstance hzInstance;

    public static synchronized void ensureHazelcastNode(Configuration configuration) {
        if (hzInstance == null) {
            Config config = null;
            // TODO File configFile = Configuration.getInstance().findConfigFile("hazelcast.config");
            File configFile = null;
            if (configFile != null) {
                try (FileInputStream configStream = new FileInputStream(configFile)) {
                    config = new XmlConfigBuilder(configStream).build();
                } catch (IOException ex) {
                    LOGGER.warning("Could not load Hazelcast config file " + configFile, ex);
                }
            }
            if (config == null) {
                config = new Config(configuration.getHazelcastInstance());
                GroupConfig groupConfig = config.getGroupConfig();
                groupConfig.setName(configuration.getHazelcastUser());
            }
            hzInstance = Hazelcast.getOrCreateHazelcastInstance(config);
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // TODO ensureHazelcastNode();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // TODO AkubraDOManager.shutdown();
        if (hzInstance != null) {
            hzInstance.shutdown();
        }
    }
}
