/*
 * Copyright (C) 2010 Pavel Stastny
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

import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.impl.utils.InternalSaxUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.logging.Logger;

import static org.ceskaexpedice.akubra.impl.utils.InternalSaxUtils.FOUND;

public final class RelsExtInternalSaxUtils {
    static final Logger LOGGER = Logger.getLogger(RelsExtInternalSaxUtils.class.getName());

    private RelsExtInternalSaxUtils() {
    }

    public static String getPidOfFirstChild(InputStream foxml) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GetPidOfFirstChildSaxHandler handler = new GetPidOfFirstChildSaxHandler();

            try {
                saxParser.parse(foxml, handler);
            } catch (SAXException e) {
                if (!InternalSaxUtils.FOUND.equals(e.getMessage())) {
                    throw e;
                }
            }
            return handler.getFirstChildPid();
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    public static String getFirstReplicatedFrom(InputStream foxml) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser saxParser = factory.newSAXParser();
            GetFirstReplicatedFromSaxHandler handler = new GetFirstReplicatedFromSaxHandler();
            try {
                saxParser.parse(foxml, handler);
            } catch (SAXException e) {
                if (!FOUND.equals(e.getMessage())) {
                    throw e;
                }
            }
            return handler.getReplicatedFrom();
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    public static String getTilesUrl(InputStream foxml) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser saxParser = factory.newSAXParser();
            GetTilesUrlSaxHandler handler = new GetTilesUrlSaxHandler();
            try {
                saxParser.parse(foxml, handler);
            } catch (SAXException e) {
                if (!FOUND.equals(e.getMessage())) {
                    throw e;
                }
            }
            return handler.getTilesUrl();
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    public static String getModel(InputStream foxml) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GetModelSaxHandler handler = new GetModelSaxHandler();

            try {
                saxParser.parse(foxml, handler);
            } catch (SAXException e) {
                if (!InternalSaxUtils.FOUND.equals(e.getMessage())) {
                    throw e;
                }
            }
            return handler.getModel();
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

}
