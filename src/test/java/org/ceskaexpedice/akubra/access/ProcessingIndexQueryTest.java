package org.ceskaexpedice.akubra.access;

import org.ceskaexpedice.akubra.AbstractFunctionalTest;
import org.ceskaexpedice.akubra.core.RepositoryConfiguration;
import org.ceskaexpedice.hazelcast.ServerNode;
import org.junit.jupiter.api.*;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ProcessingIndexQueryTest
 * !!! It requires Solr instance running with processing index containing appropriate testing data to pass tests
 */
public class ProcessingIndexQueryTest extends AbstractFunctionalTest {

    private static RepositoryAccess repositoryAccess;

    private final boolean debugPrint = true;

    @BeforeEach
    void beforeEach() {
        super.setUp();
        URL resource = ProcessingIndexQueryTest.class.getClassLoader().getResource("data");
        String testRepoPath = resource.getFile() + "/";
        RepositoryConfiguration config = new RepositoryConfiguration.Builder()
                .processingIndexHost(getProperty("processingIndexHost", null))
                .objectStorePath(testRepoPath + "objectStore")
                .objectStorePattern("##/##")
                .datastreamStorePath(testRepoPath + "datastreamStore")
                .datastreamStorePattern("##/##")
                .cacheTimeToLiveExpiration(60)
                .hazelcastInstance("akubrasync")
                .hazelcastUser("dev")
                .build();
        ServerNode.ensureHazelcastNode(config);
        repositoryAccess = RepositoryAccessFactory.createRepositoryAccess(config);
    }

    @AfterEach
    void afterAll() {
        repositoryAccess.shutdown();
        ServerNode.shutdown();
    }

    @Test
    void testQueryProcessingIndex() {
        String model = "page";
        String query = String.format("type:description AND model:%s", "model\\:" + model);
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .sortField("title")
                .ascending(true)
                .rows(10)
                .pageIndex(0)
                .fieldsToFetch(List.of("source", "_version_"))
                .build();
        repositoryAccess.queryProcessingIndex(params, processingIndexItem -> {
            Object source = processingIndexItem.getFieldValue("source");
            assertNotNull(source);
            Optional<Long> version = processingIndexItem.getFieldValueAs("_version_", Long.class);
            assertNotNull(version.get());
            debugPrint(source + "," + version.get());
        });
    }

    private void debugPrint(String msg) {
        if(debugPrint){
            System.out.println(msg);
        }
    }

}
