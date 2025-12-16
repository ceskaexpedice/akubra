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

import org.ceskaexpedice.akubra.relsext.RelsExtUtils;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RelsExtInternalSaxUtilsTest {


    @Test
    public void testFirstPageSaxUtils() {
        InputStream is = RelsExtInternalSaxUtilsTest.class.getResourceAsStream("info%3Afedora%2Fuuid%3A7e6619ab-e814-4505-a2c1-c94acd8bb3ea");
        assertNotNull(is);
        String pidOfFirstChild = RelsExtInternalSaxUtils.getPidOfFirstChild(is);
        Assertions.assertNotNull(pidOfFirstChild);
        Assertions.assertEquals(pidOfFirstChild, "uuid:4d1d136f-d5a0-11f0-965f-001b63bd97ba");
    }
}
