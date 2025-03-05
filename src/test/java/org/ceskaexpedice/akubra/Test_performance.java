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
    void testGet() {
        long startTime = System.currentTimeMillis();
        ConcurrencyUtils.runTask(1000, () -> {
            System.out.println(Thread.currentThread().getName());
            for (int i = 0; i < 1000; i++) {
                long start = System.currentTimeMillis();
                // old: marshall + locks - 40 min, no locks - 17,5 min
                DigitalObjectWrapper digitalObjectWrapper = akubraRepository.get(PID_TITLE_PAGE);
                // new: bytes no locks:
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
    void testExists() {
        long startTime = System.currentTimeMillis();
        ConcurrencyUtils.runTask(1000, () -> {
            System.out.println(Thread.currentThread().getName());
            for (int i = 0; i < 1000; i++) {
                long start = System.currentTimeMillis();
                boolean exists = akubraRepository.exists(PID_TITLE_PAGE);
                // old: 16 min
                // new: 1,4 min
                if (i % 50 == 0) {
                    System.out.println(Thread.currentThread().getName() + ": " + i + ",Time: " + (System.currentTimeMillis() - start));
                }
            }
        });
        System.out.println("Time taken: " + (System.currentTimeMillis() - startTime));
    }

    @Disabled
    @Test
    void testDatastreamExists() {
        long startTime = System.currentTimeMillis();
        ConcurrencyUtils.runTask(1000, () -> {
            System.out.println(Thread.currentThread().getName());
            for (int i = 0; i < 1000; i++) {
                long start = System.currentTimeMillis();
                boolean exists = akubraRepository.datastreamExists(PID_TITLE_PAGE, KnownDatastreams.IMG_FULL);
                // new 6,3 min (bytes + sax)
                // old 45,7 (marshall + DigitalObject test) + lock; 16,5 no locks
                // TODO zamek pry nemusi byt ...
                if (i % 50 == 0) {
                    System.out.println(Thread.currentThread().getName() + ": " + i + ",Time: " + (System.currentTimeMillis() - start));
                }
            }
        });
        System.out.println("Time taken: " + (System.currentTimeMillis() - startTime));
    }

}
