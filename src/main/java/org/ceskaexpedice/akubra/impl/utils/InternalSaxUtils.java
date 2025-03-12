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
package org.ceskaexpedice.akubra.impl.utils;

import org.ceskaexpedice.akubra.RepositoryException;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.logging.Logger;

public final class InternalSaxUtils {
    private static final Logger LOGGER = Logger.getLogger(InternalSaxUtils.class.getName());
    public static final String FOUND = "FOUND";

    private InternalSaxUtils() {
    }

    public static String getModsPartType(InputStream foxml) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GetModsPartTypeSaxHandler handler = new GetModsPartTypeSaxHandler();
            try {
                saxParser.parse(foxml, handler);
            } catch (SAXException e) {
                if (!FOUND.equals(e.getMessage())) {
                    throw e;
                }
            }
            return handler.getPartType();
        } catch (Exception e) {
            throw new RepositoryException("Error processing MODS XML: " + e.getMessage(), e);
        }
    }

}
