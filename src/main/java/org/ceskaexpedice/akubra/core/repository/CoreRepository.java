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
package org.ceskaexpedice.akubra.core.repository;

import org.ceskaexpedice.akubra.processingindex.ProcessingIndex;
import org.ceskaexpedice.fedoramodel.DigitalObject;

import java.io.InputStream;
import java.util.concurrent.locks.Lock;

/**
 * Core access to Akubra repository.
 * This interface provides the primary functions for ingesting, accessing, and managing digital objects
 * in the repository. It serves as the core for repository access.
 * It also handles repository object locks and data marshaling operations.
 */
public interface CoreRepository {

    /**
     * Checks if a digital object exists in the repository and whether it is a raw object.
     *
     * @param pid The unique identifier of the object.
     * @return true if the object exists and is a raw object, false otherwise.
     */
    boolean objectExists(String pid);

    /**
     * Ingests a new digital object into the repository from the provided object representation.
     *
     * @param digitalObject The digital object to be ingested.
     * @return The ingested RepositoryObject.
     */
    RepositoryObject ingestObject(DigitalObject digitalObject);

    /**
     * Creates a new object or retrieves an existing one from the repository.
     *
     * @param pid The unique identifier of the object.
     * @return The RepositoryObject, either newly created or fetched from the repository.
     */
    RepositoryObject createOrGetObject(String pid);

    /**
     * Retrieves an object from the repository.
     *
     * @param pid The unique identifier of the object.
     * @return The RepositoryObject corresponding to the given pid.
     */
    RepositoryObject getObject(String pid);

    /**
     * Retrieves an object from the repository with the option to use cache.
     *
     * @param pid The unique identifier of the object.
     * @param useCache If true, the cache will be used to fetch the object; otherwise, the repository is queried directly.
     * @return The RepositoryObject corresponding to the given pid.
     */
    RepositoryObject getObject(String pid, boolean useCache);

    /**
     * Resolves archived datastreams for the provided digital object.
     *
     * @param obj The digital object whose datastreams need to be resolved.
     */
    void resolveArchivedDatastreams(DigitalObject obj);

    /**
     * Marshals a digital object into an InputStream representation.
     *
     * @param obj The digital object to be marshaled.
     * @return An InputStream representation of the digital object.
     */
    InputStream marshallObject(DigitalObject obj);

    /**
     * Unmarshals an InputStream into a digital object.
     *
     * @param inputStream The InputStream to be unmarshaled.
     * @return The unmarshaled digital object.
     */
    DigitalObject unmarshallObject(InputStream inputStream);

    /**
     * Deletes a digital object from the repository.
     *
     * @param pid The unique identifier of the object to be deleted.
     */
    void deleteObject(String pid);

    /**
     * Deletes a digital object from the repository, with options for handling related data and relations.
     *
     * @param pid The unique identifier of the object to be deleted.
     * @param deleteDataOfManagedDatastreams If true, the managed datastreams of the object will also be removed.
     * @param deleteRelationsWithThisAsTarget If true, relations where this object is the target will be removed from the resource index.
     */
    void deleteObject(String pid, boolean deleteDataOfManagedDatastreams, boolean deleteRelationsWithThisAsTarget);

    /**
     * Retrieves a read lock for a digital object.
     *
     * @param pid The unique identifier of the object.
     * @return A Lock instance that ensures read access to the object.
     */
    Lock getReadLock(String pid);

    /**
     * Retrieves a write lock for a digital object.
     *
     * @param pid The unique identifier of the object.
     * @return A Lock instance that ensures write access to the object.
     */
    Lock getWriteLock(String pid);

    /**
     * Returns a processing index  for the repository.
     *
     * @return A ProcessingIndex instance for processing index feeding and reading.
     */
    ProcessingIndex getProcessingIndex();

    /**
     * Shuts down the repository access system.
     * This method is typically called when the repository access system is no longer needed.
     */
    void shutdown();
}

