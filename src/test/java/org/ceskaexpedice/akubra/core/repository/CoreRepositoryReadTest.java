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
package org.ceskaexpedice.akubra.core.repository;

import org.ceskaexpedice.akubra.RepositoryConfiguration;
import org.ceskaexpedice.akubra.core.CoreRepositoryFactory;
import org.ceskaexpedice.akubra.core.lock.hazelcast.HazelcastConfiguration;
import org.ceskaexpedice.akubra.core.lock.hazelcast.ServerNode;
import org.ceskaexpedice.akubra.testutils.TestUtilities;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.locks.Lock;

import static org.junit.jupiter.api.Assertions.*;

public class CoreRepositoryReadTest {
    private static final String PID_TITLE_PAGE = "uuid:12993b4a-71b4-4f19-8953-0701243cc25d";
    private static final String PID_NOT_EXISTS = "uuid:92993b4a-71b4-4f19-8953-0701243cc25d";

    private static CoreRepository coreRepository;
    private static Properties testsProperties;

    @BeforeAll
    static void beforeAll() {
        testsProperties = TestUtilities.loadProperties();
        HazelcastConfiguration hazelcastConfig = TestUtilities.createHazelcastConfig(testsProperties);
        ServerNode.ensureHazelcastNode(hazelcastConfig);

        URL resource = TestUtilities.class.getClassLoader().getResource("data");
        RepositoryConfiguration config = TestUtilities.createRepositoryConfig(resource.getFile(), testsProperties, hazelcastConfig);
        coreRepository = CoreRepositoryFactory.createRepository(config);
    }

    @AfterAll
    static void afterAll() {
        coreRepository.shutdown();
        ServerNode.shutdown();
    }

    @Test
    void testObjectExists() {
        boolean objectExists = coreRepository.objectExists(PID_TITLE_PAGE);
        assertTrue(objectExists);
    }

    @Test
    void testGetObject() {
        final RepositoryObject[] repositoryObject = {null};
        assertThrows(RepositoryException.class, () -> {
            repositoryObject[0] = coreRepository.getObject("WrongPidFormat", true);
        });
        repositoryObject[0] = coreRepository.getObject(PID_NOT_EXISTS, true);
        assertNull(repositoryObject[0]);
        repositoryObject[0] = coreRepository.getObject(PID_TITLE_PAGE, true);
        assertNotNull(repositoryObject[0]);
    }

    @Test
    void testResolveArchivedDatastreams() {
        RepositoryObject repositoryObject = coreRepository.getObject(PID_TITLE_PAGE);
        RepositoryDatastream thumbStream = repositoryObject.getStream(KnownDatastreams.IMG_THUMB.name());
        assertNull(thumbStream.getDatastream().getDatastreamVersion().get(0).getBinaryContent());
        coreRepository.resolveArchivedDatastreams(repositoryObject.getDigitalObject());
        assertNotNull(thumbStream.getDatastream().getDatastreamVersion().get(0).getBinaryContent());
    }

    @Test
    void testMarshalling() {
        RepositoryObject repositoryObject = coreRepository.getObject(PID_TITLE_PAGE);
        InputStream inputStream = coreRepository.marshallObject(repositoryObject.getDigitalObject());
        assertNotNull(inputStream);
        DigitalObject digitalObject = coreRepository.unmarshallObject(inputStream);
        assertEquals(repositoryObject.getDigitalObject().getDatastream().size(), digitalObject.getDatastream().size());
    }


    @Test
    void testLocks_simple() {
        Lock readLock = null;
        try {
            readLock = coreRepository.getReadLock(PID_TITLE_PAGE);
            assertNotNull(readLock);
        } finally {
            if(readLock != null){
                readLock.unlock();
            }
        }
    }

    @Test
    void testGetProcessingIndexFeeder() {
        ProcessingIndexFeeder processingIndexFeeder = coreRepository.getProcessingIndexFeeder();
        assertNotNull(processingIndexFeeder);
    }


}
