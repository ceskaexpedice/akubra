/*
 * Copyright (C) 2025 Inovatika
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ceskaexpedice.akubra;

import org.apache.commons.lang3.tuple.Pair;
import org.ceskaexpedice.akubra.config.RepositoryConfiguration;
import org.ceskaexpedice.akubra.processingindex.*;
import org.ceskaexpedice.akubra.relsext.KnownRelations;
import org.ceskaexpedice.hazelcast.HazelcastConfiguration;
import org.ceskaexpedice.hazelcast.HazelcastServerNode;
import org.ceskaexpedice.test.AkubraTestsUtils;
import org.ceskaexpedice.test.IntegrationTestsUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

import static org.ceskaexpedice.test.AkubraTestsUtils.*;
import static org.ceskaexpedice.test.IntegrationTestsUtils.debugPrint;
import static org.junit.jupiter.api.Assertions.assertNotNull;
// TODO make decision about Solr test instance and test data, mapping, etc

/**
 * ProcessingIndexTest
 * !!! It requires Solr instance running with processing index containing appropriate testing data to pass the tests
 */
public class ProcessingIndexReadTest_integration {
    private static Properties testsProperties;
    private static AkubraRepository akubraRepository;

    @BeforeAll
    static void beforeAll() {
        testsProperties = IntegrationTestsUtils.loadProperties();
        RepositoryConfiguration config = AkubraTestsUtils.createRepositoryConfig(TEST_REPOSITORY.toFile().getAbsolutePath(), testsProperties, null);
        akubraRepository = AkubraRepositoryFactory.createRepository(config);
    }

    @BeforeEach
    void beforeEach() {
        IntegrationTestsUtils.checkIntegrationTestsIgnored(testsProperties);
    }

    @AfterAll
    static void afterAll() {
        if (akubraRepository != null) {
            akubraRepository.shutdown();
        }
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
                .fieldsToFetch(List.of("source"))
                .build();
        akubraRepository.pi().iterate(params, processingIndexItem -> {
            assertNotNull(processingIndexItem.source());
            debugPrint(processingIndexItem.toString(), testsProperties);
        });
    }

    @Test
    void testIterate_cursor() {
        String model = "page";
        String query = String.format("type:description AND model:%s", "model\\:" + model);
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .sortField("title")
                .ascending(true)
                .cursorMark(ProcessingIndex.CURSOR_MARK_START)
                .fieldsToFetch(List.of("source"))
                .build();
        akubraRepository.pi().iterate(params, processingIndexItem -> {
            assertNotNull(processingIndexItem.source());
            debugPrint(processingIndexItem.toString(), testsProperties);
        });
    }

    @Test
    void testGetParents() {
        List<ProcessingIndexItem> parents = akubraRepository.pi().getParents(PID_TITLE_PAGE);
        for (ProcessingIndexItem item : parents) {
            debugPrint(item.toString(), testsProperties);
        }
    }

    @Test
    void testGetParentsWithRelation() {
        List<ProcessingIndexItem> parents = akubraRepository.pi().getParents(KnownRelations.HAS_PAGE.toString(), PID_TITLE_PAGE);
        debugPrint(parents.toString(), testsProperties);
    }

    @Test
    void testGetOwnedAndFosteredParents() {
        OwnedAndFosteredParents parents = akubraRepository.pi().getOwnedAndFosteredParents(PID_TITLE_PAGE);
        debugPrint("Own", testsProperties);
        debugPrint(parents.own().toString(), testsProperties);
        debugPrint("Foster", testsProperties);
        for (ProcessingIndexItem item : parents.foster()) {
            debugPrint(item.toString(), testsProperties);
        }
    }

    @Test
    void testGetChildren() {
        List<ProcessingIndexItem> children = akubraRepository.pi().getChildren(KnownRelations.HAS_PAGE.toString(), PID_MONOGRAPH);
        for (ProcessingIndexItem item : children) {
            debugPrint(item.toString(), testsProperties);
        }
    }

    @Test
    void testGetOwnedAndFosteredChildren() {
        OwnedAndFosteredChildren childrenRelation = akubraRepository.pi().getOwnedAndFosteredChildren(PID_MONOGRAPH);
        debugPrint("Own", testsProperties);
        for (ProcessingIndexItem item : childrenRelation.own()) {
            debugPrint(item.toString(), testsProperties);
        }
        debugPrint("Foster", testsProperties);
        for (ProcessingIndexItem item : childrenRelation.foster()) {
            debugPrint(item.toString(), testsProperties);
        }
    }

    @Test
    void testGetModel() {
        String model = akubraRepository.pi().getModel(PID_MONOGRAPH);
        debugPrint(model, testsProperties);
    }

    @Test
    void testGetByModelWithCursor() {
        CursorItemsPair cursorItemsPair = akubraRepository.pi().getByModelWithCursor("page", true, "*", 5);
        debugPrint(cursorItemsPair.nextCursor(), testsProperties);
        for (ProcessingIndexItem item : cursorItemsPair.items()) {
            debugPrint(item.toString(), testsProperties);
        }
    }

    @Test
    void testGetByModelWithSize() {
        SizeItemsPair sizeItemsPair = akubraRepository.pi().getByModel("page", null, 5, 0);
        debugPrint(sizeItemsPair.size().toString(), testsProperties);
        for (ProcessingIndexItem item : sizeItemsPair.items()) {
            debugPrint(item.toString(), testsProperties);
        }
    }

    @Test
    void testGetByModel() {
        List<ProcessingIndexItem> page = akubraRepository.pi().getByModel("page", true, 0, 5);
        for (ProcessingIndexItem item : page) {
            debugPrint(item.toString(), testsProperties);
        }
    }

    @Test
    void testExtractStructureInfo() {
        JSONObject extractStructureInfo = akubraRepository.pi().extractStructureInfo(PID_MONOGRAPH);
        debugPrint(extractStructureInfo.toString(), testsProperties);
    }

    @Test
    void testGetModelsCount() {
        List<Pair<String, Long>> allFedoraModelsAsList = akubraRepository.pi().getModelsCount();
        debugPrint(allFedoraModelsAsList.toString(), testsProperties);
    }

}
