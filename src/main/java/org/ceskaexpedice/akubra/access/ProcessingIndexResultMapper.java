package org.ceskaexpedice.akubra.access;

import java.util.List;

@FunctionalInterface
public interface ProcessingIndexResultMapper<T> {

    T map(List<ProcessingIndexItem> documents, long totalRecords);

}