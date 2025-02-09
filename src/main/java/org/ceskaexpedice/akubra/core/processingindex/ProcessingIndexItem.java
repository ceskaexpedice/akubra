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
