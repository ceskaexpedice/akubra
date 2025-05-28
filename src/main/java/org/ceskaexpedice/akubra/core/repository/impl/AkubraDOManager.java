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

import ca.thoughtwire.lock.DistributedLockService;
import org.akubraproject.BlobStore;
import org.akubraproject.fs.FSBlobStore;
import org.akubraproject.map.IdMapper;
import org.akubraproject.map.IdMappingBlobStore;
import org.apache.commons.io.IOUtils;
import org.ceskaexpedice.akubra.DistributedLocksException;
import org.ceskaexpedice.akubra.LockOperation;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.config.RepositoryConfiguration;
import org.ceskaexpedice.fedoramodel.*;
import org.ceskaexpedice.hazelcast.HazelcastClientNode;
import org.ceskaexpedice.hazelcast.HazelcastConfiguration;
import org.fcrepo.server.errors.LowlevelStorageException;
import org.fcrepo.server.errors.ObjectAlreadyInLowlevelStorageException;
import org.fcrepo.server.errors.ObjectNotInLowlevelStorageException;
import org.fcrepo.server.storage.lowlevel.ICheckable;
import org.fcrepo.server.storage.lowlevel.ILowlevelStorage;
import org.fcrepo.server.storage.lowlevel.akubra.AkubraLowlevelStorage;
import org.fcrepo.server.storage.lowlevel.akubra.HashPathIdMapper;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AkubraDOManager
 */
class AkubraDOManager {
    private static final int UNMARSHALLER_POOL_CAPACITY = 50; // TODO make it configurable
    private static final Logger LOGGER = Logger.getLogger(AkubraDOManager.class.getName());

    private RepositoryConfiguration configuration;
    private ILowlevelStorage storage;

    private DistributedLockService lockService;
    private HazelcastClientNode hazelcastClientNode;

    private final BlockingQueue<Unmarshaller> unmarshallerPool = new LinkedBlockingQueue<>(UNMARSHALLER_POOL_CAPACITY);
    private Marshaller marshaller;

    AkubraDOManager(RepositoryConfiguration configuration) {
        try {
            this.initialize(configuration);
            this.configuration = configuration;
            this.storage = initLowLevelStorage();
        } catch (Exception ex) {
            throw new RepositoryException(ex);
        }
    }

    private void initialize(RepositoryConfiguration configuration) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DigitalObject.class);
            for (int i = 0; i < UNMARSHALLER_POOL_CAPACITY; i++) {
                unmarshallerPool.offer(jaxbContext.createUnmarshaller());
            }
            marshaller = jaxbContext.createMarshaller();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Cannot init JAXB", e);
            throw new RepositoryException(e);
        }
    }

    private DistributedLockService getLockService() {
        HazelcastConfiguration hazelcastConfiguration = configuration.getHazelcastConfiguration();
        if(hazelcastConfiguration == null){
            return null;
        }
        if (lockService == null) { // First check (without lock)
            synchronized (this) {
                if (lockService == null) { // Second check (within lock)
                    try {
                        hazelcastClientNode = new HazelcastClientNode();
                        hazelcastClientNode.ensureHazelcastNode(hazelcastConfiguration);
                        lockService = DistributedLockService.newHazelcastLockService(hazelcastClientNode);
                    } catch (Exception e) {
                        throw new DistributedLocksException(DistributedLocksException.LOCK_SERVER_ERROR, e);
                    }
                }
            }
        }
        return lockService;
    }

    private ILowlevelStorage initLowLevelStorage() throws Exception {
        return createAkubraLowLevelStorage();
    }

    private AkubraLowlevelStorage createAkubraLowLevelStorage() throws Exception {
        BlobStore fsObjectStore = new FSBlobStore(new URI("urn:example.org:fsObjectStore"), new File(configuration.getObjectStorePath()));
        IdMapper fsObjectStoreMapper = new HashPathIdMapper(configuration.getObjectStorePattern());
        BlobStore objectStore = new IdMappingBlobStore(new URI("urn:example.org:objectStore"), fsObjectStore, fsObjectStoreMapper);
        BlobStore fsDatastreamStore = new FSBlobStore(new URI("urn:example.org:fsDatastreamStore"), new File(configuration.getDatastreamStorePath()));
        IdMapper fsDatastreamStoreMapper = new HashPathIdMapper(configuration.getDatastreamStorePattern());
        BlobStore datastreamStore = new IdMappingBlobStore(new URI("urn:example.org:datastreamStore"), fsDatastreamStore, fsDatastreamStoreMapper);
        AkubraLowlevelStorage retval = new AkubraLowlevelStorage(objectStore, datastreamStore, true, true);
        return retval;
    }

    DigitalObject readObjectFromStorage(String pid) {
        DigitalObject retval;
        Object obj;
        try (InputStream inputStream = this.storage.retrieveObject(pid);) {
            Unmarshaller unmarshaller = unmarshallerPool.take();
            obj = unmarshaller.unmarshal(inputStream);
            unmarshallerPool.offer(unmarshaller);
        } catch (ObjectNotInLowlevelStorageException ex) {
            return null;
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
        retval = (DigitalObject) obj;
        return retval;
    }

    InputStream retrieveDatastream(String dsKey) {
        try {
            return storage.retrieveDatastream(dsKey);
        } catch (LowlevelStorageException e) {
            throw new RepositoryException(e);
        }
    }

    InputStream retrieveObject(String objectKey) {
        try {
            return storage.retrieveObject(objectKey);
        } catch (LowlevelStorageException e) {
            throw new RepositoryException(e);
        }
    }

    byte[] retrieveObjectBytes(String pid) {
        try (InputStream io = storage.retrieveObject(pid)) {
            return IOUtils.toByteArray(io);
        } catch (ObjectNotInLowlevelStorageException e) {
            return null;
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    void deleteObject(String pid, boolean includingManagedDatastreams) {
        doWithWriteLock(pid, () -> {
            DigitalObject object = readObjectFromStorage(pid);
            if (includingManagedDatastreams) {
                for (DatastreamType datastreamType : object.getDatastream()) {
                    removeManagedStream(datastreamType);
                }
            }
            try {
                storage.removeObject(pid);
            } catch (LowlevelStorageException e) {
                LOGGER.severe("Could not remove object from Akubra: " + e);
            }
            return null;
        });
    }

    void deleteStream(String pid, String streamId) {
        doWithWriteLock(pid, () -> {
            DigitalObject object = readObjectFromStorage(pid);
            List<DatastreamType> datastreamList = object.getDatastream();
            Iterator<DatastreamType> iterator = datastreamList.iterator();
            while (iterator.hasNext()) {
                DatastreamType datastreamType = iterator.next();
                if (streamId.equals(datastreamType.getID())) {
                    removeManagedStream(datastreamType);
                    iterator.remove();
                    break;
                }
            }
            try {
                setLastModified(object);
                StringWriter stringWriter = new StringWriter();
                synchronized (marshaller) {
                    marshaller.marshal(object, stringWriter);
                }
                addOrReplaceObject(pid, new ByteArrayInputStream(stringWriter.toString().getBytes("UTF-8")));

            } catch (Exception e) {
                LOGGER.severe("Could not replace object in Akubra: " + e + ", pid:'" + pid + "'");
            }
            return null;
        });
    }

    private void removeManagedStream(DatastreamType datastreamType) {
        if ("M".equals(datastreamType.getCONTROLGROUP())) {
            for (DatastreamVersionType datastreamVersionType : datastreamType.getDatastreamVersion()) {
                if ("INTERNAL_ID".equals(datastreamVersionType.getContentLocation().getTYPE())) {
                    try {
                        storage.removeDatastream(datastreamVersionType.getContentLocation().getREF());
                    } catch (LowlevelStorageException e) {
                        LOGGER.severe("Could not remove managed datastream from Akubra: " + e);
                    }
                }
            }
        }
    }

    void write(DigitalObject object, String streamId) {
        doWithWriteLock(object.getPID(), () -> {
            List<DatastreamType> datastreamList = object.getDatastream();
            Iterator<DatastreamType> iterator = datastreamList.iterator();
            while (iterator.hasNext()) {
                DatastreamType datastream = iterator.next();
                ensureDsVersionCreatedDate(datastream);
                if (streamId != null && streamId.equals(datastream.getID())) {
                    convertManagedStream(object.getPID(), datastream);
                    break;
                } else {
                    convertManagedStream(object.getPID(), datastream);
                }
            }
            try {
                setLastModified(object);
                ensureCreatedDate(object);
                ensureActive(object);
                StringWriter stringWriter = new StringWriter();
                synchronized (marshaller) {
                    marshaller.marshal(object, stringWriter);
                }
                addOrReplaceObject(object.getPID(), new ByteArrayInputStream(stringWriter.toString().getBytes("UTF-8")));
            } catch (Exception e) {
                LOGGER.severe("Could not replace object in Akubra: " + e);
            }
            return null;
        });
    }

    InputStream marshallObject(DigitalObject object) {
        try {
            StringWriter stringWriter = new StringWriter();
            synchronized (marshaller) {
                marshaller.marshal(object, stringWriter);
            }
            return new ByteArrayInputStream(stringWriter.toString().getBytes("UTF-8"));
        } catch (Exception e) {
            LOGGER.severe("Could not marshall object: " + e);
            throw new RepositoryException(e);
        }
    }

    DigitalObject unmarshallObject(InputStream inputStream) {
        try {
            Object obj;
            Unmarshaller unmarshaller = unmarshallerPool.take();
            obj = unmarshaller.unmarshal(inputStream);
            unmarshallerPool.offer(unmarshaller);
            return (DigitalObject) obj;
        } catch (Exception e) {
            LOGGER.severe("Could not unmarshall object: " + e);
            throw new RepositoryException(e);
        }
    }


    private void setLastModified(DigitalObject object) {
        boolean propertyExists = false;
        List<PropertyType> propertyTypeList = object.getObjectProperties().getProperty();
        for (PropertyType propertyType : propertyTypeList) {
            if ("info:fedora/fedora-system:def/view#lastModifiedDate".equals(propertyType.getNAME())) {
                propertyType.setVALUE(RepositoryUtils.currentTimeString());
                propertyExists = true;
                break;
            }
        }
        if (!propertyExists) {
            propertyTypeList.add(RepositoryUtils.createProperty("info:fedora/fedora-system:def/view#lastModifiedDate", RepositoryUtils.currentTimeString()));
        }
    }

    private void ensureCreatedDate(DigitalObject object) {
        boolean propertyExists = false;
        List<PropertyType> propertyTypeList = object.getObjectProperties().getProperty();
        for (PropertyType propertyType : propertyTypeList) {
            if ("info:fedora/fedora-system:def/model#createdDate".equals(propertyType.getNAME())) {
                propertyExists = true;
                break;
            }
        }
        if (!propertyExists) {
            propertyTypeList.add(RepositoryUtils.createProperty("info:fedora/fedora-system:def/model#createdDate", RepositoryUtils.currentTimeString()));
        }
    }

    private void ensureActive(DigitalObject object) {
        boolean propertyExists = false;
        List<PropertyType> propertyTypeList = object.getObjectProperties().getProperty();
        for (PropertyType propertyType : propertyTypeList) {
            if ("info:fedora/fedora-system:def/model#state".equals(propertyType.getNAME())) {
                propertyExists = true;
                break;
            }
        }
        if (!propertyExists) {
            propertyTypeList.add(RepositoryUtils.createProperty("info:fedora/fedora-system:def/model#state", "Active"));
        }
    }

    private void ensureDsVersionCreatedDate(DatastreamType datastream) {
        if (datastream != null) {
            for (DatastreamVersionType datastreamVersion : datastream.getDatastreamVersion()) {
                XMLGregorianCalendar created = datastreamVersion.getCREATED();
                if (created == null) {
                    datastreamVersion.setCREATED(RepositoryUtils.getCurrentXMLGregorianCalendar());
                }
            }
        }
    }

    private void convertManagedStream(String pid, DatastreamType datastream) {
        if ("M".equals(datastream.getCONTROLGROUP())) {
            for (DatastreamVersionType datastreamVersion : datastream.getDatastreamVersion()) {
                if (datastreamVersion.getBinaryContent() != null) {
                    try {
                        String ref = pid + "+" + datastream.getID() + "+" + datastreamVersion.getID();
                        addOrReplaceDatastream(ref, new ByteArrayInputStream(datastreamVersion.getBinaryContent()));
                        datastreamVersion.setBinaryContent(null);
                        ContentLocationType contentLocationType = new ContentLocationType();
                        contentLocationType.setTYPE("INTERNAL_ID");
                        contentLocationType.setREF(ref);
                        datastreamVersion.setContentLocation(contentLocationType);
                    } catch (LowlevelStorageException e) {
                        LOGGER.severe("Could not remove managed datastream from Akubra: " + e);
                    }
                }
            }
        }
    }

    void resolveArchivedDatastreams(DigitalObject object) {
        for (DatastreamType datastreamType : object.getDatastream()) {
            resolveArchiveManagedStream(datastreamType);
        }
    }

    private void resolveArchiveManagedStream(DatastreamType datastream) {
        if ("M".equals(datastream.getCONTROLGROUP())) {
            for (DatastreamVersionType datastreamVersion : datastream.getDatastreamVersion()) {
                try {
                    InputStream stream = retrieveDatastream(datastreamVersion.getContentLocation().getREF());
                    datastreamVersion.setBinaryContent(IOUtils.toByteArray(stream));
                    datastreamVersion.setContentLocation(null);
                } catch (Exception ex) {
                    LOGGER.severe("Could not resolve archive managed datastream: " + ex);
                }
            }
        }
    }

    void addOrReplaceObject(String pid, InputStream content) throws LowlevelStorageException {
        if (((ICheckable) storage).objectExists(pid)) {
            storage.replaceObject(pid, content, null);
        } else {
            storage.addObject(pid, content, null);
        }
    }

    void addOrReplaceDatastream(String pid, InputStream content) throws LowlevelStorageException {
        if (storage instanceof AkubraLowlevelStorage) {
            if (((AkubraLowlevelStorage) storage).datastreamExists(pid)) {
                storage.replaceDatastream(pid, content, null);
            } else {
                storage.addDatastream(pid, content, null);
            }
        } else {
            try {
                storage.addDatastream(pid, content, null);
            } catch (ObjectAlreadyInLowlevelStorageException oailse) {
                storage.replaceDatastream(pid, content, null);
            }
        }
    }

    <T> T doWithReadLock(String pid, LockOperation<T> operation) {
        return doWithLock(pid, operation, false);
    }

    <T> T doWithWriteLock(String pid, LockOperation<T> operation) {
        return doWithLock(pid, operation, true);
    }

    private <T> T doWithLock(String pid, LockOperation<T> operation, boolean writeLock) {
        Lock lock = null;
        DistributedLockService lockService = getLockService();
        if(lockService != null){
            try {
                ReadWriteLock readWriteLock = lockService.getReentrantReadWriteLock(pid);
                lock = writeLock ? readWriteLock.writeLock() : readWriteLock.readLock();
            } catch (Exception e) {
                throw new DistributedLocksException(DistributedLocksException.LOCK_SERVER_ERROR, e);
            }
            if (lock == null) {
                throw new DistributedLocksException(DistributedLocksException.LOCK_NULL, "Null lock acquired");
            }
            boolean tryLock;
            try {
                tryLock = lock.tryLock(configuration.getLockTimeoutInSec(), TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new DistributedLocksException(DistributedLocksException.LOCK_SERVER_ERROR, e);
            }
            if (!tryLock) {
                throw new DistributedLocksException(DistributedLocksException.LOCK_TIMEOUT, "Lock timed out after sec:" + configuration.getLockTimeoutInSec());
            }
        }
        try {
            return operation.execute();
        } catch (Exception e) {
            throw new RepositoryException(e);
        } finally {
            if(lock != null){
                lock.unlock();
            }
        }
    }

    void shutdown() {
        if (lockService != null) {
            lockService.shutdown();
        }
        if (hazelcastClientNode != null) {
            hazelcastClientNode.shutdown();
        }
    }

    RepositoryConfiguration getConfiguration() {
        return configuration;
    }
}
