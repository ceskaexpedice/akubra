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
package org.ceskaexpedice.akubra.impl.utils;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.ceskaexpedice.akubra.*;
import org.ceskaexpedice.akubra.relsext.KnownRelations;
import org.ceskaexpedice.akubra.relsext.RelsExtHandler;
import org.ceskaexpedice.akubra.pid.LexerException;
import org.ceskaexpedice.akubra.pid.PIDParser;
import org.ceskaexpedice.akubra.relsext.TreeNodeProcessStackAware;
import org.ceskaexpedice.akubra.relsext.TreeNodeProcessor;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.w3c.dom.*;

import javax.xml.xpath.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Utility class to handle various RELS EXT related tasks
 */
public final class RelsExtInternalUtils {
    static final Logger LOGGER = Logger.getLogger(RelsExtInternalUtils.class.getName());

    private RelsExtInternalUtils() {
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

    public static List<String> getPidsFromTree(String pid, AkubraRepository akubraRepository) {
        final List<String> retval = new ArrayList<>();
        processInTree(pid, new TreeNodeProcessor() {
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
        return retval;
    }

    public static String findFirstViewablePidFromTree(String pid, AkubraRepository akubraRepository) {
        final List<String> foundPids = new ArrayList<String>();
        processInTree(pid, new TreeNodeProcessor() {
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
            public void process(String pid, int level) {
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
                    throw new RepositoryException(e);
                }
            }
        }, akubraRepository);

        return foundPids.isEmpty() ? null : foundPids.get(0);
    }

    public static void processInTree(String pid, TreeNodeProcessor processor, AkubraRepository akubraRepository) {
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
                processInTreeInternal(pid, relsExt, processor, 0, new Stack<String>(), akubraRepository, factory);
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

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
    }

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
                return !elm.getLocalName().equals(RelsExtHandler.RDF_ELEMENT) && !elm.getLocalName().equals(RelsExtHandler.RDF_DESCRIPTION_ELEMENT);
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

    private static boolean processInTreeInternal(String pid, Document relsExt, TreeNodeProcessor processor, int level,
                                                 Stack<String> pidStack, AkubraRepository akubraRepository, XPathFactory xPathFactory)
            throws XPathExpressionException, LexerException {
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
                                    iterationgRelsExt = akubraRepository.re().get(objectId).asDom(false);
                                } catch (Exception ex) {
                                    LOGGER.warning("could not read RELS-EXT, skipping branch [" + (level + 1)
                                            + "] and pid (" + objectId + "):" + ex);
                                }
                                breakProcessing = processInTreeInternal(pidParser.getObjectPid(), iterationgRelsExt,
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

}
