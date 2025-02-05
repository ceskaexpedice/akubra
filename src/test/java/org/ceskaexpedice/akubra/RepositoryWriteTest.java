package org.ceskaexpedice.akubra;

import org.apache.commons.io.FileUtils;
import org.ceskaexpedice.akubra.core.CoreRepositoryFactory;
import org.ceskaexpedice.akubra.core.repository.ProcessingIndexFeeder;
import org.ceskaexpedice.akubra.testutils.TestUtilities;
import org.ceskaexpedice.akubra.core.lock.hazelcast.HazelcastConfiguration;
import org.ceskaexpedice.akubra.core.lock.hazelcast.ServerNode;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

import java.nio.file.Path;

import static org.mockito.Mockito.*;

public class RepositoryWriteTest {
    private static final Path TEST_REPOSITORY = Path.of("src/test/resources/data");
    private static final Path TEST_OUTPUT_REPOSITORY = Path.of("testoutput/data");
    private static final String pidMonograph = "uuid:5035a48a-5e2e-486c-8127-2fa650842e46";
    private static final String pidTitlePage = "uuid:12993b4a-71b4-4f19-8953-0701243cc25d";
    private static final String pidImported = "uuid:32993b4a-71b4-4f19-8953-0701243cc25d";

    private static Properties testsProperties;
    private static HazelcastConfiguration hazelcastConfig;
    private static ProcessingIndexFeeder mockFeeder;
    private static Repository repository;

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
            repository = RepositoryFactory.createRepository(config);
        }
    }

    @AfterAll
    static void afterAll() {
        repository.shutdown();
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
        DigitalObject digitalObjectImported = repository.getObject(pidImported, FoxmlType.regular);
        Assertions.assertNull(digitalObjectImported);
        Path importFile = Path.of("src/test/resources/titlePageImport.xml");
        InputStream inputStream = Files.newInputStream(importFile);
        DigitalObject digitalObject = repository.unmarshallObject(inputStream);
        // ingest document
        reset(mockFeeder);
        repository.ingest(digitalObject);
        // test ingest result
        digitalObjectImported = repository.getObject(pidImported, FoxmlType.regular);
        Assertions.assertNotNull(digitalObjectImported);
        verify(mockFeeder, times(1)).rebuildProcessingIndex(any(), any());
    }

    @Test
    void testDeleteObject() {
        // TODO
    }

    @Test
    void testCreateXMLDatastream() {
        // TODO
    }

    @Test
    void testCreateManagedDatastream() {
        // TODO
    }

    @Test
    void testCreateRedirectedDatastream() {
        // TODO
    }

    @Test
    void testDeleteDatastream() {
        // TODO
    }

    @Test
    void testRelsExtAddRelation() {
        // TODO
    }

    @Test
    void testRelsExtRemoveRelation() {
        // TODO
    }

    @Test
    void testRelsExtAddLiteral() {
        // TODO
    }

    @Test
    void testRelsExtRemoveLiteral() {
        // TODO
    }

}
