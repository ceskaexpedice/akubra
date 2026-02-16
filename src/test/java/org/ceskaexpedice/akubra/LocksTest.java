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

import org.ceskaexpedice.akubra.config.RepositoryConfiguration;
import org.ceskaexpedice.hazelcast.HazelcastConfiguration;
import org.ceskaexpedice.hazelcast.HazelcastServerNode;
import org.ceskaexpedice.testutils.AkubraTestsUtils;
import org.ceskaexpedice.testutils.ConcurrencyUtils;
import org.ceskaexpedice.testutils.IntegrationTestsUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static org.ceskaexpedice.testutils.AkubraTestsUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class LocksTest {
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

    @Test
    void testLockSecondInsideFirst() {
        String pid = PID_MONOGRAPH;
        String pid1 = PID_TITLE_PAGE;
        Boolean result = akubraRepository.doWithLock(pid, () -> {
            akubraRepository.get(pid);
            Boolean result1 = akubraRepository.doWithLock(pid1, () -> {
                akubraRepository.get(pid1);
                return true;
            });
            return result1;
        });
        assertTrue(result);
    }

    @Test
    void testReentrant() {
        String pid = PID_MONOGRAPH;
        Boolean result = akubraRepository.doWithLock(pid, () -> {
            akubraRepository.get(pid);
            Boolean result1 = akubraRepository.doWithLock(pid, () -> {
                akubraRepository.get(pid);
                return true;
            });
            return result1;
        });
        assertTrue(result);
    }

    @Test
    void testMutualExclusion() throws Exception {
        CountDownLatch t1Acquired = new CountDownLatch(1);
        CountDownLatch t2Entered = new CountDownLatch(1);
        AtomicLong t2EnterTime = new AtomicLong();
        AtomicLong t1ReleaseTime = new AtomicLong();

        Thread t1 = new Thread(() -> {
            akubraRepository.doWithLock("pid1", () -> {
                System.out.println("T1 acquired lock");
                t1Acquired.countDown();
                sleep(5000); // hold lock
                t1ReleaseTime.set(System.currentTimeMillis());
                System.out.println("T1 releasing lock");
                return null;
            });
        });

        Thread t2 = new Thread(() -> {
            try {
                t1Acquired.await();
                System.out.println("T2 trying lock");
                akubraRepository.doWithLock("pid1", () -> {
                    t2EnterTime.set(System.currentTimeMillis());
                    System.out.println("T2 acquired lock");
                    t2Entered.countDown();
                    return null;
                });
            } catch (InterruptedException ignored) {
            }
        });

        long start = System.currentTimeMillis();
        t1.start();
        t2.start();
        t2Entered.await(); // wait until T2 actually gets lock
        t1.join();
        t2.join();

        long waitedMillis = t2EnterTime.get() - start;
        assertTrue(waitedMillis >= 4500, "T2 should NOT acquire lock immediately (should wait ~5 sec)");
        assertTrue(t2EnterTime.get() >= t1ReleaseTime.get(), "T2 must acquire lock only AFTER T1 released it");
    }

    @Test
    void testDeadLock() {
        AtomicBoolean deadlockReleased = new AtomicBoolean(false);
        ConcurrencyUtils.runFactoryTasks(2, new Function<>() {

            @Override
            public ConcurrencyUtils.TestTask apply(Integer taskNumber) {
                if (taskNumber == 1) {
                    return new ConcurrencyUtils.TestTask(taskNumber + "") {
                        @Override
                        public void run() {
                            super.run();
                            akubraRepository.doWithLock(PID_MONOGRAPH, () -> {
                                IntegrationTestsUtils.debugPrint(Thread.currentThread().getName() + ": acquiredWriteLock for " + PID_MONOGRAPH, testsProperties);
                                AkubraTestsUtils.sleep(2000);

                                IntegrationTestsUtils.debugPrint(Thread.currentThread().getName() + ": attempt to acquire readLock for " + PID_TITLE_PAGE, testsProperties);
                                try {
                                    akubraRepository.doWithLock(PID_TITLE_PAGE, () -> {
                                        // never gets here
                                        return null;
                                    });
                                } catch (DistributedLocksException e) {
                                    if (e.getCode().equals(DistributedLocksException.LOCK_TIMEOUT)) {
                                        deadlockReleased.set(true);
                                    }
                                }
                                return null;
                            });
                        }
                    };
                } else {
                    return new ConcurrencyUtils.TestTask(taskNumber + "") {
                        @Override
                        public void run() {
                            super.run();
                            akubraRepository.doWithLock(PID_TITLE_PAGE, () -> {
                                IntegrationTestsUtils.debugPrint(Thread.currentThread().getName() + ": acquiredWriteLock for " + PID_TITLE_PAGE, testsProperties);

                                IntegrationTestsUtils.debugPrint(Thread.currentThread().getName() + ": attempt to acquire readLock for " + PID_MONOGRAPH, testsProperties);
                                try {
                                    akubraRepository.doWithLock(PID_MONOGRAPH, () -> {
                                        // never gets here
                                        return null;
                                    });
                                } catch (DistributedLocksException e) {
                                    if (e.getCode().equals(DistributedLocksException.LOCK_TIMEOUT)) {
                                        deadlockReleased.set(true);
                                    }
                                }
                                return null;
                            });
                        }
                    };
                }
            }
        });
        assertTrue(deadlockReleased.get());
    }

}
