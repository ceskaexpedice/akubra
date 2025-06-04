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

import org.akubraproject.map.IdMapper;
import org.apache.commons.io.IOUtils;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.LockOperation;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.config.RepositoryConfiguration;
import org.ceskaexpedice.akubra.core.repository.CoreRepository;
import org.ceskaexpedice.akubra.core.repository.RepositoryDatastream;
import org.ceskaexpedice.akubra.core.repository.RepositoryObject;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndex;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndexItem;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndexQueryParameters;
import org.ceskaexpedice.fedoramodel.*;
import org.fcrepo.server.storage.lowlevel.akubra.HashPathIdMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.ceskaexpedice.akubra.core.repository.impl.RepositoryUtils.getBlobId;
import static org.ceskaexpedice.akubra.core.repository.impl.RepositoryUtils.validateId;

/**
 * CoreRepositoryImpl
 * @author pavels
 */
public class CoreRepositoryImpl implements CoreRepository {

    private static final Logger LOGGER = Logger.getLogger(CoreRepositoryImpl.class.getName());
    private final IdMapper idMapper;

    private AkubraDOManager manager;
    private ProcessingIndex processingIndex;

    public CoreRepositoryImpl(RepositoryConfiguration configuration) {
        super();
        this.manager = new AkubraDOManager(configuration);
        idMapper = new HashPathIdMapper(manager.getConfiguration().getDatastreamStorePattern());
    }

    public void setProcessingIndex(ProcessingIndex processingIndex) {
        this.processingIndex = processingIndex;
    }

    @Override
    public boolean exists(String pid) {
        try {
            File file = getObjectStorePath(pid);
            return file == null ? false : file.exists();
        } catch (Exception e) {
            LOGGER.warning("Exception while checking if object exists: " + e.getMessage());
            return false;
        }
    }

    @Override
    public File getObjectStorePath(String pid) {
        try {
            URI blobId = getBlobId(pid);
            URI internalId = idMapper.getInternalId(blobId);
            URI canonicalId = validateId(internalId);
            File file = new File(manager.getConfiguration().getObjectStorePath(), canonicalId.getRawSchemeSpecificPart());
            return file;
        } catch (Exception e) {
            LOGGER.warning("Exception while checking if object exists: " + e.getMessage());
            return null;
        }
    }

    @Override
    public RepositoryObject ingest(DigitalObject digitalObject) {
        if (exists(digitalObject.getPID())) {
            throw new RepositoryException("Ingested object exists:" + digitalObject.getPID());
        } else {
            RepositoryObjectImpl obj = new RepositoryObjectImpl(digitalObject);
            manager.write(obj.getDigitalObject(), null);
            processingIndex.rebuildProcessingIndex(obj.getPid(), null);
            return obj;
        }
    }

    @Override
    public RepositoryObject getAsRepositoryObject(String pid) {
        DigitalObject digitalObject = this.manager.readObjectFromStorage(pid);
        if (digitalObject == null) {
            return null;
        }
        RepositoryObjectImpl obj = new RepositoryObjectImpl(digitalObject);
        return obj;
    }

    @Override
    public byte[] getAsBytes(String pid) {
        return this.manager.retrieveObjectBytes(pid);
    }

    @Override
    public void resolveArchivedDatastreams(DigitalObject obj) {
        manager.resolveArchivedDatastreams(obj);
    }

    @Override
    public void delete(String pid, boolean deleteDataOfManagedDatastreams, boolean deleteRelationsWithThisAsTarget) {
        try {
            this.manager.deleteObject(pid, deleteDataOfManagedDatastreams);
            try {
                // delete relations with this object as a source
                this.processingIndex.deleteByRelationsForPid(pid);
                // possibly delete relations with this object as a target
                if (deleteRelationsWithThisAsTarget) {
                    this.processingIndex.deleteByTargetPid(pid);
                }
                // delete this object's description
                this.processingIndex.deleteDescriptionByPid(pid);
            } catch (Throwable th) {
                LOGGER.log(Level.SEVERE, "Cannot update processing index for " + pid + " - reindex manually.", th);
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        } finally {
            try {
                this.processingIndex.commit();
                LOGGER.info("CALLED PROCESSING INDEX COMMIT AFTER DELETE " + pid);
            } catch (Exception e) {
                throw new RepositoryException(e);
            }
        }
    }

    @Override
    public void delete(String pid) {
        delete(pid, true, true);
    }

    @Override
    public InputStream marshall(DigitalObject obj) {
        return manager.marshallObject(obj);
    }

    @Override
    public DigitalObject unmarshall(InputStream inputStream) {
        return manager.unmarshallObject(inputStream);
    }

    @Override
    public InputStream getDatastreamContent(String pid, String dsId) {
        byte[] asBytes = getAsBytes(pid);
        if(asBytes == null) {
            return null;
        }
        InputStream streamContent = RepositoryUtils.getDatastreamContent(pid, new ByteArrayInputStream(asBytes), dsId, this);
        if(streamContent == null) {
            return null;
        }
        return streamContent;
    }

    @Override
    public InputStream retrieveDatastreamByInternalId(String dsKey) {
        return this.manager.retrieveDatastream(dsKey);
    }

    @Override
    public boolean datastreamExists(String pid, String dsId) {
        byte[] asBytes = getAsBytes(pid);
        if(asBytes == null) {
            return false;
        }
        boolean exists = RepositoryUtils.datastreamExists(new ByteArrayInputStream(asBytes), dsId);
        return exists;
    }

    @Override
    public RepositoryDatastream createXMLDatastream(RepositoryObject repositoryObject, String dsId, String mimeType, InputStream input) {
        DatastreamType datastreamType = createDatastreamHeader(repositoryObject.getDigitalObject(), dsId, mimeType, "X");
        XmlContentType xmlContentType = new XmlContentType();
        xmlContentType.getAny().add(elementFromInputStream(input));
        datastreamType.getDatastreamVersion().get(0).setXmlContent(xmlContentType);

        RepositoryDatastream ds = new RepositoryDatastreamImpl(datastreamType, dsId, RepositoryDatastream.Type.DIRECT);

        try {
            manager.write(repositoryObject.getDigitalObject(), dsId);
            if (dsId.equals(KnownDatastreams.RELS_EXT.toString())) {
                try {
                    // process rels-ext and create all children and relations
                    //this.processingIndex.deleteByRelationsForPid(repositoryObject.getPid());

                    String query = "source:\"" + repositoryObject.getPid() + "\" AND type:\"relation\"";
                    List<ProcessingIndexItem> pids = new ArrayList<>();
                    ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                            .rows(10_000) // TODO: Do it better- maximum number of relations
                            .queryString(query)
                            .fieldsToFetch(Arrays.asList("pid"))
                            .build();
                    this.processingIndex.lookAt(params, pids::add);

                    input.reset();
                    processingIndex.rebuildProcessingIndex(repositoryObject.getPid(),updateRequest -> {
                        pids.stream().forEach(pid-> {
                            updateRequest.deleteById(pid.pid());
                        });
                    });
                } catch (Throwable th) {
                    LOGGER.log(Level.SEVERE, "Cannot update processing index for " + repositoryObject.getPid() + " - reindex manually.", th);
                }
            }
            return ds;
        } catch (Exception ex) {
            throw new RepositoryException(ex);
        }
    }

    @Override
    public RepositoryDatastream createManagedDatastream(RepositoryObject repositoryObject, String dsId, String mimeType, InputStream input) {
        DatastreamType datastreamType = createDatastreamHeader(repositoryObject.getDigitalObject(), dsId, mimeType, "M");

        try {
            datastreamType.getDatastreamVersion().get(0).setBinaryContent(IOUtils.toByteArray(input));
            RepositoryDatastream ds = new RepositoryDatastreamImpl(datastreamType, dsId, RepositoryDatastream.Type.DIRECT);
            manager.write(repositoryObject.getDigitalObject(), dsId);
            return ds;
        } catch (Exception ex) {
            throw new RepositoryException(ex);
        }
    }

    @Override
    public RepositoryDatastream createRedirectedDatastream(RepositoryObject repositoryObject, String dsId, String url, String mimeType) {
        DatastreamType datastreamType = createDatastreamHeader(repositoryObject.getDigitalObject(), dsId, mimeType, "E");
        ContentLocationType contentLocationType = new ContentLocationType();
        contentLocationType.setTYPE("URL");
        contentLocationType.setREF(url);
        datastreamType.getDatastreamVersion().get(0).setContentLocation(contentLocationType);

        RepositoryDatastream ds = new RepositoryDatastreamImpl(datastreamType, dsId, RepositoryDatastream.Type.INDIRECT);

        manager.write(repositoryObject.getDigitalObject(), dsId);
        return ds;
    }

    @Override
    public void deleteDatastream(String pid, String dsId) {
        manager.deleteStream(pid, dsId);
        if (dsId.equals(KnownDatastreams.RELS_EXT.toString())) {
            try {
                this.processingIndex.deleteByRelationsForPid(pid);
            } catch (Throwable th) {
                LOGGER.log(Level.SEVERE, "Cannot update processing index for " + pid + " - reindex manually.", th);
            }
        }
    }

    @Override
    public ProcessingIndex getProcessingIndex() {
        return this.processingIndex;
    }

    @Override
    public <T> T doWithReadLock(String pid, LockOperation<T> operation){
        return manager.doWithReadLock(pid, operation);
    }

    @Override
    public <T> T doWithWriteLock(String pid, LockOperation<T> operation){
        return manager.doWithWriteLock(pid, operation);
    }

    @Override
    public void shutdown() {
        manager.shutdown();
    }

    private DatastreamType createDatastreamHeader(DigitalObject digitalObject, String streamId, String mimeType, String controlGroup) {
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

}
