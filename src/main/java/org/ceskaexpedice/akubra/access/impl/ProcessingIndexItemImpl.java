package org.ceskaexpedice.akubra.access.impl;

import org.apache.solr.common.SolrDocument;
import org.ceskaexpedice.akubra.access.ProcessingIndexItem;

// TODO get rid of SolrDocument here
public class ProcessingIndexItemImpl implements ProcessingIndexItem {
    private SolrDocument document;

    public ProcessingIndexItemImpl(SolrDocument document) {
        this.document = document;
    }

    public SolrDocument getDocument() {
        return document;
    }
}
