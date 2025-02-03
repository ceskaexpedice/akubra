package org.ceskaexpedice.akubra.access;

import org.ceskaexpedice.akubra.RepositoryAccess;
import org.ceskaexpedice.akubra.RepositoryAccessFactory;
import org.ceskaexpedice.akubra.TestsUtilities;
import org.ceskaexpedice.akubra.RepositoryConfiguration;
import org.ceskaexpedice.hazelcast.HazelcastConfiguration;
import org.ceskaexpedice.hazelcast.ServerNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RepositoryAccessWriteTest {

    private static RepositoryAccess repositoryAccess;
    private static Properties testsProperties;

    @BeforeAll
    static void beforeAll() {
        testsProperties = TestsUtilities.loadProperties();
        HazelcastConfiguration hazelcastConfig = TestsUtilities.createHazelcastConfig(testsProperties);
        ServerNode.ensureHazelcastNode(hazelcastConfig);

        RepositoryConfiguration config = TestsUtilities.createRepositoryConfig(testsProperties, hazelcastConfig);
        repositoryAccess = RepositoryAccessFactory.createRepositoryAccess(config);
    }

    @AfterAll
    static void afterAll() {
        repositoryAccess.shutdown();
        ServerNode.shutdown();
    }

    @Test
    void testDummy() {
    }

}
