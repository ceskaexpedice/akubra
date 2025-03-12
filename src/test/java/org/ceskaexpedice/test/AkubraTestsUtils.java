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
package org.ceskaexpedice.test;

import org.ceskaexpedice.akubra.config.HazelcastConfiguration;
import org.ceskaexpedice.akubra.config.RepositoryConfiguration;

import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

public final class AkubraTestsUtils {
    public static final Path TEST_REPOSITORY = Path.of("src/test/resources/data");
    public static final Path TEST_OUTPUT_REPOSITORY = Path.of("testoutput/data");
    public static final String PID_TITLE_PAGE = "uuid:12993b4a-71b4-4f19-8953-0701243cc25d";
    public static final String PID_NOT_EXISTS = "uuid:92993b4a-71b4-4f19-8953-0701243cc25d";
    public static final String PID_MONOGRAPH = "uuid:5035a48a-5e2e-486c-8127-2fa650842e46";
    public static final String PID_IMPORTED = "uuid:32993b4a-71b4-4f19-8953-0701243cc25d";

    private AkubraTestsUtils() {}

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
                .processingIndexHost(FunctionalTestsUtils.getProperty("processingIndexHost", null, props))
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
