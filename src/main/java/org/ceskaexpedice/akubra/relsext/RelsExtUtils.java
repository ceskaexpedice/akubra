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
package org.ceskaexpedice.akubra.relsext;

import org.ceskaexpedice.akubra.*;
import org.ceskaexpedice.akubra.pid.LexerException;
import org.ceskaexpedice.akubra.pid.PIDParser;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.w3c.dom.*;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Utility class to handle various RELS EXT related tasks
 */
public final class RelsExtUtils {
    static final Logger LOGGER = Logger.getLogger(RelsExtUtils.class.getName());

    private RelsExtUtils() {
    }

    public static Element getRELSEXTFromGivenFOXML(Document document) {
        List<Element> elms = DomUtils.getElementsRecursive(document.getDocumentElement(), (elm) -> {
            if (elm.getLocalName().equals("datastream")) {
                String id = elm.getAttribute("ID");
                return id.equals(KnownDatastreams.RELS_EXT.toString());
            }
            return false;
        });
        if (elms.size() == 1) {
            return elms.get(0);
        } else return null;
    }

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

    public static String getTilesUrl(Element relsExt) {
        try {
            XPathFactory xpfactory = XPathFactory.newInstance();
            XPath xpath = xpfactory.newXPath();
            xpath.setNamespaceContext(new RepositoryNamespaceContext());
            XPathExpression expr = xpath.compile("//kramerius:tiles-url/text()");
            Object tiles = expr.evaluate(relsExt, XPathConstants.NODE);
            if (tiles != null) {
                String data = ((Text) tiles).getData();
                return data != null ? data.trim() : null;
            } else return null;
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    static Element getRDFDescriptionElement(Element element) {
        Element foundElement = DomUtils.findElement(element, "Description", RepositoryNamespaces.RDF_NAMESPACE_URI);
        return foundElement;
    }

    public static List<String> getLicenses(Element document) {
        List<Element> elms = DomUtils.getElementsRecursive(document, (elm) -> {
            return (elm.getLocalName().equals("license"));
        });
        List<String> collect = elms.stream().map(Element::getTextContent).collect(Collectors.toList());
        return collect;
    }

    public static List<String> getContainsLicenses(Element document) {
        List<Element> elms = DomUtils.getElementsRecursive(document, (elm) -> {
            return (elm.getLocalName().equals("containsLicense"));
        });
        List<String> collect = elms.stream().map(Element::getTextContent).collect(Collectors.toList());
        return collect;
    }

    public static void addRDFLiteral(Element relsExt, String license, String elmName) {
        Element rdfDescriptionElement = getRDFDescriptionElement(relsExt);
        if (rdfDescriptionElement != null) {
            Document document = rdfDescriptionElement.getOwnerDocument();
            Element containsLicense = document.createElementNS(RepositoryNamespaces.ONTOLOGY_RELATIONSHIP_NAMESPACE_URI, elmName);
            containsLicense.setTextContent(license);
            rdfDescriptionElement.appendChild(containsLicense);
        }
    }

}
