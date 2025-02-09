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

/**
 * Represents the FOXML (Fedora Object XML) type of a digital object.
 * <p>
 * This enum differentiates between objects that are stored as complete
 * archives and those that are managed within the repository.
 * </p>
 *
 * <p>FOXML is a format used in Fedora repositories to represent digital objects,
 * including their metadata, content, and relationships.</p>
 *
 * @author Inovatika
 */
public enum FoxmlType {

    /**
     * The object is stored as an archival package,
     * typically preserving all associated metadata and content.
     */
    archive,

    /**
     * The object is actively managed within the repository,
     * with content stored separately from its metadata.
     */
    managed
}

