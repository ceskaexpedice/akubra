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
package org.ceskaexpedice.akubra.core.processingindex;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CursorMarkParams;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.RepositoryNamespaces;
import org.ceskaexpedice.akubra.config.RepositoryConfiguration;
import org.ceskaexpedice.akubra.core.repository.CoreRepository;
import org.ceskaexpedice.akubra.core.repository.RepositoryDatastream;
import org.ceskaexpedice.akubra.core.repository.impl.RepositoryUtils;
import org.ceskaexpedice.akubra.impl.utils.StructureInfoDom4jUtils;
import org.ceskaexpedice.akubra.processingindex.*;
import org.ceskaexpedice.akubra.impl.utils.ProcessingIndexUtils;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * ProcessingIndexFeeder
 *
 * @author pstastny
 */
public class ProcessingIndexSolr implements ProcessingIndex {

    private static final int MAX_TOTAL_TITLE_CHARS = 5000;
    private static final int MAX_SINGLE_TITLE_CHARS = 1000;

    private static final Logger LOGGER = Logger.getLogger(ProcessingIndexSolr.class.getName());

    private SolrClient solrUpdateClient;
    private SolrClient solrQueryClient;
    private CoreRepository coreRepository;
    RepositoryConfiguration repositoryConfiguration;

    public ProcessingIndexSolr(RepositoryConfiguration configuration, CoreRepository coreRepository) {
        super();
        this.solrUpdateClient = createProcessingUpdateClient(configuration);
        this.solrQueryClient = createProcessingQueryClient(configuration);
        this.coreRepository = coreRepository;
        this.repositoryConfiguration = configuration;
    }

    ProcessingIndexSolr(){}


    public void lookAt(ProcessingIndexQueryParameters params, Consumer<ProcessingIndexItem> action) {
        try {
            SolrQuery solrQuery = new SolrQuery(params.getQueryString());
            solrQuery.setRows(params.getRows());
            if (params.getCursorMark() == null) {
                int offset = params.getOffset() != -1 ? params.getOffset() : params.getPageIndex() * params.getRows();
                solrQuery.setStart(offset);
                QueryResponse response = this.solrQueryClient.query(solrQuery);
                response.getResults().forEach((doc) -> {
                    action.accept(ProcessingIndexUtils.fromSolrDocument(doc));
                });
            } else {
                throw new IllegalArgumentException("cursorMark is not supported in lookAt method");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String iterate(ProcessingIndexQueryParameters params, Consumer<ProcessingIndexItem> action) {
        try {
            SolrQuery solrQuery = new SolrQuery(params.getQueryString());
            solrQuery.setRows(params.getRows());
            if (params.getCursorMark() == null) {
                if (params.getSortField() != null) {
                    solrQuery.setSort(params.getSortField(), params.isAscending() ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
                }
                int offset = params.getOffset() != -1 ? params.getOffset() : params.getPageIndex() * params.getRows();
                solrQuery.setStart(offset);
                QueryResponse response = this.solrQueryClient.query(solrQuery);
                response.getResults().forEach((doc) -> {
                    action.accept(ProcessingIndexUtils.fromSolrDocument(doc));
                });
            } else {
                solrQuery.addSort(params.getSortField(), params.isAscending() ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
                solrQuery.addSort(UNIQUE_KEY, SolrQuery.ORDER.asc);
                String cursorMark = params.getCursorMark();
                boolean done = false;
                while (!done) {
                    solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
                    QueryResponse response = this.solrQueryClient.query(solrQuery);
                    response.getResults().forEach((doc) -> {
                        action.accept(ProcessingIndexUtils.fromSolrDocument(doc));
                    });
                    String nextCursorMark = response.getNextCursorMark();
                    if (cursorMark.equals(nextCursorMark)) {
                        done = true;
                    } else {
                        if (params.isStopAfterCursorMark()) {
                            return nextCursorMark;
                        }
                    }
                    cursorMark = nextCursorMark;
                }
            }
            return null;
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public List<ProcessingIndexItem> getParents(String targetPid) {
        return ProcessingIndexUtils.getParents(targetPid, coreRepository);
    }

    @Override
    public List<ProcessingIndexItem> getParents(String relation, String targetPid) {
        return ProcessingIndexUtils.getParents(relation, targetPid, coreRepository);
    }

    @Override
    public OwnedAndFosteredParents getOwnedAndFosteredParents(String targetPid) {
        return ProcessingIndexUtils.getParentsRelation(targetPid, coreRepository);
    }


    public ConflictingOwnedAndFosteredParents getConflictingOwnerAndFosteredParents(String targetPid) {
        return ProcessingIndexUtils.getConflictingOwnerAndFosteredParents(targetPid, coreRepository);
    }


    @Override
    public List<ProcessingIndexItem> getChildren(String relation, String targetPid) {
        return ProcessingIndexUtils.getChildren(relation, targetPid, coreRepository);
    }

    @Override
    public OwnedAndFosteredChildren getOwnedAndFosteredChildren(String pid) {
        return ProcessingIndexUtils.getChildrenRelation(pid, coreRepository);
    }

    @Override
    public String getModel(String pid) {
        return ProcessingIndexUtils.getModel(pid, coreRepository);
    }

    @Override
    public CursorItemsPair getByModelWithCursor(String model, boolean ascendingOrder, String cursor, int limit) {
        return ProcessingIndexUtils.getByModelWithCursor(model, ascendingOrder, cursor, limit, coreRepository);
    }

    @Override
    public SizeItemsPair getByModel(String model, String titlePrefix, int rows, int pageIndex) {
        return ProcessingIndexUtils.getByModel(model, titlePrefix, rows, pageIndex, coreRepository);
    }

    @Override
    public List<ProcessingIndexItem> getByModel(String model, boolean ascendingOrder, int offset, int limit) {
        return ProcessingIndexUtils.getByModel(model, ascendingOrder, offset, limit, coreRepository);
    }

    @Override
    public JSONObject extractStructureInfo(String pid) {
        return StructureInfoDom4jUtils.extractStructureInfo(pid, coreRepository);
    }

    @Override
    public List<Pair<String, Long>> getModelsCount() {
        try {
            QueryResponse response = this.solrQueryClient.query(new SolrQuery("type:description").setRows(0).setFacet(true).addFacetField("model"));
            List<FacetField.Count> values = response.getFacetField("model").getValues();
            return values.stream().map(c -> {
                long count = c.getCount();
                String cname = c.getName();
                return Pair.of(cname, count);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void doWithCommit(OperationsHandler op) throws RepositoryException {
        try {
            op.operations();
        } finally {
            commit();
        }
    }

    @Override
    public void deleteProcessingIndex() {
        try {
            this.solrUpdateClient.deleteByQuery("*:*");
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void deleteByPid(String pid) {
        try {
            this.solrUpdateClient.deleteByQuery("source:\"" + pid + "\"");
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void deleteByTargetPid(String pid) {
        try {
            this.solrUpdateClient.deleteByQuery("targetPid:\"" + pid + "\"");
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void deleteDescriptionByPid(String pid) {
        try {
            this.solrUpdateClient.deleteByQuery("source:\"" + pid + "\" AND type:\"description\"");
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void deleteByRelationsForPid(String pid) {
        try {
            String query = "source:\"" + pid + "\" AND type:\"relation\"";
            this.solrUpdateClient.deleteByQuery(query);
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public List<String> getStreamNames(String targetPid) {
        List<String> streamNames = new ArrayList<>();
        try {
            SolrQuery solrQuery = new SolrQuery(String.format("source:\"%s\" AND type:description",targetPid));
            solrQuery.setRows(1);
            QueryResponse response = this.solrQueryClient.query(solrQuery);
            long numFound = response.getResults().getNumFound();
            if (numFound > 0) {
                SolrDocument doc = response.getResults().get(0);
                doc.getFieldValues("streams").forEach(stream -> {
                    streamNames.add(stream.toString());
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return streamNames;
    }

    @Override
    public List<String> getChildrenStreamNames(String targetPid) {
        List<String> streamNames = new ArrayList<>();
        try {
            SolrQuery solrQuery = new SolrQuery(String.format("source:\"%s\" AND type:relation  AND relation:hasPage",targetPid));
            solrQuery.setRows(0);
            solrQuery.setFacet(true);
            solrQuery.addFacetField("targetPidStreams");
            QueryResponse response = this.solrQueryClient.query(solrQuery);
            long numFound = response.getResults().getNumFound();
            if (numFound > 0) {
                List<FacetField.Count> targetPidStreams = response.getFacetField("targetPidStreams").getValues();
                targetPidStreams.forEach(targetPidStream -> {
                    streamNames.add(targetPidStream.getName());
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return streamNames;
    }

    @Override
    public boolean containsRelation(String sourcePid, String relation) {
        try {
            SolrQuery solrQuery = new SolrQuery(String.format("source:\"%s\" AND type:relation  AND relation:%s",sourcePid,relation));
            solrQuery.setRows(0);
            QueryResponse response = this.solrQueryClient.query(solrQuery);
            long numFound = response.getResults().getNumFound();
            return numFound > 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void rebuildProcessingIndex(String pid, Consumer<UpdateRequest> updateRequestCustomizer) {

        List<SolrInputDocument> batch = new ArrayList<>();
        try {
            //List<String> streamNames = coreRepository.getAsRepositoryObject(pid).getStreams().stream().map(RepositoryDatastream::getName).collect(Collectors.toList());

            InputStream isRelsExt = coreRepository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT.toString());
            String stringRelsExt = IOUtils.toString(isRelsExt, StandardCharsets.UTF_8);
            RelsExtSPARQLBuilder sparqlBuilder = new RelsExtSPARQLBuilderImpl();

            sparqlBuilder.sparqlProps(stringRelsExt.trim(), (object, localName) -> {
                processRelsExtRelationAndFeedProcessingIndex(solrQueryClient, coreRepository, pid, object, localName, batch);
                return object;
            });

        } catch (Exception e) {
            throw new RepositoryException(e);
        } finally {
            if (batch.size() > 0) {
                UpdateRequest req = new UpdateRequest();
                batch.forEach(doc-> {req.add(doc);});

                if (updateRequestCustomizer != null) {
                    updateRequestCustomizer.accept(req);
                }

                LOGGER.fine(String.format("Update batch with size %s",  req.getDocuments().size()));
                try {
                    UpdateResponse response = req.process(solrUpdateClient);
                    LOGGER.fine("qtime:"+response.getQTime());
                } catch (SolrServerException | IOException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage());
                }
            }
        }
    }

    private static void diff(String message, long rebuildStop, long rebuildStart) {
        long diff = rebuildStop - rebuildStart;
        String m = String.format("\t"+message+"; diff=%d", diff);
        LOGGER.info(m);
    }


    /**
     * Process one relation and feed processing index
     */
    private void processRelsExtRelationAndFeedProcessingIndex(SolrClient solrQueryClient, CoreRepository coreRepository, String pid, String object, String localName, List<SolrInputDocument> batch) {
        if (localName.equals("hasModel")) {
            try {
                List<String> streamNames = coreRepository.getAsRepositoryObject(pid).getStreams().stream().map(RepositoryDatastream::getName).collect(Collectors.toList());
                boolean dcStreamExists = this.coreRepository.datastreamExists(pid, KnownDatastreams.BIBLIO_DC.name());
                // TODO: Biblio mods ukladat jinam ??
                boolean modsStreamExists = this.coreRepository.datastreamExists(pid, KnownDatastreams.BIBLIO_MODS.name());
                if (dcStreamExists || modsStreamExists) {
                    try {
                        if (dcStreamExists) {
                            List<String> dcTList = dcTitle(pid);
                            if (dcTList != null && !dcTList.isEmpty()) {
                                List<SolrInputDocument> docs = prepareDescriptionDocument(solrQueryClient,pid, object, dcTList.stream().collect(Collectors.joining(" ")).trim(), RepositoryUtils.getAkubraInternalId(pid, repositoryConfiguration.getObjectStorePattern()), new Date() ,streamNames);
                                batch.addAll(docs);
                            } else {
                                List<SolrInputDocument> docs = prepareDescriptionDocument(solrQueryClient, pid, object, "", RepositoryUtils.getAkubraInternalId(pid, repositoryConfiguration.getObjectStorePattern()), new Date(),streamNames);
                                batch.addAll(docs);
                            }
                        } else if (modsStreamExists) {
                            // czech title or default
                            List<String> modsTList = modsTitle(pid, "cze");
                            if (modsTList != null && !modsTList.isEmpty()) {
                                List<SolrInputDocument> docs = prepareDescriptionDocument(solrQueryClient,pid, object,  modsTList.stream().collect(Collectors.joining(" ")), RepositoryUtils.getAkubraInternalId(pid, repositoryConfiguration.getObjectStorePattern()), new Date(), ProcessingIndexSolr.TitleType.mods, streamNames);
                                batch.addAll(docs);
                            } else {
                                List<SolrInputDocument> docs = prepareDescriptionDocument(solrQueryClient, pid, object, "", RepositoryUtils.getAkubraInternalId(pid, repositoryConfiguration.getObjectStorePattern()), new Date(), streamNames);
                                batch.addAll(docs);
                            }
                        }
                    } catch (ParserConfigurationException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);

                        List<SolrInputDocument> docs = prepareDescriptionDocument(solrQueryClient,pid, object, "", RepositoryUtils.getAkubraInternalId(pid, repositoryConfiguration.getObjectStorePattern()), new Date(), streamNames);
                        batch.addAll(docs);
                   } catch (SAXException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);

                        List<SolrInputDocument> docs = prepareDescriptionDocument(solrQueryClient, pid, object, "", RepositoryUtils.getAkubraInternalId(pid, repositoryConfiguration.getObjectStorePattern()), new Date(), streamNames);
                        batch.addAll(docs);
                    }
                } else {
                    LOGGER.info("Index description without dc or mods");

                    List<SolrInputDocument> docs = prepareDescriptionDocument(solrQueryClient,pid, object, "", RepositoryUtils.getAkubraInternalId(pid, repositoryConfiguration.getObjectStorePattern()), new Date(), streamNames);
                    batch.addAll(docs);
                }
            } catch (Throwable th) {
                LOGGER.log(Level.SEVERE, "Cannot update processing index for " + pid + " - reindex manually.", th);
            }
        } else {
            try {
                SolrInputDocument sdoc = prepareRelationDocument(pid, localName, object,coreRepository);
                 batch.add(sdoc);
            } catch (Throwable th) {
                LOGGER.log(Level.SEVERE, "Cannot update processing index for " + pid + " - reindex manually.", th);
            }
        }
    }

//    private void indexDescription(String pid, String model, String title, ProcessingIndex.TitleType ttype) {
//        this.feedDescriptionDocument(pid, model, title.trim(), RepositoryUtils.getAkubraInternalId(pid, repositoryConfiguration.getObjectStorePattern()), new Date(), ttype);
//    }
//
//    private void indexDescription(String pid, String model, String title) {
//        this.feedDescriptionDocument(pid, model, title.trim(), RepositoryUtils.getAkubraInternalId(pid, repositoryConfiguration.getObjectStorePattern()), new Date());
//    }

    private List<String> dcTitle(String pid) {
        InputStream streamContent = coreRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_DC.toString());
        Element title = DomUtils.findElement(DomUtils.streamToDocument(streamContent, true).getDocumentElement(), "title", RepositoryNamespaces.DC_NAMESPACE_URI);
        return title != null ? Arrays.asList(title.getTextContent()) : new ArrayList<>();
    }

    private List<String> modsTitle(String pid, String lang) throws RepositoryException, ParserConfigurationException, SAXException, IOException {
        InputStream streamContent = coreRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_MODS.toString());
        Element docElement = DomUtils.streamToDocument(streamContent, true).getDocumentElement();

        List<Element> elements = DomUtils.getElementsRecursive(docElement, new DomUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                if (element.getNamespaceURI() != null &&  element.getNamespaceURI().equals(RepositoryNamespaces.BIBILO_MODS_URI)) {
                    if (element.getLocalName().equals("title") && element.hasAttribute("lang") && element.getAttribute("lang").equals("cze")) {
                        return true;
                    }
                }
                return false;
            }
        });


        if (elements.isEmpty()) {
            elements = DomUtils.getElementsRecursive(docElement, new DomUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    if (element.getNamespaceURI() != null &&  element.getNamespaceURI().equals(RepositoryNamespaces.BIBILO_MODS_URI)) {
                        // TODO: Change it
                        if (element.getLocalName().equals("title")) {
                            return true;
                        }
                    }
                    return false;
                }
            });

        }
        return extractTitles(elements);
    }

    private static List<String> extractTitles(List<Element> elements) {
        List<String> result = new ArrayList<>();
        int totalChars = 0;
        for (Element e : elements) {
            String text = e.getTextContent();
            if (text == null || text.isBlank()) {
                continue;
            }
            if (text.length() > MAX_SINGLE_TITLE_CHARS) {
                text = text.substring(0, MAX_SINGLE_TITLE_CHARS);
            }
            if (totalChars + text.length() > MAX_TOTAL_TITLE_CHARS) {
                break;
            }
            result.add(text);
            totalChars += text.length();
        }
        return result;
    }

    @Override
    public void commit() {
        try {
            this.solrUpdateClient.commit();
            LOGGER.info("Processing index commit ");
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

//    private UpdateResponse feedDescriptionDocument( String sourcePid, String model, String title, String ref, Date date, TitleType ttype,List<String> streamNames) {
//        SolrInputDocument sdoc = prepareDescriptionDocument(sourcePid, model, title, ref, date, ttype, streamNames);
//        return feedDescriptionDocument(sdoc);
//    }
//
//    private UpdateResponse feedDescriptionDocument(String sourcePid, String model, String title, String ref, Date date,List<String> streamNames) {
//        SolrInputDocument sdoc = prepareDescriptionDocument(sourcePid, model, title, ref, date, streamNames);
//        return feedDescriptionDocument(sdoc);
//    }

    private static List<String> findIdentifiers(SolrClient solrQueryClient, String targetPid) {
        List<String> identifiers = new ArrayList<>();
        try {
            QueryResponse response = solrQueryClient.query(
                new SolrQuery("*")
                    .addFilterQuery("type:relation")
                    .addFilterQuery(String.format( "targetPid:\"%s\"",targetPid))
                    .addFilterQuery("NOT targetPidStreams:*")
                    .setRows(1000)
            );
            SolrDocumentList result = response.getResults();
            if (result.getNumFound() >0) {
                result.stream().forEach(item -> {
                    identifiers.add(item.get("pid").toString());
                });
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error while trying to find identifiers", e);
        }
        return identifiers;
    }


    private static List<SolrInputDocument> prepareDescriptionDocument(SolrClient solrQueryClient,String sourcePid, String model, String title, String ref, Date date, TitleType ttype, List<String> streamNames) {

        List<SolrInputDocument> retDocuments = new ArrayList<>();

        SolrInputDocument descriptionDocument = new SolrInputDocument();
        descriptionDocument.addField("source", sourcePid);
        descriptionDocument.addField("type", TYPE_DESC);
        descriptionDocument.addField("model", model);
        if (ttype.equals(TitleType.mods)) {
            descriptionDocument.addField("mods.title", title);
        } else {
            descriptionDocument.addField("dc.title", title);
        }
        descriptionDocument.addField("ref", ref);
        descriptionDocument.addField("date", date);
        descriptionDocument.addField("streams", streamNames);
        descriptionDocument.addField("pid", TYPE_DESC + "|" + sourcePid);
        retDocuments.add(descriptionDocument);

        // in case of related relation is already indexed without streams
        List<String> identifiers = findIdentifiers(solrQueryClient, sourcePid);
        identifiers.forEach(pid-> {
            SolrInputDocument atomicUpdateDocument = new SolrInputDocument();
            atomicUpdateDocument.addField("pid", pid);
            Map<String, List<String>> setField = new HashMap<>();
            setField.put("set", streamNames);
            atomicUpdateDocument.addField("targetPidStreams", setField);
            retDocuments.add(atomicUpdateDocument);
        });
        return retDocuments;
    }

    private static List<SolrInputDocument> prepareDescriptionDocument(SolrClient solrQueryClient,String sourcePid, String model, String title, String ref, Date date, List<String> streamNames) {
        List<SolrInputDocument> retDocuments = new ArrayList<>();

        SolrInputDocument sdoc = new SolrInputDocument();
        sdoc.addField("source", sourcePid);
        sdoc.addField("type", TYPE_DESC);
        sdoc.addField("model", model);
        sdoc.addField("dc.title", title);
        sdoc.addField("ref", ref);
        sdoc.addField("date", date);
        sdoc.addField("streams", streamNames);
        sdoc.addField("pid", TYPE_DESC + "|" + sourcePid);
        retDocuments.add(sdoc);

        // in case of related relation is already indexed without streams
        List<String> identifiers = findIdentifiers(solrQueryClient, sourcePid);

        identifiers.forEach(pid-> {
            SolrInputDocument atomicUpdateDocument = new SolrInputDocument();
            atomicUpdateDocument.addField("pid", pid);
            Map<String, List<String>> setField = new HashMap<>();
            setField.put("set", streamNames);
            atomicUpdateDocument.addField("targetPidStreams", setField);
            retDocuments.add(atomicUpdateDocument);
        });

        return retDocuments;
    }

    private static SolrInputDocument prepareRelationDocument(String sourcePid, String relation, String targetPid, CoreRepository coreRepository) {
        SolrInputDocument sdoc = new SolrInputDocument();
        sdoc.addField("source", sourcePid);
        sdoc.addField("type", TYPE_RELATION);
        sdoc.addField("relation", relation);
        sdoc.addField("targetPid", targetPid);
        sdoc.addField("pid", TYPE_RELATION + "|" + sourcePid + "|" + relation + "|" + targetPid);

        if (coreRepository.exists(targetPid)) {
            List<RepositoryDatastream> streams = coreRepository.getAsRepositoryObject(targetPid).getStreams();
            List<String> streamNames =  streams.stream().map(RepositoryDatastream::getName).collect(Collectors.toList());
            sdoc.addField("targetPidStreams", streamNames);
        }
        return sdoc;
    }

    private UpdateResponse feedDescriptionDocument(SolrInputDocument doc) {
        try {
            UpdateResponse response = this.solrUpdateClient.add(doc);
            return response;
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    private UpdateResponse feedRelationDocument(String sourcePid, String relation, String targetPid) {
        SolrInputDocument sdoc = prepareRelationDocument(sourcePid, relation, targetPid, coreRepository);
        return feedRelationDocument(sdoc);
    }


    private UpdateResponse feedRelationDocument(SolrInputDocument sdoc) {
        try {
            UpdateResponse resp = this.solrUpdateClient.add(sdoc);
            return resp;
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    private static SolrClient createProcessingQueryClient(RepositoryConfiguration configuration) {
        String processingSolrHost = configuration.getProcessingIndexHost();
        return new HttpSolrClient.Builder(processingSolrHost).build();
    }

    private static SolrClient createProcessingUpdateClient(RepositoryConfiguration configuration) {
        String processingSolrHost = configuration.getProcessingIndexHost();
        return new ConcurrentUpdateSolrClient.Builder(processingSolrHost).withQueueSize(100).build();
    }

}
