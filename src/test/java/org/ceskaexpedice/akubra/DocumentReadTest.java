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
import org.ceskaexpedice.akubra.misc.MiscHelper;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndex;
import org.ceskaexpedice.akubra.relsext.RelsExtHelper;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.ceskaexpedice.akubra.utils.StringUtils;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.ceskaexpedice.test.AkubraTestsUtils;
import org.ceskaexpedice.test.IntegrationTestsUtils;
import org.dom4j.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import static org.ceskaexpedice.test.AkubraTestsUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class DocumentReadTest {
    private static AkubraRepository akubraRepository;
    private static Properties testsProperties;

    @BeforeAll
    static void beforeAll() {
        testsProperties = IntegrationTestsUtils.loadProperties();
        HazelcastConfiguration hazelcastConfig = AkubraTestsUtils.createHazelcastConfig(testsProperties);
        HazelcastServerNode.ensureHazelcastNode(hazelcastConfig);

        RepositoryConfiguration config = AkubraTestsUtils.createRepositoryConfig(TEST_REPOSITORY.toFile().getAbsolutePath(), testsProperties, hazelcastConfig);
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
        IntegrationTestsUtils.debugPrint(StringUtils.streamToString(digitalObject), testsProperties);
    }

    @Test
    void testGet_asXmlDom4j() {
        Document asXmlDom4j = akubraRepository.get(PID_TITLE_PAGE).asDom4j(true);
        assertNotNull(asXmlDom4j);
        IntegrationTestsUtils.debugPrint(asXmlDom4j.asXML(), testsProperties);
    }

    @Test
    void testGet_asXmlDom() {
        org.w3c.dom.Document asXmlDom = akubraRepository.get(PID_TITLE_PAGE).asDom(false);
        assertNotNull(asXmlDom);
        IntegrationTestsUtils.debugPrint(DomUtils.toString(asXmlDom.getDocumentElement(), true), testsProperties);
    }

    @Test
    void testGet_asString() {
        String asString = akubraRepository.get(PID_TITLE_PAGE).asString();
        assertNotNull(asString);
        IntegrationTestsUtils.debugPrint(asString, testsProperties);
    }

    @Test
    void testExport_asStream() {
        InputStream objectStream = akubraRepository.export(PID_TITLE_PAGE).asInputStream();
        assertNotNull(objectStream);
        IntegrationTestsUtils.debugPrint(StringUtils.streamToString(objectStream), testsProperties);
    }

    @Test
    void testGetProperty() {
        String propertyOwnerId = akubraRepository.getProperties(PID_TITLE_PAGE).getProperty("info:fedora/fedora-system:def/model#ownerId");
        assertEquals("fedoraAdmin", propertyOwnerId);
        Date propertyCreated = akubraRepository.getProperties(PID_TITLE_PAGE).getPropertyCreated();
        assertNotNull(propertyCreated);
        String propertyLabel = akubraRepository.getProperties(PID_TITLE_PAGE).getPropertyLabel();
        assertEquals("- none -", propertyLabel);
        Date propertyLastModified = akubraRepository.getProperties(PID_TITLE_PAGE).getPropertyLastModified();
        assertNotNull(propertyLastModified);
    }

    @Test
    void testMarshalling() {
        DigitalObject digitalObject = akubraRepository.get(PID_TITLE_PAGE).asDigitalObject();
        InputStream inputStream = akubraRepository.marshall(digitalObject);
        assertNotNull(inputStream);
        digitalObject = akubraRepository.unmarshall(inputStream);
        assertEquals(digitalObject.getDatastream().size(), digitalObject.getDatastream().size());
    }

    @Test
    void testGetProcessingIndex() {
        ProcessingIndex processingIndex = akubraRepository.pi();
        assertNotNull(processingIndex);
    }

    @Test
    void testGetRelsExtHandler() {
        RelsExtHelper re = akubraRepository.re();
        assertNotNull(re);
    }

    @Test
    void testGetMiscHandler() {
        MiscHelper mi = akubraRepository.mi();
        assertNotNull(mi);
    }

}
