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
package org.ceskaexpedice.akubra.impl.utils;

import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.RepositoryNamespaces;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import static org.ceskaexpedice.akubra.impl.utils.InternalSaxUtils.FOUND;

public class GetModsPartTypeSaxHandler extends DefaultHandler {
    private boolean insideBiblioMods = false;
    private boolean insideXmlContent = false;
    private boolean insideModsCollection = false;
    private boolean insideMods = false;
    private String partType = null;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("datastream".equals(qName) && KnownDatastreams.BIBLIO_MODS.toString().equals(attributes.getValue("ID"))) {
            insideBiblioMods = true;
        }
        if (insideBiblioMods && "xmlContent".equals(qName)) {
            insideXmlContent = true;
        }
        if (insideXmlContent && "modsCollection".equals(localName) && uri.equals(RepositoryNamespaces.BIBILO_MODS_URI)) {
            insideModsCollection = true;
        }
        if (insideModsCollection && "mods".equals(localName) && uri.equals(RepositoryNamespaces.BIBILO_MODS_URI)) {
            insideMods = true;
        }
        // Extract type from <mods:part type="...">
        if (insideMods && "part".equals(localName) && uri.equals(RepositoryNamespaces.BIBILO_MODS_URI)) {
            partType = attributes.getValue("type");
            throw new SAXException(FOUND); // Stop parsing early if match found
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if ("datastream".equals(qName)) {
            insideBiblioMods = false;
        }
        if ("xmlContent".equals(qName)) {
            insideXmlContent = false;
        }
        if ("modsCollection".equals(localName) && uri.equals(RepositoryNamespaces.BIBILO_MODS_URI)) {
            insideModsCollection = false;
        }
        if ("mods".equals(localName) && uri.equals(RepositoryNamespaces.BIBILO_MODS_URI)) {
            insideMods = false;
        }
    }

    public String getPartType() {
        return partType;
    }
}
