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

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Date;

/**
 * Interface that defines the properties of an object, including the ability to retrieve
 * property values, labels, and metadata such as creation and modification timestamps.
 * Implementing classes should provide the logic for handling properties and metadata.
 *
 * @author pavels, petrp
 *
 */
public interface ObjectProperties {
    DateTimeFormatter TIMESTAMP_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss.")
            .appendFraction(ChronoField.MILLI_OF_SECOND, 1, 3, false)
            .appendPattern("'Z'")
            .toFormatter();

    /**
     * Retrieves the value of a property by its name.
     *
     * @param propertyName The name of the property to retrieve.
     * @return The value of the specified property.
     */
    String getProperty(String propertyName);

    /**
     * Retrieves the label  property.
     *
     * @return The label property.
     */
    String getPropertyLabel();

    /**
     * Retrieves the created property.
     *
     * @return The created property.
     */
    Date getPropertyCreated();

    /**
     * Retrieves the last modified property.
     *
     * @return The last modifified property.
     */
    Date getPropertyLastModified();
}

