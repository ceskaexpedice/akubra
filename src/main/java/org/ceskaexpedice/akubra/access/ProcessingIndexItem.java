package org.ceskaexpedice.akubra.access;

import java.util.Optional;

public interface ProcessingIndexItem {

    Object getFieldValue(String fieldName);

    <T> Optional<T> getFieldValueAs(String fieldName, Class<T> type);
}
