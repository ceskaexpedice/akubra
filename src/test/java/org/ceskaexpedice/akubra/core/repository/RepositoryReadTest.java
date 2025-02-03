package org.ceskaexpedice.akubra.core.repository;

import org.ceskaexpedice.akubra.TestsUtilities;
import org.ceskaexpedice.akubra.RepositoryConfiguration;
import org.ceskaexpedice.akubra.core.RepositoryFactory;
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexFeeder;
import org.ceskaexpedice.hazelcast.HazelcastConfiguration;
import org.ceskaexpedice.hazelcast.ServerNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

// TODO
public class RepositoryReadTest {

    private static Repository repository;
    private static Properties testsProperties;

    @BeforeAll
    static void beforeAll() {
        testsProperties = TestsUtilities.loadProperties();
        HazelcastConfiguration hazelcastConfig = TestsUtilities.createHazelcastConfig(testsProperties);
        ServerNode.ensureHazelcastNode(hazelcastConfig);

        RepositoryConfiguration config = TestsUtilities.createRepositoryConfig(testsProperties, hazelcastConfig);
        repository = RepositoryFactory.createRepository(config);
    }

    @AfterAll
    static void afterAll() {
        repository.shutdown();
        ServerNode.shutdown();
    }

    @Test
    void testObjectExists() {
        boolean objectExists = repository.objectExists("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        assertTrue(objectExists);
    }

    @Test
    void testGetObject() {
        RepositoryObject repositoryObject = repository.getObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46", true);
        // TODO
        RepositoryDatastream dc = repositoryObject.getStream("DC");
        System.out.println(convertUsingBytes(dc.getLastVersionContent()));
        assertTrue(1 == 1);
    }

    @Test
    void testGetProcessingIndexFeeder() {
        ProcessingIndexFeeder processingIndexFeeder = repository.getProcessingIndexFeeder();
        assertNotNull(processingIndexFeeder);
    }

    private static String convertUsingBytes(InputStream inputStream) {
        byte[] bytes = null;
        try {
            bytes = inputStream.readAllBytes();
            return new String(bytes, "UTF-8"); // Specify encoding, e.g., UTF-8
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
