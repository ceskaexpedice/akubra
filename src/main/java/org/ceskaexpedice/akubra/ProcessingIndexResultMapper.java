package org.ceskaexpedice.akubra;

import org.apache.solr.common.SolrDocument;

import java.util.List;

@FunctionalInterface
public interface ProcessingIndexResultMapper<T> {

    T map(List<ProcessingIndexItem> documents, long totalRecords);

}