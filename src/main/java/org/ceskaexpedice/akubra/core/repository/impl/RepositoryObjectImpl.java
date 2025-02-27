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
package org.ceskaexpedice.akubra.core.repository.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexSolr;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.RepositoryNamespaces;
import org.ceskaexpedice.akubra.core.repository.*;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.ceskaexpedice.akubra.utils.StringUtils;
import org.ceskaexpedice.akubra.utils.pid.PIDParser;
import org.ceskaexpedice.fedoramodel.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * RepositoryObjectImpl
 *
 * @author pavels
 */
class RepositoryObjectImpl implements RepositoryObject {
    private static final Logger LOGGER = Logger.getLogger(RepositoryObjectImpl.class.getName());
    private static final String RDF_DESCRIPTION_ELEMENT = "Description";
    private static final String RDF_ELEMENT = "RDF";
    private AkubraDOManager manager;
    private DigitalObject digitalObject;
    private ProcessingIndexSolr feeder;

    RepositoryObjectImpl(DigitalObject digitalObject, AkubraDOManager manager, ProcessingIndexSolr feeder) {
        super();
        this.manager = manager;
        this.feeder = feeder;
        this.digitalObject = digitalObject;
    }

    @Override
    public DigitalObject getDigitalObject() {
        return digitalObject;
    }

    @Override
    public InputStream getFoxml() {
        return manager.retrieveObject(getPid());
    }

    @Override
    public String getPid() {
        return digitalObject.getPID();
    }

    @Override
    public Date getPropertyLastModified() {
        return RepositoryUtils.getLastModified(digitalObject);
    }

    @Override
    public List<RepositoryDatastream> getStreams() {
        List<RepositoryDatastream> list = new ArrayList<>();
        List<DatastreamType> datastreamList = digitalObject.getDatastream();
        for (DatastreamType datastreamType : datastreamList) {
            list.add(new RepositoryDatastreamImpl(datastreamType, datastreamType.getID(), controlGroup2Type(datastreamType.getCONTROLGROUP()),manager));
        }
        return list;
    }

    @Override
    public RepositoryDatastream getStream(String streamId) {
        List<DatastreamType> datastreamList = digitalObject.getDatastream();
        for (DatastreamType datastreamType : datastreamList) {
            if (streamId.equals(datastreamType.getID())) {
                return new RepositoryDatastreamImpl(datastreamType, datastreamType.getID(), controlGroup2Type(datastreamType.getCONTROLGROUP()),manager);
            }
        }
        return null;
    }

    @Override
    public boolean streamExists(String streamId) {
        return RepositoryUtils.streamExists(digitalObject, streamId);
    }

    @Override
    public RepositoryDatastream createXMLStream(String streamId, String mimeType, InputStream input) {
        DatastreamType datastreamType = createDatastreamHeader(streamId, mimeType, "X");
        XmlContentType xmlContentType = new XmlContentType();
        xmlContentType.getAny().add(elementFromInputStream(input));
        datastreamType.getDatastreamVersion().get(0).setXmlContent(xmlContentType);

        RepositoryDatastream ds = new RepositoryDatastreamImpl(datastreamType, streamId, RepositoryDatastream.Type.DIRECT, manager);

        try {
            manager.write(digitalObject, streamId);
            if (streamId.equals(KnownDatastreams.RELS_EXT.toString())) {
                try {
                    // process rels-ext and create all children and relations
                    this.feeder.deleteByRelationsForPid(getPid());
                    input.reset();
                    feeder.rebuildProcessingIndex(this, input);
                } catch (Throwable th) {
                    LOGGER.log(Level.SEVERE, "Cannot update processing index for " + getPid() + " - reindex manually.", th);
                }
            }
            return ds;
        } catch (Exception ex) {
            throw new RepositoryException(ex);
        }
    }

    @Override
    public RepositoryDatastream createManagedStream(String streamId, String mimeType, InputStream input) {
        DatastreamType datastreamType = createDatastreamHeader(streamId, mimeType, "M");

        try {
            datastreamType.getDatastreamVersion().get(0).setBinaryContent(IOUtils.toByteArray(input));
            RepositoryDatastream ds = new RepositoryDatastreamImpl(datastreamType, streamId, RepositoryDatastream.Type.DIRECT, manager);
            manager.write(digitalObject, streamId);
            return ds;
        } catch (Exception ex) {
            throw new RepositoryException(ex);
        }
    }

    @Override
    public RepositoryDatastream createRedirectedStream(String streamId, String url, String mimeType) {
        DatastreamType datastreamType = createDatastreamHeader(streamId, mimeType, "E");
        ContentLocationType contentLocationType = new ContentLocationType();
        contentLocationType.setTYPE("URL");
        contentLocationType.setREF(url);
        datastreamType.getDatastreamVersion().get(0).setContentLocation(contentLocationType);

        RepositoryDatastream ds = new RepositoryDatastreamImpl(datastreamType, streamId, RepositoryDatastream.Type.INDIRECT, manager);

        manager.write(digitalObject, streamId);
        return ds;
    }

    @Override
    public void deleteStream(String streamId) {
        manager.deleteStream(getPid(), streamId);
        if (streamId.equals(KnownDatastreams.RELS_EXT.toString())) {
            try {
                this.feeder.deleteByRelationsForPid(this.getPid());
            } catch (Throwable th) {
                LOGGER.log(Level.SEVERE, "Cannot update processing index for " + getPid() + " - reindex manually.", th);
            }
        }
    }

    private DatastreamType createDatastreamHeader(String streamId, String mimeType, String controlGroup) {
        List<DatastreamType> datastreamList = digitalObject.getDatastream();
        Iterator<DatastreamType> iterator = datastreamList.iterator();
        while (iterator.hasNext()) {
            DatastreamType datastreamType = iterator.next();
            if (streamId.equals(datastreamType.getID())) {
                iterator.remove();
            }
        }
        DatastreamType datastreamType = new DatastreamType();
        datastreamType.setID(streamId);
        datastreamType.setCONTROLGROUP(controlGroup);
        datastreamType.setSTATE(StateType.A);
        datastreamType.setVERSIONABLE(false);
        List<DatastreamVersionType> datastreamVersion = datastreamType.getDatastreamVersion();
        DatastreamVersionType datastreamVersionType = new DatastreamVersionType();
        datastreamVersionType.setID(streamId + ".0");
        datastreamVersionType.setCREATED(org.ceskaexpedice.akubra.core.repository.impl.RepositoryUtils.getCurrentXMLGregorianCalendar());
        datastreamVersionType.setMIMETYPE(mimeType);
        String formatUri = RepositoryUtils.getFormatUriForDS(streamId);
        if (formatUri != null) {
            datastreamVersionType.setFORMATURI(formatUri);
        }
        datastreamVersion.add(datastreamVersionType);
        datastreamList.add(datastreamType);
        return datastreamType;
    }

    private RepositoryDatastreamImpl.Type controlGroup2Type(String controlGroup) {
        if ("E".equals(controlGroup) || "R".equals(controlGroup)) {
            return RepositoryDatastream.Type.INDIRECT;
        } else {
            return RepositoryDatastream.Type.DIRECT;
        }
    }

    private static Element elementFromInputStream(InputStream in) {
        DocumentBuilderFactory factory;
        DocumentBuilder builder = null;
        Document ret = null;

        try {
            factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            builder = factory.newDocumentBuilder();
            ret = builder.parse(new InputSource(in));
            if (ret != null) {
                return ret.getDocumentElement();
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void relsExtAddRelation(String relation, String namespace, String targetRelation) {
        try {
            RepositoryDatastream stream = this.getStream(KnownDatastreams.RELS_EXT.toString());
            Document document = DomUtils.streamToDocument(stream.getLastVersionContent(), true);
            Element rdfDesc = DomUtils.findElement(document.getDocumentElement(), RDF_DESCRIPTION_ELEMENT, RepositoryNamespaces.RDF_NAMESPACE_URI);
            Element subElm = document.createElementNS(namespace, relation);
            subElm.setAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "rdf:resource", targetRelation);
            rdfDesc.appendChild(subElm);
            relsExtChangeRelations(document);
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void relsExtAddLiteral(String relation, String namespace, String value) {
        try {
            RepositoryDatastream stream = this.getStream(KnownDatastreams.RELS_EXT.toString());
            Document document = DomUtils.streamToDocument(stream.getLastVersionContent(), true);
            Element rdfDesc = DomUtils.findElement(document.getDocumentElement(), RDF_DESCRIPTION_ELEMENT, RepositoryNamespaces.RDF_NAMESPACE_URI);
            Element subElm = document.createElementNS(namespace, relation);
            subElm.setTextContent(value);
            rdfDesc.appendChild(subElm);
            relsExtChangeRelations(document);
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void relsExtRemoveLiteral(String relation, String namespace, String value) {
        try {
            RepositoryDatastream stream = this.getStream(KnownDatastreams.RELS_EXT.toString());
            Document document = DomUtils.streamToDocument(stream.getLastVersionContent(), true);

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
                relsExtChangeRelations(document);
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }

    }

    @Override
    public void relsExtRemoveRelation(String relation, String namespace, String targetRelation) {
        try {
            final String targetPID = targetRelation.startsWith(PIDParser.INFO_FEDORA_PREFIX) ? targetRelation : PIDParser.INFO_FEDORA_PREFIX + targetRelation;
            RepositoryDatastream stream = this.getStream(KnownDatastreams.RELS_EXT.toString());
            Document document = DomUtils.streamToDocument(stream.getLastVersionContent(), true);
            Element relationElement = DomUtils.findElement(document.getDocumentElement(), (element) -> {
                String elmNamespace = element.getNamespaceURI();
                String elmLocalname = element.getLocalName();
                String elmResourceAttribute = element.getAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource");
                return (elmNamespace.equals(namespace)) && (elmLocalname.equals(relation)) && elmResourceAttribute.equals(targetPID);
            });
            if (relationElement != null) {
                relationElement.getParentNode().removeChild(relationElement);
                relsExtChangeRelations(document);
            } else {
                LOGGER.warning("Cannot find relation '" + namespace + relation);
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void relsExtRemoveRelationsByNameAndNamespace(String relation, String namespace) {
        try {
            RepositoryDatastream stream = this.getStream(KnownDatastreams.RELS_EXT.toString());
            Document document = DomUtils.streamToDocument(stream.getLastVersionContent(), true);
            List<Element> relationElements = DomUtils.getElementsRecursive(document.getDocumentElement(), (element) -> {
                String elmNamespace = element.getNamespaceURI();
                String elmLocalname = element.getLocalName();
                return (elmNamespace.equals(namespace)) && (elmLocalname.equals(relation));
            });
            if (!relationElements.isEmpty()) {
                relationElements.stream().forEach((elm) -> {
                    elm.getParentNode().removeChild(elm);
                });
                relsExtChangeRelations(document);
            } else {
                LOGGER.info("Cannot find relation '" + namespace + relation);
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void relsExtRemoveRelationsByNamespace(String namespace) {
        try {
            RepositoryDatastream stream = this.getStream(KnownDatastreams.RELS_EXT.toString());
            Document document = DomUtils.streamToDocument(stream.getLastVersionContent(), true);
            List<Element> relationElements = DomUtils.getElementsRecursive(document.getDocumentElement(), (element) -> {
                String elmNamespace = element.getNamespaceURI();
                return (elmNamespace.equals(namespace));
            });


            // Change RELS-EXT relations
            if (!relationElements.isEmpty()) {
                relationElements.stream().forEach((elm) -> {
                    elm.getParentNode().removeChild(elm);
                });
                relsExtChangeRelations(document);
            } else {
                LOGGER.warning("Cannot find relation '" + namespace);
            }

        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    private void relsExtChangeRelations(Document document) throws TransformerException {
        StringWriter stringWriter = new StringWriter();
        DomUtils.print(document, stringWriter);

        this.deleteStream(KnownDatastreams.RELS_EXT.toString());
        this.createXMLStream(KnownDatastreams.RELS_EXT.toString(), "text/xml", new ByteArrayInputStream(stringWriter.toString().getBytes(Charset.forName("UTF-8"))));
    }

    @Override
    public List<Triple<String, String, String>> relsExtGetRelations(String namespace) {
        try {
            Document metadata = DomUtils.streamToDocument(getStream(KnownDatastreams.RELS_EXT.toString()).getLastVersionContent(), true);
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

    @Override
    public List<Triple<String, String, String>> relsExtGetLiterals(String namespace) {
        try {
            Document metadata = DomUtils.streamToDocument(getStream(KnownDatastreams.RELS_EXT.toString()).getLastVersionContent(), true);

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

    private Element relsExtFindRelationElement(String relation, String namespace, String targetRelation) {
        final String targetPID = targetRelation.startsWith(PIDParser.INFO_FEDORA_PREFIX) ? targetRelation : PIDParser.INFO_FEDORA_PREFIX + targetRelation;
        RepositoryDatastream stream = this.getStream(KnownDatastreams.RELS_EXT.toString());
        if (stream == null) {
            throw new RepositoryException("FOXML object " + this.getPid() + "does not have RELS-EXT stream ");
        }
        Document document;
        try {
            document = DomUtils.streamToDocument(stream.getLastVersionContent(), true);
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
        Element relationElement = DomUtils.findElement(document.getDocumentElement(), (element) -> {
            String elmNamespace = element.getNamespaceURI();
            String elmLocalname = element.getLocalName();
            String elmResourceAttribute = element.getAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource");
            return (elmNamespace.equals(namespace)) && (elmLocalname.equals(relation)) && elmResourceAttribute.equals(targetPID);
        });
        return relationElement;
    }

    @Override
    public boolean relsExtRelationExists(String relation, String namespace, String targetRelation) {
        Element foundElement = relsExtFindRelationElement(relation, namespace, targetRelation);
        return foundElement != null;
    }

    @Override
    public boolean relsExtLiteralExists(String relation, String namespace, String value) {
        try {
            Document metadata = DomUtils.streamToDocument(getStream(KnownDatastreams.RELS_EXT.toString()).getLastVersionContent(), true);
            Element foundElement = DomUtils.findElement(metadata.getDocumentElement(), (element) -> {
                String elmNamespace = element.getNamespaceURI();
                String elmName = element.getLocalName();
                if (elmName.equals(relation) && namespace.equals(elmNamespace)) {
                    String cont = element.getTextContent();
                    return cont.endsWith(value);
                }
                return false;
            });
            return foundElement != null;
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public boolean relsExtRelationsExists(String relation, String namespace) {
        try {
            Document metadata = DomUtils.streamToDocument(this.getStream(KnownDatastreams.RELS_EXT.toString()).getLastVersionContent(), true);
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

    @Override
    public void relsExtRemoveRelations() {
        if (this.streamExists(KnownDatastreams.RELS_EXT.toString())) {
            this.relsExtRemoveRelationsByNamespace(RepositoryNamespaces.KRAMERIUS_URI);
            this.relsExtRemoveRelationsByNameAndNamespace("isMemberOfCollection", RepositoryNamespaces.RDF_NAMESPACE_URI);
            this.deleteStream(KnownDatastreams.RELS_EXT.toString());
        }
    }

    @Override
    public void rebuildProcessingIndex() {
        RepositoryDatastream stream = this.getStream(KnownDatastreams.RELS_EXT.toString());
        InputStream content = stream.getLastVersionContent();
        feeder.rebuildProcessingIndex(this, content);
    }


}
