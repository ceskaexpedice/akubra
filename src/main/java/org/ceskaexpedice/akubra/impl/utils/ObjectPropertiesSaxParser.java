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

import org.ceskaexpedice.akubra.RepositoryException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ObjectPropertiesSaxParser {

    private static final Logger LOGGER = Logger.getLogger(ObjectPropertiesSaxParser.class.getName());
    private final Map<String, String> properties = new HashMap<>();

    public ObjectPropertiesSaxParser(InputStream foxml) {
        parseObjectProperties(foxml);
    }

    private void parseObjectProperties(InputStream foxml) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            ObjectPropertiesHandler handler = new ObjectPropertiesHandler();
            saxParser.parse(foxml, handler);
            properties.putAll(handler.getProperties());
        } catch (Exception e) {
            throw new RepositoryException("Error processing object properties XML: " + e.getMessage(), e);
        }
    }

    public String getProperty(String propertyName) {
        return properties.get(propertyName);
    }

    static class ObjectPropertiesHandler extends DefaultHandler {
        private boolean insideObjectProperties = false;
        private final Map<String, String> properties = new HashMap<>();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if ("objectProperties".equals(qName)) {
                insideObjectProperties = true;
            }
            if (insideObjectProperties && "property".equals(qName)) {
                String name = attributes.getValue("NAME");
                String value = attributes.getValue("VALUE");
                if (name != null && value != null) {
                    properties.put(name, value);
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if ("objectProperties".equals(qName)) {
                insideObjectProperties = false;
            }
        }

        public Map<String, String> getProperties() {
            return properties;
        }
    }

}