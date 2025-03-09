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
import org.ceskaexpedice.akubra.core.repository.RepositoryDatastream;
import org.ceskaexpedice.test.FunctionalTestsUtils;
import org.ceskaexpedice.akubra.impl.utils.DomUtils;
import org.dom4j.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import static org.ceskaexpedice.akubra.AkubraTestsUtils.PID_TITLE_PAGE;
import static org.junit.jupiter.api.Assertions.*;

public class DatastreamReadTest {
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
    void testDatastreamExists() {
        boolean exists = akubraRepository.datastreamExists(PID_TITLE_PAGE, KnownDatastreams.BIBLIO_DC);
        assertTrue(exists);
    }

    @Test
    void testGetDatastreamMetadata() {
        DatastreamMetadata datastreamMetadata = akubraRepository.getDatastreamMetadata(PID_TITLE_PAGE, KnownDatastreams.BIBLIO_DC);
        assertNotNull(datastreamMetadata);

        assertEquals("DC", datastreamMetadata.getId());
        assertEquals("text/xml", datastreamMetadata.getMimetype());
        assertEquals(RepositoryDatastream.Type.DIRECT, datastreamMetadata.getType());
        assertEquals("X", datastreamMetadata.getControlGroup());
        assertEquals(0, datastreamMetadata.getSize());
        assertNull(datastreamMetadata.getLocation());
        // TODO assertEquals("Mon Feb 26 15:40:29 CET 2018", datastreamMetadata.getLastModified().toString());
        // TODO assertEquals("Mon Feb 26 15:40:29 CET 2018", datastreamMetadata.getCreateDate().toString());
    }

    @Test
    void testGetDatastreamContent_asStream() {
        InputStream imgThumb = akubraRepository.getDatastreamContent(PID_TITLE_PAGE, KnownDatastreams.IMG_THUMB).asInputStream();
        assertNotNull(imgThumb);
    }

    @Test
    void testGetDatastreamContent_asXmlDom() {
        org.w3c.dom.Document xmlDom = akubraRepository.getDatastreamContent(PID_TITLE_PAGE, KnownDatastreams.BIBLIO_DC).asDom(false);
        assertNotNull(xmlDom);
        FunctionalTestsUtils.debugPrint(DomUtils.toString(xmlDom.getDocumentElement(), true), testsProperties);
    }

    @Test
    void testGetDatastreamContent_asXmlDom4j() {
        Document xmlDom4j = akubraRepository.getDatastreamContent(PID_TITLE_PAGE, KnownDatastreams.BIBLIO_DC).asDom4j(true);
        assertNotNull(xmlDom4j);
        FunctionalTestsUtils.debugPrint(xmlDom4j.asXML(), testsProperties);
    }

    @Test
    void testGetDatastreamContent_asString() {
        String dc = akubraRepository.getDatastreamContent(PID_TITLE_PAGE, KnownDatastreams.BIBLIO_DC).asString();
        assertNotNull(dc);
        FunctionalTestsUtils.debugPrint(dc, testsProperties);
    }

    @Test
    void testGetDatastreamNames() {
        List<String> datastreamNames = akubraRepository.getDatastreamNames(PID_TITLE_PAGE);
        assertEquals(9, datastreamNames.size());
        FunctionalTestsUtils.debugPrint(String.join(", ", datastreamNames), testsProperties);
    }

}
