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
import org.ceskaexpedice.test.FunctionalTestsUtils;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.ceskaexpedice.akubra.utils.StringUtils;
import org.dom4j.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

import static org.ceskaexpedice.akubra.AkubraTestsUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class FoxmlReadTest {
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
        boolean objectExists = akubraRepository.exists(PID_TITLE_PAGE);
        assertTrue(objectExists);
        objectExists = akubraRepository.exists(PID_NOT_EXISTS);
        assertFalse(objectExists);
        objectExists = akubraRepository.exists("Wrong_PID_Format");
        assertFalse(objectExists);
    }

    @Test
    void testGet_asStream() {
        InputStream digitalObject = akubraRepository.get(PID_TITLE_PAGE).asInputStream();
        assertNotNull(digitalObject);
        FunctionalTestsUtils.debugPrint(StringUtils.streamToString(digitalObject), testsProperties);
    }

    @Test
    void testGet_asXmlDom4j() {
        Document asXmlDom4j = akubraRepository.get(PID_TITLE_PAGE).asDom4j(true);
        assertNotNull(asXmlDom4j);
        FunctionalTestsUtils.debugPrint(asXmlDom4j.asXML(), testsProperties);
    }

    @Test
    void testGet_asXmlDom() {
        org.w3c.dom.Document asXmlDom = akubraRepository.get(PID_TITLE_PAGE).asDom(false);
        assertNotNull(asXmlDom);
        FunctionalTestsUtils.debugPrint(DomUtils.toString(asXmlDom.getDocumentElement(), true), testsProperties);
    }

    @Test
    void testGet_asString() {
        String asString = akubraRepository.get(PID_TITLE_PAGE).asString();
        assertNotNull(asString);
        FunctionalTestsUtils.debugPrint(asString, testsProperties);
    }

    @Test
    void testExport_asStream() {
        InputStream objectStream = akubraRepository.export(PID_TITLE_PAGE).asInputStream();
        assertNotNull(objectStream);
        FunctionalTestsUtils.debugPrint(StringUtils.streamToString(objectStream), testsProperties);
    }

    @Test
    void testGetProperty() {
        String propertyOwnerId = akubraRepository.getProperties(PID_TITLE_PAGE).getProperty("info:fedora/fedora-system:def/model#ownerId");
        assertEquals("fedoraAdmin", propertyOwnerId);
        Date propertyCreated = akubraRepository.getProperties(PID_TITLE_PAGE).getPropertyCreated();
        assertNull(propertyCreated); // no milliseconds in test data
        String propertyLabel = akubraRepository.getProperties(PID_TITLE_PAGE).getPropertyLabel();
        assertEquals("- none -", propertyLabel);
        Date propertyLastModified = akubraRepository.getProperties(PID_TITLE_PAGE).getPropertyLastModified();
        // TODO AK_NEW assertEquals("2024-05-20T13:03:27.151", propertyLastModified.toString());
    }

    @Test
    void testDoWithLocks_simple() {
        String pid = PID_MONOGRAPH;
        String pid1 = PID_TITLE_PAGE;
        Boolean result = akubraRepository.doWithWriteLock(pid, () -> {
            akubraRepository.get(pid);
            Boolean result1 = akubraRepository.doWithReadLock(pid1, () -> {
                akubraRepository.get(pid1);
                return true;
            });
            return result1;
        });
        assertTrue(result);
    }
}
