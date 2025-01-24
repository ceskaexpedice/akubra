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
 * This is main point to access to fedora through REST-API
 *
 * @author pavels
 * 
 */
public interface RepositoryAccess {

    // object
    boolean objectExists(String pid);

    ResultWrapper getObject(String pid, FoxmlType foxmlType);

    RepositoryObjectProperties getObjectProperties(String pid);

    //void ingestObject(org.dom4j.Document foxmlDoc, String pid);

    //void deleteObject(String pid, boolean deleteDataOfManagedDatastreams);

    // datastream
    boolean datastreamExists(String pid, String dsId);

    //- getMimeType , getCreatedData, (typ x,M,....control-group)
    DatastreamMetadata getDatastreamMetadata(String pid, String dsId);

    ResultWrapper getDatastreamContent(String pid, String dsId);

    ResultWrapper getDatastreamContent(String pid, String dsId, String version);

    RelsExtWrapper processDatastreamRelsExt(String pid);

    List<String> getDatastreamNames(String pid);


    // Processing index
    void queryProcessingIndex(ProcessingIndexQueryParameters params, Consumer<ProcessingIndexItem> mapper);

    //------------- podpora zamku zvlast

    /*
    void updateInlineXmlDatastream(String pid, KnownDatastreams dsId, org.dom4j.Document streamDoc, String formatUri);

    void setDatastreamXml(String pid, KnownDatastreams dsId, org.dom4j.Document ds);

    public void updateBinaryDatastream(String pid, KnownDatastreams dsId, String mimeType, byte[] byteArray);

    public void deleteDatastream(String pid, KnownDatastreams dsId);


    void ingestObject(org.dom4j.Document foxmlDoc, String pid);

    void deleteObject(String pid, boolean deleteDataOfManagedDatastreams);

*/

    default void shutdown(){};

}
