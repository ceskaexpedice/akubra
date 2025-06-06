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
 * Defines known XML format URIs used in Fedora digital objects.
 * <p>
 * This class provides constants for commonly used XML format namespaces
 * in Fedora, such as RELS-EXT, MODS, and Dublin Core (DC).
 * </p>
 */
public class KnownXmlFormatUris {

    /**
     * URI for the Fedora RELS-EXT (Relationships) XML format.
     */
    public static final String RELS_EXT = "info:fedora/fedora-system:FedoraRELSExt-1.0";

    /**
     * URI for the MODS (Metadata Object Description Schema) XML format.
     */
    public static final String BIBLIO_MODS = "http://www.loc.gov/mods/v3";

    /**
     * URI for the Dublin Core (DC) XML format used in OAI-PMH.
     */
    public static final String BIBLIO_DC = "http://www.openarchives.org/OAI/2.0/oai_dc/";
}
