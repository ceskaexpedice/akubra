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

import org.apache.solr.common.SolrDocument;

import java.util.Collection;
import java.util.Optional;

/**
 * Represents an item in the processing index, encapsulating a Solr document.
 * Provides methods to retrieve field values from the document, either as raw objects
 * or cast to a specific type.
 *
 * This class offers functionality to retrieve Solr document field values in a safe way,
 * either returning them directly or wrapped in an {@link Optional} if the value is not found
 * or not of the expected type.
 *
 * @author petr
 */
public class ProcessingIndexItem {

    private SolrDocument solrDocument;

    /**
     * Constructs a ProcessingIndexItem with the given SolrDocument.
     *
     * @param document The SolrDocument to be associated with this item.
     */
    ProcessingIndexItem(SolrDocument document) {
        this.solrDocument = document;
    }

    /**
     * Retrieves the value of the specified field from the SolrDocument.
     *
     * @param fieldName The name of the field whose value is to be retrieved.
     * @return The value of the field, or {@code null} if the field is not present.
     */
    public Object getFieldValue(String fieldName) {
        return solrDocument.getFieldValue(fieldName);
    }

    /**
     * Retrieves the value of the specified field from the SolrDocument and casts it
     * to the given type, wrapped in an {@link Optional}.
     *
     * This method avoids {@link ClassCastException} by checking the type of the field
     * value before casting.
     *
     * @param fieldName The name of the field whose value is to be retrieved.
     * @param type The class type to which the field value should be cast.
     * @param <T> The type of the field value.
     * @return An {@link Optional} containing the field value if it exists and can be cast
     *         to the specified type, otherwise an empty {@link Optional}.
     */
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

    /**
     * @return
     */
    public Collection<String> getFieldNames() {
        return solrDocument.getFieldNames();
    }

}
