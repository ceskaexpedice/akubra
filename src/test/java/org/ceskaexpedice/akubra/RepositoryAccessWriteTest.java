package org.ceskaexpedice.akubra;

import org.apache.commons.io.FileUtils;
import org.ceskaexpedice.akubra.testutils.TestUtilities;
import org.ceskaexpedice.akubra.utils.Utils;
import org.ceskaexpedice.hazelcast.HazelcastConfiguration;
import org.ceskaexpedice.hazelcast.ServerNode;
import org.ceskaexpedice.jaxbmodel.DigitalObject;
import org.junit.jupiter.api.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Properties;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RepositoryAccessWriteTest {
    private static final Path TEST_RESOURCES = Path.of("src/test/resources/data");
    private static final Path TEST_OUTPUT = Path.of("testoutput/data");
    private static final String pidMonograph = "uuid:5035a48a-5e2e-486c-8127-2fa650842e46";
    private static final String pidTitlePage = "uuid:12993b4a-71b4-4f19-8953-0701243cc25d";

    private static RepositoryAccess repositoryAccess;
    private static Properties testsProperties;
    private static HazelcastConfiguration hazelcastConfig;

    @BeforeAll
    static void beforeAll() {
        testsProperties = TestUtilities.loadProperties();
        hazelcastConfig = TestUtilities.createHazelcastConfig(testsProperties);
        ServerNode.ensureHazelcastNode(hazelcastConfig);
    }

    @AfterAll
    static void afterAll() {
        ServerNode.shutdown();
    }

    @BeforeEach
    void beforeEach() throws IOException {
        if (Files.exists(TEST_OUTPUT)) {
            FileUtils.deleteDirectory(TEST_OUTPUT.toFile());
        }
        Files.createDirectories(TEST_OUTPUT);
        FileUtils.copyDirectory(TEST_RESOURCES.toFile(), TEST_OUTPUT.toFile());
        RepositoryConfiguration config = TestUtilities.createRepositoryConfig(TEST_OUTPUT.toFile().getAbsolutePath(), testsProperties, hazelcastConfig);
        repositoryAccess = RepositoryAccessFactory.createRepositoryAccess(config);
    }

    @AfterEach
    void afterEach() {
        repositoryAccess.shutdown();
    }

    @Test
    void testIngest() throws IOException {
        Path importFile = Path.of("src/test/resources/titlePageImport.xml");
        InputStream inputStream = Files.newInputStream(importFile);
        DigitalObject digitalObject = repositoryAccess.unmarshallStream(inputStream);
        System.out.println("digitalObject: " + digitalObject.getPID());
//        repositoryAccess.ingest(digitalObject);

    }


}
