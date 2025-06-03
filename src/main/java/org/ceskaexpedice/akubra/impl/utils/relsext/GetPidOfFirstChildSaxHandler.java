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
package org.ceskaexpedice.akubra.impl.utils.relsext;

import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.relsext.KnownRelations;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import static org.ceskaexpedice.akubra.impl.utils.InternalSaxUtils.FOUND;

public class GetPidOfFirstChildSaxHandler extends DefaultHandler {
    private boolean insideRelsExt = false;
    private boolean insideXmlContent = false;
    private boolean insideRdfDescription = false;
    private String firstChildPid = null;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("datastream".equals(localName) && KnownDatastreams.RELS_EXT.toString().equals(attributes.getValue("ID"))) {
            insideRelsExt = true;
        }
        if (insideRelsExt && "xmlContent".equals(localName)) {
            insideXmlContent = true;
        }
        if (insideXmlContent && "Description".equals(localName)) {
            insideRdfDescription = true;
        }
        if (insideRdfDescription) {
            for (KnownRelations target : KnownRelations.values()) {
                if (localName.equals(String.valueOf(target.toString()))) { // Matches namespace-prefixed element names

                    String resource = attributes.getValue("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource");
                    if (resource != null && resource.startsWith("info:fedora/")) {
                        firstChildPid = resource.substring("info:fedora/".length());
                        throw new SAXException(FOUND); // Stop parsing early
                    }
                }
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if ("datastream".equals(localName)) {
            insideRelsExt = false;
        }
        if ("xmlContent".equals(localName)) {
            insideXmlContent = false;
        }
        if ("Description".equals(localName)) {
            insideRdfDescription = false;
        }
    }

    public String getFirstChildPid() {
        return firstChildPid;
    }
}
