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
package org.ceskaexpedice.akubra.access;

import java.util.List;
import java.util.function.Consumer;

/**
 * This is main and only repository access point
 *
 * @author pavels
 * 
 */
public interface RepositoryAccess {

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
    ContentWrapper getObject(String pid, FoxmlType foxmlType);

    /**
     * @param pid
     * @return
     */
    ObjectProperties getObjectProperties(String pid);

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
    ContentWrapper getDatastreamContent(String pid, String dsId);

    /**
     * @param pid
     * @return
     */
    RelsExtWrapper processDatastreamRelsExt(String pid);

    /**
     * @param pid
     * @return
     */
    List<String> getDatastreamNames(String pid);

    /**
     * @param params
     * @param mapper
     */
    void queryProcessingIndex(ProcessingIndexQueryParameters params, Consumer<ProcessingIndexItem> mapper);

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


    //------------- podpora zamku zvlast
    //void ingestObject(org.dom4j.Document foxmlDoc, String pid);

    //void deleteObject(String pid, boolean deleteDataOfManagedDatastreams);

    /*
    void updateInlineXmlDatastream(String pid, KnownDatastreams dsId, org.dom4j.Document streamDoc, String formatUri);

    void setDatastreamXml(String pid, KnownDatastreams dsId, org.dom4j.Document ds);

    public void updateBinaryDatastream(String pid, KnownDatastreams dsId, String mimeType, byte[] byteArray);

    public void deleteDatastream(String pid, KnownDatastreams dsId);


    void ingestObject(org.dom4j.Document foxmlDoc, String pid);

    void deleteObject(String pid, boolean deleteDataOfManagedDatastreams);

*/

}
