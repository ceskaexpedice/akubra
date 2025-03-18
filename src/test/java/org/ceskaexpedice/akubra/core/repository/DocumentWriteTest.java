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
import org.ceskaexpedice.akubra.HazelcastServerNode;
import org.ceskaexpedice.akubra.config.HazelcastConfiguration;
import org.ceskaexpedice.akubra.config.RepositoryConfiguration;
import org.ceskaexpedice.akubra.core.CoreRepositoryFactory;
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexSolr;
import org.ceskaexpedice.akubra.core.repository.impl.CoreRepositoryImpl;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.ceskaexpedice.test.AkubraTestsUtils;
import org.ceskaexpedice.test.IntegrationTestsUtils;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.ceskaexpedice.test.AkubraTestsUtils.*;
import static org.mockito.Mockito.*;

public class DocumentWriteTest {
    private static Properties testsProperties;
    private static ProcessingIndexSolr mockFeeder;
    private static CoreRepository coreRepository;

    @BeforeAll
    static void beforeAll() {
        testsProperties = IntegrationTestsUtils.loadProperties();
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
    void testIngest() throws IOException {
        // prepare import document
        RepositoryObject digitalObjectImported = coreRepository.getAsRepositoryObject(PID_IMPORTED);
        Assertions.assertNull(digitalObjectImported);
        Path importFile = Path.of("src/test/resources/titlePageImport.xml");
        InputStream inputStream = Files.newInputStream(importFile);
        DigitalObject digitalObject = coreRepository.unmarshall(inputStream);
        // ingest document
        reset(mockFeeder);
        coreRepository.ingest(digitalObject);
        // test ingest result
        digitalObjectImported = coreRepository.getAsRepositoryObject(PID_IMPORTED);
        Assertions.assertNotNull(digitalObjectImported);
        verify(mockFeeder, times(1)).rebuildProcessingIndex(any());
    }

    @Test
    void testDeleteObject() {
        RepositoryObject repositoryObject = coreRepository.getAsRepositoryObject(PID_TITLE_PAGE);
        Assertions.assertNotNull(repositoryObject);
        reset(mockFeeder);
        coreRepository.delete(PID_TITLE_PAGE);
        repositoryObject = coreRepository.getAsRepositoryObject(PID_TITLE_PAGE);
        Assertions.assertNull(repositoryObject);
        verify(mockFeeder, times(1)).deleteByRelationsForPid(eq(PID_TITLE_PAGE));
        verify(mockFeeder, times(1)).deleteByTargetPid(eq(PID_TITLE_PAGE));
        verify(mockFeeder, times(1)).deleteDescriptionByPid(eq(PID_TITLE_PAGE));
    }

}
