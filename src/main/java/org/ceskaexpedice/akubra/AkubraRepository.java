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

import org.ceskaexpedice.akubra.misc.MiscHelper;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndex;
import org.ceskaexpedice.akubra.relsext.RelsExtHelper;
import org.ceskaexpedice.fedoramodel.DigitalObject;

import java.io.InputStream;
import java.util.List;

/**
 * Main repository access point for managing digital objects and datastreams.
 * Provides methods for object lifecycle management, metadata retrieval,
 * and processing index manipulation.
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
    boolean exists(String pid);

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
    DigitalObjectWrapper get(String pid);

    /**
     * Retrieves a digital object in an archive format with all streams content inside.
     *
     * @param pid       The persistent identifier of the object.
     * @return The digital object in an archive format, or {@code null} if not found.
     */
    DigitalObjectWrapper export(String pid);

    /**
     * Retrieves metadata properties of a digital object.
     *
     * @param pid The persistent identifier of the object.
     * @return The object properties containing metadata.
     */
    DigitalObjectMetadata getMetadata(String pid);

    /**
     * Deletes a digital object from the repository.
     *
     * @param pid The persistent identifier of the object to delete.
     */
    void delete(String pid);

    /**
     * Deletes an object from the repository with optional removal of related data.
     *
     * @param pid                             The persistent identifier of the object.
     * @param deleteDataOfManagedDatastreams  If {@code true}, deletes associated managed datastreams.
     * @param deleteRelationsWithThisAsTarget If {@code true}, removes relations where this object is the target.
     */
    void delete(String pid, boolean deleteDataOfManagedDatastreams, boolean deleteRelationsWithThisAsTarget);

    /**
     * Marshalls a digital object into an XML representation.
     *
     * @param obj The digital object to serialize.
     * @return An {@link InputStream} containing the serialized XML representation.
     */
    InputStream marshall(DigitalObject obj);

    /**
     * Unmarshalls a digital object from an XML input stream.
     *
     * @param inputStream The input stream containing the XML representation.
     * @return The deserialized digital object.
     */
    DigitalObject unmarshall(InputStream inputStream);

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

    /**
     * Creates an XML datastream for a digital object.
     *
     * @param pid        The persistent identifier of the object.
     * @param dsId       The datastream identifier as enum.
     * @param mimeType   The MIME type of the datastream.
     * @param xmlContent The XML content as an input stream.
     */
    void createXMLDatastream(String pid, KnownDatastreams dsId, String mimeType, InputStream xmlContent);

    /**
     * Updates an XML datastream for a digital object.
     *
     * @param pid        The persistent identifier of the object.
     * @param dsId       The datastream identifier.
     * @param mimeType   The MIME type of the datastream.
     * @param xmlContent The XML content as an input stream.
     */
    void updateXMLDatastream(String pid, String dsId, String mimeType, InputStream xmlContent);

    /**
     * Updates an XML datastream for a digital object.
     *
     * @param pid        The persistent identifier of the object.
     * @param dsId       The datastream identifier as enum.
     * @param mimeType   The MIME type of the datastream.
     * @param xmlContent The XML content as an input stream.
     */
    void updateXMLDatastream(String pid, KnownDatastreams dsId, String mimeType, InputStream xmlContent);

    /**
     * Creates a managed datastream for a digital object.
     *
     * @param pid           The persistent identifier of the object.
     * @param dsId          The datastream identifier.
     * @param mimeType      The MIME type of the datastream.
     * @param binaryContent The binary content as an input stream.
     */
    void createManagedDatastream(String pid, String dsId, String mimeType, InputStream binaryContent);

    /**
     * Creates a managed datastream for a digital object.
     *
     * @param pid           The persistent identifier of the object.
     * @param dsId          The datastream identifier as enum.
     * @param mimeType      The MIME type of the datastream.
     * @param binaryContent The binary content as an input stream.
     */
    void createManagedDatastream(String pid, KnownDatastreams dsId, String mimeType, InputStream binaryContent);

    /**
     * Updates a managed datastream for a digital object.
     *
     * @param pid           The persistent identifier of the object.
     * @param dsId          The datastream identifier.
     * @param mimeType      The MIME type of the datastream.
     * @param binaryContent The binary content as an input stream.
     */
    void updateManagedDatastream(String pid, String dsId, String mimeType, InputStream binaryContent);

    /**
     * Updates a managed datastream for a digital object.
     *
     * @param pid           The persistent identifier of the object.
     * @param dsId          The datastream identifier as enum.
     * @param mimeType      The MIME type of the datastream.
     * @param binaryContent The binary content as an input stream.
     */
    void updateManagedDatastream(String pid, KnownDatastreams dsId, String mimeType, InputStream binaryContent);

    /**
     * Creates an external datastream linking to an external resource. (Type of datastream is 'E')
     *
     * @param pid      The persistent identifier of the object.
     * @param dsId     The datastream identifier.
     * @param url      The external URL.
     * @param mimeType The MIME type of the datastream.
     */
    void createExternalDatastream(String pid, String dsId, String url, String mimeType);

    /**
     * Creates an external datastream linking to an external resource. (Type of datastream is 'E')
     *
     * @param pid      The persistent identifier of the object.
     * @param dsId     The datastream identifier as enum.
     * @param url      The external URL.
     * @param mimeType The MIME type of the datastream.
     */
    void createExternalDatastream(String pid, KnownDatastreams dsId, String url, String mimeType);

    /**
     * Updates a redirected datastream linking to an external resource.
     *
     * @param pid      The persistent identifier of the object.
     * @param dsId     The datastream identifier.
     * @param url      The external URL.
     * @param mimeType The MIME type of the datastream.
     */
    void updateExternalDatastream(String pid, String dsId, String url, String mimeType);

    /**
     * Updates a redirected datastream linking to an external resource.
     *
     * @param pid      The persistent identifier of the object.
     * @param dsId     The datastream identifier as enum.
     * @param url      The external URL.
     * @param mimeType The MIME type of the datastream.
     */
    void updateExternalDatastream(String pid, KnownDatastreams dsId, String url, String mimeType);

    /**
     * Checks if a datastream exists for a given object.
     *
     * @param pid  The persistent identifier of the object.
     * @param dsId The datastream identifier.
     * @return {@code true} if the datastream exists, {@code false} otherwise.
     */
    boolean datastreamExists(String pid, String dsId);

    /**
     * Checks if a datastream exists for a given object.
     *
     * @param pid  The persistent identifier of the object.
     * @param dsId The datastream identifier as enum.
     * @return {@code true} if the datastream exists, {@code false} otherwise.
     */
    boolean datastreamExists(String pid, KnownDatastreams dsId);

    /**
     * Retrieves metadata of a specific datastream.
     *
     * @param pid  The persistent identifier of the object.
     * @param dsId The datastream identifier.
     * @return Metadata of the datastream.
     */
    DatastreamMetadata getDatastreamMetadata(String pid, String dsId);

    /**
     * Retrieves metadata of a specific datastream.
     *
     * @param pid  The persistent identifier of the object.
     * @param dsId The datastream identifier as enum.
     * @return Metadata of the datastream.
     */
    DatastreamMetadata getDatastreamMetadata(String pid, KnownDatastreams dsId);

    /**
     * Retrieves the content of a datastream.
     *
     * @param pid  The persistent identifier of the object.
     * @param dsId The datastream identifier.
     * @return A wrapper object containing various formats of datastream content.
     */
    DatastreamContentWrapper getDatastreamContent(String pid, String dsId);

    /**
     * Retrieves the content of a datastream.
     *
     * @param pid  The persistent identifier of the object.
     * @param dsId The datastream identifier as enum.
     * @return A wrapper object containing various formats of datastream content.
     */
    DatastreamContentWrapper getDatastreamContent(String pid, KnownDatastreams dsId);

    /**
     * Deletes a datastream from an object.
     *
     * @param pid  The persistent identifier of the object.
     * @param dsId The datastream identifier.
     */
    void deleteDatastream(String pid, String dsId);

    /**
     * Deletes a datastream from an object.
     *
     * @param pid  The persistent identifier of the object as enum.
     * @param dsId The datastream identifier.
     */
    void deleteDatastream(String pid, KnownDatastreams dsId);

    /**
     * Retrieves a list of datastream names associated with the specified Fedora object.
     *
     * @param pid the persistent identifier (PID) of the Fedora object
     * @return a list of datastream names associated with the specified PID
     */
    List<String> getDatastreamNames(String pid);

    //-------------------- processing index -----------------------

    /**
     * Returns a processing index interface for the repository.
     *
     * @return A ProcessingIndex instance for processing index feeding and reading.
     */
    ProcessingIndex pi();

    //-------------------- RelsExt -----------------------

    /**
     * Returns a helper interface for the RELS EXT stream type manipulation.
     *
     * @return A RELS EXT helper.
     */
    RelsExtHelper re();

    //-------------------- Misc -----------------------

    /**
     * Returns a helper interface for the misc manipulation.
     *
     * @return A misc helper.
     */
    MiscHelper mi();

    //-------------------- Locks ---------------------------

    /**
     * Executes an operation with a lock on the specified object.
     *
     * @param pid       The persistent identifier of the object.
     * @param operation The operation to execute.
     * @param <T>       The return type of the operation.
     * @return The result of the operation.
     */
    <T> T doWithLock(String pid, LockOperation<T> operation);

    /**
     * Shuts down the repository, releasing resources.
     */
    void shutdown();

}
