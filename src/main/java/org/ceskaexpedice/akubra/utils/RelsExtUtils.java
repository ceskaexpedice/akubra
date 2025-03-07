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

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.ceskaexpedice.akubra.*;
import org.ceskaexpedice.akubra.relsext.KnownRelations;
import org.ceskaexpedice.akubra.utils.pid.LexerException;
import org.ceskaexpedice.akubra.utils.pid.PIDParser;
import org.w3c.dom.*;

import javax.xml.xpath.*;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Utility class to handle various RELS EXT related tasks
 */
public final class RelsExtUtils {
    public static final Logger LOGGER = Logger.getLogger(RelsExtUtils.class.getName());
    public static final String CACHE_RELS_EXT_LITERAL = "kramerius4://deepZoomCache";
    private static final String RDF_DESCRIPTION_ELEMENT = "Description";
    private static final String RDF_ELEMENT = "RDF";

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

    public static String getModel(InputStream doc) {
        return getModel(DomUtils.streamToDocument(doc).getDocumentElement());
    }

    /** Get model
     * @throws LexerException */
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

    public static String getFirstItemPid(InputStream relsExt) {
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

    public static Element getRDFDescriptionElement(Element element) {
        Element foundElement = DomUtils.findElement(element, "Description", RepositoryNamespaces.RDF_NAMESPACE_URI);
        return foundElement;
    }

    public static String getRelsExtTilesUrl(Document document) {
        try {
            XPathFactory xpfactory = XPathFactory.newInstance();
            XPath xpath = xpfactory.newXPath();
            xpath.setNamespaceContext(new RepositoryNamespaceContext());
            XPathExpression expr = xpath.compile("//kramerius:tiles-url/text()");
            Object tiles = expr.evaluate(document.getDocumentElement(), XPathConstants.NODE);
            if (tiles != null) {
                String data = ((Text) tiles).getData();
                return data != null ? data.trim() : null;
            } else return null;
        } catch (XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }
        /** Returns tiles url  from given RELS-EXT element */
    public static String getRelsExtTilesUrl(InputStream documentStream) {
        Document document = DomUtils.streamToDocument(documentStream);
        return getRelsExtTilesUrl(document);
    }

    public static List<String> getLicenses(Document document) {
        return getLicenses(document.getDocumentElement());
    }

    public static List<String> getLicenses(Element document) {
        List<Element> elms = DomUtils.getElementsRecursive(document, (elm) -> {
            return (elm.getLocalName().equals("license"));
        });
        List<String> collect = elms.stream().map(Element::getTextContent).collect(Collectors.toList());
        return collect;
    }

    public static List<String> getContainsLicenses(Document document) {
        return getContainsLicenses(document.getDocumentElement());
    }

    public static List<String> getContainsLicenses(Element document) {
        List<Element> elms = DomUtils.getElementsRecursive(document, (elm) -> {
            return (elm.getLocalName().equals("containsLicense"));
        });
        List<String> collect = elms.stream().map(Element::getTextContent).collect(Collectors.toList());
        return collect;
    }

    public static List<Pair<String, String>> getRelations(InputStream documentS) {
        Document document = DomUtils.streamToDocument(documentS);
        List<Pair<String, String>> pairs = new ArrayList<>();
        List<String> names = Arrays.stream(KnownRelations.values()).map(KnownRelations::toString).collect(Collectors.toList());
        List<Element> elms = DomUtils.getElementsRecursive(document.getDocumentElement(), new DomUtils.ElementsFilter() {

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
    }

    public synchronized static void addRDFLiteral(Element relsExt, String license, String elmName){
        Element rdfDescriptionElement = getRDFDescriptionElement(relsExt);
        if (rdfDescriptionElement != null) {
            Document document = rdfDescriptionElement.getOwnerDocument();
            Element containsLicense = document.createElementNS(RepositoryNamespaces.ONTOLOGY_RELATIONSHIP_NAMESPACE_URI, elmName);
            containsLicense.setTextContent(license);
            rdfDescriptionElement.appendChild(containsLicense);
        }
    }

    public static void processSubtree(String pid, TreeNodeProcessor processor, AkubraRepository akubraRepository) throws ProcessSubtreeException {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            Document relsExt = null;
            try {
                // should be from
                if (akubraRepository.re().exists(pid)) {
                    relsExt = akubraRepository.re().get(pid).asDom(false);
                } else {
                    LOGGER.warning("could not read root RELS-EXT, skipping object  (" + pid + ")");
                }
            } catch (Exception ex) {
                LOGGER.warning("could not read root RELS-EXT, skipping object  (" + pid + "):" + ex);
            }
            if (!processor.skipBranch(pid, 0)) {
                processSubtreeInternal(pid, relsExt, processor, 0, new Stack<String>(), akubraRepository, factory);
            }
        } catch (Exception e) {
            throw new ProcessSubtreeException(e);
        }
    }

    private static boolean processSubtreeInternal(String pid, Document relsExt, TreeNodeProcessor processor, int level,
                                                  Stack<String> pidStack, AkubraRepository akubraRepository, XPathFactory xPathFactory)
            throws XPathExpressionException, LexerException, ProcessSubtreeException {
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

    public static List<String> getPids(String pid, AkubraRepository akubraRepository) {
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
            throw new RepositoryException(e);
        }
        return retval;
    }

    public static String findFirstViewablePid(String pid, AkubraRepository akubraRepository) {
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
                            if (akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_FULL)) {
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
            throw new RepositoryException(e);
        }

        return foundPids.isEmpty() ? null : foundPids.get(0);
    }

    public static List<Triple<String, String, String>> relsExtGetRelations(Document metadata, String namespace) {
        try {
            List<Triple<String, String, String>> retvals = DomUtils.getElementsRecursive(metadata.getDocumentElement(), (element) -> {
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

    public static List<Triple<String, String, String>> relsExtGetLiterals(Document metadata, String namespace) {
        try {
            List<Triple<String, String, String>> retvals = DomUtils.getElementsRecursive(metadata.getDocumentElement(), (element) -> {
                String elmNamespace = element.getNamespaceURI();
                if (namespace != null) {
                    return namespace.equals(elmNamespace) && !element.hasAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource") && StringUtils.isAnyString(element.getTextContent());
                } else {
                    return !element.hasAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource") && StringUtils.isAnyString(element.getTextContent());
                }
            }).stream().filter((elm) -> {
                return !elm.getLocalName().equals(RDF_ELEMENT) && !elm.getLocalName().equals(RDF_DESCRIPTION_ELEMENT);
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

    public static boolean relsExtRelationsExists(Document metadata, String relation, String namespace) {
        try {
            Element foundElement = DomUtils.findElement(metadata.getDocumentElement(), (element) -> {
                String elmNamespace = element.getNamespaceURI();
                String elmName = element.getLocalName();
                return (elmName.equals(relation) && namespace.equals(elmNamespace));
            });
            return foundElement != null;
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

}
