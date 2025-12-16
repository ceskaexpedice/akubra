/*
 * Copyright (C) 2025  Inovatika
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
package org.ceskaexpedice.akubra.core.repository.impl;

import org.apache.commons.io.IOUtils;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.RepositoryNamespaces;
import org.ceskaexpedice.akubra.utils.Dom4jUtils;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.QName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RepositoryUtilsTest {

    @Test
    public void testReadXMLDatastreamFromFOXML() throws IOException {
        InputStream is = RepositoryUtilsTest.class.getResourceAsStream("info%253Afedora%252Fuuid%253Aab7e5a2c-bddb-11e0-bff9-0016e6840575");
        assertNotNull(is);
        InputStream bilioMods = RepositoryUtils.getDatastreamContent("", is, "BIBLIO_MODS", null);
        Document parsedDocument = Dom4jUtils.streamToDocument(bilioMods, true);
        assertNotNull(parsedDocument);
    }


    @Test
    public void testReadXMLNDK_1_BIBLIO_MODS_GetContent() throws IOException {
        InputStream is = RepositoryUtilsTest.class.getResourceAsStream("info%253Afedora%252Fuuid%253A99a5df44-55a4-4ed3-8e1f-e8c49ebcd603");
        assertNotNull(is);
        InputStream bilioMods = RepositoryUtils.getDatastreamContent("",is, "BIBLIO_MODS", null);
        Document parsedDocument = Dom4jUtils.streamToDocument(bilioMods, true);
        assertNotNull(parsedDocument);
    }


    @Test
    public void testReadXMLNDK_1_BIBLIO_MODS_Exits() throws IOException {
        InputStream is = RepositoryUtilsTest.class.getResourceAsStream("info%253Afedora%252Fuuid%253A99a5df44-55a4-4ed3-8e1f-e8c49ebcd603");
        assertNotNull(is);
        boolean biblioModsExists = RepositoryUtils.datastreamExists(is, "BIBLIO_MODS");
        assertTrue(biblioModsExists);
    }


    @Test
    public void testReadXMLNDK_1_RELS_EXT_GetContent() throws IOException {
        InputStream is = RepositoryUtilsTest.class.getResourceAsStream("info%253Afedora%252Fuuid%253A99a5df44-55a4-4ed3-8e1f-e8c49ebcd603");
        assertNotNull(is);
        //TODO: Core repisotory is null ??  Should test with core repository
        InputStream bilioMods = RepositoryUtils.getDatastreamContent("", is, "RELS-EXT", null);
        Document parsedDocument = Dom4jUtils.streamToDocument(bilioMods, true);
        assertNotNull(parsedDocument);
    }


    @Test
    public void testReadXMLNDK_1_RELS_EXT_Exits() throws IOException {
        InputStream is = RepositoryUtilsTest.class.getResourceAsStream("info%253Afedora%252Fuuid%253A99a5df44-55a4-4ed3-8e1f-e8c49ebcd603");
        assertNotNull(is);
        boolean relsExt = RepositoryUtils.datastreamExists(is, "RELS-EXT");
        assertTrue(relsExt);
    }

    @Test
    public void testReadBiBLIO_MODSNDK_2_GetContent() throws IOException {
        InputStream is = RepositoryUtilsTest.class.getResourceAsStream("info%253Afedora%252Fuuid%253Aa47ea67b-87d5-4a05-a817-aa65b26eb515");
        assertNotNull(is);
        InputStream biblioMods = RepositoryUtils.getDatastreamContent("",is, "BIBLIO_MODS", null);
        Element docElement = DomUtils.streamToDocument(biblioMods, true).getDocumentElement();
        assertTrue(docElement != null);
    }


    @Test
    public void testReadRELEXT_MultiversionedGetContent() throws IOException {
        InputStream is = RepositoryUtilsTest.class.getResourceAsStream("info%253Afedora%252Fuuid%253A534faa9e-b675-46f3-a4db-e107127a1112");
        assertNotNull(is);
        InputStream datastreamContent = RepositoryUtils.getDatastreamContent("uuid:534faa9e-b675-46f3-a4db-e107127a1112", is, "RELS-EXT", null);
        org.w3c.dom.Document document = DomUtils.streamToDocument(datastreamContent, true);
        String attribute = document.getDocumentElement().getAttribute("id-test");
        Assertions.assertTrue(attribute != null);
        Assertions.assertEquals("final-version", attribute);
    }

    //https://github.com/ceskaexpedice/kramerius/issues/1229
    @Test
    public void testReadRELSEXTAndDOM4JPath() throws IOException {
        InputStream works = RepositoryUtilsTest.class.getResourceAsStream("works.xml");
        InputStream doesntWork = RepositoryUtilsTest.class.getResourceAsStream("does-not-work.xml");
        assertNotNull(works);
        assertNotNull(doesntWork);

        InputStream worksContent = RepositoryUtils.getDatastreamContent("uuid:308d3b50-a5d3-11f0-95c3-0050568d319f", works, "RELS-EXT", null);
        Document worksContentRelsExt = Dom4jUtils.streamToDocument(worksContent, true);

        List<Node> worksLicense = Dom4jUtils.buildXpath("/rdf:RDF/rdf:Description/rel:license").selectNodes(worksContentRelsExt);
        List<Node> worksLicenseNew = Dom4jUtils.buildXpath(
                "/rdf:RDF/rdf:Description/rel:license" +
                        "| /rdf:RDF/rdf:Description/rel:licenses" +
                        "| /rdf:RDF/rdf:Description/rel:licence" +
                        "| /rdf:RDF/rdf:Description/rel:licences" +
                        "| /rdf:RDF/rdf:Description/rel:dnnt-label" +
                        "| /rdf:RDF/rdf:Description/rel:dnnt-labels"
        ).selectNodes(worksContentRelsExt);

        Assertions.assertTrue(!worksLicense.isEmpty());
        Assertions.assertTrue(!worksLicenseNew.isEmpty());
        Assertions.assertTrue(worksLicense.size() == 1);
        Assertions.assertTrue(worksLicense.size() == worksLicenseNew.size());


        InputStream doesntWorkContent = RepositoryUtils.getDatastreamContent("uuid:308d3b50-a5d3-11f0-95c3-0050568d319f", doesntWork, "RELS-EXT", null);
        Document doesntWorkRelsExt = Dom4jUtils.streamToDocument(doesntWorkContent, true);

        List<Node> doesntWorkLicense = Dom4jUtils.buildXpath("/rdf:RDF/rdf:Description/rel:license").selectNodes(doesntWorkRelsExt);
        List<Node> doesntWorkLicenseNew = Dom4jUtils.buildXpath(
                "/rdf:RDF/rdf:Description/rel:license" +
                        "| /rdf:RDF/rdf:Description/rel:licenses" +
                        "| /rdf:RDF/rdf:Description/rel:licence" +
                        "| /rdf:RDF/rdf:Description/rel:licences" +
                        "| /rdf:RDF/rdf:Description/rel:dnnt-label" +
                        "| /rdf:RDF/rdf:Description/rel:dnnt-labels"
        ).selectNodes(doesntWorkRelsExt);

        Assertions.assertTrue(doesntWorkLicense.isEmpty());
        Assertions.assertTrue(!doesntWorkLicenseNew.isEmpty());
        Assertions.assertTrue(doesntWorkLicenseNew.size() == 1);


    }


}
