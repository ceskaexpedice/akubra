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

import com.qbizm.kramerius.imp.jaxb.DigitalObject;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexFeeder;

/**
 * The simple object model represents access to fedora 4 repository
 * It is basic tool for ingesting also it is basic point for FedoraAccess facade
 * @author pavels
 */
public interface Repository {
    /**
     * Returns true if object objectExists and if it is raw kramerius object
     * @param pid
     * @return
     * @throws RepositoryException
     */
    boolean  objectExists(String pid);

    /**
     * Returns object
     * @param pid
     * @return
     * @throws RepositoryException
     */
    RepositoryObject getObject(String pid);

    /**
     * Creates an empty object or finds existing object
     * @param pid Identification of the object
     * @return
     * @throws RepositoryException
     */
    RepositoryObject createOrFindObject(String pid);

    /**
     * Ingest new digital object from the provided object representation
     * @param contents
     * @return
     * @throws RepositoryException
     */
    RepositoryObject ingestObject(DigitalObject contents);

    /**
     * Deletes object
     * @param pid
     * @throws RepositoryException
     */
    void deleteObject(String pid);

    /**
     * Deletes object, possibly without removing relations pointing at this object (from Resource index)
     * @param pid
     * @param deleteDataOfManagedDatastreams if true, also managed datastreams of this object will be removed from the Repository (files in Akubra)
     * @param deleteRelationsWithThisAsTarget if true, also relations with this object as a target will be removed from Resource index.
     *                                         Which might not be desirable, for example if you want to replace the object with newer version, but keep relations pointing at it.
     *
     * @throws RepositoryException
     */
    void deleteObject(String pid, boolean deleteDataOfManagedDatastreams, boolean deleteRelationsWithThisAsTarget);

    /**
     * Commits current transaction
     * @throws RepositoryException
     */
    void commitTransaction();

    /**
     * Rolls back current transaction
     * @throws RepositoryException
     */
    void rollbackTransaction();

    /**
     * Returns processing index feeder
     * @return
     * @throws RepositoryException
     */
    ProcessingIndexFeeder getProcessingIndexFeeder();

}
