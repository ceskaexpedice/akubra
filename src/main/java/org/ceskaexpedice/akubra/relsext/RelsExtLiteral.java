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
 * Represents a literal item in the RelsExt part of the Akubra Fefora model stream.
 * This class extends {@link RelsExtItem} to hold a literal content value in addition
 * to the namespace and local name.
 */
public class RelsExtLiteral extends RelsExtItem {

    // The literal content associated with this RelsExt item.
    private String content;

    /**
     * Constructs a RelsExtLiteral with the specified namespace, local name, and content.
     *
     * @param namespace The namespace of the RelsExt item.
     * @param localName The local name of the RelsExt item.
     * @param content The literal content associated with this RelsExt item.
     */
    public RelsExtLiteral(String namespace, String localName, String content) {
        super(namespace, localName);
        this.content = content;
    }

    /**
     * Retrieves the content of this RelsExtLiteral item.
     *
     * @return The content of the RelsExtLiteral item.
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns a string representation of the RelsExtLiteral item, combining the
     * namespace, local name, and content.
     *
     * @return A string representation of the RelsExtLiteral item.
     */
    @Override
    public String toString() {
        return super.toString() + " " + content;
    }
}

