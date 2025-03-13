/*
 * Copyright (C) 2010 Pavel Stastny
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

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.RepositoryNamespaceContext;
import org.ceskaexpedice.akubra.RepositoryNamespaces;
import org.ceskaexpedice.akubra.pid.LexerException;
import org.ceskaexpedice.akubra.pid.PIDParser;
import org.ceskaexpedice.akubra.relsext.KnownRelations;
import org.ceskaexpedice.akubra.relsext.RelsExtHelper;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.ceskaexpedice.akubra.utils.StringUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.xpath.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Utility class to handle various RELS EXT related tasks
 */
public final class RelsExtInternalDomUtils {
    static final Logger LOGGER = Logger.getLogger(RelsExtInternalDomUtils.class.getName());

    private RelsExtInternalDomUtils() {
    }

    public static boolean relationsExists(Document relsExt, String relation, String namespace) {
        try {
            Element foundElement = DomUtils.findElement(relsExt.getDocumentElement(), (element) -> {
                String elmNamespace = element.getNamespaceURI();
                String elmName = element.getLocalName();
                return (elmName.equals(relation) && namespace.equals(elmNamespace));
            });
            return foundElement != null;
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    public static String getResourcePid(Document relsExt, String localName, String namespace, boolean appendPrefix) {
        try {
            Element foundElement = DomUtils.findElement(relsExt.getDocumentElement(), localName, namespace);
            if (foundElement != null) {
                String sform = foundElement.getAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource");
                PIDParser pidParser = new PIDParser(sform);
                pidParser.disseminationURI();
                String pidVolume = pidParser.getObjectId();
                if (appendPrefix) {
                    pidVolume = "uuid:" + pidVolume;
                }
                return pidVolume;
            } else {
                return "";
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RepositoryException(e);
        }
    }

    public static String getElementValue(Document relsExt, String xpathExpression) {
        try {
            XPathFactory xpfactory = XPathFactory.newInstance();
            XPath xpath = xpfactory.newXPath();
            xpath.setNamespaceContext(new RepositoryNamespaceContext());
            XPathExpression expr = xpath.compile(xpathExpression);
            Object tiles = expr.evaluate(relsExt.getDocumentElement(), XPathConstants.NODE);
            if (tiles != null) {
                String data = ((Text) tiles).getData();
                return data != null ? data.trim() : null;
            } else return null;
        } catch (XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    /* TODO
    public static String getModel(Element el) {
        //<hasModel xmlns="
        try {
            Element foundElement = DomUtils.findElement(el, "hasModel", RepositoryNamespaces.FEDORA_MODELS_URI);
            if (foundElement != null) {
                String sform = foundElement.getAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource");
                PIDParser pidParser = new PIDParser(sform);
                pidParser.disseminationURI();
                return pidParser.getObjectId();
            } else {
                throw new IllegalArgumentException("cannot find model of given document");
            }
        } catch (LexerException e) {
            throw new RepositoryException(e);
        }
    }

     */

    /* TODO
    public static String getFirstVolumePid(InputStream relsExt) {
        try {
            Document document = DomUtils.streamToDocument(relsExt);
            Element foundElement = DomUtils.findElement(document.getDocumentElement(), "hasVolume", RepositoryNamespaces.KRAMERIUS_URI);
            if (foundElement != null) {
                String sform = foundElement.getAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource");
                PIDParser pidParser = new PIDParser(sform);
                pidParser.disseminationURI();
                String pidVolume = "uuid:" + pidParser.getObjectId();
                return pidVolume;
            } else {
                return "";
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RepositoryException(e);
        }
    }

     */

    /* TODO
    static String getFirstItemPid(InputStream relsExt) {
        try {
            Document document = DomUtils.streamToDocument(relsExt);
            Element foundElement = DomUtils.findElement(document.getDocumentElement(), "hasItem", RepositoryNamespaces.KRAMERIUS_URI);
            if (foundElement != null) {
                String sform = foundElement.getAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource");
                PIDParser pidParser = new PIDParser(sform);
                pidParser.disseminationURI();
                String pidItem = "uuid:" + pidParser.getObjectId();
                return pidItem;
            } else {
                return "";
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

     */

    /* TODO AK_NEW
     public static List<Pair<String, String>> getRelations(Document relsExt) {
        List<Pair<String, String>> pairs = new ArrayList<>();
        List<String> names = Arrays.stream(KnownRelations.values()).map(KnownRelations::toString).collect(Collectors.toList());
        List<Element> elms = DomUtils.getElementsRecursive(relsExt.getDocumentElement(), new DomUtils.ElementsFilter() {

            @Override
            public boolean acceptElement(Element element) {
                String namespaceUri = element.getNamespaceURI();
                if (namespaceUri.equals(RepositoryNamespaces.KRAMERIUS_URI)) {
                    String nodeName = element.getLocalName();
                    return names.contains(nodeName);
                }
                return false;
            }

        });

        elms.stream().forEach(elm -> {
            try {
                String attrVal = elm.getAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource");
                PIDParser pidParser = new PIDParser(attrVal);
                pidParser.disseminationURI();
                String objectPid = pidParser.getObjectPid();
                pairs.add(Pair.of(elm.getLocalName(), objectPid));
            } catch (DOMException | LexerException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        });

        return pairs;
    }*/

    public static List<Triple<String, String, String>> getRelations(Document relsExt, String namespace) {
        try {
            List<Triple<String, String, String>> retvals = DomUtils.getElementsRecursive(relsExt.getDocumentElement(), (element) -> {
                String elmNamespace = element.getNamespaceURI();
                if (namespace != null) {
                    return namespace.equals(elmNamespace) && element.hasAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource");
                } else {
                    return element.hasAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource");
                }
            }).stream().map((elm) -> {
                String resource = elm.getAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource");
                if (resource.startsWith(PIDParser.INFO_FEDORA_PREFIX)) {
                    resource = resource.substring(PIDParser.INFO_FEDORA_PREFIX.length());
                }

                Triple<String, String, String> triple = new ImmutableTriple<>(elm.getNamespaceURI(), elm.getLocalName(), resource);
                return triple;
            }).collect(Collectors.toList());
            Collections.reverse(retvals);
            return retvals;
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    public static List<Triple<String, String, String>> getLiterals(Document relsExt, String namespace) {
        try {
            List<Triple<String, String, String>> retvals = DomUtils.getElementsRecursive(relsExt.getDocumentElement(), (element) -> {
                String elmNamespace = element.getNamespaceURI();
                if (namespace != null) {
                    return namespace.equals(elmNamespace) && !element.hasAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource") && StringUtils.isAnyString(element.getTextContent());
                } else {
                    return !element.hasAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource") && StringUtils.isAnyString(element.getTextContent());
                }
            }).stream().filter((elm) -> {
                return !elm.getLocalName().equals(RelsExtHelper.RDF_ELEMENT) && !elm.getLocalName().equals(RelsExtHelper.RDF_DESCRIPTION_ELEMENT);
            }).map((elm) -> {
                String content = elm.getTextContent();
                Triple<String, String, String> triple = new ImmutableTriple<>(elm.getNamespaceURI(), elm.getLocalName(), content);
                return triple;
            }).collect(Collectors.toList());

            Collections.reverse(retvals);
            return retvals;
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }


}
