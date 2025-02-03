/*
 * Copyright (C) 2012 Pavel Stastny
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
import org.ceskaexpedice.jaxbmodel.DigitalObject;
import org.dom4j.Document;

import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

/**
 * This is main and only repository access point
 *
 * @author pavels, petrp
 * 
 */
public interface RepositoryAccess {

    //---------------- Object -------------------------------

    /**
     * @param digitalObject
     */
    void ingest(DigitalObject digitalObject);

    /**
     * @param pid
     * @return
     */
    boolean objectExists(String pid);

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
     * @param pid
     * @param deleteDataOfManagedDatastreams if true, also managed datastreams of this object will be removed from the Repository (files in Akubra)
     * @param deleteRelationsWithThisAsTarget if true, also relations with this object as a target will be removed from Resource index.
     *                                         Which might not be desirable, for example if you want to replace the object with newer version, but keep relations pointing at it.
     *
     * @throws
     */
    void deleteObject(String pid, boolean deleteDataOfManagedDatastreams, boolean deleteRelationsWithThisAsTarget);

    /**
     * @param obj
     * @return
     */
    InputStream marshallObject(DigitalObject obj);

    //-------------------- Datastream ---------------------------
    // TODO add datastream 3x - inline xml, binary, redirect

    DigitalObject unmarshallStream(InputStream inputStream);

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

    // TODO delete datastream

    /**
     * @param pid
     * @return
     */
    RelsExtWrapper processDatastreamRelsExt(String pid);

    // TODO add relation
    // TODO delete relation

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



    /*
    void updateInlineXmlDatastream(String pid, KnownDatastreams dsId, org.dom4j.Document streamDoc, String formatUri);

    void setDatastreamXml(String pid, KnownDatastreams dsId, org.dom4j.Document ds);

    public void updateBinaryDatastream(String pid, KnownDatastreams dsId, String mimeType, byte[] byteArray);

    public void deleteDatastream(String pid, KnownDatastreams dsId);


*/

}
