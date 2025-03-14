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

package org.ceskaexpedice.akubra.relsext;

import org.ceskaexpedice.akubra.DatastreamContentWrapper;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.List;

/**
 * Main repository access point for managing RELS EXT datastream relations.
 *
 * @author pavels, petrp
 */
public interface RelsExtHelper {
    String RDF_DESCRIPTION_ELEMENT = "Description";
    String RDF_ELEMENT = "RDF";

    // ------ CRUD for the whole RELS EXT stream ------------------------------------------

    /**
     * Checks if a RELS EXT exists for a given object.
     *
     * @param pid           The persistent identifier of the object.
     * @return {@code true} if the stream exists, {@code false} otherwise.
     */
    boolean exists(String pid);

    /**
     * Retrieves the RELS-EXT datastream content of a digital object.
     *
     * @param pid The persistent identifier of the object.
     * @return A {@link DatastreamContentWrapper} containing the RELS-EXT datastream content.
     */
    DatastreamContentWrapper get(String pid);

    /**
     * Updates the RELS-EXT datastream content of a digital object.
     *
     * @param pid        The persistent identifier of the object.
     * @param xmlContent The new RELS-EXT datastream content as an InputStream.
     */
    void update(String pid, InputStream xmlContent);

    // ------ Detailed information from RELS EXT ------------------------------------------

    /**
     * Checks if a relation exists for a given object.
     *
     * @param pid           The persistent identifier of the object.
     * @param relation      The relationship type (e.g., "isPartOf").
     * @param namespace     The namespace URI for the relationship.
     * @return {@code true} if the relation exists, {@code false} otherwise.
     */
    boolean relationExists(String pid, String relation, String namespace);

    /**
     * Retrieves all relations for a given object within a specified namespace.
     *
     * @param pid       The persistent identifier of the object.
     * @param namespace The namespace URI to filter relations.
     * @return A list of {@link RelsExtRelation} representing the relations.
     */
    List<RelsExtRelation> getRelations(String pid, String namespace);

    /**
     * Retrieves all literals for a given object within a specified namespace.
     *
     * @param pid       The persistent identifier of the object.
     * @param namespace The namespace URI to filter literals.
     * @return A list of {@link RelsExtLiteral} representing the literals.
     */
    List<RelsExtLiteral> getLiterals(String pid, String namespace);

    /**
     * Retrieves the URL of the tileset associated with the given object.
     *
     * @param pid The persistent identifier of the object.
     * @return The URL as a String.
     */
    String getTilesUrl(String pid);

    /**
     * Retrieves the PID of the first child object in the hierarchy.
     *
     * @param pid The persistent identifier of the object.
     * @return The PID of the first child.
     */
    String getPidOfFirstChild(String pid);

    /**
     * Retrieves the PID of the first object from which the current object was replicated.
     *
     * @param pid The persistent identifier of the object.
     * @return The PID of the source object.
     */
    String getFirstReplicatedFrom(String pid);

    /**
     * Retrieves the model type of the given object.
     *
     * @param pid The persistent identifier of the object.
     * @return The model name as a String.
     */
    String getModel(String pid);

    /**
     * Processes the RELS-EXT tree structure of a given object.
     *
     * @param pid       The persistent identifier of the object.
     * @param processor A {@link TreeNodeProcessor} to handle each node.
     */
    void processInTree(String pid, TreeNodeProcessor processor);

    /**
     * Retrieves the PID of the first viewable object in the hierarchy.
     *
     * @param pid The persistent identifier of the object.
     * @return The PID of the first viewable object.
     */
    String getFirstViewablePidInTree(String pid);

    /**
     * Retrieves a list of all PIDs within the tree structure of the given object.
     *
     * @param pid The persistent identifier of the object.
     * @return A list of PIDs.
     */
    List<String> getPidsInTree(String pid);

    // ------ CDU of individual relation or literal ------------------------------------------

    /**
     * Adds a new relationship to the RELS-EXT datastream of a digital object.
     *
     * @param pid           The persistent identifier of the object.
     * @param relation      The relationship type (e.g., "isPartOf").
     * @param namespace     The namespace URI for the relationship.
     * @param targetRelation The target object of the relationship.
     */
    void addRelation(String pid, String relation, String namespace, String targetRelation);

    /**
     * Removes a specific relationship from the RELS-EXT datastream of a digital object.
     *
     * @param pid           The persistent identifier of the object.
     * @param relation      The relationship type.
     * @param namespace     The namespace URI of the relationship.
     * @param targetRelation The target object of the relationship.
     */
    void removeRelation(String pid, String relation, String namespace, String targetRelation);

    /**
     * Removes a specific relationship from the RELS-EXT datastream of a digital object.
     *
     * @param pid           The persistent identifier of the object.
     * @param relation      The relationship type.
     * @param namespace     The namespace URI of the relationship.
     */
    void removeRelationsByNameAndNamespace(String pid, String relation, String namespace);

    /**
     * Removes a specific relationship from the RELS-EXT datastream of a digital object.
     *
     * @param pid           The persistent identifier of the object.
     * @param namespace     The namespace URI of the relationship.
     */
    void removeRelationsByNamespace(String pid, String namespace);

    /**
     * Adds a literal value to the RELS-EXT datastream of a digital object.
     *
     * @param pid       The persistent identifier of the object.
     * @param relation  The property name.
     * @param namespace The namespace URI for the property.
     * @param value     The literal value to be stored.
     */
    void addLiteral(String pid, String relation, String namespace, String value);

    /**
     * Removes a specific literal value from the RELS-EXT datastream of a digital object.
     *
     * @param pid       The persistent identifier of the object.
     * @param relation  The property name.
     * @param namespace The namespace URI of the property.
     * @param value     The literal value to be removed.
     */
    void removeLiteral(String pid, String relation, String namespace, String value);

}
