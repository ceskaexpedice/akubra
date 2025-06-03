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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

public class DatastreamMetadataSaxHandler extends DefaultHandler {
    private final String dsId;
    private boolean insideTargetDatastream = false;
    private boolean insideDatastreamVersion = false;

    private final Map<String, String> metadata = new HashMap<>();

    public DatastreamMetadataSaxHandler(String dsId) {
        this.dsId = dsId;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // Enter <datastream ID="...">
        if ("datastream".equals(localName) && dsId.equals(attributes.getValue("ID"))) {
            insideTargetDatastream = true;

            // Collect metadata from <datastream>
            metadata.put("ID", attributes.getValue("ID"));
            metadata.put("CONTROL_GROUP", attributes.getValue("CONTROL_GROUP"));
            metadata.put("STATE", attributes.getValue("STATE"));
            metadata.put("VERSIONABLE", attributes.getValue("VERSIONABLE"));
        }

        // Inside <datastreamVersion>
        if (insideTargetDatastream && "datastreamVersion".equals(localName)) {
            insideDatastreamVersion = true;

            // Collect metadata from <datastreamVersion>
            metadata.put("MIMETYPE", attributes.getValue("MIMETYPE"));
            metadata.put("CREATED", attributes.getValue("CREATED"));
            metadata.put("SIZE", attributes.getValue("SIZE"));
        }

        // Extract <contentLocation REF="...">
        if (insideDatastreamVersion && "contentLocation".equals(localName)) {
            metadata.put("LOCATION", attributes.getValue("REF"));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if ("datastreamVersion".equals(localName)) {
            insideDatastreamVersion = false;
        }
        if ("datastream".equals(localName)) {
            insideTargetDatastream = false;
        }
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }
}
