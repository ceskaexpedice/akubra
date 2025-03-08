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
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexSolr;
import org.ceskaexpedice.akubra.core.repository.CoreRepository;
import org.ceskaexpedice.akubra.core.repository.RepositoryObject;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndex;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.fcrepo.server.storage.lowlevel.akubra.HashPathIdMapper;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.ceskaexpedice.akubra.core.repository.impl.RepositoryUtils.*;

/**
 * CoreRepositoryImpl
 * @author pavels
 */
public class CoreRepositoryImpl implements CoreRepository {

    private static final Logger LOGGER = Logger.getLogger(CoreRepositoryImpl.class.getName());
    private final IdMapper idMapper;

    private AkubraDOManager manager;
    private ProcessingIndexSolr feeder;

    public CoreRepositoryImpl(ProcessingIndexSolr feeder, AkubraDOManager manager) {
        super();
        this.feeder = feeder;
        this.manager = manager;
        idMapper = new HashPathIdMapper(manager.getConfiguration().getDatastreamStorePattern());
    }

    @Override
    public boolean exists(String ident) {
        try {
            URI blobId = getBlobId(ident);
            URI internalId = idMapper.getInternalId(blobId);
            URI canonicalId = validateId(internalId);
            File file = new File(manager.getConfiguration().getObjectStorePath(), canonicalId.getRawSchemeSpecificPart());
            return file.exists();
        } catch (UnsupportedIdException e) {
            return false;
        } catch (Exception e) {
            LOGGER.warning("Exception while checking if object exists: " + e.getMessage());
            return false;
        }
    }

    @Override
    public RepositoryObject getAsRepositoryObject(String pid) {
        DigitalObject digitalObject = this.manager.readObjectFromStorage(pid);
        if (digitalObject == null) {
            return null;
        }
        RepositoryObjectImpl obj = new RepositoryObjectImpl(digitalObject, this.manager, this.feeder);
        return obj;
    }

    @Override
    public byte[] getAsBytes(String pid) {
        return this.manager.retrieveObjectBytes(pid);
    }

    @Override
    public InputStream retrieveDatastream(String dsKey) {
        return this.manager.retrieveDatastream(dsKey);
    }

    @Override
    public RepositoryObject ingest(DigitalObject digitalObject) {
        if (exists(digitalObject.getPID())) {
            throw new RepositoryException("Ingested object exists:" + digitalObject.getPID());
        } else {
            RepositoryObjectImpl obj = new RepositoryObjectImpl(digitalObject, this.manager, this.feeder);
            manager.write(obj.getDigitalObject(), null);
            obj.rebuildProcessingIndex();
            return obj;
        }
    }

    @Override
    public void delete(String pid, boolean deleteDataOfManagedDatastreams, boolean deleteRelationsWithThisAsTarget) {
        try {
            this.manager.deleteObject(pid, deleteDataOfManagedDatastreams);
            try {
                // delete relations with this object as a source
                this.feeder.deleteByRelationsForPid(pid);
                // possibly delete relations with this object as a target
                if (deleteRelationsWithThisAsTarget) {
                    this.feeder.deleteByTargetPid(pid);
                }
                // delete this object's description
                this.feeder.deleteDescriptionByPid(pid);
            } catch (Throwable th) {
                LOGGER.log(Level.SEVERE, "Cannot update processing index for " + pid + " - reindex manually.", th);
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        } finally {
            try {
                this.feeder.commit();
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
    public ProcessingIndex getProcessingIndex() {
        return this.feeder;
    }

    @Override
    public void resolveArchivedDatastreams(DigitalObject obj) {
        manager.resolveArchivedDatastreams(obj);
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
    public Lock getReadLock(String pid) {
        Lock readLock = manager.getReadLock(pid);
        return readLock;
    }

    @Override
    public Lock getWriteLock(String pid) {
        Lock writeLock = manager.getWriteLock(pid);
        return writeLock;
    }

    @Override
    public void shutdown() {
        manager.shutdown();
    }

}
