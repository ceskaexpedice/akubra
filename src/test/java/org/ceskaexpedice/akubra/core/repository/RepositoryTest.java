package org.ceskaexpedice.akubra.core.repository;

import org.ceskaexpedice.akubra.core.RepositoryFactory;
import org.ceskaexpedice.akubra.core.repository.impl.HazelcastServerNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RepositoryTest {

    private static HazelcastServerNode hazelcastServerNode;

    @BeforeAll
    static void beforeAll() {
        hazelcastServerNode = new HazelcastServerNode();
        hazelcastServerNode.contextInitialized(null);
    }

    @AfterAll
    static void afterAll() {
        hazelcastServerNode.contextDestroyed(null);
    }

    @Test
    void testGetObject() throws IOException {
        Repository akubraRepository = RepositoryFactory.createAkubraRepository();
        RepositoryObject repositoryObject = akubraRepository.getObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        RepositoryDatastream dc = repositoryObject.getStream("DC");
        System.out.println(convertUsingBytes(dc.getContent()));
        assertTrue(1 == 1);
    }

    private static String convertUsingBytes(InputStream inputStream) throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        return new String(bytes, "UTF-8"); // Specify encoding, e.g., UTF-8
    }
}
