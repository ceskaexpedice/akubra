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
import java.util.Stack;
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
    public static String getModel(Element relsExt) throws LexerException {
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

    public static String getModelName(String pid, AkubraRepository akubraRepository) {
        Document document = akubraRepository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT).asDom(false);
        return getModelName(document);
    }

    public static String getModelName(Document relsExt) {
        try {
            //TODO: Duplicate code in RelsExt helper -> mn
            Element foundElement = DomUtils.findElement(relsExt.getDocumentElement(), "hasModel", RepositoryNamespaces.FEDORA_MODELS_URI);
            if (foundElement != null) {
                String sform = foundElement.getAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource");
                PIDParser pidParser = new PIDParser(sform);
                pidParser.disseminationURI();
                return pidParser.getObjectId();
            } else {
                throw new IllegalArgumentException("cannot find model of given document");
            }
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        }
    }

    public static String getFirstVolumePid(Document relsExt) throws IOException {
        try {
            Element foundElement = DomUtils.findElement(relsExt.getDocumentElement(), "hasVolume", RepositoryNamespaces.KRAMERIUS_URI);
            if (foundElement != null) {
                String sform = foundElement.getAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource");
                PIDParser pidParser = new PIDParser(sform);
                pidParser.disseminationURI();
                String pidVolume = "uuid:" + pidParser.getObjectId();
                return pidVolume;
            } else {
                return "";
            }
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        }
    }

    public static String getFirstVolumePid(String pid, AkubraRepository akubraRepository) throws IOException {
        Document doc = akubraRepository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT).asDom(false);
        return getFirstVolumePid(doc);
    }

    public static String getFirstItemPid(Document relsExt) throws IOException {
        try {
            Element foundElement = DomUtils.findElement(relsExt.getDocumentElement(), "hasItem", RepositoryNamespaces.KRAMERIUS_URI);
            if (foundElement != null) {
                String sform = foundElement.getAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource");
                PIDParser pidParser = new PIDParser(sform);
                pidParser.disseminationURI();
                String pidItem = "uuid:" + pidParser.getObjectId();
                return pidItem;
            } else {
                return "";
            }
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        }
    }

    public static String getFirstItemPid(String pid, AkubraRepository akubraRepository) throws IOException {
        Document doc = akubraRepository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT).asDom(false);
        return getFirstItemPid(doc);
    }

    public static Element getRDFDescriptionElement(Element relsExt){
        Element foundElement = DomUtils.findElement(relsExt, "Description", RepositoryNamespaces.RDF_NAMESPACE_URI);
        return foundElement;
    }

    /** Returns replicatedFrom url from given  RELS-EXT element*/
    public static String getReplicatedFromUrl(String uuid, AkubraRepository akubraRepository) {
        Document relsExt = akubraRepository.getDatastreamContent(uuid, KnownDatastreams.RELS_EXT).asDom(false);
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
        Document relsExt = akubraRepository.getDatastreamContent(uuid, KnownDatastreams.RELS_EXT).asDom(false);
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

    public static void processSubtree(String pid, TreeNodeProcessor processor, AkubraRepository akubraRepository) throws ProcessSubtreeException, IOException {
        try {
            XPathFactory factory = XPathFactory.newInstance(); // TODO AK_NEW is it ok to create
            Document relsExt = null;
            try {
                // should be from
                if (akubraRepository.datastreamExists(pid, KnownDatastreams.RELS_EXT.toString())) {
                    relsExt = akubraRepository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT).asDom(false);
                } else {
                    LOGGER.warning("could not read root RELS-EXT, skipping object  (" + pid + ")");
                }
            } catch (Exception ex) {
                LOGGER.warning("could not read root RELS-EXT, skipping object  (" + pid + "):" + ex);
            }
            if (!processor.skipBranch(pid, 0)) {
                processSubtreeInternal(pid, relsExt, processor, 0, new Stack<String>(), akubraRepository, factory);
            }
        } catch (LexerException e) {
            LOGGER.warning("Error in pid: " + pid);
            throw new ProcessSubtreeException(e);
        } catch (XPathExpressionException e) {
            throw new ProcessSubtreeException(e);
        }
    }

    private static boolean processSubtreeInternal(String pid, Document relsExt, TreeNodeProcessor processor, int level,
                                             Stack<String> pidStack, AkubraRepository akubraRepository, XPathFactory xPathFactory)
            throws XPathExpressionException, LexerException, IOException, ProcessSubtreeException {
        processor.process(pid, level);
        boolean breakProcessing = processor.breakProcessing(pid, level);
        if (breakProcessing) {
            return breakProcessing;
        }
        if (relsExt == null) {
            return false;
        }
        XPath xpath = xPathFactory.newXPath();
        xpath.setNamespaceContext(new RepositoryNamespaceContext());
        XPathExpression expr = xpath.compile("/rdf:RDF/rdf:Description/*");
        NodeList nodes = (NodeList) expr.evaluate(relsExt, XPathConstants.NODESET);

        if (pidStack.contains(pid)) {
            LOGGER.log(Level.WARNING, "Cyclic reference on " + pid);
            return breakProcessing;
        }
        pidStack.push(pid);
        changeStack(processor, pidStack);
        for (int i = 0, ll = nodes.getLength(); i < ll; i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element iteratingElm = (Element) node;
                String namespaceURI = iteratingElm.getNamespaceURI();
                if (namespaceURI != null && (namespaceURI.equals(RepositoryNamespaces.ONTOLOGY_RELATIONSHIP_NAMESPACE_URI)
                        || namespaceURI.equals(RepositoryNamespaces.RDF_NAMESPACE_URI))) {
                    String attVal = iteratingElm.getAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource");
                    if (!attVal.trim().equals("")) {
                        PIDParser pidParser = new PIDParser(attVal);
                        pidParser.disseminationURI();
                        String objectId = pidParser.getObjectPid();
                        if (pidParser.getNamespaceId().equals("uuid")) {
                            if (!processor.skipBranch(objectId, level + 1)) {
                                Document iterationgRelsExt = null;

                                try {
                                    iterationgRelsExt = akubraRepository.getDatastreamContent(objectId, KnownDatastreams.RELS_EXT).asDom(false);
                                } catch (Exception ex) {
                                    LOGGER.warning("could not read RELS-EXT, skipping branch [" + (level + 1)
                                            + "] and pid (" + objectId + "):" + ex);
                                }
                                breakProcessing = processSubtreeInternal(pidParser.getObjectPid(), iterationgRelsExt,
                                        processor, level + 1, pidStack, akubraRepository, xPathFactory);

                                if (breakProcessing) {
                                    break;
                                }
                            } else {
                                LOGGER.fine("skipping branch [" + (level + 1) + "] and pid (" + objectId + ")");
                            }
                        }
                    }

                }
            }
        }
        pidStack.pop();
        changeStack(processor, pidStack);
        return breakProcessing;
    }

    private static void changeStack(TreeNodeProcessor processor, Stack<String> pidStack) {
        if (processor instanceof TreeNodeProcessStackAware) {
            TreeNodeProcessStackAware stackAware = (TreeNodeProcessStackAware) processor;
            stackAware.changeProcessingStack(pidStack);
        }
    }

    public static List<String> getPids(String pid, AkubraRepository akubraRepository) throws IOException {
        final List<String> retval = new ArrayList<>();
        try {
            processSubtree(pid, new TreeNodeProcessor() {
                @Override
                public void process(String pid, int level) {
                    retval.add(pid);
                }

                @Override
                public boolean breakProcessing(String pid, int level) {
                    return false;
                }

                @Override
                public boolean skipBranch(String pid, int level) {
                    return false;
                }
            }, akubraRepository);
        } catch (ProcessSubtreeException e) {
            throw new IOException(e);
        }
        return retval;
    }

    public static String findFirstViewablePid(String pid, AkubraRepository akubraRepository) throws IOException {
        final List<String> foundPids = new ArrayList<String>();
        try {
            processSubtree(pid, new TreeNodeProcessor() {
                boolean breakProcess = false;
                int previousLevel = 0;

                @Override
                public boolean breakProcessing(String pid, int level) {
                    return breakProcess;
                }

                @Override
                public boolean skipBranch(String pid, int level) {
                    return false;
                }

                @Override
                public void process(String pid, int level) throws ProcessSubtreeException {
                    try {
                        if (previousLevel < level || level == 0) {
                            if (akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_FULL.toString())) {
                                foundPids.add(pid);
                                breakProcess = true;
                            }
                        } else if (previousLevel > level) {
                            breakProcess = true;
                        } else if ((previousLevel == level) && (level != 0)) {
                            breakProcess = true;
                        }
                        previousLevel = level;
                    } catch (Exception e) {
                        throw new ProcessSubtreeException(e);
                    }
                }
            }, akubraRepository);
        } catch (ProcessSubtreeException e) {
            throw new IOException(e);
        }

        return foundPids.isEmpty() ? null : foundPids.get(0);
    }

}
