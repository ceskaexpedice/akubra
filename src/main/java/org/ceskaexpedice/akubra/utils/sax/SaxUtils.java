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
package org.ceskaexpedice.akubra.utils.sax;

import org.akubraproject.UnsupportedIdException;
import org.akubraproject.map.IdMapper;
import org.apache.commons.io.IOUtils;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.KnownXmlFormatUris;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.core.repository.CoreRepository;
import org.ceskaexpedice.akubra.core.repository.impl.AkubraDOManager;
import org.ceskaexpedice.akubra.core.repository.impl.RepositoryUtils;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.ceskaexpedice.akubra.utils.SafeSimpleDateFormat;
import org.ceskaexpedice.fedoramodel.*;
import org.fcrepo.common.Constants;
import org.fcrepo.common.FaultException;
import org.fcrepo.common.PID;
import org.fcrepo.server.errors.MalformedPidException;
import org.fcrepo.server.storage.lowlevel.akubra.HashPathIdMapper;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import static org.ceskaexpedice.akubra.core.repository.impl.RepositoryUtils.readFromURL;

public final class SaxUtils {
    private static final Logger LOGGER = Logger.getLogger(SaxUtils.class.getName());

    private SaxUtils() {
    }

    public static InputStream getStreamContent(InputStream foxml, String dsId, CoreRepository coreRepository) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            StreamContentHandler handler = new StreamContentHandler(dsId);
            try {
                saxParser.parse(foxml, handler);
            } catch (SAXException e) {
                if (!"STOP_PARSING".equals(e.getMessage())) {
                    throw e; // Only propagate real errors
                }
            }
            // Handle <contentLocation> case
            if (handler.getContentLocationRef() != null) {
                String ref = handler.getContentLocationRef();
                String type = handler.getContentLocationType();
                if ("URL".equals(type)) {
                    if (ref.startsWith(RepositoryUtils.LOCAL_REF_PREFIX)) {
                        String[] refArray = ref.replace(RepositoryUtils.LOCAL_REF_PREFIX, "").split("/");
                        if (refArray.length == 2) {
                            return coreRepository.retrieveDatastream(refArray[0] + "+" + refArray[1] + "+" + refArray[1] + ".0");
                        } else {
                            throw new IOException("Invalid datastream local reference: " + ref);
                        }
                    } else {
                        return readFromURL(ref);
                    }
                } else {
                    return coreRepository.retrieveDatastream(ref);
                }
            }
            // Handle <xmlContent> case
            if (handler.getXmlContentStream() != null) {
                return handler.getXmlContentStream();
            }
            throw new RepositoryException("Datastream with ID '" + dsId + "' not found or has no relevant content.");
        } catch (Exception e) {
            throw new RepositoryException("Error processing XML file: " + e.getMessage(), e);
        }
    }

    public static boolean containsDatastream(InputStream foxml, String datastreamId) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            FindDatastreamHandler handler = new FindDatastreamHandler(datastreamId);
            saxParser.parse(foxml, handler);
        } catch (SAXException e) {
            return "Found".equals(e.getMessage()); // Catches the forced stop
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
        return false; // If parsing completes, datastream was not found
    }

}
