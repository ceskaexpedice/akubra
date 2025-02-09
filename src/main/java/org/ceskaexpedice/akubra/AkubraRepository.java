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
package org.ceskaexpedice.akubra;

import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexItem;
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexQueryParameters;
import org.ceskaexpedice.fedoramodel.DigitalObject;

import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

/**
 * This is main and only repository access point
 *
 * @author pavels, petrp
 */
public interface AkubraRepository {

    //---------------- Object -------------------------------

    /**
     * @param pid
     * @return
     */
    boolean objectExists(String pid);

    /**
     * @param digitalObject
     */
    void ingest(DigitalObject digitalObject);

    /**
     * @param pid
     * @return
     */
    DigitalObject getObject(String pid);

    /**
     * @param pid
     * @param foxmlType
     * @return
     */
    DigitalObject getObject(String pid, FoxmlType foxmlType);

    /**
     * @param pid
     * @return
     */
    ObjectProperties getObjectProperties(String pid);

    /**
     * @param pid
     */
    void deleteObject(String pid);

    /**
     * Deletes object, possibly without removing relations pointing at this object (from Resource index)
     *
     * @param pid
     * @param deleteDataOfManagedDatastreams  if true, also managed datastreams of this object will be removed from the Repository (files in Akubra)
     * @param deleteRelationsWithThisAsTarget if true, also relations with this object as a target will be removed from Resource index.
     *                                        Which might not be desirable, for example if you want to replace the object with newer version, but keep relations pointing at it.
     * @throws
     */
    void deleteObject(String pid, boolean deleteDataOfManagedDatastreams, boolean deleteRelationsWithThisAsTarget);

    /**
     * @param obj
     * @return
     */
    InputStream marshallObject(DigitalObject obj);

    /**
     * @param inputStream
     * @return
     */
    DigitalObject unmarshallObject(InputStream inputStream);

    //-------------------- Datastream ---------------------------

    /**
     * @param pid
     * @param dsId
     * @param mimeType
     * @param xmlContent
     */
    void createXMLDatastream(String pid, String dsId, String mimeType, InputStream xmlContent);

    /**
     * @param pid
     * @param dsId
     * @param mimeType
     * @param binaryContent
     */
    void createManagedDatastream(String pid, String dsId, String mimeType, InputStream binaryContent);

    /**
     * @param pid
     * @param dsId
     * @param url
     * @param mimeType
     */
    void createRedirectedDatastream(String pid, String dsId, String url, String mimeType);

    /**
     * @param pid
     * @param dsId
     * @return
     */
    boolean datastreamExists(String pid, String dsId);

    /**
     * @param pid
     * @param dsId
     * @return
     */
    DatastreamMetadata getDatastreamMetadata(String pid, String dsId);

    /**
     * @param pid
     * @param dsId
     * @return
     */
    InputStream getDatastreamContent(String pid, String dsId);

    /**
     * @param pid
     * @param dsId
     */
    void deleteDatastream(String pid, String dsId);

    /**
     * @param pid
     * @return
     */
    RelsExtWrapper relsExtGet(String pid);

    /**
     * @param pid
     * @param relation
     * @param namespace
     * @param targetRelation
     */
    void relsExtAddRelation(String pid, String relation, String namespace, String targetRelation);

    /**
     * @param pid
     * @param relation
     * @param namespace
     * @param targetRelation
     */
    void relsExtRemoveRelation(String pid, String relation, String namespace, String targetRelation);

    /**
     * @param pid
     * @param relation
     * @param namespace
     * @param value
     */
    void relsExtAddLiteral(String pid, String relation, String namespace, String value);

    /**
     * @param pid
     * @param relation
     * @param namespace
     * @param value
     */
    void relsExtRemoveLiteral(String pid, String relation, String namespace, String value);

    /**
     * @param pid
     * @return
     */
    List<String> getDatastreamNames(String pid);

    // ---------------- Misc ------------------------------------------------------

    /**
     * @param params
     * @param action
     */
    void iterateProcessingIndex(ProcessingIndexQueryParameters params, Consumer<ProcessingIndexItem> action);

    /**
     *
     */
    void shutdown();

    /**
     * @param pid
     * @param operation
     * @param <T>
     * @return
     */
    <T> T doWithReadLock(String pid, LockOperation<T> operation);

    /**
     * @param pid
     * @param operation
     * @param <T>
     * @return
     */
    <T> T doWithWriteLock(String pid, LockOperation<T> operation);

}
