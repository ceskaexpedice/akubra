package org.ceskaexpedice.akubra.core.repository;

import org.ceskaexpedice.akubra.testutils.TestUtilities;
import org.ceskaexpedice.akubra.RepositoryConfiguration;
import org.ceskaexpedice.akubra.core.RepositoryFactory;
import org.ceskaexpedice.akubra.utils.Utils;
import org.ceskaexpedice.hazelcast.HazelcastConfiguration;
import org.ceskaexpedice.hazelcast.ServerNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

// TODO
public class RepositoryReadTest {

    private static Repository repository;
    private static Properties testsProperties;

    @BeforeAll
    static void beforeAll() {
        testsProperties = TestUtilities.loadProperties();
        HazelcastConfiguration hazelcastConfig = TestUtilities.createHazelcastConfig(testsProperties);
        ServerNode.ensureHazelcastNode(hazelcastConfig);

        URL resource = TestUtilities.class.getClassLoader().getResource("data");
        RepositoryConfiguration config = TestUtilities.createRepositoryConfig(resource.getFile(), testsProperties, hazelcastConfig);
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
        System.out.println(Utils.streamToString(dc.getLastVersionContent()));
        assertTrue(1 == 1);
    }

    @Test
    void testGetProcessingIndexFeeder() {
        ProcessingIndexFeeder processingIndexFeeder = repository.getProcessingIndexFeeder();
        assertNotNull(processingIndexFeeder);
    }


}
