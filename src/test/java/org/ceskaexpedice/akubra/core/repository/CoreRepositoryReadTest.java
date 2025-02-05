package org.ceskaexpedice.akubra.core.repository;

import org.ceskaexpedice.akubra.testutils.TestUtilities;
import org.ceskaexpedice.akubra.RepositoryConfiguration;
import org.ceskaexpedice.akubra.core.CoreRepositoryFactory;
import org.ceskaexpedice.akubra.core.lock.hazelcast.HazelcastConfiguration;
import org.ceskaexpedice.akubra.core.lock.hazelcast.ServerNode;
import org.ceskaexpedice.akubra.utils.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class CoreRepositoryReadTest {

    private static CoreRepository coreRepository;
    private static Properties testsProperties;

    @BeforeAll
    static void beforeAll() {
        testsProperties = TestUtilities.loadProperties();
        HazelcastConfiguration hazelcastConfig = TestUtilities.createHazelcastConfig(testsProperties);
        ServerNode.ensureHazelcastNode(hazelcastConfig);

        URL resource = TestUtilities.class.getClassLoader().getResource("data");
        RepositoryConfiguration config = TestUtilities.createRepositoryConfig(resource.getFile(), testsProperties, hazelcastConfig);
        coreRepository = CoreRepositoryFactory.createRepository(config);
    }

    @AfterAll
    static void afterAll() {
        coreRepository.shutdown();
        ServerNode.shutdown();
    }

    @Test
    void testObjectExists() {
        boolean objectExists = coreRepository.objectExists("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        assertTrue(objectExists);
    }

    @Test
    void testGetObject() {
        RepositoryObject repositoryObject = coreRepository.getObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46", true);
        // TODO
        RepositoryDatastream dc = repositoryObject.getStream("DC");
        System.out.println(StringUtils.streamToString(dc.getLastVersionContent()));
        assertTrue(1 == 1);
    }

    @Test
    void testResolveArchivedDatastreams() {
        // TODO
    }

    @Test
    void testMarshalObject() {
        // TODO
    }

    @Test
    void testUnmarshalObject() {
        // TODO
    }

    @Test
    void testLocks() {
        // TODO
    }

    @Test
    void testGetProcessingIndexFeeder() {
        ProcessingIndexFeeder processingIndexFeeder = coreRepository.getProcessingIndexFeeder();
        assertNotNull(processingIndexFeeder);
    }


}
