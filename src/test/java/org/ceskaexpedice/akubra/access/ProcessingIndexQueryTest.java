package org.ceskaexpedice.akubra.access;

import org.ceskaexpedice.akubra.AbstractFunctionalTest;
import org.ceskaexpedice.akubra.core.RepositoryConfiguration;
import org.ceskaexpedice.hazelcast.ServerNode;
import org.junit.jupiter.api.*;

import java.net.URL;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProcessingIndexQueryTest extends AbstractFunctionalTest {

    private static RepositoryAccess repositoryAccess;

    @BeforeEach
    void beforeEach() {
        super.setUp();
        URL resource = ProcessingIndexQueryTest.class.getClassLoader().getResource("data");
        String testRepoPath = resource.getFile() + "/";
        //String testRepoPath = "c:\\Users\\petr\\.kramerius4\\data\\";
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
        String query = String.format("type:description AND model:%s", "model\\:" + model); //prvni "model:" je filtr na solr pole, druhy "model:" je hodnota pole, coze  uprime zbytecne
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .sortField("title")
                .ascending(true)
                .rows(10)
                .pageIndex(0)
                .fieldsToFetch(List.of("source"))
                .build();
        repositoryAccess.queryProcessingIndex(params, new Consumer<ProcessingIndexItem>() {
            @Override
            public void accept(ProcessingIndexItem processingIndexItem) {
                // TODO
               // System.out.println(((ProcessingIndexItemImpl)processingIndexItem).getDocument());
            }
        });

    }

}
