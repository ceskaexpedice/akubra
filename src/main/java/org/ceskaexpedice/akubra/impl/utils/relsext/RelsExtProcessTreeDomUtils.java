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

import org.ceskaexpedice.akubra.*;
import org.ceskaexpedice.akubra.pid.LexerException;
import org.ceskaexpedice.akubra.pid.PIDParser;
import org.ceskaexpedice.akubra.relsext.TreeNodeProcessStackAware;
import org.ceskaexpedice.akubra.relsext.TreeNodeProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class RelsExtProcessTreeDomUtils {
    static final Logger LOGGER = Logger.getLogger(RelsExtProcessTreeDomUtils.class.getName());

    private RelsExtProcessTreeDomUtils() {
    }

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
                    relsExt = akubraRepository.re().get(pid).asDom(true);
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
                                    iterationgRelsExt = akubraRepository.re().get(objectId).asDom(true);
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
