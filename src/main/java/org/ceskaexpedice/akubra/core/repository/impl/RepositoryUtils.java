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
import org.apache.commons.io.IOUtils;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.KnownXmlFormatUris;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.ceskaexpedice.akubra.utils.SafeSimpleDateFormat;
import org.ceskaexpedice.fedoramodel.*;
import org.fcrepo.common.Constants;
import org.fcrepo.common.FaultException;
import org.fcrepo.common.PID;
import org.fcrepo.server.errors.MalformedPidException;
import org.fcrepo.server.storage.lowlevel.akubra.HashPathIdMapper;
import org.w3c.dom.Element;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
/**
 * RepositoryUtils
 */
public class RepositoryUtils {
    private static final Logger LOGGER = Logger.getLogger(RepositoryUtils.class.getName());
    private static final SafeSimpleDateFormat DATE_FORMAT = new SafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");
    private static final String LOCAL_REF_PREFIX = "http://local.fedora.server/fedora/get/";

    private RepositoryUtils() {
    }

    public static DatastreamVersionType getLastStreamVersion(DigitalObject object, String streamID) {
        for (DatastreamType datastreamType : object.getDatastream()) {
            if (streamID.equals(datastreamType.getID())) {
                return getLastStreamVersion(datastreamType);
            }
        }
        return null;
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

    static InputStream getStreamContent(DatastreamVersionType stream, AkubraDOManager manager) {
        try {
            if (stream.getXmlContent() != null) {
                StringWriter wrt = new StringWriter();
                for (Element element : stream.getXmlContent().getAny()) {
                    DomUtils.print(element, wrt);
                }
                return IOUtils.toInputStream(wrt.toString(), Charset.forName("UTF-8"));
            } else if (stream.getContentLocation() != null) {
                if (stream.getContentLocation().getTYPE().equals("INTERNAL_ID")) {
                    return manager.retrieveDatastream(stream.getContentLocation().getREF());
                } else if (stream.getContentLocation().getTYPE().equals("URL")) {
                    if (stream.getContentLocation().getREF().startsWith(LOCAL_REF_PREFIX)) {
                        String[] refArray = stream.getContentLocation().getREF().replace(LOCAL_REF_PREFIX, "").split("/");
                        if (refArray.length == 2) {
                            return manager.retrieveDatastream(refArray[0] + "+" + refArray[1] + "+" + refArray[1] + ".0");
                        } else {
                            throw new IOException("Invalid datastream local reference: " + stream.getContentLocation().getREF());
                        }
                    } else {
                        return readFromURL(stream.getContentLocation().getREF());
                    }
                } else {
                    throw new IOException("Unsupported datastream reference type: " + stream.getContentLocation().getTYPE() + "(" + stream.getContentLocation().getREF() + ")");
                }
            } else if (stream.getBinaryContent() != null) {
                LOGGER.warning("Reading binaryContent from the managed stream.");
                return new ByteArrayInputStream(stream.getBinaryContent());
            } else {
                throw new IOException("Unsupported datastream content type: " + stream.getID());
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    private static InputStream readFromURL(String url) throws IOException {
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
    public static String getAkubraInternalId(String pid) {
        if (pid == null) {
            return "";
        }
        // TODO String objectPattern = Configuration.getInstance().getProperty("objectStore.pattern");
        String objectPattern = null;
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

    public static Date getLastModified(DigitalObject object) {
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

    static DigitalObject createEmptyDigitalObject(String pid) {
        DigitalObject retval = new DigitalObject();
        retval.setPID(pid);
        retval.setVERSION("1.1");
        ObjectPropertiesType objectPropertiesType = new ObjectPropertiesType();
        List<PropertyType> propertyTypeList = objectPropertiesType.getProperty();
        propertyTypeList.add(RepositoryUtils.createProperty("info:fedora/fedora-system:def/model#state", "Active"));
        propertyTypeList.add(RepositoryUtils.createProperty("info:fedora/fedora-system:def/model#ownerId", "fedoraAdmin"));
        String currentTime = RepositoryUtils.currentTimeString();
        propertyTypeList.add(RepositoryUtils.createProperty("info:fedora/fedora-system:def/model#createdDate", currentTime));
        propertyTypeList.add(RepositoryUtils.createProperty("info:fedora/fedora-system:def/view#lastModifiedDate", currentTime));
        retval.setObjectProperties(objectPropertiesType);
        return retval;
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

    public static boolean containsDatastream(InputStream foxml, String datastreamId) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            DatastreamHandler handler = new DatastreamHandler(datastreamId);
            saxParser.parse(foxml, handler);
        } catch (SAXException e) {
            return "Found".equals(e.getMessage()); // Catches the forced stop
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
        return false; // If parsing completes, datastream was not found
    }

    private static class DatastreamHandler extends DefaultHandler {
        private final String targetId;

        public DatastreamHandler(String targetId) {
            this.targetId = targetId;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("datastream".equals(qName) && targetId.equals(attributes.getValue("ID"))) {
                throw new SAXException("Found"); // Stop parsing early
            }
        }
    }

}
