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
package org.ceskaexpedice.akubra;

import org.ceskaexpedice.akubra.config.HazelcastConfiguration;
import org.ceskaexpedice.akubra.config.RepositoryConfiguration;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.ceskaexpedice.test.ConcurrencyUtils;
import org.ceskaexpedice.test.FunctionalTestsUtils;
import org.dom4j.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Properties;

import static org.ceskaexpedice.akubra.AkubraTestsUtils.*;

public class Test_performance {
    private static AkubraRepository akubraRepository;
    private static Properties testsProperties;

    @BeforeAll
    static void beforeAll() {
        testsProperties = FunctionalTestsUtils.loadProperties();
        HazelcastConfiguration hazelcastConfig = AkubraTestsUtils.createHazelcastConfig(testsProperties);
        HazelcastServerNode.ensureHazelcastNode(hazelcastConfig);

        URL resource = FunctionalTestsUtils.class.getClassLoader().getResource("data");
        RepositoryConfiguration config = AkubraTestsUtils.createRepositoryConfig(resource.getFile(), testsProperties, hazelcastConfig);
        akubraRepository = AkubraRepositoryFactory.createRepository(config);
    }

    @AfterAll
    static void afterAll() {
        akubraRepository.shutdown();
        HazelcastServerNode.shutdown();
    }

    @Disabled
    @Test
    void testGet_concurrency() {
        long startTime = System.currentTimeMillis();
        ConcurrencyUtils.runTask(1000, () -> {
            System.out.println(Thread.currentThread().getName());
            for (int i = 0; i < 1000; i++) {
                long start = System.currentTimeMillis();
                //byte[] bytes = akubraRepository.get(PID_TITLE_PAGE).asBytes(); - 3,6 min
                // DigitalObject digitalObject = akubraRepository.get(PID_TITLE_PAGE).asDigitalObject(); - 15,6 min
                // Document dom = akubraRepository.get(PID_TITLE_PAGE).asDom(true); - 6,8 min
                // Document dom4j = akubraRepository.get(PID_TITLE_PAGE).asDom4j(true); - 7,2 min
                // String string = akubraRepository.get(PID_TITLE_PAGE).asString(); - 3,9 min
                String string = akubraRepository.get(PID_TITLE_PAGE).asString();
                if (i % 50 == 0) {
                    System.out.println(Thread.currentThread().getName() + ": " + i + ",Time: " + (System.currentTimeMillis() - start));
                }
            }
        });
        System.out.println("Time taken: " + (System.currentTimeMillis() - startTime));
    }

    @Disabled
    @Test
    void testExists_concurrency() {
        long startTime = System.currentTimeMillis();
        ConcurrencyUtils.runTask(1000, () -> {
            System.out.println(Thread.currentThread().getName());
            for (int i = 0; i < 1000; i++) {
                long start = System.currentTimeMillis();
                boolean exists = akubraRepository.exists(PID_TITLE_PAGE);
                if (i % 50 == 0) {
                    System.out.println(Thread.currentThread().getName() + ": " + i + ",Time: " + (System.currentTimeMillis() - start));
                }
            }
        });
        System.out.println("Time taken: " + (System.currentTimeMillis() - startTime));
    }

}
