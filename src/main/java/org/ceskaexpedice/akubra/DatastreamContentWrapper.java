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

import org.w3c.dom.Document;

import java.io.InputStream;

/**
 * A wrapper interface for handling various Akubra Fedora digital object datastream content formats.
 * <p>
 * This utility provides multiple representations of a Fedora datastream's content,
 * allowing it to be accessed as an {@link InputStream}, a DOM {@link Document},
 * a Dom4j {@link org.dom4j.Document}, or a {@link String}.
 * </p>
 * @author pavels, petrp
 */
public interface DatastreamContentWrapper {

    /**
     * Returns the datastream content as an {@link InputStream}.
     * <p>
     * This stream can be used to read the raw binary or textual content
     * of the Fedora datastream.
     * </p>
     *
     * @return an {@link InputStream} representing the datastream content
     */
    InputStream asInputStream();

    /**
     * Returns the datastream content as a W3C DOM {@link Document}.
     * <p>
     * This method allows parsing the content into a standard DOM document,
     * with optional namespace awareness.
     * </p>
     *
     * @param nsAware whether the parser should be namespace-aware
     * @return a {@link Document} representing the datastream content
     */
    Document asDom(boolean nsAware);

    /**
     * Returns the datastream content as a Dom4j {@link org.dom4j.Document}.
     * <p>
     * This method provides a Dom4j representation of the datastream content,
     * supporting optional namespace awareness.
     * </p>
     *
     * @param nsAware whether the parser should be namespace-aware
     * @return a {@link org.dom4j.Document} representing the datastream content
     */
    org.dom4j.Document asDom4j(boolean nsAware);

    /**
     * Returns the datastream content as a {@link String}.
     * <p>
     * This method retrieves the content as a UTF-8 encoded string,
     * which is useful for processing text-based datastreams.
     * </p>
     *
     * @return the datastream content as a {@link String}
     */
    String asString();
}
