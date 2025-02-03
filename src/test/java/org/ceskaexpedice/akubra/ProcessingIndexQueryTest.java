package org.ceskaexpedice.akubra;

import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexItem;
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexQueryParameters;
import org.ceskaexpedice.akubra.testutils.TestUtilities;
import org.ceskaexpedice.hazelcast.HazelcastConfiguration;
import org.ceskaexpedice.hazelcast.ServerNode;
import org.junit.jupiter.api.*;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

import static org.ceskaexpedice.akubra.testutils.TestUtilities.debugPrint;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ProcessingIndexQueryTest
 * !!! It requires Solr instance running with processing index containing appropriate testing data to pass tests
 */
public class ProcessingIndexQueryTest {

    private static RepositoryAccess repositoryAccess;
    private static Properties testsProperties;

    @BeforeAll
    static void beforeAll() {
        testsProperties = TestUtilities.loadProperties();
        HazelcastConfiguration hazelcastConfig = TestUtilities.createHazelcastConfig(testsProperties);
        ServerNode.ensureHazelcastNode(hazelcastConfig);

        URL resource = TestUtilities.class.getClassLoader().getResource("data");
        RepositoryConfiguration config = TestUtilities.createRepositoryConfig(resource.getFile(), testsProperties, hazelcastConfig);
        repositoryAccess = RepositoryAccessFactory.createRepositoryAccess(config);
    }

    @BeforeEach
    void beforeEach() {
        TestUtilities.checkFunctionalTestsIgnored(testsProperties);
    }

    @AfterAll
    static void afterAll() {
        if(repositoryAccess != null) {
            repositoryAccess.shutdown();
        }
        ServerNode.shutdown();
    }

    @Test
    void testIterate_page() {
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
        repositoryAccess.iterateProcessingIndex(params, new Consumer<ProcessingIndexItem>() {
            @Override
            public void accept(ProcessingIndexItem processingIndexItem) {
                Object source = processingIndexItem.getFieldValue("source");
                assertNotNull(source);
                Optional<Long> version = processingIndexItem.getFieldValueAs("_version_", Long.class);
                assertNotNull(version.get());
                debugPrint(source + "," + version.get(), testsProperties);
            }
        });
    }

    @Test
    void testIterate_cursor() {
        // TODO
    }

}
