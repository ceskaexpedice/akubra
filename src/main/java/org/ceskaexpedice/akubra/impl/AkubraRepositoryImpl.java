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
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexItem;
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexQueryParameters;
import org.ceskaexpedice.akubra.core.repository.CoreRepository;
import org.ceskaexpedice.akubra.core.repository.RepositoryDatastream;
import org.ceskaexpedice.akubra.core.repository.RepositoryException;
import org.ceskaexpedice.akubra.core.repository.RepositoryObject;
import org.ceskaexpedice.fedoramodel.DigitalObject;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * AkubraRepositoryImpl
 */
public class AkubraRepositoryImpl implements AkubraRepository {
    private static final Logger LOGGER = Logger.getLogger(AkubraRepositoryImpl.class.getName());

    private CoreRepository coreRepository;

    public AkubraRepositoryImpl(CoreRepository coreRepository) {
        this.coreRepository = coreRepository;
    }


    @Override
    public void ingest(DigitalObject digitalObject) {
        coreRepository.ingestObject(digitalObject);
        coreRepository.getProcessingIndexFeeder().commit();
    }

    @Override
    public boolean objectExists(String pid) {
        return this.coreRepository.objectExists(pid);
    }

    @Override
    public DigitalObject getObject(String pid) {
        return getObject(pid, FoxmlType.managed);
    }

    @Override
    public DigitalObject getObject(String pid, FoxmlType foxmlType) {
        Lock readLock = coreRepository.getReadLock(pid);
        try {
            RepositoryObject repositoryObject = coreRepository.getObject(pid);
            if(repositoryObject == null) {
                return null;
            }
            if (foxmlType == FoxmlType.archive) {
                DigitalObject digitalObject = repositoryObject.getDigitalObject();
                coreRepository.resolveArchivedDatastreams(digitalObject);
            }
            return repositoryObject.getDigitalObject();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public ObjectProperties getObjectProperties(String pid) {
        RepositoryObject repositoryObject = coreRepository.getObject(pid);
        if(repositoryObject == null) {
            return null;
        }
        return new ObjectPropertiesImpl(repositoryObject);
    }

    @Override
    public void deleteObject(String pid) {
        coreRepository.deleteObject(pid);
        coreRepository.getProcessingIndexFeeder().commit();
    }

    @Override
    public void deleteObject(String pid, boolean deleteDataOfManagedDatastreams, boolean deleteRelationsWithThisAsTarget) {
        coreRepository.deleteObject(pid, deleteDataOfManagedDatastreams, deleteRelationsWithThisAsTarget);
        coreRepository.getProcessingIndexFeeder().commit();
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
        if(repositoryObject == null) {
            return;
        }
        repositoryObject.createXMLStream(dsId, mimeType, xmlContent);
    }

    @Override
    public void createManagedDatastream(String pid, String dsId, String mimeType, InputStream binaryContent) {
        RepositoryObject repositoryObject = coreRepository.getObject(pid);
        if(repositoryObject == null) {
            return;
        }
        repositoryObject.createManagedStream(dsId, mimeType, binaryContent);
    }

    @Override
    public void createRedirectedDatastream(String pid, String dsId, String url, String mimeType) {
        RepositoryObject repositoryObject = coreRepository.getObject(pid);
        if(repositoryObject == null) {
            return;
        }
        repositoryObject.createRedirectedStream(dsId, url, mimeType);
    }

    @Override
    public boolean datastreamExists(String pid, String dsId) {
        RepositoryObject repositoryObject = coreRepository.getObject(pid);
        return repositoryObject.streamExists(dsId);
    }

    @Override
    public DatastreamMetadata getDatastreamMetadata(String pid, String dsId) {
        Lock readLock = coreRepository.getReadLock(pid);
        try {
            RepositoryObject object = coreRepository.getObject(pid);
            RepositoryDatastream stream = object.getStream(dsId);
            return new DatastreamMetadataImpl(stream);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public InputStream getDatastreamContent(String pid, String dsId) {
        Lock readLock = coreRepository.getReadLock(pid);
        try {
            RepositoryObject repositoryObject = coreRepository.getObject(pid);
            if(repositoryObject == null || repositoryObject.getStream(dsId) == null) {
                return null;
            }
            InputStream lastVersionContent = repositoryObject.getStream(dsId).getLastVersionContent();
            return lastVersionContent;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void deleteDatastream(String pid, String dsId) {
        RepositoryObject repositoryObject = coreRepository.getObject(pid);
        if(repositoryObject == null) {
            return;
        }
        repositoryObject.deleteStream(dsId);
    }

    @Override
    public RelsExtWrapper relsExtGet(String pid) {
        RepositoryObject repositoryObject = coreRepository.getObject(pid);
        if(repositoryObject == null) {
            return null;
        }
        return new RelsExtWrapperImpl(repositoryObject);
    }

    @Override
    public void relsExtAddRelation(String pid, String relation, String namespace, String targetRelation) {
        RepositoryObject repositoryObject = coreRepository.getObject(pid);
        if(repositoryObject == null) {
            return;
        }
        repositoryObject.relsExtAddRelation(relation, namespace, targetRelation);
    }

    @Override
    public void relsExtRemoveRelation(String pid, String relation, String namespace, String targetRelation) {
        RepositoryObject repositoryObject = coreRepository.getObject(pid);
        if(repositoryObject == null) {
            return;
        }
        repositoryObject.relsExtRemoveRelation(relation, namespace, targetRelation);
    }

    @Override
    public void relsExtAddLiteral(String pid, String relation, String namespace, String value) {
        RepositoryObject repositoryObject = coreRepository.getObject(pid);
        if(repositoryObject == null) {
            return;
        }
        repositoryObject.relsExtAddLiteral(relation, namespace, value);
    }

    @Override
    public void relsExtRemoveLiteral(String pid, String relation, String namespace, String value) {
        RepositoryObject repositoryObject = coreRepository.getObject(pid);
        if(repositoryObject == null) {
            return;
        }
        repositoryObject.relsExtRemoveLiteral(relation, namespace, value);
    }

    @Override
    public List<String> getDatastreamNames(String pid) {
        Lock readLock = coreRepository.getReadLock(pid);
        try {
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
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void iterateProcessingIndex(ProcessingIndexQueryParameters params, Consumer<ProcessingIndexItem> action) {
        coreRepository.getProcessingIndexFeeder().iterate(params, action);
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
