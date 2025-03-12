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
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CursorMarkParams;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.RepositoryNamespaces;
import org.ceskaexpedice.akubra.config.RepositoryConfiguration;
import org.ceskaexpedice.akubra.core.repository.CoreRepository;
import org.ceskaexpedice.akubra.core.repository.impl.RepositoryUtils;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndex;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndexItem;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndexQueryParameters;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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

    private static final Logger LOGGER = Logger.getLogger(ProcessingIndexSolr.class.getName());

    private SolrClient solrClient;
    private CoreRepository coreRepository;

    public ProcessingIndexSolr(RepositoryConfiguration configuration, CoreRepository coreRepository) {
        super();
        this.solrClient =  createProcessingUpdateClient(configuration);
        this.coreRepository = coreRepository;
    }

  /* TODO
  private SolrClient processingQueryClient() {
    String processingSolrHost = KConfiguration.getInstance().getSolrProcessingHost();
    return new HttpSolrClient.Builder(processingSolrHost).build();
  }*/

    private static SolrClient createProcessingUpdateClient(RepositoryConfiguration configuration) {
        String processingSolrHost = configuration.getProcessingIndexHost();
        return new ConcurrentUpdateSolrClient.Builder(processingSolrHost).withQueueSize(100).build();
    }

    @Override
    public String iterate(ProcessingIndexQueryParameters params, Consumer<ProcessingIndexItem> action) {
        try {
            SolrQuery solrQuery = new SolrQuery(params.getQueryString());
            solrQuery.setRows(params.getRows());
            if(params.getCursorMark() == null) {
                solrQuery.setSort(params.getSortField(), params.isAscending() ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
                int offset = params.getOffset() != -1 ? params.getOffset() : params.getPageIndex() * params.getRows();
                solrQuery.setStart(offset);
                QueryResponse response = this.solrClient.query(solrQuery);
                response.getResults().forEach((doc) -> {
                    action.accept(new ProcessingIndexItem(doc));
                });
            }else{
                solrQuery.addSort(params.getSortField(), params.isAscending() ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
                solrQuery.addSort(UNIQUE_KEY, SolrQuery.ORDER.asc);
                String cursorMark = params.getCursorMark();
                boolean done = false;
                while (!done) {
                    solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
                    QueryResponse response = this.solrClient.query(solrQuery);
                    response.getResults().forEach((doc) -> {
                        action.accept(new ProcessingIndexItem(doc));
                    });
                    String nextCursorMark = response.getNextCursorMark();
                    if (cursorMark.equals(nextCursorMark)) {
                        done = true;
                    }else{
                        if(params.isStopAfterCursorMark()){
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

    private UpdateResponse feedDescriptionDocument(String sourcePid, String model, String title, String ref, Date date, TitleType ttype) {
        SolrInputDocument sdoc = new SolrInputDocument();
        sdoc.addField("source", sourcePid);
        sdoc.addField("type", TYPE_DESC);
        sdoc.addField("model", model);
        if (ttype.equals(TitleType.mods)) {
            sdoc.addField("mods.title", title);
        } else {
            sdoc.addField("dc.title", title);
        }
        sdoc.addField("ref", ref);
        sdoc.addField("date", date);
        sdoc.addField("pid", TYPE_DESC + "|" + sourcePid);
        return feedDescriptionDocument(sdoc);
    }

    private UpdateResponse feedDescriptionDocument(String sourcePid, String model, String title, String ref, Date date) {
        SolrInputDocument sdoc = new SolrInputDocument();
        sdoc.addField("source", sourcePid);
        sdoc.addField("type", TYPE_DESC);
        sdoc.addField("model", model);
        sdoc.addField("dc.title", title);
        sdoc.addField("ref", ref);
        sdoc.addField("date", date);
        sdoc.addField("pid", TYPE_DESC + "|" + sourcePid);
        return feedDescriptionDocument(sdoc);
    }

    private UpdateResponse feedDescriptionDocument(SolrInputDocument doc) {
        try {
            UpdateResponse response = this.solrClient.add(doc);
            return response;
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    private UpdateResponse feedRelationDocument(String sourcePid, String relation, String targetPid) {
        SolrInputDocument sdoc = new SolrInputDocument();
        sdoc.addField("source", sourcePid);
        sdoc.addField("type", TYPE_RELATION);
        sdoc.addField("relation", relation);
        sdoc.addField("targetPid", targetPid);
        sdoc.addField("pid", TYPE_RELATION + "|" + sourcePid + "|" + relation + "|" + targetPid);
        return feedRelationDocument(sdoc);
    }

    private UpdateResponse feedRelationDocument(SolrInputDocument sdoc) {
        try {
            UpdateResponse resp = this.solrClient.add(sdoc);
            return resp;
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void deleteProcessingIndex() {
        try {
            UpdateResponse response = this.solrClient.deleteByQuery("*:*");
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void deleteByPid(String pid) {
        try {
            UpdateResponse response = this.solrClient.deleteByQuery("source:\"" + pid + "\"");
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void deleteByTargetPid(String pid) {
        try {
            UpdateResponse response = this.solrClient.deleteByQuery("targetPid:\"" + pid + "\"");
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void deleteDescriptionByPid(String pid) {
        try {
            UpdateResponse response = this.solrClient.deleteByQuery("source:\"" + pid + "\" AND type:\"description\"");
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void deleteByRelationsForPid(String pid) {
        try {
            String query = "source:\"" + pid + "\" AND type:\"relation\"";
            UpdateResponse response = this.solrClient.deleteByQuery(query);
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void rebuildProcessingIndex(String pid) {
        try {
            InputStream inputStream = coreRepository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT.toString());
            String s = IOUtils.toString(inputStream, "UTF-8");
            RELSEXTSPARQLBuilder sparqlBuilder = new RELSEXTSPARQLBuilderImpl();
            sparqlBuilder.sparqlProps(s.trim(), (object, localName) -> {
                processRELSEXTRelationAndFeedProcessingIndex(pid, object, localName);
                return object;
            });
        } catch (Exception e) {
            throw new RepositoryException(e);
        } finally {
            try {
                this.commit();
            } catch (Exception e) {
                throw new RepositoryException(e);
            }
        }
    }

    /**
     * Process one relation and feed processing index
     */
    private void processRELSEXTRelationAndFeedProcessingIndex(String pid, String object, String localName) {
        if (localName.equals("hasModel")) {
            try {
                boolean dcStreamExists = coreRepository.datastreamExists(pid, KnownDatastreams.BIBLIO_DC.name());
                // TODO: Biblio mods ukladat jinam ??
                boolean modsStreamExists = coreRepository.datastreamExists(pid, KnownDatastreams.BIBLIO_MODS.name());
                if (dcStreamExists || modsStreamExists) {
                    try {
                        //LOGGER.info("DC or BIBLIOMODS exists");
                        if (dcStreamExists) {
                            List<String> dcTList = dcTitle(pid);
                            if (dcTList != null && !dcTList.isEmpty()) {
                                this.indexDescription(pid, object, dcTList.stream().collect(Collectors.joining(" ")));
                            } else {
                                this.indexDescription(pid, object, "");
                            }
                        } else if (modsStreamExists) {
                            // czech title or default
                            List<String> modsTList = modsTitle(pid, "cze");
                            if (modsTList != null && !modsTList.isEmpty()) {
                                this.indexDescription(pid, object, modsTList.stream().collect(Collectors.joining(" ")), ProcessingIndexSolr.TitleType.mods);
                            } else {
                                this.indexDescription(pid, object, "");
                            }
                        }
                    } catch (ParserConfigurationException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        this.indexDescription(pid, object, "");
                    } catch (SAXException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        this.indexDescription(pid, object, "");
                    }
                } else {
                    LOGGER.info("Index description without dc or mods");
                    this.indexDescription(pid, object, "");
                }
            } catch (Throwable th) {
                LOGGER.log(Level.SEVERE, "Cannot update processing index for "+ pid + " - reindex manually.", th);
            }
        } else {
            try {
                this.feedRelationDocument(pid, localName, object);
            } catch (Throwable th) {
                LOGGER.log(Level.SEVERE, "Cannot update processing index for "+ pid + " - reindex manually.", th);
            }
        }
    }

    private void indexDescription(String pid, String model, String title, ProcessingIndex.TitleType ttype) throws IOException, SolrServerException {
        this.feedDescriptionDocument(pid, model, title.trim(), RepositoryUtils.getAkubraInternalId(pid), new Date(), ttype);
    }

    private void indexDescription(String pid, String model, String title) throws IOException, SolrServerException {
        this.feedDescriptionDocument(pid, model, title.trim(), RepositoryUtils.getAkubraInternalId(pid), new Date());
    }

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
                if (element.getNamespaceURI().equals(RepositoryNamespaces.BIBILO_MODS_URI)) {
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
                    if (element.getNamespaceURI().equals(RepositoryNamespaces.BIBILO_MODS_URI)) {
                        // TODO: Change it
                        if (element.getLocalName().equals("title")) {
                            return true;
                        }
                    }
                    return false;
                }
            });

        }

        return elements.stream().map(Element::getTextContent).collect(Collectors.toList());

    }

    @Override
    public void commit() {
        try {
            this.solrClient.commit();
            LOGGER.info("Processing index commit ");
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

}
