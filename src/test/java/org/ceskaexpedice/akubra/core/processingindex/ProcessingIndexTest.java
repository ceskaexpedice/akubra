package org.ceskaexpedice.akubra.core.processingindex;

import org.ceskaexpedice.akubra.RepositoryConfiguration;
import org.ceskaexpedice.akubra.core.CoreRepositoryFactory;
import org.ceskaexpedice.akubra.core.lock.hazelcast.HazelcastConfiguration;
import org.ceskaexpedice.akubra.core.lock.hazelcast.ServerNode;
import org.ceskaexpedice.akubra.core.repository.CoreRepository;
import org.ceskaexpedice.akubra.core.repository.ProcessingIndexFeeder;
import org.ceskaexpedice.akubra.testutils.TestUtilities;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

import static org.ceskaexpedice.akubra.testutils.TestUtilities.debugPrint;
import static org.junit.jupiter.api.Assertions.assertNotNull;
// TODO make decision about Solr test instance and test data, mapping, etc
/**
 * ProcessingIndexTest
 * !!! It requires Solr instance running with processing index containing appropriate testing data to pass tests
 */
public class ProcessingIndexTest {

    private static Properties testsProperties;
    private static ProcessingIndexFeeder processingIndexFeeder;
    private static CoreRepository repository;

    @BeforeAll
    static void beforeAll() {
        testsProperties = TestUtilities.loadProperties();
        HazelcastConfiguration hazelcastConfig = TestUtilities.createHazelcastConfig(testsProperties);
        ServerNode.ensureHazelcastNode(hazelcastConfig);

        URL resource = TestUtilities.class.getClassLoader().getResource("data");
        RepositoryConfiguration config = TestUtilities.createRepositoryConfig(resource.getFile(), testsProperties, hazelcastConfig);
        repository = CoreRepositoryFactory.createRepository(config);
        processingIndexFeeder = repository.getProcessingIndexFeeder();
    }

    @BeforeEach
    void beforeEach() {
        TestUtilities.checkFunctionalTestsIgnored(testsProperties);
    }

    @AfterAll
    static void afterAll() {
        if(repository != null) {
            repository.shutdown();
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
        processingIndexFeeder.iterate(params, new Consumer<ProcessingIndexItem>() {
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
