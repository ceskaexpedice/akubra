package org.ceskaexpedice.akubra.core.repository;

import org.ceskaexpedice.akubra.core.Configuration;
import org.ceskaexpedice.akubra.core.RepositoryFactory;
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexFeeder;
import org.ceskaexpedice.akubra.core.repository.impl.HazelcastServerNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class RepositoryTest {

    private static HazelcastServerNode hazelcastServerNode;
    private static Repository akubraRepository;

    @BeforeAll
    static void beforeAll() {
        Configuration config = new Configuration.Builder()
                .processingIndexHost("http://localhost:8983/solr/processing")
                .objectStorePath("c:\\Users\\petr\\.kramerius4\\data\\objectStore")
                .objectStorePattern("##/##")
                .datastreamStorePath("c:\\Users\\petr\\.kramerius4\\data\\datastreamStore")
                .datastreamStorePattern("##/##")
                .cacheTimeToLiveExpiration(60)
                .hazelcastInstance("akubrasync")
                .hazelcastUser("dev")
                .build();
        hazelcastServerNode = new HazelcastServerNode();
        // TODO hazelcastServerNode.contextInitialized(null);
        HazelcastServerNode.ensureHazelcastNode(config);
        akubraRepository = RepositoryFactory.createCoreRepository(config);
    }

    @AfterAll
    static void afterAll() {
        hazelcastServerNode.contextDestroyed(null);
    }

    @Test
    void testObjectExists() {
        boolean objectExists = akubraRepository.objectExists("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        assertTrue(objectExists);
    }

    @Test
    void testGetObject() {
        RepositoryObject repositoryObject = akubraRepository.getObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        // TODO
        RepositoryDatastream dc = repositoryObject.getStream("DC");
        System.out.println(convertUsingBytes(dc.getContent()));
        assertTrue(1 == 1);
    }

    @Test
    void testGetProcessingIndexFeeder() {
        ProcessingIndexFeeder processingIndexFeeder = akubraRepository.getProcessingIndexFeeder();
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
