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

import org.ceskaexpedice.akubra.*;
import org.ceskaexpedice.akubra.core.repository.*;
import org.ceskaexpedice.fedoramodel.DigitalObject;

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
        coreRepository.ingestObject(digitalObject);
        coreRepository.getProcessingIndex().commit();
    }

    @Override
    public boolean objectExists(String pid) {
        return this.coreRepository.objectExists(pid);
    }

    @Override
    public DigitalObjectWrapper getObject(String pid) {
        return getObject(pid, FoxmlType.managed);
    }

    @Override
    public DigitalObjectWrapper getObject(String pid, FoxmlType foxmlType) {
        RepositoryObject repositoryObject = coreRepository.getObject(pid);
        if (repositoryObject == null) {
            return new DigitalObjectWrapperImpl(null, this);
        }
        DigitalObject digitalObject = repositoryObject.getDigitalObject();
        if(foxmlType == FoxmlType.archive) {
            coreRepository.resolveArchivedDatastreams(digitalObject);
        }
        return new DigitalObjectWrapperImpl(digitalObject, this);
    }

    @Override
    public ObjectProperties getObjectProperties(String pid) {
        RepositoryObject repositoryObject = coreRepository.getObject(pid);
        if (repositoryObject == null) {
            return null;
        }
        return new ObjectPropertiesImpl(repositoryObject);
    }

    @Override
    public void deleteObject(String pid) {
        coreRepository.deleteObject(pid);
    }

    @Override
    public void deleteObject(String pid, boolean deleteDataOfManagedDatastreams, boolean deleteRelationsWithThisAsTarget) {
        coreRepository.deleteObject(pid, deleteDataOfManagedDatastreams, deleteRelationsWithThisAsTarget);
    }

    @Override
    public InputStream marshallObject(DigitalObject obj) {
        return coreRepository.marshallObject(obj);
    }

    @Override
    public DigitalObject unmarshallObject(InputStream inputStream) {
        return coreRepository.unmarshallObject(inputStream);
    }

    @Override
    public void createXMLDatastream(String pid, String dsId, String mimeType, InputStream xmlContent) {
        RepositoryObject repositoryObject = coreRepository.getObject(pid);
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
            RepositoryObject repositoryObject = coreRepository.getObject(pid);
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
        RepositoryObject repositoryObject = coreRepository.getObject(pid);
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
            RepositoryObject repositoryObject = coreRepository.getObject(pid);
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
        RepositoryObject repositoryObject = coreRepository.getObject(pid);
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
            RepositoryObject repositoryObject = coreRepository.getObject(pid);
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
        RepositoryObject repositoryObject = coreRepository.getObject(pid);
        return repositoryObject.streamExists(dsId);
    }

    @Override
    public boolean datastreamExists(String pid, KnownDatastreams dsId) {
        return datastreamExists(pid, dsId.toString());
    }

    @Override
    public DatastreamMetadata getDatastreamMetadata(String pid, String dsId) {
        RepositoryObject object = coreRepository.getObject(pid);
        RepositoryDatastream stream = object.getStream(dsId);
        return new DatastreamMetadataImpl(stream);
    }

    @Override
    public DatastreamMetadata getDatastreamMetadata(String pid, KnownDatastreams dsId) {
        return getDatastreamMetadata(pid, dsId.toString());
    }

    @Override
    public DatastreamContentWrapper getDatastreamContent(String pid, String dsId) {
        RepositoryObject repositoryObject = coreRepository.getObject(pid);
        if (repositoryObject == null || repositoryObject.getStream(dsId) == null) {
            return new DatastreamContentWrapperImpl(null);
        }
        InputStream lastVersionContent = repositoryObject.getStream(dsId).getLastVersionContent();
        return new DatastreamContentWrapperImpl(lastVersionContent);
    }

    @Override
    public DatastreamContentWrapper getDatastreamContent(String pid, KnownDatastreams dsId) {
        return getDatastreamContent(pid, dsId.toString());
    }

    @Override
    public void deleteDatastream(String pid, String dsId) {
        RepositoryObject repositoryObject = coreRepository.getObject(pid);
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
        RepositoryObject object = coreRepository.getObject(pid);
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
