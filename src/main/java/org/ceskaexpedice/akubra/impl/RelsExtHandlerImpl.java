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

import org.apache.commons.lang3.tuple.Triple;
import org.ceskaexpedice.akubra.*;
import org.ceskaexpedice.akubra.impl.utils.RelsExtUtils;
import org.ceskaexpedice.akubra.relsext.RelsExtHandler;
import org.ceskaexpedice.akubra.relsext.RelsExtLiteral;
import org.ceskaexpedice.akubra.relsext.RelsExtRelation;
import org.ceskaexpedice.akubra.impl.utils.DomUtils;
import org.ceskaexpedice.akubra.impl.utils.TreeNodeProcessor;
import org.ceskaexpedice.akubra.impl.utils.pid.PIDParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
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
    public boolean exists(String pid) {
        return akubraRepository.datastreamExists(pid, KnownDatastreams.RELS_EXT);
    }

    @Override
    public DatastreamContentWrapper get(String pid) {
        return akubraRepository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT);
    }

    @Override
    public void update(String pid, InputStream xmlContent) {
        akubraRepository.updateXMLDatastream(pid, KnownDatastreams.RELS_EXT, "text/xml", xmlContent);
    }

    @Override
    public boolean relationExists(String pid, String relation, String namespace) {
        // TODO use SAX
        DatastreamContentWrapper datastreamContent = akubraRepository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT);
        if(datastreamContent == null) {
            return false;
        }
        return RelsExtUtils.relationsExists(datastreamContent.asDom(true), relation, namespace);
    }

    @Override
    public String getElementValue(String pid, String xpathExpression) {
        // TODO use SAX
        DatastreamContentWrapper relsExtWrapper = get(pid);
        return RelsExtUtils.getElementValue(relsExtWrapper.asDom(false),xpathExpression);
    }

    @Override
    public String getTilesUrl(String pid) {
        return getElementValue(pid, "//kramerius:tiles-url/text()");
    }

    @Override
    public String getResourcePid(String pid, String localName, String namespace, boolean appendPrefix) {
        // TODO use SAX
        DatastreamContentWrapper relsExtWrapper = get(pid);
        return RelsExtUtils.getResourcePid(relsExtWrapper.asDom(false), localName, namespace, appendPrefix);
    }

    @Override
    public String getModel(String pid) {
        return getResourcePid(pid, "hasModel", RepositoryNamespaces.FEDORA_MODELS_URI, false);
    }

    @Override
    public String getFirstVolumePid(String pid) {
        return getResourcePid(pid, "hasVolume", RepositoryNamespaces.KRAMERIUS_URI, true);
    }

    @Override
    public String getFirstItemId(String pid) {
        return getResourcePid(pid, "hasItem", RepositoryNamespaces.KRAMERIUS_URI, false);
    }

    @Override
    public String getFirstViewablePidFromTree(String pid) {
        return RelsExtUtils.findFirstViewablePidFromTree(pid, akubraRepository);
    }

    @Override
    public List<String> getPidsFromTree(String pid) {
        return RelsExtUtils.getPidsFromTree(pid, akubraRepository);
    }

    @Override
    public List<RelsExtRelation> getRelations(String pid, String namespace) {
        // TODO use SAX
        DatastreamContentWrapper relsExtWrapper = get(pid);
        List<RelsExtRelation> rels = new ArrayList<>();
        List<Triple<String, String, String>> triples = RelsExtUtils.getRelations(relsExtWrapper.asDom(true), namespace);
        for (Triple<String, String, String> triple : triples) {
            RelsExtRelation relsExtRelation = new RelsExtRelation(triple.getLeft(), triple.getMiddle(), triple.getRight());
            rels.add(relsExtRelation);
        }
        return rels;
    }

    @Override
    public List<RelsExtLiteral> getLiterals(String pid, String namespace) {
        // TODO use SAX
        DatastreamContentWrapper relsExtWrapper = get(pid);
        List<RelsExtLiteral> rels = new ArrayList<>();
        List<Triple<String, String, String>> triples = RelsExtUtils.getLiterals(relsExtWrapper.asDom(true), namespace);
        for (Triple<String, String, String> triple : triples) {
            RelsExtLiteral relsExtLiteral = new RelsExtLiteral(triple.getLeft(), triple.getMiddle(), triple.getRight());
            rels.add(relsExtLiteral);
        }
        return rels;
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

    private void changeRelations(String pid, Document document) throws TransformerException {
        StringWriter stringWriter = new StringWriter();
        DomUtils.print(document, stringWriter);
        update(pid, new ByteArrayInputStream(stringWriter.toString().getBytes(Charset.forName("UTF-8"))));
    }

}
