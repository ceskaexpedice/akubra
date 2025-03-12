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
import org.ceskaexpedice.akubra.core.repository.CoreRepository;
import org.ceskaexpedice.akubra.core.repository.impl.CoreRepositoryImpl;
import org.ceskaexpedice.akubra.relsext.RelsExtLiteral;
import org.ceskaexpedice.akubra.relsext.RelsExtRelation;
import org.ceskaexpedice.test.AkubraTestsUtils;
import org.ceskaexpedice.test.FunctionalTestsUtils;
import org.ceskaexpedice.akubra.utils.Dom4jUtils;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.dom4j.Document;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;

import static org.ceskaexpedice.test.AkubraTestsUtils.*;
import static org.mockito.Mockito.*;

public class RelsExtWriteTest {
    private static Properties testsProperties;
    private static ProcessingIndexSolr mockFeeder;
    private static AkubraRepository akubraRepository;

    @BeforeAll
    static void beforeAll() {
        testsProperties = FunctionalTestsUtils.loadProperties();
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
    void testAddRelation() {
        List<RelsExtRelation> relations = akubraRepository.re().getRelations(PID_TITLE_PAGE, null);
        Assertions.assertEquals(1, relations.size());

        akubraRepository.re().addRelation(PID_TITLE_PAGE, "kramerius:hasPage",
                "http://www.nsdl.org/ontologies/relationships#", "info:fedora/uuid:12993b4a-71b4-4f19-8953-0701243cc25d");
        relations = akubraRepository.re().getRelations(PID_TITLE_PAGE, null);
        Assertions.assertEquals(2, relations.size());

        DigitalObject digitalObject = akubraRepository.get(PID_TITLE_PAGE).asDigitalObject();
        Document document = Dom4jUtils.streamToDocument(akubraRepository.marshall(digitalObject), true);
        FunctionalTestsUtils.debugPrint(document.asXML(),testsProperties);
    }

    @Test
    void testRemoveRelation() {
        List<RelsExtRelation> relations = akubraRepository.re().getRelations(PID_TITLE_PAGE, null);
        Assertions.assertEquals(1, relations.size());

        akubraRepository.re().removeRelation(PID_TITLE_PAGE, "hasModel",
                "info:fedora/fedora-system:def/model#", "model:page");
        relations = akubraRepository.re().getRelations(PID_TITLE_PAGE, null);
        Assertions.assertEquals(0, relations.size());
    }

    @Test
    void testAddLiteral() {
        List<RelsExtLiteral> literals = akubraRepository.re().getLiterals(PID_TITLE_PAGE, null);
        Assertions.assertEquals(4, literals.size());

        akubraRepository.re().addLiteral(PID_TITLE_PAGE, "pepoItemID",
                "http://www.openarchives.org/OAI/2.0/", "uuid:12993b4a-71b4-4f19-8953-0701243cc25d");
        literals = akubraRepository.re().getLiterals(PID_TITLE_PAGE, null);
        Assertions.assertEquals(5, literals.size());

        DigitalObject digitalObject = akubraRepository.get(PID_TITLE_PAGE).asDigitalObject();
        Document document = Dom4jUtils.streamToDocument(akubraRepository.marshall(digitalObject), true);
        FunctionalTestsUtils.debugPrint(document.asXML(),testsProperties);
    }

    @Test
    void testRelsExtRemoveLiteral() {
        List<RelsExtLiteral> literals = akubraRepository.re().getLiterals(PID_TITLE_PAGE, null);
        Assertions.assertEquals(4, literals.size());

        akubraRepository.re().removeLiteral(PID_TITLE_PAGE, "itemID",
                "http://www.openarchives.org/OAI/2.0/", "uuid:12993b4a-71b4-4f19-8953-0701243cc25d");
        literals = akubraRepository.re().getLiterals(PID_TITLE_PAGE,null);
        Assertions.assertEquals(3, literals.size());
    }

}
