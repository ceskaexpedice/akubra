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
package org.ceskaexpedice.akubra.impl.utils.relsext;

import org.ceskaexpedice.akubra.core.repository.impl.RepositoryUtils;
import org.ceskaexpedice.akubra.core.repository.impl.RepositoryUtilsTest;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RelsExtInternalSaxUtilsTest {


//    @Test
//    public void testReadBiBLIO_MODSNDK_2_GetContent() throws IOException {
//        InputStream is = RepositoryUtilsTest.class.getResourceAsStream("info%253Afedora%252Fuuid%253Aa47ea67b-87d5-4a05-a817-aa65b26eb515");
//        assertNotNull(is);
//        InputStream biblioMods = RepositoryUtils.getDatastreamContent("",is, "BIBLIO_MODS", null);
//        Element docElement = DomUtils.streamToDocument(biblioMods, true).getDocumentElement();
//        assertTrue(docElement != null);
//    }

    @Test
    public void testRelsExtSaxUtils() {
        InputStream is = RelsExtInternalSaxUtilsTest.class.getResourceAsStream("info%253Afedora%252Fuuid%253A3a288340-a00e-11e8-a81d-5ef3fc9bb22f");
        assertNotNull(is);
        String pidOfFirstChild = RelsExtInternalSaxUtils.getPidOfFirstChild(is);
        System.out.println(pidOfFirstChild);
    }
}
