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

import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ceskaexpedice.akubra.ObjectProperties.TIMESTAMP_FORMATTER;

/**
 * Dom4jUtils
 */
public final class Dom4jUtils {
    private static final Namespace NS_FOXML = new Namespace("foxml", "info:fedora/fedora-system:def/foxml#");

    private Dom4jUtils() {
    }

    public static Map<String, String> NAMESPACE_URIS = new HashMap<>();

    static {
        NAMESPACE_URIS.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        NAMESPACE_URIS.put("foxml", "info:fedora/fedora-system:def/foxml#");
        //RELS-EXT
        NAMESPACE_URIS.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        NAMESPACE_URIS.put("model", "info:fedora/fedora-system:def/model#");
        NAMESPACE_URIS.put("rel", "http://www.nsdl.org/ontologies/relationships#");
        NAMESPACE_URIS.put("oai", "http://www.openarchives.org/OAI/2.0/");
        //BIBLIO_MODS
        NAMESPACE_URIS.put("mods", "http://www.loc.gov/mods/v3");
        //DC
        NAMESPACE_URIS.put("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        NAMESPACE_URIS.put("dc", "http://purl.org/dc/elements/1.1/");
        //NAMESPACE_URIS.put("", "");
    }

    public static Document parseXmlFromFile(File xmlFile) {
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(xmlFile);
            return document;
        } catch (DocumentException e) {
            throw new RepositoryException(e);
        }
    }

    public static Document parseXmlFromW3cDoc(org.w3c.dom.Document doc) {
        return parseXmlFromString(w3cDocumentToString(doc));
    }

    public static Document parseXmlFromString(String xmlString) {
        InputStream stream = null;
        try {
            SAXReader reader = new SAXReader();
            stream = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8));
            Document document = reader.read(stream);
            return document;
        } catch (DocumentException e) {
            throw new RepositoryException(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    throw new RepositoryException(e);
                }
            }
        }
    }

    public static XPath buildXpath(String xpathExpr) {
        XPath xPath = DocumentHelper.createXPath(xpathExpr);
        xPath.setNamespaceURIs(NAMESPACE_URIS);
        return xPath;
    }

    public static Element firstElementByXpath(Element root, String xpathExpr) {
        XPath xPath = buildXpath(xpathExpr);
        List<Node> result = xPath.selectNodes(root);
        if (result.size() > 0) {
            Node firstNode = result.get(0);
            if (firstNode instanceof Element) {
                return (Element) firstNode;
            }
        }
        return null;
    }

    public static List<Element> elementsByXpath(Element root, String xpathExpr) {
        List<Element> retval = new ArrayList<>();

        XPath xPath = buildXpath(xpathExpr);
        List<Node> result = xPath.selectNodes(root);
        if (result.size() > 0) {
            for (Node node : result) {
                if (node instanceof Element) {
                    retval.add((Element) node);
                }
            }
        }
        return retval;
    }


    public static String stringOrNullFromFirstElementByXpath(Element root, String xpathExpr) {
        XPath xPath = buildXpath(xpathExpr);
        List<Node> result = xPath.selectNodes(root);
        if (result.size() > 0) {
            Node firstNode = result.get(0);
            if (firstNode instanceof Element) {
                String value = ((Element) firstNode).getStringValue();
                if (value != null) {
                    String trimmed = value.trim();
                    if (!trimmed.isEmpty()) {
                        return trimmed;
                    }
                }
            }
        }
        return null;
    }


    public static String stringOrNullFromAttributeByXpath(Element root, String xpathExpr) {
        XPath xPath = buildXpath(xpathExpr);
        List<Node> result = xPath.selectNodes(root);
        if (result.size() == 1) {
            Node firstNode = result.get(0);
            if (firstNode instanceof Attribute) {
                String value = ((Attribute) firstNode).getValue();
                if (value != null) {
                    String trimmed = value.trim();
                    if (!trimmed.isEmpty()) {
                        return trimmed;
                    }
                }
            }
        }
        return null;
    }

    public static Integer integerOrNullFromAttributeByXpath(Element root, String xpathExpr) {
        String value = stringOrNullFromAttributeByXpath(root, xpathExpr);
        if (value != null) {
            return Integer.valueOf(value);
        } else {
            return null;
        }
    }

    public static String stringOrNullFromAttributeByName(Element element, String attributeName) {
        Attribute attribute = element.attribute(attributeName);
        if (attribute != null) {
            String value = attribute.getValue();
            if (value != null) {
                String trimmed = value.trim();
                if (!trimmed.isEmpty()) {
                    return trimmed;
                }
            }
        }
        return null;
    }

    public static Float floatOrNullFromAttributeByName(Element element, String attributeName) {
        String value = stringOrNullFromAttributeByName(element, attributeName);
        if (value != null) {
            return Float.valueOf(value);
        } else {
            return null;
        }
    }

    public static Integer integerOrNullFromAttributeByName(Element element, String attributeName) {
        String value = stringOrNullFromAttributeByName(element, attributeName);
        if (value != null) {
            return Integer.valueOf(value);
        } else {
            return null;
        }
    }

    private static String w3cDocumentToString(org.w3c.dom.Document doc) {
        try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (TransformerException e) {
            throw new RepositoryException(e);
        }
    }

    public static String toStringOrNull(Node node) {
        return toStringOrNull(node, true);
    }

    public static String toStringOrNull(Node node, boolean trim) {
        if (node != null) {
            String value = node.getStringValue();
            if (value != null) {
                //replace multiple white spaces with single space
                value = value.replaceAll("\\s+", " ");
                if (trim) {
                    value = value.trim();
                }
                if (!value.isEmpty()) {
                    return value;
                }
            }
        }
        return null;
    }

    public static String docToPrettyString(Document doc) throws IOException {
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        format.setIndent(true);
        format.setIndentSize(4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLWriter xmlWriter = new XMLWriter(out, format);
        xmlWriter.write(doc);
        xmlWriter.flush();
        out.close();
        return out.toString();
    }

    /**
     * InputStream is being closed here (after extracting String or error).
     *
     * @param in
     * @param nsAware if false, namespaces will be removed
     * @return Document or null (when in is null)
     * @throws IOException
     */
    public static Document streamToDocument(InputStream in, boolean nsAware) {
        try {
            if (in == null) {
                return null;
            }
            try {
                SAXReader reader = new SAXReader();
                Document doc = reader.read(in);
                if (!nsAware) {
                    doc.accept(new NamespaceRemovingVisitor(true, true));
                }
                return doc;
            } catch (DocumentException e) {
                throw new IOException(e);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    public static String getNamespaceUri(String prefix) {
        return NAMESPACE_URIS.get(prefix);
    }

    public static String extractProperty(Document foxmlDoc, String name) {
        org.dom4j.Node node = Dom4jUtils.buildXpath(String.format("/foxml:digitalObject/foxml:objectProperties/foxml:property[@NAME='%s']/@VALUE", name)).selectSingleNode(foxmlDoc);
        return node == null ? null : Dom4jUtils.toStringOrNull(node);
    }

    public static DigitalObject foxmlDocToDigitalObject(Document foxml, AkubraRepository akubraRepository) {
        DigitalObject digitalObject = akubraRepository.unmarshall(new ByteArrayInputStream(foxml.asXML().getBytes(StandardCharsets.UTF_8)));
        return digitalObject;
    }

    public static void updateLastModifiedTimestamp(Document foxml) {
        Attribute valueAttr = (Attribute) Dom4jUtils.buildXpath("/foxml:digitalObject/foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/view#lastModifiedDate']/@VALUE").selectSingleNode(foxml);
        if (valueAttr != null) {
            valueAttr.setValue(LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        } else {
            Element objectProperties = (Element) Dom4jUtils.buildXpath("/foxml:digitalObject/foxml:objectProperties").selectSingleNode(foxml);
            Element propertyLastModified = objectProperties.addElement(new QName("property", NS_FOXML));
            propertyLastModified.addAttribute("NAME", "info:fedora/fedora-system:def/view#lastModifiedDate");
            propertyLastModified.addAttribute("VALUE", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        }
    }

}
