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
package org.ceskaexpedice.akubra.core.repository.impl;

import org.apache.commons.io.IOUtils;
import org.ceskaexpedice.akubra.impl.utils.InternalSaxUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import static org.ceskaexpedice.akubra.core.repository.impl.RepositoryUtils.FOUND;

class GetDatastreamContentSaxHandler extends DefaultHandler {
    private final String targetId;

    private boolean insideTargetDatastream;
    private boolean insideDatastreamVersion;
    private boolean insideXmlContent;
    private boolean skipXmlContentTag;

    private String contentLocationRef;
    private String contentLocationType;
    private StringWriter xmlContentWriter;

    GetDatastreamContentSaxHandler(String targetId) {
        this.targetId = targetId;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("datastream".equals(qName) && targetId.equals(attributes.getValue("ID"))) {
            insideTargetDatastream = true;
        }
        if (insideTargetDatastream && "datastreamVersion".equals(qName)) {
            insideDatastreamVersion = true;
        }
        if (insideDatastreamVersion && "contentLocation".equals(qName)) {
            contentLocationRef = attributes.getValue("REF");
            contentLocationType = attributes.getValue("TYPE");
            // Stop parsing early if contentLocation is found
            throw new SAXException(FOUND);
        }
        // Start capturing <xmlContent> but **skip writing the root xmlContent tag**
        if (insideDatastreamVersion && "xmlContent".equals(qName)) {
            insideXmlContent = true;
            skipXmlContentTag = true; // Mark that we should skip <xmlContent>
            xmlContentWriter = new StringWriter();
        }
        // If inside <xmlContent>, preserve tags **but skip <xmlContent> itself**
        if (insideXmlContent) {
            if (skipXmlContentTag) {
                skipXmlContentTag = false; // Ensure first child is captured
            } else {
                xmlContentWriter.write("<" + qName);
                for (int i = 0; i < attributes.getLength(); i++) {
                    xmlContentWriter.write(" " + attributes.getQName(i) + "=\"" + attributes.getValue(i) + "\"");
                }
                xmlContentWriter.write(">");
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        if (insideXmlContent) {
            xmlContentWriter.write(new String(ch, start, length));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if ("datastreamVersion".equals(qName)) {
            insideDatastreamVersion = false;
        }
        if ("datastream".equals(qName)) {
            insideTargetDatastream = false;
        }
        if ("xmlContent".equals(qName)) {
            insideXmlContent = false;
        }
        if ("xmlContent".equals(qName)) {
            insideXmlContent = false;
            skipXmlContentTag = false; // Reset flag
            return; // Skip writing </xmlContent> tag
        }
        // If inside <xmlContent>, preserve closing tags (excluding </xmlContent>)
        if (insideXmlContent) {
            xmlContentWriter.write("</" + qName + ">");
        }
    }

    String getContentLocationRef() {
        return contentLocationRef;
    }

    String getContentLocationType() {
        return contentLocationType;
    }

    InputStream getXmlContentStream() {
        return xmlContentWriter != null ? IOUtils.toInputStream(xmlContentWriter.toString(), StandardCharsets.UTF_8) : null;
    }
}