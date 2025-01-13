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

import org.apache.commons.lang3.tuple.Triple;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * This interface represents basic repository item;
 * @author pavels
 */
public interface RepositoryObject {

    /**
     * Get path within repository
     * @return
     */
    String getPath();

    /**
     * Returns fullpath
     * @return
     * @throws
     */
    String getFullPath();

    /**
     * Return list of streams
     * @return
     * @throws
     */
    List<RepositoryDatastream> getStreams();

    /**
     * Return stream of the object
     * @param streamId Stream id
     * @return
     * @throws
     */
    RepositoryDatastream getStream(String streamId);

    /**
     * Returns true if the stream exists
     * @param streamId
     * @return
     * @throws
     */
    boolean streamExists(String streamId);

    /**
     * Returns last modified flag
     * @return
     * @throws
     */
    Date getLastModified();

    /**
     * REturns metadata document
     * @return
     * @throws
     */
    Document getMetadata();

    /**
     * Returns foxml representation
     * @return
     * @throws RepositoryException
     */
    InputStream getFoxml();

    /**
     * Create new XML stream
     * @param streamId Stream id
     * @param mimeType Mimetype of the stream
     * @param input Binary content
     * @return
     * @throws
     */
    RepositoryDatastream createStream(String streamId, String mimeType, InputStream input);

    /**
     * Create new managed stream
     * @param streamId Stream id
     * @param mimeType Mimetype of the stream
     * @param input Binary content
     * @return
     * @throws
     */
    RepositoryDatastream createManagedStream(String streamId, String mimeType, InputStream input);

    /**
     * Delete stream
     * @param streamId
     * @throws
     */
    void deleteStream(String streamId);

    /**
     * Create redirect stream
     * @param streamId Stream id
     * @param url url
     * @return
     * @throws
     */
    RepositoryDatastream createRedirectedStream(String streamId, String url, String mimeType);

    /**
     * Add relation
     * @param relation Type of relation
     * @param namespace Namespace
     * @param targetRelation Target
     * @throws
     */
    void addRelation(String relation, String namespace, String targetRelation);

    /**
     * Add literal
     * @param relation Type of relation
     * @param namespace Namespace
     * @param value Literal value
     * @throws
     */
    void addLiteral(String relation, String namespace, String value);

    /**
     * Remove relation
     * @param relation Type of relation
     * @param namespace Namespace
     * @param targetRelation Target
     * @throws
     */
    void removeRelation(String relation, String namespace, String targetRelation);

    /**
     * Remove all relations by relation type and namespace
     * @param relation Type of relation
     * @param namespace Namespace
     * @throws
     */
    void removeRelationsByNameAndNamespace(String relation, String namespace);

    /**
     * Remove all relations by namespace
     * @param namespace Namespace
     * @throws
     */
    void removeRelationsByNamespace(String namespace);

    /**
     * Remove all literal
     * @param relation Relation type
     * @param namespace Namespace
     * @param value Literal value
     * @throws
     */
    void removeLiteral(String relation, String namespace, String value);

    /**
     * Returns true if relation identified by relation type, namespace and target exists
     * @param relation Type of relation
     * @param namespace Namespace
     * @param targetRelation Target relation
     * @return
     * @throws
     */
    boolean relationExists(String relation, String namespace, String targetRelation);

    /**
     * Returns true if relations identified by relationType and namespace exists
     * @param relation Relation type
     * @param namespace Namespace
     * @return
     * @throws
     */
    boolean relationsExists(String relation, String namespace);

    /**
     * Returns true if literal exists
     * @param relation Relation type
     * @param namespace Namespace
     * @param value Value
     * @return
     * @throws
     */
    boolean  literalExists(String relation, String namespace, String value);

    /**
     * Returns all relations identified by namespace
     * @param namespace Namespace
     * @return
     * @throws
     */
    List<Triple<String, String, String>> getRelations(String namespace);

    /**
     * Returns all literals identified by namespace
     * @param namespace
     * @return
     * @throws
     */
    List<Triple<String, String, String>>  getLiterals(String namespace);

    /**
     * Remove all relations; from RELS-EXT and properties
     * @throws
     */
    void removeRelationsAndRelsExt();

    /**
     * Method is able to rebuild processing index for current object
     * @throws
     */
    void rebuildProcessingIndex();
   
}
