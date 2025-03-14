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
 * Enum representing known Fedora datastream types.
 * <p>
 * This enumeration defines a set of recognized datastreams used within Fedora,
 * including bibliographic metadata, OCR results, images, audio formats, and
 * administrative datastreams.
 * </p>
 *
 * @author pavels, petrp
 *
 */
public enum KnownDatastreams {
    RELS_EXT("RELS-EXT"),

    BIBLIO_MODS("BIBLIO_MODS"),
    BIBLIO_DC("DC"),

    OCR_ALTO("ALTO"),
    OCR_TEXT("TEXT_OCR"),

    IMG_FULL("IMG_FULL"),
    IMG_THUMB("IMG_THUMB"),
    IMG_PREVIEW("IMG_PREVIEW"),

    AUDIO_MP3("MP3"),
    AUDIO_OGG("OGG"),
    AUDIO_WAV("WAV"),

    // known but not used datastreams
    POLICY("POLICY"),
    MIGRATION("MIGRATION"),
    IMG_FULL_ADM("IMG_FULL_ADM"),
    AUDIT("AUDIT"),
    TEXT_OCR_ADM("TEXT_OCR_ADM"),
    COLLECTION_CLIPPINGS("COLLECTION_CLIPPINGS");

    private final String value;

    /**
     * Constructs a {@code KnownDatastreams} enum with the specified datastream name.
     *
     * @param value the string representation of the datastream name
     */
    KnownDatastreams(String value) {
        this.value = value;
    }

    /**
     * Returns the string representation of the datastream name.
     *
     * @return the datastream name as a {@link String}
     */
    public String toString() {
        return value;
    }
}
