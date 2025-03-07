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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ceskaexpedice.akubra.core.repository;

import org.apache.commons.lang3.tuple.Triple;
import org.ceskaexpedice.fedoramodel.DigitalObject;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * This interface represents a basic repository item in the repository system.
 * It allows for accessing and managing the metadata, streams, relations, literals,
 * and processing index of a digital object within the repository.
 *
 * @author pavels
 */
public interface RepositoryObject {

    /**
     * Returns the digital object that this repository object represents.
     *
     * @return The underlying DigitalObject associated with this repository object.
     */
    DigitalObject getDigitalObject();

    /**
     * Retrieves the unique path (PID) within the repository for the object.
     *
     * @return The PID (Persistent Identifier) of the repository object.
     */
    String getPid();

    /**
     * Returns the last modified date of the object.
     *
     * @return A Date representing the last modification time of the object.
     */
    Date getPropertyLastModified();

    /**
     * Returns the FOXML (Fedora Object XML) representation of the repository object.
     *
     * @return An InputStream containing the FOXML of the repository object.
     */
    InputStream getFoxml();

    /**
     * Retrieves a list of all streams associated with the repository object.
     *
     * @return A List of RepositoryDatastream objects associated with this repository object.
     */
    List<RepositoryDatastream> getStreams();

    /**
     * Retrieves a specific stream by its stream ID.
     *
     * @param streamId The ID of the stream to retrieve.
     * @return The RepositoryDatastream corresponding to the provided stream ID.
     */
    RepositoryDatastream getStream(String streamId);

    /**
     * Checks whether a stream with the specified ID exists for the repository object.
     *
     * @param streamId The ID of the stream to check.
     * @return True if the stream exists, false otherwise.
     */
    boolean streamExists(String streamId);

    /**
     * Creates a new XML stream for the repository object.
     *
     * @param streamId The ID of the new stream.
     * @param mimeType The MIME type of the stream.
     * @param input    The InputStream containing the content to be stored in the stream.
     * @return The created RepositoryDatastream object.
     */
    RepositoryDatastream createXMLStream(String streamId, String mimeType, InputStream input);

    /**
     * Creates a new managed stream for the repository object.
     *
     * @param streamId The ID of the new stream.
     * @param mimeType The MIME type of the stream.
     * @param input    The InputStream containing the content to be stored in the stream.
     * @return The created RepositoryDatastream object.
     */
    RepositoryDatastream createManagedStream(String streamId, String mimeType, InputStream input);

    /**
     * Creates a new redirected stream for the repository object.
     *
     * @param streamId The ID of the new stream.
     * @param url      The URL to which the stream is redirected.
     * @param mimeType The MIME type of the stream.
     * @return The created RepositoryDatastream object.
     */
    RepositoryDatastream createRedirectedStream(String streamId, String url, String mimeType);

    /**
     * Deletes the stream with the specified stream ID.
     *
     * @param streamId The ID of the stream to delete.
     */
    void deleteStream(String streamId);

    /**
     * Rebuilds the processing index for the current repository object, ensuring that all
     * relations and streams are properly indexed.
     */
    void rebuildProcessingIndex();
}
