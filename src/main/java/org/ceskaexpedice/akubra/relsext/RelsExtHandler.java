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
import org.ceskaexpedice.akubra.impl.utils.TreeNodeProcessor;

import java.io.InputStream;
import java.util.List;

/**
 * Main repository access point for managing RELS EXT datastream relations.
 *
 * @author pavels, petrp
 */
public interface RelsExtHandler {
    String CACHE_RELS_EXT_LITERAL = "kramerius4://deepZoomCache";
    String RDF_DESCRIPTION_ELEMENT = "Description";
    String RDF_ELEMENT = "RDF";

    // ------ CRUD for the whole RELS EXT stream ------------------------------------------

    boolean exists(String pid);

    /**
     * Retrieves the RELS-EXT datastream content of a digital object.
     *
     * @param pid The persistent identifier of the object.
     * @return A {@link DatastreamContentWrapper} containing the RELS-EXT datastream content.
     */
    DatastreamContentWrapper get(String pid);

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

    String getElementValue(String pid, String xpathExpression);

    String getTilesUrl(String pid);

    String getResourcePid(String pid, String localName, String namespace, boolean appendPrefix);

    String getModel(String pid);

    String getFirstVolumePid(String pid);

    String getFirstItemId(String pid);

    String getFirstViewablePidFromTree(String pid);

    List<String> getPidsFromTree(String pid);

    List<RelsExtRelation> getRelations(String pid, String namespace);

    List<RelsExtLiteral> getLiterals(String pid, String namespace);

    // ------ CDU of individula relatio or literal ------------------------------------------

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
