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

import org.ceskaexpedice.akubra.config.HazelcastConfiguration;
import org.ceskaexpedice.akubra.config.RepositoryConfiguration;
import org.ceskaexpedice.akubra.relsext.RelsExtLiteral;
import org.ceskaexpedice.akubra.relsext.RelsExtRelation;
import org.ceskaexpedice.test.AkubraTestsUtils;
import org.ceskaexpedice.test.FunctionalTestsUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URL;
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
        testsProperties = FunctionalTestsUtils.loadProperties();
        HazelcastConfiguration hazelcastConfig = AkubraTestsUtils.createHazelcastConfig(testsProperties);
        HazelcastServerNode.ensureHazelcastNode(hazelcastConfig);

        URL resource = FunctionalTestsUtils.class.getClassLoader().getResource("data");
        RepositoryConfiguration config = AkubraTestsUtils.createRepositoryConfig(resource.getFile(), testsProperties, hazelcastConfig);
        akubraRepository = AkubraRepositoryFactory.createRepository(config);
    }

    @AfterAll
    static void afterAll() {
        akubraRepository.shutdown();
        HazelcastServerNode.shutdown();
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
        FunctionalTestsUtils.debugPrint(relsExtWrapper.asDom4j(true).asXML(), testsProperties);
    }

    @Test
    void testRelationExists() {
        boolean exists = akubraRepository.re().relationExists(PID_MONOGRAPH, "hasPage", RepositoryNamespaces.KRAMERIUS_URI);
        assertTrue(exists);
    }

    @Test
    void testGetRelations() {
        List<RelsExtRelation> relations = akubraRepository.re().getRelations(PID_MONOGRAPH, null);
        assertEquals(37, relations.size());
        FunctionalTestsUtils.debugPrint(relations.toString(), testsProperties);
        relations = akubraRepository.re().getRelations(PID_MONOGRAPH, RepositoryNamespaces.KRAMERIUS_URI);
        assertEquals(36, relations.size());
        FunctionalTestsUtils.debugPrint(relations.toString(), testsProperties);
    }

    @Test
    void testGetLiterals() {
        List<RelsExtLiteral> literals = akubraRepository.re().getLiterals(PID_MONOGRAPH, null);
        assertEquals(5, literals.size());
        FunctionalTestsUtils.debugPrint(literals.toString(), testsProperties);
        literals = akubraRepository.re().getLiterals(PID_MONOGRAPH, RepositoryNamespaces.OAI_NAMESPACE_URI);
        assertEquals(1, literals.size());
        FunctionalTestsUtils.debugPrint(literals.toString(), testsProperties);
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
        assertEquals("uuid:12993b4a-71b4-4f19-8953-0701243cc25d", pidOfFirstChild);
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

}
