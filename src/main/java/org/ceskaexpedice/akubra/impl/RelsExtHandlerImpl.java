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
package org.ceskaexpedice.akubra.impl;

import org.ceskaexpedice.akubra.*;
import org.ceskaexpedice.akubra.relsext.RelsExtHandler;
import org.ceskaexpedice.akubra.relsext.RelsExtWrapper;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.ceskaexpedice.akubra.utils.ProcessSubtreeException;
import org.ceskaexpedice.akubra.utils.RelsExtUtils;
import org.ceskaexpedice.akubra.utils.TreeNodeProcessor;
import org.ceskaexpedice.akubra.utils.pid.PIDParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Logger;

/**
 * AkubraRepositoryImpl
 */
public class RelsExtHandlerImpl implements RelsExtHandler {
    private static final String RDF_DESCRIPTION_ELEMENT = "Description";
    private static final String RDF_ELEMENT = "RDF";
    private static final Logger LOGGER = Logger.getLogger(RelsExtHandlerImpl.class.getName());

    private AkubraRepository akubraRepository;

    public RelsExtHandlerImpl(AkubraRepository akubraRepository) {
        this.akubraRepository = akubraRepository;
    }

    @Override
    public RelsExtWrapper get(String pid) {
        DatastreamContentWrapper datastreamContent = akubraRepository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT);
        if(datastreamContent == null) {
            return null;
        }
        return new RelsExtWrapperImpl(datastreamContent);
    }

    @Override
    public boolean exists(String pid) {
        return akubraRepository.datastreamExists(pid, KnownDatastreams.RELS_EXT);
    }

    @Override
    public void update(String pid, InputStream binaryContent) {
        akubraRepository.updateXMLDatastream(pid, KnownDatastreams.RELS_EXT, "text/xml", binaryContent);
    }

    @Override
    public boolean relationExists(String pid, String relation, String namespace) {
        DatastreamContentWrapper datastreamContent = akubraRepository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT);
        if(datastreamContent == null) {
            return false;
        }
        return RelsExtUtils.relsExtRelationsExists(datastreamContent.asDom(true), relation, namespace);
    }

    @Override
    public void addRelation(String pid, String relation, String namespace, String targetRelation) {
        try {
            DatastreamContentWrapper datastreamContent = akubraRepository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT);
            if(datastreamContent == null) {
                // TODO log
                return;
            }
            Document document = datastreamContent.asDom(true);
            Element rdfDesc = DomUtils.findElement(document.getDocumentElement(), RDF_DESCRIPTION_ELEMENT, RepositoryNamespaces.RDF_NAMESPACE_URI);
            Element subElm = document.createElementNS(namespace, relation);
            subElm.setAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "rdf:resource", targetRelation);
            rdfDesc.appendChild(subElm);
            changeRelations(pid, document);
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void removeRelation(String pid, String relation, String namespace, String targetRelation) {
        try {
            DatastreamContentWrapper datastreamContent = akubraRepository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT);
            if(datastreamContent == null) {
                // TODO log
                return;
            }
            Document document = datastreamContent.asDom(true);
            final String targetPID = targetRelation.startsWith(PIDParser.INFO_FEDORA_PREFIX) ? targetRelation : PIDParser.INFO_FEDORA_PREFIX + targetRelation;
            Element relationElement = DomUtils.findElement(document.getDocumentElement(), (element) -> {
                String elmNamespace = element.getNamespaceURI();
                String elmLocalname = element.getLocalName();
                String elmResourceAttribute = element.getAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource");
                return (elmNamespace.equals(namespace)) && (elmLocalname.equals(relation)) && elmResourceAttribute.equals(targetPID);
            });
            if (relationElement != null) {
                relationElement.getParentNode().removeChild(relationElement);
                changeRelations(pid, document);
            } else {
                LOGGER.warning("Cannot find relation '" + namespace + relation);
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void removeRelationsByNameAndNamespace(String pid, String relation, String namespace) {
        try {
            DatastreamContentWrapper datastreamContent = akubraRepository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT);
            if(datastreamContent == null) {
                // TODO log
                return;
            }
            Document document = datastreamContent.asDom(true);
            List<Element> relationElements = DomUtils.getElementsRecursive(document.getDocumentElement(), (element) -> {
                String elmNamespace = element.getNamespaceURI();
                String elmLocalname = element.getLocalName();
                return (elmNamespace.equals(namespace)) && (elmLocalname.equals(relation));
            });
            if (!relationElements.isEmpty()) {
                relationElements.stream().forEach((elm) -> {
                    elm.getParentNode().removeChild(elm);
                });
                changeRelations(pid, document);
            } else {
                LOGGER.info("Cannot find relation '" + namespace + relation);
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void removeRelationsByNamespace(String pid, String namespace) {
        try {
            DatastreamContentWrapper datastreamContent = akubraRepository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT);
            if(datastreamContent == null) {
                // TODO log
                return;
            }
            Document document = datastreamContent.asDom(true);
            List<Element> relationElements = DomUtils.getElementsRecursive(document.getDocumentElement(), (element) -> {
                String elmNamespace = element.getNamespaceURI();
                return (elmNamespace.equals(namespace));
            });
            // Change RELS-EXT relations
            if (!relationElements.isEmpty()) {
                relationElements.stream().forEach((elm) -> {
                    elm.getParentNode().removeChild(elm);
                });
                changeRelations(pid, document);
            } else {
                LOGGER.warning("Cannot find relation '" + namespace);
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void addLiteral(String pid, String relation, String namespace, String value) {
        try {
            DatastreamContentWrapper datastreamContent = akubraRepository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT);
            if(datastreamContent == null) {
                // TODO log
                return;
            }
            Document document = datastreamContent.asDom(true);
            Element rdfDesc = DomUtils.findElement(document.getDocumentElement(), RDF_DESCRIPTION_ELEMENT, RepositoryNamespaces.RDF_NAMESPACE_URI);
            Element subElm = document.createElementNS(namespace, relation);
            subElm.setTextContent(value);
            rdfDesc.appendChild(subElm);
            changeRelations(pid, document);
        } catch (Exception e) {
            throw new RepositoryException(e);
        }

    }

    @Override
    public void removeLiteral(String pid, String relation, String namespace, String value) {
        try {
            DatastreamContentWrapper datastreamContent = akubraRepository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT);
            if(datastreamContent == null) {
                // TODO log
                return;
            }
            Document document = datastreamContent.asDom(true);
            Element rdfDesc = DomUtils.findElement(document.getDocumentElement(), RDF_DESCRIPTION_ELEMENT, RepositoryNamespaces.RDF_NAMESPACE_URI);
            List<Element> descs = DomUtils.getElementsRecursive(rdfDesc, (element) -> {
                String elmNamespace = element.getNamespaceURI();
                String elmName = element.getLocalName();
                if (elmNamespace != null && elmNamespace.equals(namespace) && elmName.equals(relation)) {
                    String content = element.getTextContent();
                    if (content.equals(value)) return true;
                }
                return false;
            });
            if (!descs.isEmpty()) {
                descs.stream().forEach(literal -> {
                    literal.getParentNode().removeChild(literal);
                });
                changeRelations(pid, document);
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public String getTilesUrl(String pid) {
        // TODO use SAX
        RelsExtWrapper relsExtWrapper = get(pid);
        Document document = DomUtils.streamToDocument(relsExtWrapper.asInputStream());
        return RelsExtUtils.getRelsExtTilesUrl(document);
    }

    @Override
    public String getModel(String pid) {
        // TODO use SAX
        RelsExtWrapper relsExtWrapper = get(pid);
        return RelsExtUtils.getModel(DomUtils.streamToDocument(relsExtWrapper.asInputStream()).getDocumentElement());
    }

    @Override
    public String getFirstViewablePid(String pid) {
        return RelsExtUtils.findFirstViewablePid(pid, akubraRepository);
    }

    @Override
    public void processSubtree(String pid, TreeNodeProcessor processor) throws ProcessSubtreeException {
        RelsExtUtils.processSubtree(pid, processor, akubraRepository);
    }

    @Override
    public List<String> getPids(String pid) {
        return RelsExtUtils.getPids(pid, akubraRepository);
    }

    @Override
    public String getFirstVolumePid(String pid) {
        // TODO use SAX
        RelsExtWrapper relsExtWrapper = get(pid);
        return RelsExtUtils.getFirstVolumePid(relsExtWrapper.asInputStream());
    }

    @Override
    public String getFirstItemPid(String pid) {
        // TODO use SAX
        RelsExtWrapper relsExtWrapper = get(pid);
        return RelsExtUtils.getFirstItemPid(relsExtWrapper.asInputStream());
    }

    private void changeRelations(String pid, Document document) throws TransformerException {
        StringWriter stringWriter = new StringWriter();
        DomUtils.print(document, stringWriter);
        update(pid, new ByteArrayInputStream(stringWriter.toString().getBytes(Charset.forName("UTF-8"))));
    }

}
