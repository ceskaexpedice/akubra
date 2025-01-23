/*
 * Copyright (C) 2016 Pavel Stastny
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

import org.ceskaexpedice.jaxbmodel.DigitalObject;
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexFeeder;

import java.io.InputStream;
import java.util.concurrent.locks.Lock;

/**
 * Represents access to Akubra repository
 * It is basic tool for ingesting and it is basic point for RepositoryAccess facade
 * @author pavels
 */
public interface Repository {

    /**
     * Returns true if object exists and if it is raw object
     * @param pid
     * @return
     * @throws
     */
    boolean  objectExists(String pid);

    /**
     * Returns object
     * @param pid
     * @return
     * @throws
     */
    RepositoryObject getObject(String pid);

    RepositoryObject getObject(String pid, boolean useCache);

    /**
     * Creates an empty object or finds an existing object
     * @param pid Identification of the object
     * @return
     * @throws
     */
    RepositoryObject createOrGetObject(String pid);

    /**
     * Ingest new digital object from the provided object representation
     * @param digitalObject
     * @return
     * @throws
     */
    RepositoryObject ingestObject(DigitalObject digitalObject);

    /**
     * Deletes object
     * @param pid
     * @throws
     */
    void deleteObject(String pid);

    /**
     * Deletes object, possibly without removing relations pointing at this object (from Resource index)
     * @param pid
     * @param deleteDataOfManagedDatastreams if true, also managed datastreams of this object will be removed from the Repository (files in Akubra)
     * @param deleteRelationsWithThisAsTarget if true, also relations with this object as a target will be removed from Resource index.
     *                                         Which might not be desirable, for example if you want to replace the object with newer version, but keep relations pointing at it.
     *
     * @throws
     */
    void deleteObject(String pid, boolean deleteDataOfManagedDatastreams, boolean deleteRelationsWithThisAsTarget);

    /**
     * Commits current transaction
     * @throws
     */
    void commitTransaction();

    /**
     * Returns processing index feeder
     * @return
     * @throws
     */
    ProcessingIndexFeeder getProcessingIndexFeeder();

    void resolveArchivedDatastreams(DigitalObject obj);

    InputStream marshallObject(DigitalObject obj);

    Lock getReadLock(String pid);
}
