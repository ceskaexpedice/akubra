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

import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.config.RepositoryConfiguration;
import org.ceskaexpedice.akubra.core.CoreRepositoryFactory;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndex;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.ceskaexpedice.hazelcast.HazelcastConfiguration;
import org.ceskaexpedice.hazelcast.HazelcastServerNode;
import org.ceskaexpedice.testutils.AkubraTestsUtils;
import org.ceskaexpedice.testutils.IntegrationTestsUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Properties;

import static org.ceskaexpedice.testutils.AkubraTestsUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class DocumentReadTest {
    private static CoreRepository coreRepository;
    private static Properties testsProperties;

    @BeforeAll
    static void beforeAll() {
        testsProperties = IntegrationTestsUtils.loadProperties();
        HazelcastConfiguration hazelcastConfig = AkubraTestsUtils.createHazelcastConfig(testsProperties);
        HazelcastServerNode.ensureHazelcastNode(hazelcastConfig);

        RepositoryConfiguration config = AkubraTestsUtils.createRepositoryConfig(TEST_REPOSITORY.toFile().getAbsolutePath(), testsProperties, hazelcastConfig);
        coreRepository = CoreRepositoryFactory.createRepository(config);
    }

    @AfterAll
    static void afterAll() {
        coreRepository.shutdown();
        HazelcastServerNode.shutdown();
    }

    @Test
    void testExists() {
        boolean objectExists = coreRepository.exists(PID_TITLE_PAGE);
        assertTrue(objectExists);
        objectExists = coreRepository.exists(PID_NOT_EXISTS);
        assertFalse(objectExists);
        objectExists = coreRepository.exists("nonsensePid");
        assertFalse(objectExists);
    }

    @Test
    void testGetAsRepositoryObject() {
        final RepositoryObject[] repositoryObject = {null};
        assertThrows(RepositoryException.class, () -> {
            repositoryObject[0] = coreRepository.getAsRepositoryObject("WrongPidFormat");
        });
        repositoryObject[0] = coreRepository.getAsRepositoryObject(PID_NOT_EXISTS);
        assertNull(repositoryObject[0]);
        repositoryObject[0] = coreRepository.getAsRepositoryObject(PID_TITLE_PAGE);
        assertNotNull(repositoryObject[0]);
    }

    @Test
    void testGetAsBytes() {
        byte[] asBytes = coreRepository.getAsBytes(PID_TITLE_PAGE);
        assertNotNull(asBytes);
        asBytes = coreRepository.getAsBytes(PID_NOT_EXISTS);
        assertNull(asBytes);
    }

    @Test
    void testResolveArchivedDatastreams() {
        RepositoryObject repositoryObject = coreRepository.getAsRepositoryObject(PID_TITLE_PAGE);
        RepositoryDatastream thumbStream = repositoryObject.getStream(KnownDatastreams.IMG_THUMB.name());
        assertNull(thumbStream.getDatastream().getDatastreamVersion().get(0).getBinaryContent());
        coreRepository.resolveArchivedDatastreams(repositoryObject.getDigitalObject());
        assertNotNull(thumbStream.getDatastream().getDatastreamVersion().get(0).getBinaryContent());
    }

    @Test
    void testMarshalling() {
        RepositoryObject repositoryObject = coreRepository.getAsRepositoryObject(PID_TITLE_PAGE);
        InputStream inputStream = coreRepository.marshall(repositoryObject.getDigitalObject());
        assertNotNull(inputStream);
        DigitalObject digitalObject = coreRepository.unmarshall(inputStream);
        assertEquals(repositoryObject.getDigitalObject().getDatastream().size(), digitalObject.getDatastream().size());
    }

    @Test
    void testGetProcessingIndex() {
        ProcessingIndex processingIndex = coreRepository.getProcessingIndex();
        assertNotNull(processingIndex);
    }

}
