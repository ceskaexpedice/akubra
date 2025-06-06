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

import org.apache.commons.io.IOUtils;
import org.ceskaexpedice.akubra.config.RepositoryConfiguration;
import org.ceskaexpedice.hazelcast.HazelcastConfiguration;
import org.ceskaexpedice.hazelcast.HazelcastServerNode;
import org.ceskaexpedice.testutils.AkubraTestsUtils;
import org.ceskaexpedice.testutils.ConcurrencyUtils;
import org.ceskaexpedice.testutils.IntegrationTestsUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.ceskaexpedice.testutils.AkubraTestsUtils.*;

public class Test_performance {
    // all time results mentioned in tests are for 100 x 1000
    private static final int N_THREADS = 1000;
    private static final int CALLS_PER_THREAD = 1000;

    private static AkubraRepository akubraRepository;
    private static Properties testsProperties;

    @BeforeAll
    static void beforeAll() {
        testsProperties = IntegrationTestsUtils.loadProperties();
        HazelcastConfiguration hazelcastConfig = AkubraTestsUtils.createHazelcastConfig(testsProperties);
        HazelcastServerNode.ensureHazelcastNode(hazelcastConfig);

        RepositoryConfiguration config = AkubraTestsUtils.createRepositoryConfig(TEST_REPOSITORY.toFile().getAbsolutePath(), testsProperties, hazelcastConfig);
        akubraRepository = AkubraRepositoryFactory.createRepository(config);
    }

    @AfterAll
    static void afterAll() {
        akubraRepository.shutdown();
        HazelcastServerNode.shutdown();
    }

    /**
     * Document FoXml read
     */
    @Disabled
    @Test
    void testDocumentRead() {
        long startTime = System.currentTimeMillis();
        ConcurrencyUtils.runTask(N_THREADS, () -> {
            System.out.println(Thread.currentThread().getName());
            for (int i = 0; i < CALLS_PER_THREAD; i++) {
                long start = System.currentTimeMillis();
                // old JAXB:
                // locks     - 40 min
                // no locks  - 17,5 min
                // new:
                // no locks:
                // byte[] bytes = akubraRepository.get(PID_TITLE_PAGE).asBytes();                        - 3,6 min
                // DigitalObject digitalObject = akubraRepository.get(PID_TITLE_PAGE).asDigitalObject(); - 15,6 min
                // Document dom = akubraRepository.get(PID_TITLE_PAGE).asDom(true);                      - 6,8 min
                // Document dom4j = akubraRepository.get(PID_TITLE_PAGE).asDom4j(true);                  - 7,2 min
                // String string = akubraRepository.get(PID_TITLE_PAGE).asString();                      - 3,9 min
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
    void testDocumentExists() {
        long startTime = System.currentTimeMillis();
        ConcurrencyUtils.runTask(1000, () -> {
            System.out.println(Thread.currentThread().getName());
            for (int i = 0; i < 1000; i++) {
                long start = System.currentTimeMillis();
                boolean exists = akubraRepository.exists(PID_TITLE_PAGE);
                // old - 16 min
                // new - 1,4 min
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
                // old:
                // locks    - 45,7 min
                // no locks - 16,5 min
                // new      - 6,3 min
                if (i % 50 == 0) {
                    System.out.println(Thread.currentThread().getName() + ": " + i + ",Time: " + (System.currentTimeMillis() - start));
                }
            }
        });
        System.out.println("Time taken: " + (System.currentTimeMillis() - startTime));
    }

    @Disabled
    @Test
    void testGetDataStreamBinaryContent() {
        long startTime = System.currentTimeMillis();
        ConcurrencyUtils.runTask(1000, () -> {
            System.out.println(Thread.currentThread().getName());
            for (int i = 0; i < 1000; i++) {
                long start = System.currentTimeMillis();
                InputStream is = akubraRepository.getDatastreamContent(PID_TITLE_PAGE, KnownDatastreams.IMG_FULL).asInputStream();

                try {
                    byte[] byteArray = IOUtils.toByteArray(is);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                // old:
                // no locks + stream to bytes - 76 min,
                // no locks, no reading of is - 18 min
                // new:
                // no locks + stream to bytes - 14,5 min
                // no locks no reading of is  - 8,2 min
                if (i % 50 == 0) {
                    System.out.println(Thread.currentThread().getName() + ": " + i + ",Time: " + (System.currentTimeMillis() - start));
                }
            }
        });
        System.out.println("Time taken: " + (System.currentTimeMillis() - startTime));
    }

    @Disabled
    @Test
    void testGetRelsExtModel() {
        long startTime = System.currentTimeMillis();
        ConcurrencyUtils.runTask(1000, () -> {
            System.out.println(Thread.currentThread().getName());
            for (int i = 0; i < 1000; i++) {
                long start = System.currentTimeMillis();
                // Dom on RelsExt: 10 min
                // Sax on foxml  : 7,2 min
                String model = akubraRepository.re().getModel(PID_TITLE_PAGE);
                if (i % 50 == 0) {
                    System.out.println(Thread.currentThread().getName() + ": " + i + ",Time: " + (System.currentTimeMillis() - start));
                }
            }
        });
        System.out.println("Time taken: " + (System.currentTimeMillis() - startTime));
    }

}
