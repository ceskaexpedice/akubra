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

import org.apache.commons.io.FileUtils;
import org.ceskaexpedice.test.AkubraTestsUtils;
import org.ceskaexpedice.akubra.HazelcastServerNode;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.config.HazelcastConfiguration;
import org.ceskaexpedice.akubra.config.RepositoryConfiguration;
import org.ceskaexpedice.akubra.core.CoreRepositoryFactory;
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexSolr;
import org.ceskaexpedice.akubra.core.repository.impl.CoreRepositoryImpl;
import org.ceskaexpedice.akubra.utils.Dom4jUtils;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.ceskaexpedice.test.FunctionalTestsUtils;
import org.dom4j.Document;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.ceskaexpedice.test.AkubraTestsUtils.*;
import static org.mockito.Mockito.*;

public class DatastreamWriteTest {
    private static Properties testsProperties;
    private static ProcessingIndexSolr mockFeeder;
    private static CoreRepository coreRepository;

    @BeforeAll
    static void beforeAll() {
        testsProperties = FunctionalTestsUtils.loadProperties();
        HazelcastConfiguration hazelcastConfig = AkubraTestsUtils.createHazelcastConfig(testsProperties);
        HazelcastServerNode.ensureHazelcastNode(hazelcastConfig);
        // configure repository
        mockFeeder = mock(ProcessingIndexSolr.class);
        RepositoryConfiguration config = AkubraTestsUtils.createRepositoryConfig(TEST_OUTPUT_REPOSITORY.toFile().getAbsolutePath(), testsProperties, hazelcastConfig);
        coreRepository = CoreRepositoryFactory.createRepository(config);
        ((CoreRepositoryImpl)coreRepository).setProcessingIndex(mockFeeder);
    }

    @AfterAll
    static void afterAll() {
        coreRepository.shutdown();
        HazelcastServerNode.shutdown();
    }

    @BeforeEach
    void beforeEach() throws IOException {
        if (Files.exists(TEST_OUTPUT_REPOSITORY)) {
            FileUtils.deleteDirectory(TEST_OUTPUT_REPOSITORY.toFile());
        }
        Files.createDirectories(TEST_OUTPUT_REPOSITORY);
        FileUtils.copyDirectory(TEST_REPOSITORY.toFile(), TEST_OUTPUT_REPOSITORY.toFile());
    }

    @AfterEach
    void afterEach() {
    }

    @Test
    void testCreateXMLDatastream() throws IOException {
        boolean datastreamExists = coreRepository.datastreamExists(PID_MONOGRAPH, "pepo");
        Assertions.assertFalse(datastreamExists);

        Path importFile = Path.of("src/test/resources/xmlStream.xml");
        InputStream inputStream = Files.newInputStream(importFile);
        RepositoryObject asRepositoryObject = coreRepository.getAsRepositoryObject(PID_MONOGRAPH);
        coreRepository.createXMLDatastream(asRepositoryObject, "pepo", "text/xml", inputStream);
        datastreamExists = coreRepository.datastreamExists(PID_MONOGRAPH, "pepo");
        Assertions.assertTrue(datastreamExists);

        DigitalObject digitalObject = coreRepository.getAsRepositoryObject(PID_MONOGRAPH).getDigitalObject();
        Document document = Dom4jUtils.streamToDocument(coreRepository.marshall(digitalObject), true);
        FunctionalTestsUtils.debugPrint(document.asXML(),testsProperties);
    }

    @Test
    void testCreateManagedDatastream() throws IOException {
        boolean datastreamExists = coreRepository.datastreamExists(PID_MONOGRAPH, "pepo");
        Assertions.assertFalse(datastreamExists);

        Path importFile = Path.of("src/test/resources/thumbnail.jpg");
        InputStream inputStream = Files.newInputStream(importFile);
        RepositoryObject asRepositoryObject = coreRepository.getAsRepositoryObject(PID_MONOGRAPH);
        coreRepository.createManagedDatastream(asRepositoryObject, "pepo", "image/jpeg", inputStream);
        datastreamExists = coreRepository.datastreamExists(PID_MONOGRAPH, "pepo");
        Assertions.assertTrue(datastreamExists);

        DigitalObject digitalObject = coreRepository.getAsRepositoryObject(PID_MONOGRAPH).getDigitalObject();
        Document document = Dom4jUtils.streamToDocument(coreRepository.marshall(digitalObject), true);
        FunctionalTestsUtils.debugPrint(document.asXML(),testsProperties);
    }

    @Test
    void testCreateRedirectedDatastream() {
        boolean datastreamExists = coreRepository.datastreamExists(PID_MONOGRAPH, "pepo");
        Assertions.assertFalse(datastreamExists);

        RepositoryObject asRepositoryObject = coreRepository.getAsRepositoryObject(PID_MONOGRAPH);
        coreRepository.createRedirectedDatastream(asRepositoryObject, "pepo", "http://www.pepo.cz", "image/jpeg");
        datastreamExists = coreRepository.datastreamExists(PID_MONOGRAPH, "pepo");
        Assertions.assertTrue(datastreamExists);

        DigitalObject digitalObject = coreRepository.getAsRepositoryObject(PID_MONOGRAPH).getDigitalObject();
        Document document = Dom4jUtils.streamToDocument(coreRepository.marshall(digitalObject), true);
        FunctionalTestsUtils.debugPrint(document.asXML(),testsProperties);
    }

    @Test
    void testDeleteDatastream() {
        boolean datastreamExists = coreRepository.datastreamExists(PID_MONOGRAPH, KnownDatastreams.RELS_EXT.toString());
        Assertions.assertTrue(datastreamExists);

        coreRepository.deleteDatastream(PID_MONOGRAPH, KnownDatastreams.RELS_EXT.toString());
        datastreamExists = coreRepository.datastreamExists(PID_MONOGRAPH, KnownDatastreams.RELS_EXT.toString());
        Assertions.assertFalse(datastreamExists);
    }

}
