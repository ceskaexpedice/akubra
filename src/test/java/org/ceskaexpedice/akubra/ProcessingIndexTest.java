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
import org.ceskaexpedice.akubra.core.lock.hazelcast.HazelcastConfiguration;
import org.ceskaexpedice.akubra.core.lock.hazelcast.ServerNode;
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexQueryParameters;
import org.ceskaexpedice.akubra.core.repository.KnownRelations;
import org.ceskaexpedice.akubra.core.repository.ProcessingIndex;
import org.ceskaexpedice.akubra.testutils.TestUtilities;
import org.ceskaexpedice.akubra.utils.ProcessingIndexUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.*;

import static org.ceskaexpedice.akubra.testutils.TestUtilities.debugPrint;
import static org.junit.jupiter.api.Assertions.assertNotNull;
// TODO make decision about Solr test instance and test data, mapping, etc
/**
 * ProcessingIndexTest
 * !!! It requires Solr instance running with processing index containing appropriate testing data to pass tests
 */
public class ProcessingIndexTest {
    private static final String PID_MONOGRAPH = "uuid:5035a48a-5e2e-486c-8127-2fa650842e46";
    private static final String PID_TITLE_PAGE = "uuid:12993b4a-71b4-4f19-8953-0701243cc25d";
    private static final String PID_IMPORTED = "uuid:32993b4a-71b4-4f19-8953-0701243cc25d";

    private static Properties testsProperties;
    private static AkubraRepository akubraRepository;

    @BeforeAll
    static void beforeAll() {
        testsProperties = TestUtilities.loadProperties();
        HazelcastConfiguration hazelcastConfig = TestUtilities.createHazelcastConfig(testsProperties);
        ServerNode.ensureHazelcastNode(hazelcastConfig);

        URL resource = TestUtilities.class.getClassLoader().getResource("data");
        RepositoryConfiguration config = TestUtilities.createRepositoryConfig(resource.getFile(), testsProperties, hazelcastConfig);
        akubraRepository = AkubraRepositoryFactory.createRepository(config);
    }

    @BeforeEach
    void beforeEach() {
        TestUtilities.checkFunctionalTestsIgnored(testsProperties);
    }

    @AfterAll
    static void afterAll() {
        if(akubraRepository != null) {
            akubraRepository.shutdown();
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
        akubraRepository.getProcessingIndex().iterate(params, processingIndexItem -> {
            Object source = processingIndexItem.getFieldValue("source");
            assertNotNull(source);
            Optional<Long> version = processingIndexItem.getFieldValueAs("_version_", Long.class);
            assertNotNull(version.get());
            debugPrint(source + "," + version.get(), testsProperties);
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
                .fieldsToFetch(List.of("source", "_version_"))
                .build();
        akubraRepository.getProcessingIndex().iterate(params, processingIndexItem -> {
            Object source = processingIndexItem.getFieldValue("source");
            assertNotNull(source);
            Optional<Long> version = processingIndexItem.getFieldValueAs("_version_", Long.class);
            assertNotNull(version.get());
            debugPrint(source + "," + version.get(), testsProperties);
        });
    }

    @Test
    void testGetParents() {
        Pair<ProcessingIndexRelation, List<ProcessingIndexRelation>> parents = ProcessingIndexUtils.getParents(PID_TITLE_PAGE, akubraRepository);
        System.out.println(parents.getLeft());
        System.out.println(parents.getRight());
    }

    @Test
    void testGetChildren() {
        Pair<List<ProcessingIndexRelation>, List<ProcessingIndexRelation>> children = ProcessingIndexUtils.getChildren(PID_MONOGRAPH, akubraRepository);
        System.out.println(children.getLeft());
        System.out.println(children.getRight());
    }

    @Test
    void testGetModel() {
        String model = ProcessingIndexUtils.getModel(PID_MONOGRAPH, akubraRepository);
        System.out.println(model);
    }

    @Test
    void testGetPidsOfObjectsByModel() {
        Pair<Long, List<String>> page = ProcessingIndexUtils.getPidsOfObjectsByModel("monograph", "", 10, 0, akubraRepository);
        System.out.println(page.getLeft());
        System.out.println(page.getRight());
    }

    @Test
    void testGetPidsOfObjectsByModel2() {
        List<String> page = ProcessingIndexUtils.getPidsOfObjectsByModel("monograph", akubraRepository);
        System.out.println(page);
    }

    @Test
    void testGetPidsOfObjectsByModel3() {
        Pair pair = ProcessingIndexUtils.getPidsOfObjectsWithTitlesByModelWithCursor("monograph", true, "*", 10, akubraRepository);
        System.out.println("titlePidPairs:"  + pair.getLeft());
        System.out.println("nextCursorMark:"  + pair.getRight());
    }

    @Test
    void testGetPidsOfObjectsByModel4() {
        List<Pair<String, String>> pair = ProcessingIndexUtils.getPidsOfObjectsWithTitlesByModel("monograph", true, 0, 10, akubraRepository);
        System.out.println(pair);
    }

    @Test
    void testGetTripletSources() {
        List<String> tripletSources = ProcessingIndexUtils.getTripletSources(KnownRelations.CONTAINS.toString(), PID_TITLE_PAGE, akubraRepository);
        System.out.println(tripletSources);
    }

    @Test
    void testGetTripletTargets() {
        List<String> tripletTargets = ProcessingIndexUtils.getTripletTargets(KnownRelations.CONTAINS.toString(), PID_TITLE_PAGE, akubraRepository);
        System.out.println(tripletTargets);
    }

    @Test
    void testFindByTargetPid() {
        List<Pair<String, String>> byTargetPid = ProcessingIndexUtils.findByTargetPid(PID_TITLE_PAGE, akubraRepository);
        System.out.println(byTargetPid);
    }

}
