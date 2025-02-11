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
package org.ceskaexpedice.akubra.testutils;

import org.ceskaexpedice.akubra.RepositoryConfiguration;
import org.ceskaexpedice.akubra.core.lock.hazelcast.HazelcastConfiguration;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * TestsUtilities
 *
 * @author ppodsednik
 */
public final class TestUtilities {
    private static final String PROPERTIES = "tests.properties";
    private static final String SKIP_FUNCTIONAL_TESTS_PROPERTY = "skipFunctionalTests";
    private static final String DEBUG_PRINT_PROPERTY = "debugPrint";

    private TestUtilities() {}

    public static void checkFunctionalTestsIgnored(Properties props) {
        assumeTrue(!isIgnored(props), "Test ignored by the property: " + SKIP_FUNCTIONAL_TESTS_PROPERTY);
    }

    public static Properties loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(TestUtilities.class.getClassLoader().getResourceAsStream(PROPERTIES));
        } catch (IOException e) {
            System.out.println("Cannot find property file, will continue anyway:" + PROPERTIES);
        }
        return properties;
    }

    private static boolean isIgnored(Properties properties) {
        String ignore = getProperty(SKIP_FUNCTIONAL_TESTS_PROPERTY, "true", properties);
        return Boolean.valueOf(ignore);
    }

    public static String getProperty(String name, String defaultValue, Properties properties) {
        String val = getSysOrEnvProperty(name);
        if (isEmpty(val) && properties != null) {
            val = (String) properties.get(name);
        }
        return isEmpty(val) ? defaultValue : val;
    }

    private static String getSysOrEnvProperty(String name) {
        String val = System.getProperty(name);
        return isEmpty(val) ? System.getenv(name) : val;
    }

    private static boolean isEmpty(String name) {
        if (name == null || name.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public static void debugPrint(String msg, Properties props) {
        String debugPrintS = getProperty(DEBUG_PRINT_PROPERTY, "true", props);
        Boolean debugPrint = Boolean.valueOf(debugPrintS);
        if(debugPrint){
            System.out.println(msg);
        }
    }

    public static HazelcastConfiguration createHazelcastConfig(Properties props) {
        HazelcastConfiguration hazelcastConfig = new HazelcastConfiguration.Builder()
                .hazelcastInstance("akubrasync")
                .hazelcastUser("dev")
                .build();
        return hazelcastConfig;
    }

    public static RepositoryConfiguration createRepositoryConfig(String repoDir, Properties props, HazelcastConfiguration hazelcastConfig) {
        String testRepoPath = repoDir + "/";
        //String testRepoPath = "c:\\Users\\petr\\.kramerius4\\data\\";
        RepositoryConfiguration config = new RepositoryConfiguration.Builder()
                .processingIndexHost(TestUtilities.getProperty("processingIndexHost", null, props))
                .objectStorePath(testRepoPath + "objectStore")
                .objectStorePattern("##/##")
                .datastreamStorePath(testRepoPath + "datastreamStore")
                .datastreamStorePattern("##/##")
                .cacheTimeToLiveExpiration(60)
                .hazelcastConfiguration(hazelcastConfig)
                .build();
        return config;
    }

}
