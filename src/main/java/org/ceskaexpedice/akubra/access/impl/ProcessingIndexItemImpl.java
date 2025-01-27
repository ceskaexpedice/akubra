package org.ceskaexpedice.akubra.access.impl;

import org.apache.solr.common.SolrDocument;
import org.ceskaexpedice.akubra.access.ProcessingIndexItem;

// TODO get rid of SolrDocument here
class ProcessingIndexItemImpl implements ProcessingIndexItem {
    private SolrDocument document;

    ProcessingIndexItemImpl(SolrDocument document) {
        this.document = document;
    }

    public SolrDocument getDocument() {
        return document;
    }
}
