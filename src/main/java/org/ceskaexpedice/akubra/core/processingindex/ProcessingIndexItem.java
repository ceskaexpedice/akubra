package org.ceskaexpedice.akubra.core.processingindex;

import org.apache.solr.common.SolrDocument;

import java.util.Optional;

/**
 * ProcessingIndexItemImpl
 *
 * @author petr
 */
public class ProcessingIndexItem {
    private SolrDocument solrDocument;

    ProcessingIndexItem(SolrDocument document) {
        this.solrDocument = document;
    }

    public Object getFieldValue(String fieldName) {
        return solrDocument.getFieldValue(fieldName);
    }

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
