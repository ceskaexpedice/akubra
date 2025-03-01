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
import org.ceskaexpedice.test.FunctionalTestsUtils;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.ceskaexpedice.akubra.AkubraTestsUtils.*;
import static org.ceskaexpedice.test.FunctionalTestsUtils.*;
import static org.mockito.Mockito.*;

public class DigitalObjectWriteTest {
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
        try (MockedStatic<CoreRepositoryFactory> mockedStatic = mockStatic(CoreRepositoryFactory.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(() -> CoreRepositoryFactory.createProcessingIndexFeeder(any())).thenReturn(mockFeeder);
            mockedStatic.when(() -> CoreRepositoryFactory.createCacheManager()).thenReturn(null);
            RepositoryConfiguration config = AkubraTestsUtils.createRepositoryConfig(TEST_OUTPUT_REPOSITORY.toFile().getAbsolutePath(), testsProperties, hazelcastConfig);
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
    void testIngest() throws IOException {
        // prepare import document
        DigitalObject digitalObjectImported = akubraRepository.getObject(PID_IMPORTED).asDigitalObject();
        Assertions.assertNull(digitalObjectImported);
        Path importFile = Path.of("src/test/resources/titlePageImport.xml");
        InputStream inputStream = Files.newInputStream(importFile);
        DigitalObject digitalObject = akubraRepository.unmarshallObject(inputStream);
        // ingest document
        reset(mockFeeder);
        akubraRepository.ingest(digitalObject);
        // test ingest result
        digitalObjectImported = akubraRepository.getObject(PID_IMPORTED).asDigitalObject();
        Assertions.assertNotNull(digitalObjectImported);
        verify(mockFeeder, times(1)).rebuildProcessingIndex(any(), any());
        verify(mockFeeder, times(1)).commit();
    }

    @Test
    void testDeleteObject() {
        DigitalObject repositoryObject = akubraRepository.getObject(PID_TITLE_PAGE).asDigitalObject();
        Assertions.assertNotNull(repositoryObject);
        reset(mockFeeder);
        akubraRepository.deleteObject(PID_TITLE_PAGE);
        repositoryObject = akubraRepository.getObject(PID_TITLE_PAGE, FoxmlType.managed).asDigitalObject();
        Assertions.assertNull(repositoryObject);
        verify(mockFeeder, times(1)).deleteByRelationsForPid(eq(PID_TITLE_PAGE));
        verify(mockFeeder, times(1)).deleteByTargetPid(eq(PID_TITLE_PAGE));
        verify(mockFeeder, times(1)).deleteDescriptionByPid(eq(PID_TITLE_PAGE));
    }

}
