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
 * Represents an item in the RelsExt part of the Akubra Fefora model stream.
 * This class is intended to hold information related to a namespace and a local name.
 * It serves as a base class for extensions that define specific RelsExt items.
 */
public abstract class RelsExtItem {

    // The namespace associated with this RelsExt item.
    private String namespace;

    // The local name of the RelsExt item.
    private String localName;

    /**
     * Constructs a RelsExtItem with the specified namespace and local name.
     *
     * @param namespace The namespace of the RelsExt item.
     * @param localName The local name of the RelsExt item.
     */
    public RelsExtItem(String namespace, String localName) {
        this.namespace = namespace;
        this.localName = localName;
    }

    /**
     * Retrieves the namespace associated with this RelsExt item.
     *
     * @return The namespace of the RelsExt item.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Retrieves the local name of this RelsExt item.
     *
     * @return The local name of the RelsExt item.
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * Returns a string representation of the RelsExt item, combining the namespace and local name.
     *
     * @return A string representation of the RelsExt item.
     */
    @Override
    public String toString() {
        return namespace + " " + localName;
    }
}
