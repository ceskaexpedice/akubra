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
package org.ceskaexpedice.akubra.core.repository.impl;

import org.akubraproject.UnsupportedIdException;
import org.akubraproject.map.IdMapper;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.KnownXmlFormatUris;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.core.repository.CoreRepository;
import org.ceskaexpedice.akubra.core.repository.RepositoryDatastream;
import org.ceskaexpedice.fedoramodel.DatastreamType;
import org.ceskaexpedice.fedoramodel.DatastreamVersionType;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.ceskaexpedice.fedoramodel.PropertyType;
import org.fcrepo.common.Constants;
import org.fcrepo.common.FaultException;
import org.fcrepo.common.PID;
import org.fcrepo.server.errors.MalformedPidException;
import org.fcrepo.server.storage.lowlevel.akubra.HashPathIdMapper;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import static org.ceskaexpedice.akubra.core.repository.CoreRepository.LOCAL_REF_PREFIX;

/**
 * RepositoryUtils
 */
public class RepositoryUtils {
    private static final Logger LOGGER = Logger.getLogger(RepositoryUtils.class.getName());
    private static final SafeSimpleDateFormat DATE_FORMAT = new SafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");
    static final String FOUND = "FOUND";
    public static final DateTimeFormatter TIMESTAMP_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.MILLI_OF_SECOND, 1, 3, true)
            .optionalEnd()
            .appendPattern("'Z'")
            .toFormatter();

    private RepositoryUtils() {
    }

    static DatastreamVersionType getLastStreamVersion(DatastreamType datastreamType) {
        List<DatastreamVersionType> datastreamVersionList = datastreamType.getDatastreamVersion();
        if (datastreamVersionList == null || datastreamVersionList.isEmpty()) {
            return null;
        } else {
            return datastreamVersionList.get(datastreamVersionList.size() - 1);
        }
    }

    static boolean streamExists(DigitalObject object, String streamID) {
        for (DatastreamType datastreamType : object.getDatastream()) {
            if (datastreamType == null) {
                LOGGER.log(Level.SEVERE, "Repository inconsistency: object %s has datastream %s that is null", new String[]{object.getPID(), streamID});
            } else {
                if (streamID.equals(datastreamType.getID())) {
                    return true;
                }
            }
        }
        return false;
    }

    static InputStream getDatastreamContent(String pid, InputStream foxml, String dsId, CoreRepository coreRepository) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser saxParser = factory.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();


            GetDatastreamContentSaxHandler handler = new GetDatastreamContentSaxHandler(dsId);
            EntityResolver entityResolver = new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    return new InputSource(new StringReader(""));
                }
            };
            // external entities
            xmlReader.setEntityResolver(entityResolver);
            xmlReader.setContentHandler(handler);

            try {
                xmlReader.parse(new InputSource(foxml));
            } catch (SAXException e) {
                if (!FOUND.equals(e.getMessage())) {
                    throw e; // Only propagate real errors
                }
            }
            // Handle <contentLocation> case
            if (handler.getContentLocationRef() != null) {
                String ref = handler.getContentLocationRef();
                String type = handler.getContentLocationType();
                if ("URL".equals(type)) {
                    if (ref.startsWith(LOCAL_REF_PREFIX)) {
                        String[] refArray = ref.replace(LOCAL_REF_PREFIX, "").split("/");
                        if (refArray.length == 2) {
                            return coreRepository.retrieveDatastreamByInternalId(refArray[0] + "+" + refArray[1] + "+" + refArray[1] + ".0");
                        } else {
                            throw new IOException("Invalid datastream local reference: " + ref);
                        }
                    } else {
                        return readFromURL(ref);
                    }
                } else {
                    return coreRepository.retrieveDatastreamByInternalId(ref);
                }
            }
            // Handle <xmlContent> case
            if (handler.getXmlContentStream() != null) {
                return handler.getXmlContentStream();
            }
            LOGGER.warning(String.format("Datastream with ID '%s' not found or has no relevant content for pid '%s'", dsId, pid));
            return null;
        } catch (Exception e) {
            throw new RepositoryException("Error processing XML file: " + e.getMessage(), e);
        }
    }

    static boolean datastreamExists(InputStream foxml, String datastreamId) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser saxParser = factory.newSAXParser();
            DatastreamExistsSaxHandler handler = new DatastreamExistsSaxHandler(datastreamId);
            saxParser.parse(foxml, handler);
        } catch (SAXException e) {
            return FOUND.equals(e.getMessage());
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
        return false;
    }

    private static InputStream readFromURL(String url) {
        try {
            URL searchURL = new URL(url);
            URLConnection conn = searchURL.openConnection();
            conn.setUseCaches(true);
            HttpURLConnection.setFollowRedirects(true);
            conn.connect();
            if ("gzip".equals(conn.getContentEncoding())) {
                return new GZIPInputStream(conn.getInputStream());
            } else {
                return conn.getInputStream();
            }
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    static XMLGregorianCalendar getCurrentXMLGregorianCalendar() {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(DATE_FORMAT.format(new Date()));
        } catch (DatatypeConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RepositoryException(e);
        }
    }

    /**
     * Return Akubra object store internal path for provided PID
     *
     * @param pid PID of the FOXML object (uuid:xxxxxx...)
     * @return internal file path relative to object store root, depends ob the property objectStore.pattern
     */
    public static String getAkubraInternalId(String pid, String objectPattern) {
        if (pid == null) {
            return "";
        }
        return getAkubraInternalIdWitPattern(pid, objectPattern);
    }

    static String getAkubraInternalIdWitPattern(String pid, String objectPattern) {
        IdMapper mapper = new HashPathIdMapper(objectPattern);
        URI extUri = null;
        try {
            extUri = new URI(new PID(pid).toURI());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        URI internalId = mapper.getInternalId(extUri);
        return internalId.toString();
    }

    static Date getLastModified(DigitalObject object) {
        for (PropertyType propertyType : object.getObjectProperties().getProperty()) {
            if ("info:fedora/fedora-system:def/view#lastModifiedDate".equals(propertyType.getNAME())) {
                try {
                    return DATE_FORMAT.parse(propertyType.getVALUE());
                } catch (ParseException e) {
                    throw new RepositoryException("Cannot parse lastModifiedDate: " + object.getPID() + ": " + propertyType.getVALUE());
                }
            }
        }
        return null;
    }

    static String currentTimeString() {
        return DATE_FORMAT.format(new Date());
    }

    static PropertyType createProperty(String name, String value) {
        PropertyType propertyType = new PropertyType();
        propertyType.setNAME(name);
        propertyType.setVALUE(value);
        return propertyType;
    }

    static String getFormatUriForDS(String dsID) {
        if (KnownDatastreams.RELS_EXT.name().equals(dsID)) {
            return KnownXmlFormatUris.RELS_EXT;
        }
        if (KnownDatastreams.BIBLIO_MODS.name().equals(dsID)) {
            return KnownXmlFormatUris.BIBLIO_MODS;
        }
        if (KnownDatastreams.BIBLIO_DC.name().equals(dsID)) {
            return KnownXmlFormatUris.BIBLIO_DC;
        }
        return null;
    }

    static URI validateId(URI blobId) throws UnsupportedIdException {
        if (blobId == null) {
            throw new NullPointerException("Id cannot be null");
        } else if (!blobId.getScheme().equalsIgnoreCase("file")) {
            throw new UnsupportedIdException(blobId, "Id must be in file scheme");
        } else {
            String path = blobId.getRawSchemeSpecificPart();
            if (path.startsWith("/")) {
                throw new UnsupportedIdException(blobId, "Id must specify a relative path");
            } else {
                try {
                    URI tmp = new URI("file:/" + path);
                    String nPath = tmp.normalize().getRawSchemeSpecificPart().substring(1);
                    if (!nPath.equals("..") && !nPath.startsWith("../")) {
                        if (nPath.endsWith("/")) {
                            throw new UnsupportedIdException(blobId, "Id cannot specify a directory");
                        } else {
                            return new URI("file:" + nPath);
                        }
                    } else {
                        throw new UnsupportedIdException(blobId, "Id cannot be outside top-level directory");
                    }
                } catch (URISyntaxException wontHappen) {
                    throw new Error(wontHappen);
                }
            }
        }
    }

    static URI getBlobId(String token) {
        try {
            int i = token.indexOf('+');
            if (i == -1) {
                return new URI(new PID(token).toURI());
            } else {
                String[] dsParts = token.substring(i + 1).split("\\+");
                if (dsParts.length != 2) {
                    throw new IllegalArgumentException(
                            "Malformed datastream token: " + token);
                }
                return new URI(Constants.FEDORA.uri
                        + token.substring(0, i) + "/"
                        + uriEncode(dsParts[0]) + "/"
                        + uriEncode(dsParts[1]));
            }
        } catch (MalformedPidException e) {
            throw new IllegalArgumentException(
                    "Malformed object token: " + token, e);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                    "Malformed object or datastream token: " + token, e);
        }
    }

    private static String uriEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException wontHappen) {
            throw new FaultException(wontHappen);
        }
    }

    static RepositoryDatastreamImpl.Type controlGroup2Type(String controlGroup) {
        if ("E".equals(controlGroup) || "R".equals(controlGroup)) {
            return RepositoryDatastream.Type.INDIRECT;
        } else {
            return RepositoryDatastream.Type.DIRECT;
        }
    }

    public static Date parseDate(String dateStr) {
        if(dateStr == null) {
            return null;
        }
        try {
            return Date.from(LocalDateTime.parse(dateStr, TIMESTAMP_FORMATTER)
                    .atZone(ZoneId.systemDefault())
                    .toInstant());
        } catch (DateTimeParseException e) {
            LOGGER.warning(String.format("Cannot parse date %s", dateStr));
            return null;
        }
    }

}
