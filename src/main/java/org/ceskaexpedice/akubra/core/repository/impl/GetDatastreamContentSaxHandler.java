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
import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.ceskaexpedice.akubra.core.repository.impl.RepositoryUtils.FOUND;

class GetDatastreamContentSaxHandler extends DefaultHandler {

    public static final Logger LOGGER = Logger.getLogger(GetDatastreamContentSaxHandler.class.getName());

    // Light rendering element; used for rendering raw xml content
    class LRElement {

        private String qName;
        private String localName;
        private String uri;

        private Map<String, String> namespaces = new LinkedHashMap<>();
        private Map<String, String> attributes = new LinkedHashMap<>();

        private List<LRElement> children = new ArrayList<>();
        private StringBuilder text = new StringBuilder();

        private LRElement parent = null;

        public LRElement(String qName, String localName, String uri) {
            this.qName = qName;
            this.localName = localName;
            this.uri = uri;
        }

        public String getqName() {
            return qName;
        }

        public String getLocalName() {
            return localName;
        }

        public String getUri() {
            return uri;
        }

        public void addNamespace(String prefix, String uri) {
            this.namespaces.put(prefix, uri);
        }

        public void removeNamespace(String prefix) {
            this.namespaces.remove(prefix);
        }

        public Map<String, String> getNamespaces() {
            return namespaces;
        }

        public void putNamespaces(Map<String,String> namespaces) {
            this.namespaces.putAll(namespaces);
        }

        public Map<String,String> getAttributes() {
            return this.attributes;
        }

        public void addAttribute(String name, String value) {
            this.attributes.put(name, value);
        }

        public void removeAttribute(String name) {
            this.attributes.remove(name);
        }

        public void setParent(LRElement parent) {
            this.parent = parent;
        }

        public LRElement getParent() {
            return parent;
        }

        public List<LRElement> getChildren() {
            return children;
        }

        public void addChild(LRElement ch) {
            ch.setParent(this);
            this.children.add(ch);
        }

        public void removeChild(LRElement ch) {
            ch.setParent(null);
            this.children.remove(ch);
        }


        public String toXml(boolean includeNamespaces) {
            // xml content contains all necessary namespaces inhereted from foxml -> we can use in rendering if necessary
            if (this.localName.equals("xmlContent") && this.getChildren().size() == 1) {
                Map<String, String> pickedNamespaces = new HashMap<>();
                Stack<LRElement> stack = new Stack<>();
                stack.push(this);
                while(!stack.isEmpty()) {
                    LRElement topElm  = stack.pop();
                    String qname = topElm.getqName();
                    String[] names = qname.split(":");
                    if (names.length > 0) {
                        String prefix = names[0];
                        if (!"".equals(prefix) && this.namespaces.containsKey(prefix)) {
                            String val = this.namespaces.get(prefix);
                            pickedNamespaces.put(prefix,val);
                        }
                    }
                    topElm.getChildren().forEach(stack::push);
                }

                LRElement firstChild = this.getChildren().get(0);
                firstChild.putNamespaces(pickedNamespaces);
                return firstChild.toXml(true);
            } else  {
                StringBuilder sb = new StringBuilder();
                sb.append("<").append(qName);
                if (includeNamespaces) {
                    for (Map.Entry<String, String> ns : namespaces.entrySet()) {
                        String prefix = ns.getKey();
                        String uri = ns.getValue();
                        if (prefix == null || prefix.isEmpty()) {
                            sb.append(" xmlns=\"").append(uri).append("\"");
                        } else {
                            sb.append(" xmlns:").append(prefix).append("=\"").append(uri).append("\"");
                        }
                    }
                }

//                if (parent != null && parent.getNamespaces().containsKey("") && this.getNamespaces().containsKey("") && (!parent.getNamespaces().get("").equals(this.getNamespaces().get("")))) {
//                    // different -> write it
//                    sb.append(" xmlns=\"").append(getNamespaces().get("")).append("\"");
//                }


                for (Map.Entry<String, String> attr : attributes.entrySet()) {
                    sb.append(" ").append(attr.getKey()).append("=\"").append(attr.getValue()).append("\"");
                }

                if (children.isEmpty() && text.length() == 0) {
                    sb.append("/>");
                } else {
                    sb.append(">");
                    if (text.length() > 0) {
                        sb.append(text);
                    }
                    for (LRElement child : children) {
                        sb.append(child.toXml(includeNamespaces));
                    }
                    sb.append("</").append(qName).append(">");
                }
                return sb.toString();
            }
        }
    }
    // Rendering root
    LRElement lightRenderingRoot = null;

    private final String targetId;

    private boolean insideTargetDatastream;
    private boolean insideDatastreamVersion;
    private boolean insideXmlContent;

    private String contentLocationRef;
    private String contentLocationType;

    private String versionable = "false";
    private int lastAcceptedVersion = -1;
    private int currentVersion = -1;

    private final Map<String, String> pendingMappings = new LinkedHashMap<>();
    private final Map<String, String> allSeenNamespaces = new LinkedHashMap<>();
    private final Deque<LRElement> elementStack = new ArrayDeque<>();

    @Override
    public void startPrefixMapping(String prefix, String uri) {
        String safePrefix = prefix == null ? "" : prefix;
        pendingMappings.put(safePrefix, uri);
        allSeenNamespaces.putIfAbsent(safePrefix, uri);
    }

    GetDatastreamContentSaxHandler(String targetId) {
        this.targetId = targetId;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        String formatted = String.format("(uri:'%s', localName:'%s', qName:'%s')",uri, localName, qName);
        StringBuilder debugMsg = new StringBuilder("===> [startElement]:" + formatted + "\nAttributes:\n");
        for (int i = 0; i < attributes.getLength(); i++) {
            String attrName = attributes.getQName(i);
            String attrValue = attributes.getValue(i);
            debugMsg.append(" - ").append(attrName).append(" = ").append(attrValue).append("\n");
        }
        LOGGER.log(Level.FINE, debugMsg.toString());


        if ("datastream".equals(localName) && "info:fedora/fedora-system:def/foxml#".equals(uri) &&
                targetId.equals(attributes.getValue("ID"))) {
            this.versionable = attributes.getValue("VERSIONABLE");
            insideTargetDatastream = true;
        }
        if (insideTargetDatastream && "datastreamVersion".equals(localName)) {
            insideDatastreamVersion = true;
            String versionName =  attributes.getValue("ID");
            if (versionName.contains(".")) {
                versionName = versionName.substring(versionName.indexOf(".")+1);
                this.currentVersion = Integer.parseInt(versionName);
                LOGGER.fine("Current version: " + this.currentVersion);
            }

        }

        if (insideDatastreamVersion && "contentLocation".equals(localName)) {
            contentLocationRef = attributes.getValue("REF");
            contentLocationType = attributes.getValue("TYPE");
            StringBuilder locDebugMsg = new StringBuilder(String.format("===> [startElement-findLocation]: contentLocationRef:'%s', contentLocationType:'%s'", contentLocationRef, contentLocationType));
            LOGGER.fine(locDebugMsg.toString());
            // Stop parsing early if contentLocation is found
            throw new SAXException(FOUND);
        }
        if (insideTargetDatastream && insideDatastreamVersion && "xmlContent".equals(localName)) {
            insideXmlContent = true;
        }

        if (insideTargetDatastream && insideXmlContent) {
            LRElement element = new LRElement(qName, localName, uri);
            element.putNamespaces(pendingMappings);

            for (int i = 0; i < attributes.getLength(); i++) {
                element.addAttribute(attributes.getQName(i), attributes.getValue(i));
            }

            if (elementStack.isEmpty()) {
                lightRenderingRoot = element;
            } else {
                elementStack.peek().addChild(element);
            }
            elementStack.push(element);
            pendingMappings.clear();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        if (insideXmlContent && !elementStack.isEmpty()) {
            String raw = new String(ch, start, length);
            String escaped = StringEscapeUtils.escapeXml10(raw);

            StringBuilder debugMsg = new StringBuilder("===> [characters] Escaped text: '" + escaped +"'");
            LOGGER.log(Level.FINE, debugMsg.toString());

            elementStack.peek().text.append(escaped);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        String formatted = String.format("(uri:'%s', localName:'%s', qName:'%s')",uri, localName, qName);
        StringBuilder debugMsg = new StringBuilder("===> [endElement]: " + formatted);

        LOGGER.log(Level.FINE, debugMsg.toString());

        if ("datastreamVersion".equals(localName)) {
            insideDatastreamVersion = false;
        }
        if ("datastream".equals(localName)) {
            insideTargetDatastream = false;
        }

        if ("xmlContent".equals(localName)) {
            insideXmlContent = false;
            if (insideTargetDatastream) {
                if (currentVersion > lastAcceptedVersion) {
                    lastAcceptedVersion = currentVersion;
                    LOGGER.fine("Accepted version: " + this.lastAcceptedVersion);
                }
            }
            return;
        }
        if (!elementStack.isEmpty()) {
            elementStack.pop();
        }
    }

    String getContentLocationRef() {
        return contentLocationRef;
    }

    String getContentLocationType() {
        return contentLocationType;
    }

    public String getVersionable() {
        return versionable;
    }

    public int getLastAcceptedVersion() {
        return lastAcceptedVersion;
    }

    InputStream getXmlContentStream() {
        if (lightRenderingRoot == null) return null;
        // all namespaces to root
        lightRenderingRoot.putNamespaces(allSeenNamespaces);
        String content = lightRenderingRoot.toXml(true);
        return IOUtils.toInputStream(content, StandardCharsets.UTF_8);
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
        return new InputSource(new StringReader(""));
    }
}
