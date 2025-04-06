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

import org.ceskaexpedice.akubra.config.RepositoryConfiguration;
import org.ceskaexpedice.akubra.relsext.RelsExtLiteral;
import org.ceskaexpedice.akubra.relsext.RelsExtRelation;
import org.ceskaexpedice.akubra.relsext.TreeNodeProcessor;
import org.ceskaexpedice.hazelcast.HazelcastConfiguration;
import org.ceskaexpedice.hazelcast.HazelcastServerNode;
import org.ceskaexpedice.test.AkubraTestsUtils;
import org.ceskaexpedice.test.IntegrationTestsUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

import static org.ceskaexpedice.test.AkubraTestsUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class RelsExtReadTest {
    private static AkubraRepository akubraRepository;
    private static Properties testsProperties;

    @BeforeAll
    static void beforeAll() {
        testsProperties = IntegrationTestsUtils.loadProperties();
        RepositoryConfiguration config = AkubraTestsUtils.createRepositoryConfig(TEST_REPOSITORY.toFile().getAbsolutePath(), testsProperties, null);
        akubraRepository = AkubraRepositoryFactory.createRepository(config);
    }

    @AfterAll
    static void afterAll() {
        akubraRepository.shutdown();
    }

    @Test
    void testExists() {
        boolean objectExists = akubraRepository.re().exists(PID_TITLE_PAGE);
        assertTrue(objectExists);
    }

    @Test
    void testGet() {
        DatastreamContentWrapper relsExtWrapper = akubraRepository.re().get(PID_MONOGRAPH);
        assertNotNull(relsExtWrapper);
        IntegrationTestsUtils.debugPrint(relsExtWrapper.asDom4j(true).asXML(), testsProperties);
    }

    @Test
    void testRelationExists() {
        boolean exists = akubraRepository.re().relationExists(PID_MONOGRAPH, "hasPage", RepositoryNamespaces.KRAMERIUS_URI);
        assertTrue(exists);
    }

    @Test
    void testGetRelations() {
        List<RelsExtRelation> relations = akubraRepository.re().getRelations(PID_MONOGRAPH, null);
        assertEquals(PID_MONOGRAPH_RELATIONS, relations.size());
        IntegrationTestsUtils.debugPrint(relations.toString(), testsProperties);
        relations = akubraRepository.re().getRelations(PID_MONOGRAPH, RepositoryNamespaces.KRAMERIUS_URI);
        assertEquals(PID_MONOGRAPH_RELATIONS - 1, relations.size());
        IntegrationTestsUtils.debugPrint(relations.toString(), testsProperties);
    }

    @Test
    void testGetLiterals() {
        List<RelsExtLiteral> literals = akubraRepository.re().getLiterals(PID_MONOGRAPH, null);
        assertEquals(PID_MONOGRAPH_LTERALS, literals.size());
        IntegrationTestsUtils.debugPrint(literals.toString(), testsProperties);
        literals = akubraRepository.re().getLiterals(PID_MONOGRAPH, RepositoryNamespaces.OAI_NAMESPACE_URI);
        assertEquals(1, literals.size());
        IntegrationTestsUtils.debugPrint(literals.toString(), testsProperties);
    }

    @Test
    void testGetTilesUrl() {
        String tilesUrl = akubraRepository.re().getTilesUrl(PID_MONOGRAPH);
        assertNull(tilesUrl);
        tilesUrl = akubraRepository.re().getTilesUrl(PID_TILES);
        assertEquals("kramerius4://deepZoomCache", tilesUrl);
    }

    @Test
    void testPidOfFirstChild() {
        String pidOfFirstChild = akubraRepository.re().getPidOfFirstChild(PID_MONOGRAPH);
        assertEquals(PID_TITLE_PAGE, pidOfFirstChild);
    }

    @Test
    void testGetReplicateFrom() {
        String firstReplicatedFrom = akubraRepository.re().getFirstReplicatedFrom(PID_MONOGRAPH);
        assertEquals("http://vmkramerius.incad.cz:18080/search/handle/uuid:5035a48a-5e2e-486c-8127-2fa650842e46", firstReplicatedFrom);
    }

    @Test
    void testGetModel() {
        String model = akubraRepository.re().getModel(PID_MONOGRAPH);
        assertEquals("monograph", model);
    }

    @Test
    void testProcessInTree() {
        final int[] counter = {0};
        akubraRepository.re().processInTree(PID_MONOGRAPH, new TreeNodeProcessor() {
            @Override
            public void process(String pid, int level) {
                counter[0]++;
            }

            @Override
            public boolean skipBranch(String pid, int level) {
                return false;
            }

            @Override
            public boolean breakProcessing(String pid, int level) {
                return false;
            }
        });
        assertEquals(PID_MONOGRAPH_RELATIONS, counter[0]);
    }

    @Test
    void testGetFirstViewablePidInTree() {
        String firstViewablePidInTree = akubraRepository.re().getFirstViewablePidInTree(PID_MONOGRAPH);
        assertEquals(PID_TITLE_PAGE, firstViewablePidInTree);
    }

    @Test
    void testGetPidsInTree() {
        List<String> pidsInTree = akubraRepository.re().getPidsInTree(PID_MONOGRAPH);
        assertEquals(PID_MONOGRAPH_RELATIONS, pidsInTree.size());
    }

}
