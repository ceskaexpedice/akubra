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

import java.time.LocalDateTime;

/**
 * Interface that defines the properties of an object, including the ability to retrieve
 * property values, labels, and metadata such as creation and modification timestamps.
 * Implementing classes should provide the logic for handling properties and metadata.
 */
public interface ObjectProperties {

    /**
     * Retrieves the value of a property by its name.
     *
     * @param propertyName The name of the property to retrieve.
     * @return The value of the specified property.
     */
    String getProperty(String propertyName);

    /**
     * Retrieves the label or description of the property.
     *
     * @return The label of the property.
     */
    String getPropertyLabel();

    /**
     * Retrieves the timestamp when the property was created.
     *
     * @return The creation timestamp of the property.
     */
    LocalDateTime getPropertyCreated();

    /**
     * Retrieves the timestamp when the property was last modified.
     *
     * @return The last modification timestamp of the property.
     */
    LocalDateTime getPropertyLastModified();
}

