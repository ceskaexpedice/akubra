package org.ceskaexpedice.akubra.core.repository.impl;

import ca.thoughtwire.lock.DistributedLockService;
import com.hazelcast.core.*;
import org.ceskaexpedice.akubra.core.RepositoryConfiguration;
import org.akubraproject.BlobStore;
import org.akubraproject.fs.FSBlobStore;
import org.akubraproject.map.IdMapper;
import org.akubraproject.map.IdMappingBlobStore;
import org.apache.commons.io.IOUtils;
import org.ceskaexpedice.akubra.core.repository.RepositoryException;
import org.ceskaexpedice.hazelcast.ClientNode;
import org.ceskaexpedice.jaxbmodel.*;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
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
import java.io.*;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AkubraDOManager {
    private static final Logger LOGGER = Logger.getLogger(AkubraDOManager.class.getName());
    private RepositoryConfiguration configuration;
    private ILowlevelStorage storage;

    private static DistributedLockService lockService;
    private static ITopic<String> cacheInvalidator;

    private static Cache<String, DigitalObject> objectCache;
    private static final String DIGITALOBJECT_CACHE_ALIAS = "DigitalObjectCache";

    private static Unmarshaller unmarshaller;
    private static Marshaller marshaller;

    private void initializeStatics(RepositoryConfiguration configuration) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DigitalObject.class);
            unmarshaller = jaxbContext.createUnmarshaller();
            //JAXBContext jaxbdatastreamContext = JAXBContext.newInstance(DatastreamType.class);
            marshaller = jaxbContext.createMarshaller();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Cannot init JAXB", e);
            throw new RepositoryException(e);
        }
        ClientNode.ensureHazelcastNode(configuration.getHazelcastConfiguration());
        lockService = DistributedLockService.newHazelcastLockService(ClientNode.getHzInstance());
        cacheInvalidator = ClientNode.getHzInstance().getTopic("cacheInvalidator");
        cacheInvalidator.addMessageListener(new MessageListener<String>() {
            @Override
            public void onMessage(Message<String> message) {
                if (objectCache != null && message != null) {
                    objectCache.remove(message.getMessageObject());
                }
            }
        });
    }

    public AkubraDOManager(CacheManager cacheManager, RepositoryConfiguration configuration) {
        try {
            this.initializeStatics(configuration);
            this.configuration = configuration;
            this.storage = initLowLevelStorage();
            if (cacheManager != null) {
                objectCache = cacheManager.getCache(DIGITALOBJECT_CACHE_ALIAS, String.class, DigitalObject.class);
                if (objectCache == null) {
                    objectCache = cacheManager.createCache(DIGITALOBJECT_CACHE_ALIAS,
                            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, DigitalObject.class,
                                            ResourcePoolsBuilder.heap(3000))
                                    .withExpiry(Expirations.timeToLiveExpiration(
                                            Duration.of(configuration.getCacheTimeToLiveExpiration(), TimeUnit.SECONDS))).build());
                }
            }
        } catch (Exception ex) {
            throw new RepositoryException(ex);
        }
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

    /**
     * Loads and unmarshalls DigitalObject from Akubra storage, using cache if possible
     *
     * @param pid
     * @return
     * @throws IOException
     */
    DigitalObject readObjectFromStorage(String pid) {
        return readObjectFromStorageOrCache(pid, true);
    }

    /**
     * Loads and unmarshalls fresh copy of DigitalObject from Akubra storage, bypassing the cache
     * Intended for use in FedoraAccess.getFoxml, which resolves internal managed datastreams to base64 binary content
     *
     * @param pid
     * @return
     * @throws IOException
     */
    DigitalObject readObjectCloneFromStorage(String pid) {
        return readObjectFromStorageOrCache(pid, false);
    }

    DigitalObject readObjectFromStorageOrCache(String pid, boolean useCache) {
        DigitalObject retval = useCache ? objectCache.get(pid) : null;
        if (retval == null) {
            Object obj;
            Lock lock = getReadLock(pid);
            try (InputStream inputStream = this.storage.retrieveObject(pid);) {
                synchronized (unmarshaller) {
                    obj = unmarshaller.unmarshal(inputStream);
                }
            } catch (ObjectNotInLowlevelStorageException ex) {
                return null;
            } catch (Exception e) {
                throw new RepositoryException(e);
            } finally {
                lock.unlock();
            }
            retval = (DigitalObject) obj;
            if (useCache) {
                objectCache.put(pid, retval);
            }
        }
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
        Lock lock = getReadLock(objectKey);
        try {
            return storage.retrieveObject(objectKey);
        } catch (LowlevelStorageException e) {
            throw new RepositoryException(e);
        } finally {
            lock.unlock();
        }
    }

    void deleteObject(String pid, boolean includingManagedDatastreams) {
        Lock lock = getWriteLock(pid);
        try {
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
        } finally {
            invalidateCache(pid);
            lock.unlock();
        }
    }

    void deleteStream(String pid, String streamId) {
        Lock lock = getWriteLock(pid);
        try {
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
        } finally {
            invalidateCache(pid);
            lock.unlock();
        }
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

    void commit(DigitalObject object, String streamId) {
        Lock lock = getWriteLock(object.getPID());
        try {
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
        } finally {
            invalidateCache(object.getPID());
            lock.unlock();
        }
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
            throw new RuntimeException(e);
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

    static Lock getWriteLock(String pid) {
        if (pid == null) {
            throw new IllegalArgumentException("pid cannot be null");
        }
        ReadWriteLock lock = lockService.getReentrantReadWriteLock(pid);
        lock.writeLock().lock();
        return lock.writeLock();
    }

    static Lock getReadLock(String pid) {
        if (pid == null) {
            throw new IllegalArgumentException("pid cannot be null");
        }
        ReadWriteLock lock = lockService.getReentrantReadWriteLock(pid);
        lock.readLock().lock();
        return lock.readLock();
    }

    private static void invalidateCache(String pid) {
        cacheInvalidator.publish(pid);
    }

    static void shutdown() {
        if(lockService != null) {
            lockService.shutdown();
        }
        ClientNode.shutdown();
    }
}
