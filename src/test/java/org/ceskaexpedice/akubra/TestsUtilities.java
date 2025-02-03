/**
 * Copyright ©2019 Accenture and/or its affiliates. All Rights Reserved.
 * <p>
 * Permission to any use, copy, modify, and distribute this software and
 * its documentation for any purpose is subject to a licensing agreement
 * duly entered into with the copyright owner or its affiliate.
 * <p>
 * All information contained herein is, and remains the property of Accenture
 * and/or its affiliates and its suppliers, if any.  The intellectual and
 * technical concepts contained herein are proprietary to Accenture and/or
 * its affiliates and its suppliers and may be covered by one or more patents
 * or pending patent applications in one or more jurisdictions worldwide,
 * and are protected by trade secret or copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless
 * prior written permission is obtained from Accenture and/or its affiliates.
 */
package org.ceskaexpedice.akubra;

import org.ceskaexpedice.hazelcast.HazelcastConfiguration;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * TestsUtilities
 *
 * @author ppodsednik
 */
public final class TestsUtilities {
    private static final String PROPERTIES = "tests.properties";
    private static final String SKIP_FUNCTIONAL_TESTS_PROPERTY = "skipFunctionalTests";
    private static final String DEBUG_PRINT_PROPERTY = "debugPrint";

    private TestsUtilities() {}

    public static void checkFunctionalTestsIgnored(Properties props) {
        assumeTrue(!isIgnored(props), "Test ignored by the property: " + SKIP_FUNCTIONAL_TESTS_PROPERTY);
    }

    public static Properties loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(TestsUtilities.class.getClassLoader().getResourceAsStream(PROPERTIES));
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

    public static RepositoryConfiguration createRepositoryConfig(Properties props, HazelcastConfiguration hazelcastConfig) {
        URL resource = TestsUtilities.class.getClassLoader().getResource("data");
        String testRepoPath = resource.getFile() + "/";
        //String testRepoPath = "c:\\Users\\petr\\.kramerius4\\data\\";
        RepositoryConfiguration config = new RepositoryConfiguration.Builder()
                .processingIndexHost(TestsUtilities.getProperty("processingIndexHost", null, props))
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
