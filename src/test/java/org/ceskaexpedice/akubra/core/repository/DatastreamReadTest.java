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

import org.ceskaexpedice.akubra.HazelcastServerNode;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.config.HazelcastConfiguration;
import org.ceskaexpedice.akubra.config.RepositoryConfiguration;
import org.ceskaexpedice.akubra.core.CoreRepositoryFactory;
import org.ceskaexpedice.akubra.utils.Dom4jUtils;
import org.ceskaexpedice.test.AkubraTestsUtils;
import org.ceskaexpedice.test.IntegrationTestsUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Properties;

import static org.ceskaexpedice.test.AkubraTestsUtils.PID_TITLE_PAGE;
import static org.ceskaexpedice.test.AkubraTestsUtils.TEST_REPOSITORY;
import static org.junit.jupiter.api.Assertions.*;

public class DatastreamReadTest {
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
    void testDatastreamExists() {
        boolean exists = coreRepository.datastreamExists(PID_TITLE_PAGE, KnownDatastreams.BIBLIO_DC.toString());
        assertTrue(exists);
        exists = coreRepository.datastreamExists(PID_TITLE_PAGE, "unknownStream");
        assertFalse(exists);
    }

    @Test
    void testGetDatastreamContent() {
        InputStream imgThumb = coreRepository.getDatastreamContent(PID_TITLE_PAGE, "nonExistentStream");
        assertNull(imgThumb);
        imgThumb = coreRepository.getDatastreamContent(PID_TITLE_PAGE, KnownDatastreams.IMG_THUMB.toString());
        assertNotNull(imgThumb);
        InputStream relsExtIs = coreRepository.getDatastreamContent(PID_TITLE_PAGE, KnownDatastreams.RELS_EXT.toString());
        assertNotNull(relsExtIs);
        IntegrationTestsUtils.debugPrint(Dom4jUtils.streamToDocument(relsExtIs, true).asXML(), testsProperties);
        /* Uncomment for check
        try {
            IOUtils.copy(imgThumb, new FileOutputStream("c:\\tmp\\pepoTest.jpg"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
         */
    }

    @Test
    void testRetrieveDatastreamByInternalId() {
        final String DS_KEY = "uuid:12993b4a-71b4-4f19-8953-0701243cc25d+IMG_THUMB+IMG_THUMB.0";
        InputStream imgThumb = coreRepository.retrieveDatastreamByInternalId(DS_KEY);
        assertNotNull(imgThumb);
    }

}
