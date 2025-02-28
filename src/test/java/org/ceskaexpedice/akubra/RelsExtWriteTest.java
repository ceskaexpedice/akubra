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
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexSolr;
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
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;

import static org.ceskaexpedice.akubra.testutils.TestUtilities.*;
import static org.mockito.Mockito.*;

public class RelsExtWriteTest {
    private static Properties testsProperties;
    private static ProcessingIndexSolr mockFeeder;
    private static AkubraRepository akubraRepository;

    @BeforeAll
    static void beforeAll() {
        testsProperties = TestUtilities.loadProperties();
        HazelcastConfiguration hazelcastConfig = TestUtilities.createHazelcastConfig(testsProperties);
        HazelcastServerNode.ensureHazelcastNode(hazelcastConfig);
        // configure akubraRepository
        mockFeeder = mock(ProcessingIndexSolr.class);
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
