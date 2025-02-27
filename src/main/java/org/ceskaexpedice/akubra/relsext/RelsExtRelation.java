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

/**
 * Represents a relation item in the RelsExt part of the Akubra Fefora model stream.
 * This class extends {@link RelsExtItem} and holds a reference to another resource
 * in addition to the namespace and local name.
 */
public class RelsExtRelation extends RelsExtItem {

    // The resource associated with this RelsExt relation.
    private String resource;

    /**
     * Constructs a RelsExtRelation with the specified namespace, local name, and resource.
     *
     * @param namespace The namespace of the RelsExt item.
     * @param localName The local name of the RelsExt item.
     * @param resource The resource related to this RelsExt item.
     */
    public RelsExtRelation(String namespace, String localName, String resource) {
        super(namespace, localName);
        this.resource = resource;
    }

    /**
     * Retrieves the resource associated with this RelsExtRelation item.
     *
     * @return The resource associated with the RelsExtRelation item.
     */
    public String getResource() {
        return resource;
    }

    /**
     * Returns a string representation of the RelsExtRelation item, combining the
     * namespace, local name, and resource.
     *
     * @return A string representation of the RelsExtRelation item.
     */
    @Override
    public String toString() {
        return super.toString() + " " + resource;
    }
}

