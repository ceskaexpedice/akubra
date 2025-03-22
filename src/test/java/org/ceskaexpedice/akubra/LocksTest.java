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
import org.ceskaexpedice.test.AkubraTestsUtils;
import org.ceskaexpedice.test.ConcurrencyUtils;
import org.ceskaexpedice.test.IntegrationTestsUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.Stack;
import java.util.function.Function;

import static org.ceskaexpedice.test.AkubraTestsUtils.*;
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
        Boolean result = akubraRepository.doWithWriteLock(pid, () -> {
            akubraRepository.get(pid);
            Boolean result1 = akubraRepository.doWithReadLock(pid1, () -> {
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
        Boolean result = akubraRepository.doWithWriteLock(pid, () -> {
            akubraRepository.get(pid);
            Boolean result1 = akubraRepository.doWithWriteLock(pid, () -> {
                akubraRepository.get(pid);
                return true;
            });
            return result1;
        });
        assertTrue(result);
    }

    @Test
    void testReadLockAndReadLock() {
        Stack<String> result = new Stack<>();
        ConcurrencyUtils.runFactoryTasks(2, new Function<>() {
            @Override
            public ConcurrencyUtils.TestTask apply(Integer taskNumber) {
                if (taskNumber == 1) {
                    return new ConcurrencyUtils.TestTask(taskNumber + "") {
                        @Override
                        public void run() {
                            super.run();
                            String pid = PID_MONOGRAPH;
                            akubraRepository.doWithReadLock(pid, () -> {
                                result.push(Thread.currentThread().getName());
                                IntegrationTestsUtils.debugPrint(Thread.currentThread().getName() + ": acquiredReadLock for " + pid, testsProperties);
                                AkubraTestsUtils.sleep(2000);
                                return null;
                            });
                            IntegrationTestsUtils.debugPrint(Thread.currentThread().getName() + ": releasedReadLock for " + pid, testsProperties);
                            result.push(Thread.currentThread().getName());
                        }
                    };
                } else {
                    return new ConcurrencyUtils.TestTask(taskNumber + "") {
                        @Override
                        public void run() {
                            super.run();
                            String pid = PID_MONOGRAPH;
                            AkubraTestsUtils.sleep(1000); // make sure the first thread has time to acquire lock
                            akubraRepository.doWithReadLock(pid, () -> {
                                result.push(Thread.currentThread().getName());
                                IntegrationTestsUtils.debugPrint(Thread.currentThread().getName() + ": acquiredReadLock for " + pid, testsProperties);
                                return null;
                            });
                            IntegrationTestsUtils.debugPrint(Thread.currentThread().getName() + ": releasedReadLock for " + pid, testsProperties);
                            result.push(Thread.currentThread().getName());
                        }
                    };
                }
            }
        });
        String last = result.pop();
        String secondLast = result.pop();
        assertNotEquals(last, secondLast);
        // Ensure stack is empty after test
        String thirdLast = result.pop();
        String fourthLast = result.pop();
        assertTrue(result.isEmpty());
    }

    @Test
    void testReadLockAndWriteLock() {
        Stack<String> result = new Stack<>();
        ConcurrencyUtils.runFactoryTasks(2, new Function<>() {
            @Override
            public ConcurrencyUtils.TestTask apply(Integer taskNumber) {
                if (taskNumber == 1) {
                    return new ConcurrencyUtils.TestTask(taskNumber + "") {
                        @Override
                        public void run() {
                            super.run();
                            String pid = PID_MONOGRAPH;
                            akubraRepository.doWithReadLock(pid, () -> {
                                result.push(Thread.currentThread().getName());
                                IntegrationTestsUtils.debugPrint(Thread.currentThread().getName() + ": acquiredReadLock for " + pid, testsProperties);
                                AkubraTestsUtils.sleep(2000);
                                return null;
                            });
                            IntegrationTestsUtils.debugPrint(Thread.currentThread().getName() + ": releasedReadLock for " + pid, testsProperties);
                            result.push(Thread.currentThread().getName());
                        }
                    };
                } else {
                    return new ConcurrencyUtils.TestTask(taskNumber + "") {
                        @Override
                        public void run() {
                            super.run();
                            String pid = PID_MONOGRAPH;
                            AkubraTestsUtils.sleep(1000); // make sure the first thread has time to acquire lock
                            akubraRepository.doWithWriteLock(pid, () -> {
                                result.push(Thread.currentThread().getName());
                                IntegrationTestsUtils.debugPrint(Thread.currentThread().getName() + ": acquiredWriteLock for " + pid, testsProperties);
                                return null;
                            });
                            IntegrationTestsUtils.debugPrint(Thread.currentThread().getName() + ": releasedWriteLock for " + pid, testsProperties);
                            result.push(Thread.currentThread().getName());
                        }
                    };
                }
            }
        });
        String last = result.pop();
        String secondLast = result.pop();
        assertEquals(last, secondLast);
        String thirdLast = result.pop();
        String fourthLast = result.pop();
        assertEquals(thirdLast, fourthLast);
        assertTrue(result.isEmpty());
    }

    @Test
    void testWriteLockAndReadLock() {
        Stack<String> result = new Stack<>();
        ConcurrencyUtils.runFactoryTasks(2, new Function<>() {
            @Override
            public ConcurrencyUtils.TestTask apply(Integer taskNumber) {
                if (taskNumber == 1) {
                    return new ConcurrencyUtils.TestTask(taskNumber + "") {
                        @Override
                        public void run() {
                            super.run();
                            String pid = PID_MONOGRAPH;
                            akubraRepository.doWithWriteLock(pid, () -> {
                                result.push(Thread.currentThread().getName());
                                IntegrationTestsUtils.debugPrint(Thread.currentThread().getName() + ": acquiredWriteLock for " + pid, testsProperties);
                                AkubraTestsUtils.sleep(2000);
                                return null;
                            });
                            IntegrationTestsUtils.debugPrint(Thread.currentThread().getName() + ": releasedWriteLock for " + pid, testsProperties);
                            result.push(Thread.currentThread().getName());
                        }
                    };
                } else {
                    return new ConcurrencyUtils.TestTask(taskNumber + "") {
                        @Override
                        public void run() {
                            super.run();
                            String pid = PID_MONOGRAPH;
                            AkubraTestsUtils.sleep(1000); // make sure the first thread has time to acquire lock
                            akubraRepository.doWithReadLock(pid, () -> {
                                result.push(Thread.currentThread().getName());
                                IntegrationTestsUtils.debugPrint(Thread.currentThread().getName() + ": acquiredReadLock for " + pid, testsProperties);
                                return null;
                            });
                            IntegrationTestsUtils.debugPrint(Thread.currentThread().getName() + ": releasedReadLock for " + pid, testsProperties);
                            result.push(Thread.currentThread().getName());
                        }
                    };
                }
            }
        });
        String last = result.pop();
        String secondLast = result.pop();
        assertEquals(last, secondLast);
        String thirdLast = result.pop();
        String fourthLast = result.pop();
        assertEquals(thirdLast, fourthLast);
        assertTrue(result.isEmpty());
    }

    @Test
    void testWriteLockAndWriteLock() {
        Stack<String> result = new Stack<>();
        ConcurrencyUtils.runFactoryTasks(2, new Function<>() {
            @Override
            public ConcurrencyUtils.TestTask apply(Integer taskNumber) {
                if (taskNumber == 1) {
                    return new ConcurrencyUtils.TestTask(taskNumber + "") {
                        @Override
                        public void run() {
                            super.run();
                            String pid = PID_MONOGRAPH;
                            akubraRepository.doWithWriteLock(pid, () -> {
                                result.push(Thread.currentThread().getName());
                                IntegrationTestsUtils.debugPrint(Thread.currentThread().getName() + ": acquiredWriteLock for " + pid, testsProperties);
                                AkubraTestsUtils.sleep(2000);
                                return null;
                            });
                            IntegrationTestsUtils.debugPrint(Thread.currentThread().getName() + ": releasedWriteLock for " + pid, testsProperties);
                            result.push(Thread.currentThread().getName());
                        }
                    };
                } else {
                    return new ConcurrencyUtils.TestTask(taskNumber + "") {
                        @Override
                        public void run() {
                            super.run();
                            String pid = PID_MONOGRAPH;
                            AkubraTestsUtils.sleep(1000); // make sure the first thread has time to acquire lock
                            akubraRepository.doWithWriteLock(pid, () -> {
                                result.push(Thread.currentThread().getName());
                                IntegrationTestsUtils.debugPrint(Thread.currentThread().getName() + ": acquiredWriteLock for " + pid, testsProperties);
                                return null;
                            });
                            IntegrationTestsUtils.debugPrint(Thread.currentThread().getName() + ": releasedWriteLock for " + pid, testsProperties);
                            result.push(Thread.currentThread().getName());
                        }
                    };
                }
            }
        });
        String last = result.pop();
        String secondLast = result.pop();
        assertEquals(last, secondLast);
        String thirdLast = result.pop();
        String fourthLast = result.pop();
        assertEquals(thirdLast, fourthLast);
        assertTrue(result.isEmpty());
    }

}
