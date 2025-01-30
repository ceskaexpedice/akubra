package org.ceskaexpedice.akubra.core.processingindex;

import org.ceskaexpedice.akubra.core.repository.KnownDatastreams;
import org.ceskaexpedice.akubra.core.repository.impl.RepositoryUtils;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.ceskaexpedice.akubra.core.repository.RepositoryNamespaces;
import org.ceskaexpedice.akubra.core.repository.RepositoryException;
import org.ceskaexpedice.akubra.core.repository.RepositoryObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CursorMarkParams;
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
public class ProcessingIndexFeeder {
    
    private enum TitleType {
        dc,mods;
    }
    private static final String TYPE_RELATION = "relation";
    private static final String TYPE_DESC = "description";

    private static final Logger LOGGER = Logger.getLogger(ProcessingIndexFeeder.class.getName());
    private SolrClient solrClient;

    public ProcessingIndexFeeder(SolrClient solrClient) {
        super();
        this.solrClient = solrClient;
    }

    // TODO maybe we need also a method to get back next cursor mark
    public void iterate(ProcessingIndexQueryParameters params, Consumer<ProcessingIndexItem> action) {
        try {
            SolrQuery solrQuery = new SolrQuery(params.getQueryString());
            solrQuery.setSort(params.getSortField(), params.isAscending() ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
            solrQuery.setRows(params.getRows());
            if(params.getCursorMark() == null) {
                int offset = params.getPageIndex() * params.getRows();
                solrQuery.setStart(offset);
                QueryResponse response = this.solrClient.query(solrQuery);
                response.getResults().forEach((doc) -> {
                    action.accept(new ProcessingIndexItem(doc));
                });
            }else{
                String cursorMark = params.getCursorMark();
                boolean done = false;
                while (!done) {
                    solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
                    QueryResponse response = this.solrClient.query(solrQuery);
                    String nextCursorMark = response.getNextCursorMark();
                    response.getResults().forEach((doc) -> {
                        action.accept(new ProcessingIndexItem(doc));
                    });
                    if (cursorMark.equals(nextCursorMark)) {
                        done = true;
                    }
                    cursorMark = nextCursorMark;
                }
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    // TODO check update methods
    public UpdateResponse feedDescriptionDocument(String sourcePid, String model, String title, String ref, Date date, TitleType ttype) throws IOException, SolrServerException {

        //String processingSolrHost = KConfiguration.getInstance().getSolrProcessingHost();

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
    public UpdateResponse feedDescriptionDocument(String sourcePid, String model, String title, String ref, Date date) throws IOException, SolrServerException {
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
    public UpdateResponse feedDescriptionDocument(SolrInputDocument doc) throws IOException, SolrServerException {
        UpdateResponse response = this.solrClient.add(doc);
        return response;
    }

    public UpdateResponse feedRelationDocument(String sourcePid, String relation, String targetPid) throws IOException, SolrServerException {
        SolrInputDocument sdoc = new SolrInputDocument();
        sdoc.addField("source", sourcePid);
        sdoc.addField("type", TYPE_RELATION);
        sdoc.addField("relation", relation);
        sdoc.addField("targetPid", targetPid);
        sdoc.addField("pid", TYPE_RELATION + "|" + sourcePid + "|" + relation + "|" + targetPid);
        return feedRelationDocument(sdoc);
    }
    public UpdateResponse feedRelationDocument(SolrInputDocument sdoc) throws IOException, SolrServerException {
        UpdateResponse resp = this.solrClient.add(sdoc);
        return resp;
    }

    public UpdateResponse deleteProcessingIndex() throws IOException, SolrServerException {
        UpdateResponse response = this.solrClient.deleteByQuery("*:*");
        return response;
    }
    public UpdateResponse deleteByPid(String pid) throws IOException, SolrServerException {
        UpdateResponse response = this.solrClient.deleteByQuery("source:\"" + pid + "\"");
        return response;
    }
    public UpdateResponse deleteByTargetPid(String pid) throws IOException, SolrServerException {
        UpdateResponse response = this.solrClient.deleteByQuery("targetPid:\"" + pid + "\"");
        return response;
    }
    public UpdateResponse deleteDescriptionByPid(String pid) throws IOException, SolrServerException {
        UpdateResponse response = this.solrClient.deleteByQuery("source:\"" + pid + "\" AND type:\"description\"");
        return response;
    }
    public UpdateResponse deleteByRelationsForPid(String pid) throws IOException, SolrServerException {
        String query = "source:\"" + pid + "\" AND type:\"relation\"";
        UpdateResponse response = this.solrClient.deleteByQuery(query);
        return response;
    }

    public void rebuildProcessingIndex(RepositoryObject repositoryObject, InputStream content) throws RepositoryException {
        try {
            String s = IOUtils.toString(content, "UTF-8");
            RELSEXTSPARQLBuilder sparqlBuilder = new RELSEXTSPARQLBuilderImpl();
            sparqlBuilder.sparqlProps(s.trim(), (object, localName) -> {
                processRELSEXTRelationAndFeedProcessingIndex(repositoryObject, object, localName);
                return object;
            });
        } catch (IOException e) {
            throw new RepositoryException(e);
        } catch (SAXException e) {
            throw new RepositoryException(e);
        } catch (ParserConfigurationException e) {
            throw new RepositoryException(e);
        } finally {
            try {
                this.commit();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SolrServerException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Process one relation and feed processing index
     */
    private void processRELSEXTRelationAndFeedProcessingIndex(RepositoryObject repositoryObject, String object, String localName) {
        if (localName.equals("hasModel")) {
            try {
                boolean dcStreamExists = repositoryObject.streamExists(KnownDatastreams.BIBLIO_DC.name());
                // TODO: Biblio mods ukladat jinam ??
                boolean modsStreamExists = repositoryObject.streamExists(KnownDatastreams.BIBLIO_MODS.name());
                if (dcStreamExists || modsStreamExists) {
                    try {
                        //LOGGER.info("DC or BIBLIOMODS exists");
                        if (dcStreamExists) {
                            List<String> dcTList = dcTitle(repositoryObject);
                            if (dcTList != null && !dcTList.isEmpty()) {
                                this.indexDescription(repositoryObject.getPid(), object, dcTList.stream().collect(Collectors.joining(" ")));
                            } else {
                                this.indexDescription(repositoryObject.getPid(), object, "");
                            }
                        } else if (modsStreamExists) {
                            // czech title or default
                            List<String> modsTList = modsTitle(repositoryObject, "cze");
                            if (modsTList != null && !modsTList.isEmpty()) {
                                this.indexDescription(repositoryObject.getPid(), object, modsTList.stream().collect(Collectors.joining(" ")), ProcessingIndexFeeder.TitleType.mods);
                            } else {
                                this.indexDescription(repositoryObject.getPid(), object, "");
                            }
                        }
                    } catch (ParserConfigurationException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        this.indexDescription(repositoryObject.getPid(), object, "");
                    } catch (SAXException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        this.indexDescription(repositoryObject.getPid(), object, "");
                    }
                } else {
                    LOGGER.info("Index description without dc or mods");
                    this.indexDescription(repositoryObject.getPid(), object, "");
                }
            } catch (Throwable th) {
                LOGGER.log(Level.SEVERE, "Cannot update processing index for "+ repositoryObject.getPid() + " - reindex manually.", th);
            }
        } else {
            try {
                this.feedRelationDocument(repositoryObject.getPid(), localName, object);
            } catch (Throwable th) {
                LOGGER.log(Level.SEVERE, "Cannot update processing index for "+ repositoryObject.getPid() + " - reindex manually.", th);
            }
        }
    }

    private void indexDescription(String pid, String model, String title, ProcessingIndexFeeder.TitleType ttype) throws IOException, SolrServerException {
        this.feedDescriptionDocument(pid, model, title.trim(), RepositoryUtils.getAkubraInternalId(pid), new Date(), ttype);
    }

    private void indexDescription(String pid, String model, String title) throws IOException, SolrServerException {
        this.feedDescriptionDocument(pid, model, title.trim(), RepositoryUtils.getAkubraInternalId(pid), new Date());
    }

    private List<String> dcTitle(RepositoryObject repositoryObject) throws RepositoryException, ParserConfigurationException, SAXException, IOException {
        InputStream stream = repositoryObject.getStream(KnownDatastreams.BIBLIO_DC.name()).getLastVersionContent();
        Element title = DomUtils.findElement(DomUtils.parseDocument(stream, true).getDocumentElement(), "title", RepositoryNamespaces.DC_NAMESPACE_URI);
        return title != null ? Arrays.asList(title.getTextContent()) : new ArrayList<>();
    }

    private List<String> modsTitle(RepositoryObject repositoryObject, String lang) throws RepositoryException, ParserConfigurationException, SAXException, IOException {
        InputStream stream = repositoryObject.getStream(KnownDatastreams.BIBLIO_MODS.name()).getLastVersionContent();
        Element docElement = DomUtils.parseDocument(stream, true).getDocumentElement();

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
    // commit to solr
    public void commit() throws IOException, SolrServerException {
        this.solrClient.commit();
        LOGGER.info("Processing index commit ");
    }



}
