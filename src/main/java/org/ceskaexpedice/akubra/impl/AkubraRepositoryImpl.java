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
package org.ceskaexpedice.akubra.impl;

import org.apache.commons.io.IOUtils;
import org.ceskaexpedice.akubra.*;
import org.ceskaexpedice.akubra.core.repository.CoreRepository;
import org.ceskaexpedice.akubra.core.repository.RepositoryDatastream;
import org.ceskaexpedice.akubra.core.repository.RepositoryObject;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndex;
import org.ceskaexpedice.akubra.relsext.RelsExtHandler;
import org.ceskaexpedice.akubra.utils.sax.SaxUtils;
import org.ceskaexpedice.fedoramodel.DigitalObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * AkubraRepositoryImpl
 */
public class AkubraRepositoryImpl implements AkubraRepository {
    private static final Logger LOGGER = Logger.getLogger(AkubraRepositoryImpl.class.getName());

    private CoreRepository coreRepository;
    private RelsExtHandler relsExtHandler;

    public AkubraRepositoryImpl(CoreRepository coreRepository) {
        this.coreRepository = coreRepository;
        this.relsExtHandler = new RelsExtHandlerImpl(coreRepository);
    }


    @Override
    public void ingest(DigitalObject digitalObject) {
        coreRepository.ingest(digitalObject);
        coreRepository.getProcessingIndex().commit();
    }

    @Override
    public boolean exists(String pid) {
        return this.coreRepository.exists(pid);
    }

    @Override
    public DigitalObjectWrapper get(String pid) {
        byte[] objectBytes = coreRepository.getAsBytes(pid);
        if(objectBytes == null || objectBytes.length == 0) {
            return null;
        }
        return new DigitalObjectWrapperImpl(objectBytes, this);
    }

    @Override
    public DigitalObjectWrapper export(String pid) {
        // TODO AK_NEW make it more efficient
        RepositoryObject repositoryObject = coreRepository.getAsRepositoryObject(pid);
        if (repositoryObject == null) {
            return new DigitalObjectWrapperImpl(null, this);
        }
        DigitalObject digitalObject = repositoryObject.getDigitalObject();
        coreRepository.resolveArchivedDatastreams(digitalObject);
        InputStream inputStream = coreRepository.marshall(digitalObject);
        byte[] byteArray;
        try {
            byteArray = IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
        return new DigitalObjectWrapperImpl(byteArray, this);
    }

    @Override
    public ObjectProperties getProperties(String pid) {
        RepositoryObject repositoryObject = coreRepository.getAsRepositoryObject(pid);
        if (repositoryObject == null) {
            return null;
        }
        return new ObjectPropertiesImpl(repositoryObject);
    }

    @Override
    public void delete(String pid) {
        coreRepository.delete(pid);
    }

    @Override
    public void delete(String pid, boolean deleteDataOfManagedDatastreams, boolean deleteRelationsWithThisAsTarget) {
        coreRepository.delete(pid, deleteDataOfManagedDatastreams, deleteRelationsWithThisAsTarget);
    }

    @Override
    public InputStream marshall(DigitalObject obj) {
        return coreRepository.marshall(obj);
    }

    @Override
    public DigitalObject unmarshall(InputStream inputStream) {
        return coreRepository.unmarshall(inputStream);
    }

    @Override
    public void createXMLDatastream(String pid, String dsId, String mimeType, InputStream xmlContent) {
        RepositoryObject repositoryObject = coreRepository.getAsRepositoryObject(pid);
        if (repositoryObject == null) {
            return;
        }
        repositoryObject.createXMLStream(dsId, mimeType, xmlContent);
    }

    @Override
    public void createXMLDatastream(String pid, KnownDatastreams dsId, String mimeType, InputStream xmlContent) {
        createXMLDatastream(pid, dsId.toString(), mimeType, xmlContent);
    }

    @Override
    public void updateXMLDatastream(String pid, String dsId, String mimeType, InputStream binaryContent) {
        doWithWriteLock(pid, () -> {
            RepositoryObject repositoryObject = coreRepository.getAsRepositoryObject(pid);
            if (repositoryObject == null) {
                return null;
            }
            repositoryObject.deleteStream(dsId);
            repositoryObject.createXMLStream(dsId, mimeType, binaryContent);
            return null;
        });
    }

    @Override
    public void updateXMLDatastream(String pid, KnownDatastreams dsId, String mimeType, InputStream binaryContent) {
        updateXMLDatastream(pid, dsId.toString(), mimeType, binaryContent);
    }

    @Override
    public void createManagedDatastream(String pid, String dsId, String mimeType, InputStream binaryContent) {
        RepositoryObject repositoryObject = coreRepository.getAsRepositoryObject(pid);
        if (repositoryObject == null) {
            return;
        }
        repositoryObject.createManagedStream(dsId, mimeType, binaryContent);
    }

    @Override
    public void createManagedDatastream(String pid, KnownDatastreams dsId, String mimeType, InputStream binaryContent) {
        createManagedDatastream(pid, dsId.toString(), mimeType, binaryContent);
    }

    @Override
    public void updateManagedDatastream(String pid, String dsId, String mimeType, InputStream binaryContent) {
        doWithWriteLock(pid, () -> {
            RepositoryObject repositoryObject = coreRepository.getAsRepositoryObject(pid);
            if (repositoryObject == null) {
                return null;
            }
            repositoryObject.deleteStream(dsId);
            repositoryObject.createManagedStream(dsId, mimeType, binaryContent);
            return null;
        });
    }

    @Override
    public void updateManagedDatastream(String pid, KnownDatastreams dsId, String mimeType, InputStream binaryContent) {
        updateManagedDatastream(pid, dsId.toString(), mimeType, binaryContent);
    }

    @Override
    public void createRedirectedDatastream(String pid, String dsId, String url, String mimeType) {
        RepositoryObject repositoryObject = coreRepository.getAsRepositoryObject(pid);
        if (repositoryObject == null) {
            return;
        }
        repositoryObject.createRedirectedStream(dsId, url, mimeType);
    }

    @Override
    public void createRedirectedDatastream(String pid, KnownDatastreams dsId, String url, String mimeType) {
        createRedirectedDatastream(pid, dsId.toString(), url, mimeType);
    }

    @Override
    public void updateRedirectedDatastream(String pid, String dsId, String url, String mimeType) {
        doWithWriteLock(pid, () -> {
            RepositoryObject repositoryObject = coreRepository.getAsRepositoryObject(pid);
            if (repositoryObject == null) {
                return null;
            }
            repositoryObject.deleteStream(dsId);
            repositoryObject.createRedirectedStream(dsId, url, mimeType);
            return null;
        });
    }

    @Override
    public void updateRedirectedDatastream(String pid, KnownDatastreams dsId, String url, String mimeType) {
        updateRedirectedDatastream(pid, dsId.toString(), url, mimeType);
    }

    @Override
    public boolean datastreamExists(String pid, String dsId) {
        boolean exists = SaxUtils.containsDatastream(get(pid).asInputStream(), dsId);
        return exists;
    }

    @Override
    public boolean datastreamExists(String pid, KnownDatastreams dsId) {
        return datastreamExists(pid, dsId.toString());
    }

    @Override
    public DatastreamMetadata getDatastreamMetadata(String pid, String dsId) {
        RepositoryObject object = coreRepository.getAsRepositoryObject(pid);
        RepositoryDatastream stream = object.getStream(dsId);
        return new DatastreamMetadataImpl(stream);
    }

    @Override
    public DatastreamMetadata getDatastreamMetadata(String pid, KnownDatastreams dsId) {
        return getDatastreamMetadata(pid, dsId.toString());
    }

    @Override
    public DatastreamContentWrapper getDatastreamContent(String pid, String dsId) {
        // TODO check exists
        InputStream streamContent = SaxUtils.getStreamContent(get(pid).asInputStream(), dsId, coreRepository);
        return new DatastreamContentWrapperImpl(streamContent);
    }

    @Override
    public DatastreamContentWrapper getDatastreamContent(String pid, KnownDatastreams dsId) {
        return getDatastreamContent(pid, dsId.toString());
    }

    @Override
    public void deleteDatastream(String pid, String dsId) {
        RepositoryObject repositoryObject = coreRepository.getAsRepositoryObject(pid);
        if (repositoryObject == null) {
            return;
        }
        repositoryObject.deleteStream(dsId);
    }

    @Override
    public void deleteDatastream(String pid, KnownDatastreams dsId) {
        deleteDatastream(pid, dsId.toString());
    }

    @Override
    public List<String> getDatastreamNames(String pid) {
        RepositoryObject object = coreRepository.getAsRepositoryObject(pid);
        List<RepositoryDatastream> streams = object.getStreams();
        return streams.stream().map(it -> {
            try {
                return it.getName();
            } catch (RepositoryException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                return null;
            }
        }).collect(Collectors.toList());
    }

    @Override
    public ProcessingIndex getProcessingIndex() {
        return coreRepository.getProcessingIndex();
    }

    @Override
    public RelsExtHandler getRelsExtHandler() {
        return relsExtHandler;
    }

    @Override
    public <T> T doWithReadLock(String pid, LockOperation<T> operation) {
        Lock readLock = coreRepository.getReadLock(pid);
        try {
            return operation.execute();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public <T> T doWithWriteLock(String pid, LockOperation<T> operation) {
        Lock writeLock = coreRepository.getWriteLock(pid);
        try {
            return operation.execute();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void shutdown() {
        coreRepository.shutdown();
    }

}
