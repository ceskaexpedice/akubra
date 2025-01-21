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
import org.ceskaexpedice.model.DigitalObject;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * This interface represents basic repository item;
 * @author pavels
 */
public interface RepositoryObject {

    DigitalObject getDigitalObject();

    /**
     * Get path within repository
     * @return
     */
    String getPid();

    /**
     * Returns last modified flag
     * @return
     * @throws
     */
    Date getPropertyLastModified();

    /**
     * Returns foxml representation
     * @return
     */
    InputStream getFoxml();

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
     * Create new XML stream
     * @param streamId Stream id
     * @param mimeType Mimetype of the stream
     * @param input Binary content
     * @return
     * @throws
     */
    RepositoryDatastream createXMLStream(String streamId, String mimeType, InputStream input);

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
     * Create redirect stream
     * @param streamId Stream id
     * @param url url
     * @return
     * @throws
     */
    RepositoryDatastream createRedirectedStream(String streamId, String url, String mimeType);

    /**
     * Delete stream
     * @param streamId
     * @throws
     */
    void deleteStream(String streamId);

    /**
     * Returns all relations identified by namespace
     * @param namespace Namespace
     * @return
     * @throws
     */
    List<Triple<String, String, String>> relsExtGetRelations(String namespace);

    /**
     * Add relation
     * @param relation Type of relation
     * @param namespace Namespace
     * @param targetRelation Target
     * @throws
     */
    void relsExtAddRelation(String relation, String namespace, String targetRelation);

    /**
     * Remove all relations by relation type and namespace
     * @param relation Type of relation
     * @param namespace Namespace
     * @throws
     */
    void relsExtRemoveRelationsByNameAndNamespace(String relation, String namespace);

    /**
     * Remove relation
     * @param relation Type of relation
     * @param namespace Namespace
     * @param targetRelation Target
     * @throws
     */
    void relsExtRemoveRelation(String relation, String namespace, String targetRelation);

    /**
     * Remove all relations by namespace
     * @param namespace Namespace
     * @throws
     */
    void relsExtRemoveRelationsByNamespace(String namespace);

    /**
     * Returns true if relation identified by relation type, namespace and target exists
     * @param relation Type of relation
     * @param namespace Namespace
     * @param targetRelation Target relation
     * @return
     * @throws
     */
    boolean relsExtRelationExists(String relation, String namespace, String targetRelation);

    /**
     * Returns true if relations identified by relationType and namespace exists
     * @param relation Relation type
     * @param namespace Namespace
     * @return
     * @throws
     */
    boolean relsExtRelationsExists(String relation, String namespace);

    /**
     * Remove all relations; from RELS-EXT and properties
     * @throws
     */
    void relsExtRemoveRelations();

    /**
     * Returns all literals identified by namespace
     * @param namespace
     * @return
     * @throws
     */
    List<Triple<String, String, String>> relsExtGetLiterals(String namespace);

    /**
     * Add literal
     * @param relation Type of relation
     * @param namespace Namespace
     * @param value Literal value
     * @throws
     */
    void relsExtAddLiteral(String relation, String namespace, String value);

    /**
     * Remove all literal
     * @param relation Relation type
     * @param namespace Namespace
     * @param value Literal value
     * @throws
     */
    void relsExtRemoveLiteral(String relation, String namespace, String value);

    /**
     * Returns true if literal exists
     * @param relation Relation type
     * @param namespace Namespace
     * @param value Value
     * @return
     * @throws
     */
    boolean relsExtLiteralExists(String relation, String namespace, String value);


    /**
     * Method is able to rebuild processing index for current object
     * @throws
     */
    void rebuildProcessingIndex();


}
