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

import org.apache.commons.io.IOUtils;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexSolr;
import org.ceskaexpedice.akubra.core.repository.RepositoryDatastream;
import org.ceskaexpedice.akubra.core.repository.RepositoryObject;
import org.ceskaexpedice.fedoramodel.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * RepositoryObjectImpl
 *
 * @author pavels
 */
class RepositoryObjectImpl implements RepositoryObject {
    private static final Logger LOGGER = Logger.getLogger(RepositoryObjectImpl.class.getName());
    private static final String RDF_DESCRIPTION_ELEMENT = "Description";
    private static final String RDF_ELEMENT = "RDF";
    private AkubraDOManager manager;
    private DigitalObject digitalObject;
    private ProcessingIndexSolr feeder;

    RepositoryObjectImpl(DigitalObject digitalObject, AkubraDOManager manager, ProcessingIndexSolr feeder) {
        super();
        this.manager = manager;
        this.feeder = feeder;
        this.digitalObject = digitalObject;
    }

    @Override
    public DigitalObject getDigitalObject() {
        return digitalObject;
    }

    @Override
    public InputStream getFoxml() {
        return manager.retrieveObject(getPid());
    }

    @Override
    public String getPid() {
        return digitalObject.getPID();
    }

    @Override
    public Date getPropertyLastModified() {
        return RepositoryUtils.getLastModified(digitalObject);
    }

    @Override
    public List<RepositoryDatastream> getStreams() {
        List<RepositoryDatastream> list = new ArrayList<>();
        List<DatastreamType> datastreamList = digitalObject.getDatastream();
        for (DatastreamType datastreamType : datastreamList) {
            list.add(new RepositoryDatastreamImpl(datastreamType, datastreamType.getID(), controlGroup2Type(datastreamType.getCONTROLGROUP()),manager));
        }
        return list;
    }

    @Override
    public RepositoryDatastream getStream(String streamId) {
        List<DatastreamType> datastreamList = digitalObject.getDatastream();
        for (DatastreamType datastreamType : datastreamList) {
            if (streamId.equals(datastreamType.getID())) {
                return new RepositoryDatastreamImpl(datastreamType, datastreamType.getID(), controlGroup2Type(datastreamType.getCONTROLGROUP()),manager);
            }
        }
        return null;
    }

    @Override
    public boolean streamExists(String streamId) {
        return RepositoryUtils.streamExists(digitalObject, streamId);
    }

    @Override
    public RepositoryDatastream createXMLStream(String streamId, String mimeType, InputStream input) {
        DatastreamType datastreamType = createDatastreamHeader(streamId, mimeType, "X");
        XmlContentType xmlContentType = new XmlContentType();
        xmlContentType.getAny().add(elementFromInputStream(input));
        datastreamType.getDatastreamVersion().get(0).setXmlContent(xmlContentType);

        RepositoryDatastream ds = new RepositoryDatastreamImpl(datastreamType, streamId, RepositoryDatastream.Type.DIRECT, manager);

        try {
            manager.write(digitalObject, streamId);
            if (streamId.equals(KnownDatastreams.RELS_EXT.toString())) {
                try {
                    // process rels-ext and create all children and relations
                    this.feeder.deleteByRelationsForPid(getPid());
                    input.reset();
                    feeder.rebuildProcessingIndex(this, input);
                } catch (Throwable th) {
                    LOGGER.log(Level.SEVERE, "Cannot update processing index for " + getPid() + " - reindex manually.", th);
                }
            }
            return ds;
        } catch (Exception ex) {
            throw new RepositoryException(ex);
        }
    }

    @Override
    public RepositoryDatastream createManagedStream(String streamId, String mimeType, InputStream input) {
        DatastreamType datastreamType = createDatastreamHeader(streamId, mimeType, "M");

        try {
            datastreamType.getDatastreamVersion().get(0).setBinaryContent(IOUtils.toByteArray(input));
            RepositoryDatastream ds = new RepositoryDatastreamImpl(datastreamType, streamId, RepositoryDatastream.Type.DIRECT, manager);
            manager.write(digitalObject, streamId);
            return ds;
        } catch (Exception ex) {
            throw new RepositoryException(ex);
        }
    }

    @Override
    public RepositoryDatastream createRedirectedStream(String streamId, String url, String mimeType) {
        DatastreamType datastreamType = createDatastreamHeader(streamId, mimeType, "E");
        ContentLocationType contentLocationType = new ContentLocationType();
        contentLocationType.setTYPE("URL");
        contentLocationType.setREF(url);
        datastreamType.getDatastreamVersion().get(0).setContentLocation(contentLocationType);

        RepositoryDatastream ds = new RepositoryDatastreamImpl(datastreamType, streamId, RepositoryDatastream.Type.INDIRECT, manager);

        manager.write(digitalObject, streamId);
        return ds;
    }

    @Override
    public void deleteStream(String streamId) {
        manager.deleteStream(getPid(), streamId);
        if (streamId.equals(KnownDatastreams.RELS_EXT.toString())) {
            try {
                this.feeder.deleteByRelationsForPid(this.getPid());
            } catch (Throwable th) {
                LOGGER.log(Level.SEVERE, "Cannot update processing index for " + getPid() + " - reindex manually.", th);
            }
        }
    }

    private DatastreamType createDatastreamHeader(String streamId, String mimeType, String controlGroup) {
        List<DatastreamType> datastreamList = digitalObject.getDatastream();
        Iterator<DatastreamType> iterator = datastreamList.iterator();
        while (iterator.hasNext()) {
            DatastreamType datastreamType = iterator.next();
            if (streamId.equals(datastreamType.getID())) {
                iterator.remove();
            }
        }
        DatastreamType datastreamType = new DatastreamType();
        datastreamType.setID(streamId);
        datastreamType.setCONTROLGROUP(controlGroup);
        datastreamType.setSTATE(StateType.A);
        datastreamType.setVERSIONABLE(false);
        List<DatastreamVersionType> datastreamVersion = datastreamType.getDatastreamVersion();
        DatastreamVersionType datastreamVersionType = new DatastreamVersionType();
        datastreamVersionType.setID(streamId + ".0");
        datastreamVersionType.setCREATED(org.ceskaexpedice.akubra.core.repository.impl.RepositoryUtils.getCurrentXMLGregorianCalendar());
        datastreamVersionType.setMIMETYPE(mimeType);
        String formatUri = RepositoryUtils.getFormatUriForDS(streamId);
        if (formatUri != null) {
            datastreamVersionType.setFORMATURI(formatUri);
        }
        datastreamVersion.add(datastreamVersionType);
        datastreamList.add(datastreamType);
        return datastreamType;
    }

    private RepositoryDatastreamImpl.Type controlGroup2Type(String controlGroup) {
        if ("E".equals(controlGroup) || "R".equals(controlGroup)) {
            return RepositoryDatastream.Type.INDIRECT;
        } else {
            return RepositoryDatastream.Type.DIRECT;
        }
    }

    private static Element elementFromInputStream(InputStream in) {
        DocumentBuilderFactory factory;
        DocumentBuilder builder = null;
        Document ret = null;

        try {
            factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            builder = factory.newDocumentBuilder();
            ret = builder.parse(new InputSource(in));
            if (ret != null) {
                return ret.getDocumentElement();
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void rebuildProcessingIndex() {
        RepositoryDatastream stream = this.getStream(KnownDatastreams.RELS_EXT.toString());
        InputStream content = stream.getLastVersionContent();
        feeder.rebuildProcessingIndex(this, content);
    }


}
