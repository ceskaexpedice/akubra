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
package org.ceskaexpedice.akubra.utils;

import org.apache.commons.lang3.tuple.Pair;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.core.repository.*;
import org.ceskaexpedice.akubra.utils.pid.LexerException;
import org.ceskaexpedice.akubra.utils.pid.PIDParser;
import org.w3c.dom.*;

import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class RelsExtUtils {

    public static final Logger LOGGER = Logger.getLogger(RelsExtUtils.class.getName());


    private RelsExtUtils() {}
    
    
    public static Element getRELSEXTFromGivenFOXML(Element foxmlElement) {
        List<Element> elms = DomUtils.getElementsRecursive(foxmlElement, (elm)->{
            if (elm.getLocalName().equals("datastream")) {
                String id = elm.getAttribute("ID");
                return id.equals(KnownDatastreams.RELS_EXT.toString());
            } return false;
        });
        if (elms.size() == 1) {
            return elms.get(0);
        } else return null;
    }

    /** Get model 
     * @throws LexerException */
    public static String getModel(Element relsExt) throws XPathExpressionException, LexerException {
        //<hasModel xmlns="
        Element foundElement = DomUtils.findElement(relsExt, "hasModel", RepositoryNamespaces.FEDORA_MODELS_URI);
        if (foundElement != null) {
            String sform = foundElement.getAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource");
            PIDParser pidParser = new PIDParser(sform);
            pidParser.disseminationURI();
            return pidParser.getObjectId();
        } else {
            throw new IllegalArgumentException("cannot find model of given document");
        }
    }
    
    public static Element getRDFDescriptionElement(Element relsExt) throws XPathExpressionException, LexerException {
        Element foundElement = DomUtils.findElement(relsExt, "Description", RepositoryNamespaces.RDF_NAMESPACE_URI);
        return foundElement;
    }

    /** Returns replicatedFrom url from given  RELS-EXT element*/
    public static String getReplicatedFromUrl(String uuid, AkubraRepository akubraRepository) {
        InputStream inputStream = akubraRepository.getDatastreamContent(uuid, KnownDatastreams.RELS_EXT.toString());
        Document relsExt = DomUtils.streamToDocument(inputStream);
        return getReplicatedFromUrl(relsExt);
    }

    /** Returns replicatedFrom url from given  RELS-EXT element */
    private static String getReplicatedFromUrl(Document relsExt) {
        try {
            XPathFactory xpfactory = XPathFactory.newInstance();
            XPath xpath = xpfactory.newXPath();
            xpath.setNamespaceContext(new RepositoryNamespaceContext());
            XPathExpression expr = xpath.compile("//kramerius:replicatedFrom/text()");
            Object tiles = expr.evaluate(relsExt, XPathConstants.NODE);
            if (tiles != null) return ((Text) tiles).getData();
            else return null;
        } catch (XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    /** Returns replicatedFrom url from given  RELS-EXT element */
    public static String getRelsExtTilesUrl(String uuid, AkubraRepository akubraRepository) {
        InputStream inputStream = akubraRepository.getDatastreamContent(uuid, KnownDatastreams.RELS_EXT.toString());
        Document relsExt = DomUtils.streamToDocument(inputStream);
        return getRelsExtTilesUrl(relsExt.getDocumentElement());
    }

    /** Returns tiles url  from given RELS-EXT element */
    public static String getRelsExtTilesUrl(Element reslExtDoc) {
        try {
            XPathFactory xpfactory = XPathFactory.newInstance();
            XPath xpath = xpfactory.newXPath();
            xpath.setNamespaceContext(new RepositoryNamespaceContext());
            XPathExpression expr = xpath.compile("//kramerius:tiles-url/text()");
            Object tiles = expr.evaluate(reslExtDoc, XPathConstants.NODE);
            if (tiles != null) {
                String data = ((Text) tiles).getData();
                return data != null ? data.trim() : null;
            }
            else return null;
        } catch (XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    public static String getRelsExtTilesUrl(Document reslExtDoc) {
        return getRelsExtTilesUrl(reslExtDoc.getDocumentElement());
    }

    public static String getDonator(Document reslExtDoc) {
        return getDonator(reslExtDoc.getDocumentElement());
    }
    
    /** Returns donator label  from given RELS-EXT element */
    public static String getDonator(Element reslExtDoc) {
        try {
            XPathFactory xpfactory = XPathFactory.newInstance();
            XPath xpath = xpfactory.newXPath();
            xpath.setNamespaceContext(new RepositoryNamespaceContext());
            XPathExpression expr = xpath.compile("//kramerius:hasDonator");
            Object donator = expr.evaluate(reslExtDoc, XPathConstants.NODE);
            if (donator != null) {
                Element elm =  (Element) donator;
                Attr ref = elm.getAttributeNodeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource");
                if (ref != null) {
                    try {
                        PIDParser pidParser = new PIDParser(ref.getValue());
                        pidParser.disseminationURI();
                        return pidParser.getObjectPid();
                    } catch (LexerException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                        return null;
                    }
                } else return null;
            } else return null;
        } catch (XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    public static final String CACHE_RELS_EXT_LITERAL = "kramerius4://deepZoomCache";
    
    
    public static List<String> getLicenses(Element relsExt) {
        List<Element> elms = DomUtils.getElementsRecursive(relsExt, (elm)->{
            return (elm.getLocalName().equals("license"));
        });
        List<String> collect = elms.stream().map(Element::getTextContent).collect(Collectors.toList());
        return collect;
    }    

    public static List<String> getContainsLicenses(Element relsExt) {
        List<Element> elms = DomUtils.getElementsRecursive(relsExt, (elm)->{
            return (elm.getLocalName().equals("containsLicense"));
        });
        List<String> collect = elms.stream().map(Element::getTextContent).collect(Collectors.toList());
        return collect;
    }    

    
    public static List<Pair<String,String>> getRelations(Element relsExt) {
        List<Pair<String,String>> pairs = new ArrayList<>();
        List<String> names = Arrays.stream(KnownRelations.values()).map(KnownRelations::toString).collect(Collectors.toList());
        List<Element> elms = DomUtils.getElementsRecursive(relsExt,  new DomUtils.ElementsFilter() {

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
        
        
        elms.stream().forEach(elm-> {
          try {
            String attrVal = elm.getAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource");
              PIDParser pidParser = new PIDParser(attrVal);
              pidParser.disseminationURI();
              String objectPid = pidParser.getObjectPid();
              pairs.add(Pair.of(elm.getLocalName(), objectPid));
            } catch (DOMException | LexerException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }
        });
        
        return pairs;
    }
    
    

    public synchronized static void addRDFLiteral(Element relsExt, String license, String elmName)
            throws XPathExpressionException, LexerException {
        Element rdfDescriptionElement = getRDFDescriptionElement(relsExt);
        if (rdfDescriptionElement != null) {
            Document document = rdfDescriptionElement.getOwnerDocument();
            Element containsLicense = document.createElementNS(RepositoryNamespaces.ONTOLOGY_RELATIONSHIP_NAMESPACE_URI, elmName);
            containsLicense.setTextContent(license);
            rdfDescriptionElement.appendChild(containsLicense);
        }
    }
    

}
