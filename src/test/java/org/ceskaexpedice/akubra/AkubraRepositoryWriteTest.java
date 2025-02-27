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
import org.ceskaexpedice.akubra.config.HazelcastConfiguration;
import org.ceskaexpedice.akubra.config.RepositoryConfiguration;
import org.ceskaexpedice.akubra.core.CoreRepositoryFactory;
import org.ceskaexpedice.akubra.core.lock.hazelcast.ServerNode;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndex;
import org.ceskaexpedice.akubra.relsext.RelsExtLiteral;
import org.ceskaexpedice.akubra.relsext.RelsExtRelation;
import org.ceskaexpedice.akubra.relsext.RelsExtWrapper;
import org.ceskaexpedice.akubra.testutils.TestUtilities;
import org.ceskaexpedice.akubra.utils.Dom4jUtils;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.dom4j.Document;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.mockito.Mockito.*;

public class AkubraRepositoryWriteTest {
    private static final Path TEST_REPOSITORY = Path.of("src/test/resources/data");
    private static final Path TEST_OUTPUT_REPOSITORY = Path.of("testoutput/data");
    private static final String PID_MONOGRAPH = "uuid:5035a48a-5e2e-486c-8127-2fa650842e46";
    private static final String PID_TITLE_PAGE = "uuid:12993b4a-71b4-4f19-8953-0701243cc25d";
    private static final String PID_IMPORTED = "uuid:32993b4a-71b4-4f19-8953-0701243cc25d";

    private static Properties testsProperties;
    private static HazelcastConfiguration hazelcastConfig;
    private static ProcessingIndex mockFeeder;
    private static AkubraRepository akubraRepository;

    @BeforeAll
    static void beforeAll() {
        testsProperties = TestUtilities.loadProperties();
        hazelcastConfig = TestUtilities.createHazelcastConfig(testsProperties);
        ServerNode.ensureHazelcastNode(hazelcastConfig);
        // configure akubraRepository
        mockFeeder = mock(ProcessingIndex.class);
        try (MockedStatic<CoreRepositoryFactory> mockedStatic = mockStatic(CoreRepositoryFactory.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(() -> CoreRepositoryFactory.createProcessingIndexFeeder(any())).thenReturn(mockFeeder);
            mockedStatic.when(() -> CoreRepositoryFactory.createCacheManager()).thenReturn(null);
            RepositoryConfiguration config = TestUtilities.createRepositoryConfig(TEST_OUTPUT_REPOSITORY.toFile().getAbsolutePath(), testsProperties, hazelcastConfig);
            akubraRepository = AkubraRepositoryFactory.createRepository(config);
        }
    }

    @AfterAll
    static void afterAll() {
        akubraRepository.shutdown();
        ServerNode.shutdown();
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
    void testIngest() throws IOException {
        // prepare import document
        DigitalObject digitalObjectImported = akubraRepository.getObject(PID_IMPORTED, FoxmlType.managed).asDigitalObject();
        Assertions.assertNull(digitalObjectImported);
        Path importFile = Path.of("src/test/resources/titlePageImport.xml");
        InputStream inputStream = Files.newInputStream(importFile);
        DigitalObject digitalObject = akubraRepository.unmarshallObject(inputStream);
        // ingest document
        reset(mockFeeder);
        akubraRepository.ingest(digitalObject);
        // test ingest result
        digitalObjectImported = akubraRepository.getObject(PID_IMPORTED, FoxmlType.managed).asDigitalObject();
        Assertions.assertNotNull(digitalObjectImported);
        verify(mockFeeder, times(1)).rebuildProcessingIndex(any(), any());
        verify(mockFeeder, times(1)).commit();
    }

    @Test
    void testDeleteObject() {
        DigitalObject repositoryObject = akubraRepository.getObject(PID_TITLE_PAGE, FoxmlType.managed).asDigitalObject();
        Assertions.assertNotNull(repositoryObject);
        reset(mockFeeder);
        akubraRepository.deleteObject(PID_TITLE_PAGE);
        repositoryObject = akubraRepository.getObject(PID_TITLE_PAGE, FoxmlType.managed).asDigitalObject();
        Assertions.assertNull(repositoryObject);
        verify(mockFeeder, times(1)).deleteByRelationsForPid(eq(PID_TITLE_PAGE));
        verify(mockFeeder, times(1)).deleteByTargetPid(eq(PID_TITLE_PAGE));
        verify(mockFeeder, times(1)).deleteDescriptionByPid(eq(PID_TITLE_PAGE));
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

        DigitalObject digitalObject = akubraRepository.getObject(PID_MONOGRAPH).asDigitalObject();
        Document document = Dom4jUtils.streamToDocument(akubraRepository.marshallObject(digitalObject), true);
        TestUtilities.debugPrint(document.asXML(),testsProperties);
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

        DigitalObject digitalObject = akubraRepository.getObject(PID_MONOGRAPH).asDigitalObject();
        Document document = Dom4jUtils.streamToDocument(akubraRepository.marshallObject(digitalObject), true);
        TestUtilities.debugPrint(document.asXML(),testsProperties);
    }

    @Test
    void testCreateRedirectedDatastream() {
        boolean datastreamExists = akubraRepository.datastreamExists(PID_MONOGRAPH, "pepo");
        Assertions.assertFalse(datastreamExists);

        akubraRepository.createRedirectedDatastream(PID_MONOGRAPH, "pepo", "http://www.pepo.cz", "image/jpeg");
        datastreamExists = akubraRepository.datastreamExists(PID_MONOGRAPH, "pepo");
        Assertions.assertTrue(datastreamExists);

        DigitalObject digitalObject = akubraRepository.getObject(PID_MONOGRAPH).asDigitalObject();
        Document document = Dom4jUtils.streamToDocument(akubraRepository.marshallObject(digitalObject), true);
        TestUtilities.debugPrint(document.asXML(),testsProperties);
    }

    @Test
    void testDeleteDatastream() {
        boolean datastreamExists = akubraRepository.datastreamExists(PID_MONOGRAPH, KnownDatastreams.RELS_EXT.toString());
        Assertions.assertTrue(datastreamExists);

        akubraRepository.deleteDatastream(PID_MONOGRAPH, KnownDatastreams.RELS_EXT.toString());
        datastreamExists = akubraRepository.datastreamExists(PID_MONOGRAPH, KnownDatastreams.RELS_EXT.toString());
        Assertions.assertFalse(datastreamExists);
    }

    @Test
    void testRelsExtAddRelation() {
        RelsExtWrapper relsExtWrapper = akubraRepository.getRelsExtHandler().get(PID_TITLE_PAGE);
        List<RelsExtRelation> relations = relsExtWrapper.getRelations(null);
        Assertions.assertEquals(1, relations.size());

        akubraRepository.getRelsExtHandler().addRelation(PID_TITLE_PAGE, "kramerius:hasPage",
                "http://www.nsdl.org/ontologies/relationships#", "info:fedora/uuid:12993b4a-71b4-4f19-8953-0701243cc25d");
        relsExtWrapper = akubraRepository.getRelsExtHandler().get(PID_TITLE_PAGE);
        relations = relsExtWrapper.getRelations(null);
        Assertions.assertEquals(2, relations.size());

        DigitalObject digitalObject = akubraRepository.getObject(PID_TITLE_PAGE).asDigitalObject();
        Document document = Dom4jUtils.streamToDocument(akubraRepository.marshallObject(digitalObject), true);
        TestUtilities.debugPrint(document.asXML(),testsProperties);
    }

    @Test
    void testRelsExtRemoveRelation() {
        RelsExtWrapper relsExtWrapper = akubraRepository.getRelsExtHandler().get(PID_TITLE_PAGE);
        List<RelsExtRelation> relations = relsExtWrapper.getRelations(null);
        Assertions.assertEquals(1, relations.size());

        akubraRepository.getRelsExtHandler().removeRelation(PID_TITLE_PAGE, "hasModel",
                "info:fedora/fedora-system:def/model#", "model:page");
        relsExtWrapper = akubraRepository.getRelsExtHandler().get(PID_TITLE_PAGE);
        relations = relsExtWrapper.getRelations(null);
        Assertions.assertEquals(0, relations.size());
    }

    @Test
    void testRelsExtAddLiteral() {
        RelsExtWrapper relsExtWrapper = akubraRepository.getRelsExtHandler().get(PID_TITLE_PAGE);
        List<RelsExtLiteral> literals = relsExtWrapper.getLiterals(null);
        Assertions.assertEquals(4, literals.size());

        akubraRepository.getRelsExtHandler().addLiteral(PID_TITLE_PAGE, "pepoItemID",
                "http://www.openarchives.org/OAI/2.0/", "uuid:12993b4a-71b4-4f19-8953-0701243cc25d");
        relsExtWrapper = akubraRepository.getRelsExtHandler().get(PID_TITLE_PAGE);
        literals = relsExtWrapper.getLiterals(null);
        Assertions.assertEquals(5, literals.size());

        DigitalObject digitalObject = akubraRepository.getObject(PID_TITLE_PAGE).asDigitalObject();
        Document document = Dom4jUtils.streamToDocument(akubraRepository.marshallObject(digitalObject), true);
        TestUtilities.debugPrint(document.asXML(),testsProperties);
    }

    @Test
    void testRelsExtRemoveLiteral() {
        RelsExtWrapper relsExtWrapper = akubraRepository.getRelsExtHandler().get(PID_TITLE_PAGE);
        List<RelsExtLiteral> literals = relsExtWrapper.getLiterals(null);
        Assertions.assertEquals(4, literals.size());

        akubraRepository.getRelsExtHandler().removeLiteral(PID_TITLE_PAGE, "itemID",
                "http://www.openarchives.org/OAI/2.0/", "uuid:12993b4a-71b4-4f19-8953-0701243cc25d");
        relsExtWrapper = akubraRepository.getRelsExtHandler().get(PID_TITLE_PAGE);
        literals = relsExtWrapper.getLiterals(null);
        Assertions.assertEquals(3, literals.size());
    }

}
