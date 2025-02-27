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

import org.ceskaexpedice.akubra.processingindex.ProcessingIndex;
import org.ceskaexpedice.akubra.relsext.RelsExtHandler;
import org.ceskaexpedice.fedoramodel.DigitalObject;

import java.io.InputStream;
import java.util.List;

/**
 * Main repository access point for managing digital objects and datastreams.
 * Provides methods for object lifecycle management, metadata retrieval,
 * and processing index iteration.
 *
 * <p>Implementations of this interface interact with the Akubra storage backend.</p>
 *
 * @author pavels, petrp
 */
public interface AkubraRepository {

    //---------------- Object Management -------------------------------

    /**
     * Checks if a digital object exists in the repository.
     *
     * @param pid The persistent identifier of the object.
     * @return {@code true} if the object exists, {@code false} otherwise.
     */
    boolean objectExists(String pid);

    /**
     * Ingests a new digital object into the repository.
     *
     * @param digitalObject The digital object to be stored.
     */
    void ingest(DigitalObject digitalObject);

    /**
     * Retrieves a digital object by its persistent identifier.
     *
     * @param pid The persistent identifier of the object.
     * @return The digital object, or {@code null} if not found.
     */
    DigitalObjectWrapper getObject(String pid);

    /**
     * Retrieves a digital object in a specific FOXML format.
     *
     * @param pid       The persistent identifier of the object.
     * @param foxmlType The FOXML type format.
     * @return The digital object in the specified format, or {@code null} if not found.
     */
    DigitalObjectWrapper getObject(String pid, FoxmlType foxmlType);

    /**
     * Retrieves metadata properties of a digital object.
     *
     * @param pid The persistent identifier of the object.
     * @return The object properties containing metadata.
     */
    ObjectProperties getObjectProperties(String pid);

    /**
     * Deletes a digital object from the repository.
     *
     * @param pid The persistent identifier of the object to delete.
     */
    void deleteObject(String pid);

    /**
     * Deletes an object from the repository with optional removal of related data.
     *
     * @param pid                             The persistent identifier of the object.
     * @param deleteDataOfManagedDatastreams  If {@code true}, deletes associated managed datastreams.
     * @param deleteRelationsWithThisAsTarget If {@code true}, removes relations where this object is the target.
     */
    void deleteObject(String pid, boolean deleteDataOfManagedDatastreams, boolean deleteRelationsWithThisAsTarget);

    /**
     * Marshalls a digital object into an XML representation.
     *
     * @param obj The digital object to serialize.
     * @return An {@link InputStream} containing the serialized XML representation.
     */
    InputStream marshallObject(DigitalObject obj);

    /**
     * Unmarshalls a digital object from an XML input stream.
     *
     * @param inputStream The input stream containing the XML representation.
     * @return The deserialized digital object.
     */
    DigitalObject unmarshallObject(InputStream inputStream);

    //-------------------- Datastream Management ---------------------------

    /**
     * Creates an XML datastream for a digital object.
     *
     * @param pid        The persistent identifier of the object.
     * @param dsId       The datastream identifier.
     * @param mimeType   The MIME type of the datastream.
     * @param xmlContent The XML content as an input stream.
     */
    void createXMLDatastream(String pid, String dsId, String mimeType, InputStream xmlContent);

    void createXMLDatastream(String pid, KnownDatastreams dsId, String mimeType, InputStream xmlContent);

    void updateXMLDatastream(String pid, String dsId, String mimeType, InputStream binaryContent);

    void updateXMLDatastream(String pid, KnownDatastreams dsId, String mimeType, InputStream binaryContent);

    /**
     * Creates a managed datastream for a digital object.
     *
     * @param pid           The persistent identifier of the object.
     * @param dsId          The datastream identifier.
     * @param mimeType      The MIME type of the datastream.
     * @param binaryContent The binary content as an input stream.
     */
    void createManagedDatastream(String pid, String dsId, String mimeType, InputStream binaryContent);

    void createManagedDatastream(String pid, KnownDatastreams dsId, String mimeType, InputStream binaryContent);

    void updateManagedDatastream(String pid, String dsId, String mimeType, InputStream binaryContent);

    void updateManagedDatastream(String pid, KnownDatastreams dsId, String mimeType, InputStream binaryContent);

    /**
     * Creates a redirected datastream linking to an external resource.
     *
     * @param pid      The persistent identifier of the object.
     * @param dsId     The datastream identifier.
     * @param url      The external URL.
     * @param mimeType The MIME type of the datastream.
     */
    void createRedirectedDatastream(String pid, String dsId, String url, String mimeType);

    void createRedirectedDatastream(String pid, KnownDatastreams dsId, String url, String mimeType);

    void updateRedirectedDatastream(String pid, String dsId, String url, String mimeType);

    void updateRedirectedDatastream(String pid, KnownDatastreams dsId, String url, String mimeType);

    /**
     * Checks if a datastream exists for a given object.
     *
     * @param pid  The persistent identifier of the object.
     * @param dsId The datastream identifier.
     * @return {@code true} if the datastream exists, {@code false} otherwise.
     */
    boolean datastreamExists(String pid, String dsId);

    boolean datastreamExists(String pid, KnownDatastreams dsId);

    /**
     * Retrieves metadata of a specific datastream.
     *
     * @param pid  The persistent identifier of the object.
     * @param dsId The datastream identifier.
     * @return Metadata of the datastream.
     */
    DatastreamMetadata getDatastreamMetadata(String pid, String dsId);

    DatastreamMetadata getDatastreamMetadata(String pid, KnownDatastreams dsId);

    /**
     * Retrieves the content of a datastream.
     *
     * @param pid  The persistent identifier of the object.
     * @param dsId The datastream identifier.
     * @return An input stream containing the datastream content.
     */
    DatastreamContentWrapper getDatastreamContent(String pid, String dsId);

    DatastreamContentWrapper getDatastreamContent(String pid, KnownDatastreams dsId);

    /**
     * Deletes a datastream from an object.
     *
     * @param pid  The persistent identifier of the object.
     * @param dsId The datastream identifier.
     */
    void deleteDatastream(String pid, String dsId);

    void deleteDatastream(String pid, KnownDatastreams dsId);

    public List<String> getDatastreamNames(String pid);

    //-------------------- processing index -----------------------

    /**
     * Returns a processing index  for the repository.
     *
     * @return A ProcessingIndex instance for processing index feeding and reading.
     */
    ProcessingIndex getProcessingIndex();

    //-------------------- RelsExt -----------------------

    RelsExtHandler getRelsExtHandler();

    //-------------------- Locks ---------------------------

    /**
     * Executes an operation with a read lock on the specified object.
     *
     * @param pid       The persistent identifier of the object.
     * @param operation The operation to execute.
     * @param <T>       The return type of the operation.
     * @return The result of the operation.
     */
    <T> T doWithReadLock(String pid, LockOperation<T> operation);

    /**
     * Executes an operation with a write lock on the specified object.
     *
     * @param pid       The persistent identifier of the object.
     * @param operation The operation to execute.
     * @param <T>       The return type of the operation.
     * @return The result of the operation.
     */
    <T> T doWithWriteLock(String pid, LockOperation<T> operation);

    /**
     * Shuts down the repository, releasing resources.
     */
    void shutdown();

}
