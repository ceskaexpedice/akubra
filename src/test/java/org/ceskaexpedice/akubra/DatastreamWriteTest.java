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

import org.apache.commons.io.FileUtils;
import org.ceskaexpedice.akubra.config.RepositoryConfiguration;
import org.ceskaexpedice.akubra.core.CoreRepositoryFactory;
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexSolr;
import org.ceskaexpedice.akubra.core.repository.CoreRepository;
import org.ceskaexpedice.akubra.core.repository.impl.CoreRepositoryImpl;
import org.ceskaexpedice.akubra.utils.Dom4jUtils;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.ceskaexpedice.hazelcast.HazelcastConfiguration;
import org.ceskaexpedice.hazelcast.HazelcastServerNode;
import org.ceskaexpedice.test.AkubraTestsUtils;
import org.ceskaexpedice.test.IntegrationTestsUtils;
import org.dom4j.Document;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.ceskaexpedice.test.AkubraTestsUtils.*;
import static org.mockito.Mockito.mock;

public class DatastreamWriteTest {
    private static Properties testsProperties;
    private static ProcessingIndexSolr mockFeeder;
    private static AkubraRepository akubraRepository;

    @BeforeAll
    static void beforeAll() {
        testsProperties = IntegrationTestsUtils.loadProperties();
        HazelcastConfiguration hazelcastConfig = AkubraTestsUtils.createHazelcastConfig(testsProperties);
        HazelcastServerNode.ensureHazelcastNode(hazelcastConfig);
        // configure akubraRepository
        mockFeeder = mock(ProcessingIndexSolr.class);
        RepositoryConfiguration config = AkubraTestsUtils.createRepositoryConfig(TEST_OUTPUT_REPOSITORY.toFile().getAbsolutePath(), testsProperties, hazelcastConfig);
        CoreRepository coreRepository = CoreRepositoryFactory.createRepository(config);
        ((CoreRepositoryImpl)coreRepository).setProcessingIndex(mockFeeder);
        akubraRepository = AkubraRepositoryFactory.createRepository(coreRepository);
    }

    @AfterAll
    static void afterAll() {
        akubraRepository.shutdown();
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
        boolean datastreamExists = akubraRepository.datastreamExists(PID_MONOGRAPH, "pepo");
        Assertions.assertFalse(datastreamExists);

        Path importFile = Path.of("src/test/resources/xmlStream.xml");
        InputStream inputStream = Files.newInputStream(importFile);
        akubraRepository.createXMLDatastream(PID_MONOGRAPH, "pepo", "text/xml", inputStream);
        datastreamExists = akubraRepository.datastreamExists(PID_MONOGRAPH, "pepo");
        Assertions.assertTrue(datastreamExists);

        DigitalObject digitalObject = akubraRepository.get(PID_MONOGRAPH).asDigitalObject();
        Document document = Dom4jUtils.streamToDocument(akubraRepository.marshall(digitalObject), true);
        IntegrationTestsUtils.debugPrint(document.asXML(),testsProperties);
    }

    @Test
    void testCreateManagedDatastream() throws IOException {
        boolean datastreamExists = akubraRepository.datastreamExists(PID_MONOGRAPH, "pepo");
        Assertions.assertFalse(datastreamExists);

        Path importFile = Path.of("src/test/resources/thumbnail.jpg");
        InputStream inputStream = Files.newInputStream(importFile);
        akubraRepository.createManagedDatastream(PID_MONOGRAPH, "pepo", "image/jpeg", inputStream);
        datastreamExists = akubraRepository.datastreamExists(PID_MONOGRAPH, "pepo");
        Assertions.assertTrue(datastreamExists);

        DigitalObject digitalObject = akubraRepository.get(PID_MONOGRAPH).asDigitalObject();
        Document document = Dom4jUtils.streamToDocument(akubraRepository.marshall(digitalObject), true);
        IntegrationTestsUtils.debugPrint(document.asXML(),testsProperties);
    }

    @Test
    void testCreateRedirectedDatastream() {
        boolean datastreamExists = akubraRepository.datastreamExists(PID_MONOGRAPH, "pepo");
        Assertions.assertFalse(datastreamExists);

        akubraRepository.createRedirectedDatastream(PID_MONOGRAPH, "pepo", "http://www.pepo.cz", "image/jpeg");
        datastreamExists = akubraRepository.datastreamExists(PID_MONOGRAPH, "pepo");
        Assertions.assertTrue(datastreamExists);

        DigitalObject digitalObject = akubraRepository.get(PID_MONOGRAPH).asDigitalObject();
        Document document = Dom4jUtils.streamToDocument(akubraRepository.marshall(digitalObject), true);
        IntegrationTestsUtils.debugPrint(document.asXML(),testsProperties);
    }

    @Test
    void testDeleteDatastream() {
        boolean datastreamExists = akubraRepository.datastreamExists(PID_MONOGRAPH, KnownDatastreams.RELS_EXT);
        Assertions.assertTrue(datastreamExists);

        akubraRepository.deleteDatastream(PID_MONOGRAPH, KnownDatastreams.RELS_EXT);
        datastreamExists = akubraRepository.datastreamExists(PID_MONOGRAPH, KnownDatastreams.RELS_EXT);
        Assertions.assertFalse(datastreamExists);
    }

}
