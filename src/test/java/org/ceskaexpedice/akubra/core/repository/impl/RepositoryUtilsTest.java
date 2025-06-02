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

import org.ceskaexpedice.akubra.utils.Dom4jUtils;
import org.dom4j.Document;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import static org.junit.jupiter.api.Assertions.*;

public class RepositoryUtilsTest {

    @Test
    public void testReadXMLDatastreamFromFOXML() throws IOException {
        InputStream is = RepositoryUtilsTest.class.getResourceAsStream("info%253Afedora%252Fuuid%253Aab7e5a2c-bddb-11e0-bff9-0016e6840575");
        assertNotNull(is);
        InputStream bilioMods = RepositoryUtils.getDatastreamContent(is, "BIBLIO_MODS", null);
        Document parsedDocument = Dom4jUtils.streamToDocument(bilioMods, true);
        assertNotNull(parsedDocument);
    }


    @Test
    public void testReadXMLNDK_1_BIBLIO_MODS_GetContent() throws IOException {
        InputStream is = RepositoryUtilsTest.class.getResourceAsStream("info%253Afedora%252Fuuid%253A99a5df44-55a4-4ed3-8e1f-e8c49ebcd603");
        assertNotNull(is);
        InputStream bilioMods = RepositoryUtils.getDatastreamContent(is, "BIBLIO_MODS", null);
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
        InputStream bilioMods = RepositoryUtils.getDatastreamContent(is, "RELS-EXT", null);
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
}
