package org.ceskaexpedice.akubra.core.repository.impl;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import org.ceskaexpedice.akubra.conf.Configuration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class HazelcastServerNode implements ServletContextListener {

    private static final ILogger LOGGER = Logger.getLogger(HazelcastServerNode.class);
    private static HazelcastInstance hzInstance;

    private static synchronized void ensureHazelcastNode() {
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
                config = new Config("akubrasync");
                //config = new Config(Configuration.getInstance().getConfiguration().getString("hazelcast.instance"));
                GroupConfig groupConfig = config.getGroupConfig();
// TODO                groupConfig.setName(Configuration.getInstance().getConfiguration().getString("hazelcast.user"));
                groupConfig.setName("dev");

            }
            hzInstance = Hazelcast.getOrCreateHazelcastInstance(config);
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ensureHazelcastNode();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        AkubraDOManager.shutdown();
        if (hzInstance != null) {
            hzInstance.shutdown();
        }
    }
}
