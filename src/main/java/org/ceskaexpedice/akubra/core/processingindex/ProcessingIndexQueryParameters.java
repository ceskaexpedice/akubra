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

import java.util.ArrayList;
import java.util.List;

public class ProcessingIndexQueryParameters {
    private final String queryString;
    private final String sortField;
    private final boolean ascending;
    private final int rows;
    private final int pageIndex;
    private final String cursorMark;
    private final List<String> fieldsToFetch;

    // Private constructor to enforce the use of the Builder
    private ProcessingIndexQueryParameters(Builder builder) {
        this.queryString = builder.queryString;
        this.sortField = builder.sortField;
        this.ascending = builder.ascending;
        this.rows = builder.rows;
        this.pageIndex = builder.pageIndex;
        this.cursorMark = builder.cursorMark;
        this.fieldsToFetch = builder.fieldsToFetch;
    }

    // Getters
    public String getQueryString() {
        return queryString;
    }

    public String getSortField() {
        return sortField;
    }

    public boolean isAscending() {
        return ascending;
    }

    public int getRows() {
        return rows;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public String getCursorMark() {
        return cursorMark;
    }

    public List<String> getFieldsToFetch() {
        return fieldsToFetch;
    }

    // Builder class
    public static class Builder {
        private String queryString;
        private String sortField;
        private boolean ascending = true; // Default sort order
        private int rows = 10;            // Default rows
        private int pageIndex = 0;       // Default page index
        private String cursorMark;
        private final List<String> fieldsToFetch = new ArrayList<>();

        public Builder queryString(String queryString) {
            this.queryString = queryString;
            return this;
        }

        public Builder sortField(String sortField) {
            this.sortField = sortField;
            return this;
        }

        public Builder ascending(boolean ascending) {
            this.ascending = ascending;
            return this;
        }

        public Builder rows(int rows) {
            this.rows = rows;
            return this;
        }

        public Builder pageIndex(int pageIndex) {
            this.pageIndex = pageIndex;
            return this;
        }

        public Builder cursorMark(String cursorMark) {
            this.cursorMark = cursorMark;
            return this;
        }

        public Builder addFieldToFetch(String field) {
            this.fieldsToFetch.add(field);
            return this;
        }

        public Builder fieldsToFetch(List<String> fields) {
            this.fieldsToFetch.addAll(fields);
            return this;
        }

        public ProcessingIndexQueryParameters build() {
            return new ProcessingIndexQueryParameters(this);
        }
    }
}
