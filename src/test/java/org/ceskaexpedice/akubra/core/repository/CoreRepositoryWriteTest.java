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
import org.ceskaexpedice.akubra.RepositoryConfiguration;
import org.ceskaexpedice.akubra.core.CoreRepositoryFactory;
import org.ceskaexpedice.akubra.core.lock.hazelcast.HazelcastConfiguration;
import org.ceskaexpedice.akubra.core.lock.hazelcast.ServerNode;
import org.ceskaexpedice.akubra.testutils.TestUtilities;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.mockito.Mockito.*;

public class CoreRepositoryWriteTest {
    private static final Path TEST_REPOSITORY = Path.of("src/test/resources/data");
    private static final Path TEST_OUTPUT_REPOSITORY = Path.of("testoutput/data");
    private static final String PID_MONOGRAPH = "uuid:5035a48a-5e2e-486c-8127-2fa650842e46";
    private static final String PID_TITLE_PAGE = "uuid:12993b4a-71b4-4f19-8953-0701243cc25d";
    private static final String PID_IMPORTED = "uuid:32993b4a-71b4-4f19-8953-0701243cc25d";

    private static Properties testsProperties;
    private static HazelcastConfiguration hazelcastConfig;
    private static ProcessingIndexFeeder mockFeeder;
    private static CoreRepository coreRepository;

    @BeforeAll
    static void beforeAll() {
        testsProperties = TestUtilities.loadProperties();
        hazelcastConfig = TestUtilities.createHazelcastConfig(testsProperties);
        ServerNode.ensureHazelcastNode(hazelcastConfig);
        // configure repository
        mockFeeder = mock(ProcessingIndexFeeder.class);
        try (MockedStatic<CoreRepositoryFactory> mockedStatic = mockStatic(CoreRepositoryFactory.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(() -> CoreRepositoryFactory.createProcessingIndexFeeder(any())).thenReturn(mockFeeder);
            mockedStatic.when(() -> CoreRepositoryFactory.createCacheManager()).thenReturn(null);
            RepositoryConfiguration config = TestUtilities.createRepositoryConfig(TEST_OUTPUT_REPOSITORY.toFile().getAbsolutePath(), testsProperties, hazelcastConfig);
            coreRepository = CoreRepositoryFactory.createRepository(config);
        }
    }

    @AfterAll
    static void afterAll() {
        coreRepository.shutdown();
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
        RepositoryObject digitalObjectImported = coreRepository.getObject(PID_IMPORTED);
        Assertions.assertNull(digitalObjectImported);
        Path importFile = Path.of("src/test/resources/titlePageImport.xml");
        InputStream inputStream = Files.newInputStream(importFile);
        DigitalObject digitalObject = coreRepository.unmarshallObject(inputStream);
        // ingest document
        reset(mockFeeder);
        coreRepository.ingestObject(digitalObject);
        // test ingest result
        digitalObjectImported = coreRepository.getObject(PID_IMPORTED);
        Assertions.assertNotNull(digitalObjectImported);
        verify(mockFeeder, times(1)).rebuildProcessingIndex(any(), any());
    }

    @Test
    void testCreateOrGetObject() {
        RepositoryObject repositoryObject = coreRepository.createOrGetObject(PID_MONOGRAPH);
        Assertions.assertNotNull(repositoryObject);
        repositoryObject = coreRepository.getObject(PID_IMPORTED);
        Assertions.assertNull(repositoryObject);
        repositoryObject = coreRepository.createOrGetObject(PID_IMPORTED);
        Assertions.assertNotNull(repositoryObject);
        verify(mockFeeder, times(1)).deleteByPid(eq(PID_IMPORTED));
    }

    @Test
    void testDeleteObject() {
        RepositoryObject repositoryObject = coreRepository.getObject(PID_TITLE_PAGE);
        Assertions.assertNotNull(repositoryObject);
        reset(mockFeeder);
        coreRepository.deleteObject(PID_TITLE_PAGE);
        repositoryObject = coreRepository.getObject(PID_TITLE_PAGE);
        Assertions.assertNull(repositoryObject);
        verify(mockFeeder, times(1)).deleteByRelationsForPid(eq(PID_TITLE_PAGE));
        verify(mockFeeder, times(1)).deleteByTargetPid(eq(PID_TITLE_PAGE));
        verify(mockFeeder, times(1)).deleteDescriptionByPid(eq(PID_TITLE_PAGE));
    }

}
