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

package org.ceskaexpedice.akubra;

import org.ceskaexpedice.akubra.core.repository.RepositoryDatastream;

import java.util.Date;

/**
 * Represents metadata information about a datastream in the repository.
 * <p>
 * This interface provides details such as the datastream's ID, MIME type, size,
 * storage location, and timestamps for creation and modification.
 * </p>
 *
 * <p>Implementations of this interface retrieve datastream properties
 * from the underlying Fedora-Akubra repository.</p>
 *
 * @author pavels, petrp
 */
public interface DatastreamMetadata {

    /**
     * Retrieves the identifier of the datastream.
     *
     * @return The datastream ID.
     */
    String getId();

    /**
     * Retrieves the MIME type of the datastream.
     *
     * @return The MIME type as a string.
     */
    String getMimetype();

    /**
     * Retrieves the size of the datastream in bytes.
     *
     * @return The size of the datastream.
     */
    long getSize();

    /**
     * Retrieves the control group of the datastream.
     * <p>
     * The control group defines how the datastream is managed in the repository,
     * such as whether it is inline XML, managed content, or external reference.
     * </p>
     *
     * @return The control group as a string.
     */
    String getControlGroup();

    /**
     * Retrieves the storage location of the datastream.
     * <p>
     * This could be a path, URL, or identifier pointing to the stored content.
     * </p>
     *
     * @return The location of the datastream.
     */
    String getLocation();

    /**
     * Retrieves the last modification timestamp of the datastream.
     *
     * @return The last modified date.
     */
    Date getLastModified();

    /**
     * Retrieves the creation timestamp of the datastream.
     *
     * @return The creation date.
     */
    Date getCreateDate();
}
