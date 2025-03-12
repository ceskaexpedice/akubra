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
package org.ceskaexpedice.akubra.core.repository;

import org.ceskaexpedice.fedoramodel.DatastreamType;

import java.util.Date;

/**
 * Represents a datastream in the repository system.
 * A datastream is an associated data entity within the repository object,
 * and this interface provides access to its metadata and content.
 */
public interface RepositoryDatastream {

    /**
     * Returns the associated datastream type.
     *
     * @return A DatastreamType object representing the type of this datastream.
     */
    DatastreamType getDatastream();

    /**
     * Enum representing the two types of datastreams:
     * DIRECT and INDIRECT.
     */
    enum Type {
        /**
         * A direct datastream, typically representing content directly
         * stored within the repository.
         */
        DIRECT,

        /**
         * An indirect datastream, often representing a pointer or a link
         * to external content.
         */
        INDIRECT;
    }

    /**
     * Returns the name of the datastream.
     *
     * @return A string representing the name of the datastream.
     * @throws IllegalStateException If the name cannot be retrieved.
     */
    String getName();

    /**
     * Returns the MIME type of the last version of the datastream.
     *
     * @return A string representing the MIME type of the last version.
     * @throws IllegalStateException If the MIME type cannot be determined.
     */
    String getLastVersionMimeType();

    /**
     * Returns the last modified date of the datastream.
     *
     * @return A Date object representing the last modification timestamp.
     * @throws IllegalStateException If the last modified date is unavailable.
     */
    Date getLastVersionLastModified();

    /**
     * Returns the type of the datastream (either DIRECT or INDIRECT).
     *
     * @return A Type enumeration representing the datastream's type.
     */
    Type getStreamType();

}
