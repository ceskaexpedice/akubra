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
     * Retrieves all relations associated with the specified namespace.
     *
     * @param namespace The namespace under which the relations are stored.
     * @return A List of Triple objects representing the relations.
     */
    List<Triple<String, String, String>> relsExtGetRelations(String namespace);

    /**
     * Adds a new relation to the repository object under the specified namespace.
     *
     * @param relation     The type of relation to add.
     * @param namespace    The namespace in which the relation is to be added.
     * @param targetRelation The target of the relation.
     */
    void relsExtAddRelation(String relation, String namespace, String targetRelation);

    /**
     * Removes all relations of the specified type and namespace from the repository object.
     *
     * @param relation  The type of relation to remove.
     * @param namespace The namespace from which to remove the relations.
     */
    void relsExtRemoveRelationsByNameAndNamespace(String relation, String namespace);

    /**
     * Removes a specific relation by its type, namespace, and target from the repository object.
     *
     * @param relation     The type of relation to remove.
     * @param namespace    The namespace of the relation.
     * @param targetRelation The target of the relation.
     */
    void relsExtRemoveRelation(String relation, String namespace, String targetRelation);

    /**
     * Removes all relations under the specified namespace from the repository object.
     *
     * @param namespace The namespace from which to remove the relations.
     */
    void relsExtRemoveRelationsByNamespace(String namespace);

    /**
     * Checks whether a relation identified by its type, namespace, and target exists.
     *
     * @param relation     The type of relation.
     * @param namespace    The namespace of the relation.
     * @param targetRelation The target of the relation.
     * @return True if the relation exists, false otherwise.
     */
    boolean relsExtRelationExists(String relation, String namespace, String targetRelation);

    /**
     * Checks whether any relations of the specified type exist within the specified namespace.
     *
     * @param relation  The type of relation to check.
     * @param namespace The namespace to check within.
     * @return True if relations exist, false otherwise.
     */
    boolean relsExtRelationsExists(String relation, String namespace);

    /**
     * Removes all relations from both the RELS-EXT and properties of the repository object.
     */
    void relsExtRemoveRelations();

    /**
     * Retrieves all literals associated with the specified namespace.
     *
     * @param namespace The namespace under which the literals are stored.
     * @return A List of Triple objects representing the literals.
     */
    List<Triple<String, String, String>> relsExtGetLiterals(String namespace);

    /**
     * Adds a literal value under the specified relation and namespace.
     *
     * @param relation The type of relation for the literal.
     * @param namespace The namespace under which the literal is stored.
     * @param value    The literal value to add.
     */
    void relsExtAddLiteral(String relation, String namespace, String value);

    /**
     * Removes a specific literal value under the specified relation and namespace.
     *
     * @param relation The type of relation for the literal.
     * @param namespace The namespace under which the literal is stored.
     * @param value    The literal value to remove.
     */
    void relsExtRemoveLiteral(String relation, String namespace, String value);

    /**
     * Checks if a specific literal value exists for the given relation and namespace.
     *
     * @param relation The type of relation for the literal.
     * @param namespace The namespace under which the literal is stored.
     * @param value    The literal value to check.
     * @return True if the literal exists, false otherwise.
     */
    boolean relsExtLiteralExists(String relation, String namespace, String value);

    /**
     * Rebuilds the processing index for the current repository object, ensuring that all
     * relations and streams are properly indexed.
     */
    void rebuildProcessingIndex();
}
