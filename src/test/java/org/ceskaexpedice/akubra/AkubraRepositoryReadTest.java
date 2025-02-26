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

import org.ceskaexpedice.akubra.core.lock.hazelcast.HazelcastConfiguration;
import org.ceskaexpedice.akubra.core.lock.hazelcast.ServerNode;
import org.ceskaexpedice.akubra.core.repository.RepositoryDatastream;
import org.ceskaexpedice.akubra.testutils.TestUtilities;
import org.ceskaexpedice.akubra.utils.Dom4jUtils;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.ceskaexpedice.akubra.utils.StringUtils;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.dom4j.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AkubraRepositoryReadTest
 */
public class AkubraRepositoryReadTest {
    private static final String PID_MONOGRAPH = "uuid:5035a48a-5e2e-486c-8127-2fa650842e46";
    private static final String PID_TITLE_PAGE = "uuid:12993b4a-71b4-4f19-8953-0701243cc25d";

    private static AkubraRepository akubraRepository;
    private static Properties testsProperties;

    @BeforeAll
    static void beforeAll() {
        testsProperties = TestUtilities.loadProperties();
        HazelcastConfiguration hazelcastConfig = TestUtilities.createHazelcastConfig(testsProperties);
        ServerNode.ensureHazelcastNode(hazelcastConfig);

        URL resource = TestUtilities.class.getClassLoader().getResource("data");
        RepositoryConfiguration config = TestUtilities.createRepositoryConfig(resource.getFile(), testsProperties, hazelcastConfig);
        akubraRepository = AkubraRepositoryFactory.createRepository(config);
    }

    @AfterAll
    static void afterAll() {
        akubraRepository.shutdown();
        ServerNode.shutdown();
    }

    @Test
    void testObjectExists() {
        boolean objectExists = akubraRepository.objectExists(PID_TITLE_PAGE);
        assertTrue(objectExists);
    }

    @Test
    void testGetObject_asStream() {
        InputStream digitalObject = akubraRepository.getObject(PID_TITLE_PAGE, FoxmlType.managed).asInputStream();
        assertNotNull(digitalObject);
        TestUtilities.debugPrint(StringUtils.streamToString(digitalObject), testsProperties);
    }

    @Test
    void testGetObject_asXmlDom4j() {
        Document asXmlDom4j = akubraRepository.getObject(PID_TITLE_PAGE, FoxmlType.managed).asDom4j(true);
        assertNotNull(asXmlDom4j);
        TestUtilities.debugPrint(asXmlDom4j.asXML(), testsProperties);
    }

    @Test
    void testGetObject_asXmlDom() {
        org.w3c.dom.Document asXmlDom = akubraRepository.getObject(PID_TITLE_PAGE, FoxmlType.managed).asDom(false);
        assertNotNull(asXmlDom);
        TestUtilities.debugPrint(DomUtils.toString(asXmlDom.getDocumentElement(), true), testsProperties);
    }

    @Test
    void testGetObject_asString() {
        String asString = akubraRepository.getObject(PID_TITLE_PAGE, FoxmlType.managed).asString();
        assertNotNull(asString);
        TestUtilities.debugPrint(asString, testsProperties);
    }

    @Test
    void testGetObjectArchive_asStream() {
        InputStream objectStream = akubraRepository.getObject(PID_TITLE_PAGE, FoxmlType.archive).asInputStream();
        assertNotNull(objectStream);
        TestUtilities.debugPrint(StringUtils.streamToString(objectStream), testsProperties);
    }

    @Test
    void testGetObjectProperty() {
        String propertyOwnerId = akubraRepository.getObjectProperties(PID_TITLE_PAGE).getProperty("info:fedora/fedora-system:def/model#ownerId");
        assertEquals("fedoraAdmin", propertyOwnerId);
        LocalDateTime propertyCreated = akubraRepository.getObjectProperties(PID_TITLE_PAGE).getPropertyCreated();
        assertNull(propertyCreated); // no milliseconds in test data
        String propertyLabel = akubraRepository.getObjectProperties(PID_TITLE_PAGE).getPropertyLabel();
        assertEquals("- none -", propertyLabel);
        LocalDateTime propertyLastModified = akubraRepository.getObjectProperties(PID_TITLE_PAGE).getPropertyLastModified();
        assertEquals("2024-05-20T13:03:27.151", propertyLastModified.toString());
    }

    @Test
    void testDatastreamExists() {
        boolean exists = akubraRepository.datastreamExists(PID_TITLE_PAGE, "DC");
        assertTrue(exists);
    }

    @Test
    void testGetDatastreamMetadata() {
        DatastreamMetadata datastreamMetadata = akubraRepository.getDatastreamMetadata(PID_TITLE_PAGE, "DC");
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
        InputStream imgThumb = akubraRepository.getDatastreamContent(PID_TITLE_PAGE, "IMG_THUMB");
        assertNotNull(imgThumb);
    }

    @Test
    void testGetDatastreamContent_asXmlDom() {
        org.w3c.dom.Document xmlDom = DomUtils.streamToDocument(akubraRepository.getDatastreamContent(PID_TITLE_PAGE, "DC"));
        assertNotNull(xmlDom);
        TestUtilities.debugPrint(DomUtils.toString(xmlDom.getDocumentElement(), true), testsProperties);
    }

    @Test
    void testGetDatastreamContent_asXmlDom4j() {
        Document xmlDom4j = Dom4jUtils.streamToDocument(akubraRepository.getDatastreamContent(PID_TITLE_PAGE, "DC"), true);
        assertNotNull(xmlDom4j);
        TestUtilities.debugPrint(xmlDom4j.asXML(), testsProperties);
    }

    @Test
    void testGetDatastreamContent_asString() {
        String dc = StringUtils.streamToString(akubraRepository.getDatastreamContent(PID_TITLE_PAGE, "DC"));
        assertNotNull(dc);
        TestUtilities.debugPrint(dc, testsProperties);
    }

    @Test
    void testRelsExtGet() {
        RelsExtWrapper relsExtWrapper = akubraRepository.relsExtGet(PID_MONOGRAPH);
        assertNotNull(relsExtWrapper);
        List<RelsExtRelation> relations = relsExtWrapper.getRelations(null);
        assertEquals(37, relations.size());
        List<RelsExtLiteral> literals = relsExtWrapper.getLiterals(null);
        assertEquals(5, literals.size());
    }

    @Test
    void testGetDatastreamNames() {
        List<String> datastreamNames = akubraRepository.getDatastreamNames(PID_TITLE_PAGE);
        assertEquals(9, datastreamNames.size());
        TestUtilities.debugPrint(String.join(", ", datastreamNames), testsProperties);
    }

    @Test
    void testDoWithLocks_simple() {
        String pid = PID_MONOGRAPH;
        String pid1 = PID_TITLE_PAGE;
        Boolean result = akubraRepository.doWithWriteLock(pid, () -> {
            akubraRepository.getObject(pid, FoxmlType.managed);
            Boolean result1 = akubraRepository.doWithReadLock(pid1, () -> {
                akubraRepository.getObject(pid1, FoxmlType.managed);
                return true;
            });
            return result1;
        });
        assertTrue(result);
    }

}
