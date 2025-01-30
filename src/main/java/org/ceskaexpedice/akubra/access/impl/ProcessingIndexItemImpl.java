package org.ceskaexpedice.akubra.access.impl;

import org.apache.solr.common.SolrDocument;
import org.ceskaexpedice.akubra.access.ProcessingIndexItem;

import java.util.Optional;

/**
 * ProcessingIndexItemImpl
 *
 * @author petr
 */
class ProcessingIndexItemImpl implements ProcessingIndexItem {
    private SolrDocument solrDocument;

    ProcessingIndexItemImpl(SolrDocument document) {
        this.solrDocument = document;
    }

    @Override
    public Object getFieldValue(String fieldName) {
        return solrDocument.getFieldValue(fieldName);
    }

    @Override
    public <T> Optional<T> getFieldValueAs(String fieldName, Class<T> type) {
        Object value = solrDocument.getFieldValue(fieldName);
        if (value == null) {
            return Optional.empty();
        }
        if (type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }
        return Optional.empty(); // Avoids ClassCastException
    }
}
