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

/**
 * Represents the query parameters for processing an index.
 * This class encapsulates the search parameters, including the query string, sorting options, pagination, and the fields to fetch.
 * It follows the builder pattern for flexible and readable query construction.
 *
 * @author pavels
 */
public class ProcessingIndexQueryParameters {

    private final String queryString;
    private final String sortField;
    private final boolean ascending;
    private final int rows;
    private final int pageIndex;
    private final String cursorMark;
    private final List<String> fieldsToFetch;

    /**
     * Private constructor to enforce the use of the Builder pattern.
     *
     * @param builder The Builder instance containing the parameters to set.
     */
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

    /**
     * Gets the query string for the search.
     *
     * @return The query string.
     */
    public String getQueryString() {
        return queryString;
    }

    /**
     * Gets the field name to sort by.
     *
     * @return The sort field.
     */
    public String getSortField() {
        return sortField;
    }

    /**
     * Checks if the sorting order is ascending.
     *
     * @return {@code true} if the sorting is ascending, {@code false} otherwise.
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * Gets the number of rows to be returned in the result set.
     *
     * @return The number of rows.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Gets the page index for pagination.
     *
     * @return The page index.
     */
    public int getPageIndex() {
        return pageIndex;
    }

    /**
     * Gets the cursor mark for paginated results.
     *
     * @return The cursor mark.
     */
    public String getCursorMark() {
        return cursorMark;
    }

    /**
     * Gets the list of fields to fetch.
     *
     * @return A list of field names to be fetched.
     */
    public List<String> getFieldsToFetch() {
        return fieldsToFetch;
    }

    // Builder class

    /**
     * Builder for creating instances of {@link ProcessingIndexQueryParameters}.
     * Provides methods for setting query parameters and constructing the final query object.
     */
    public static class Builder {
        private String queryString;
        private String sortField;
        private boolean ascending = true; // Default sort order
        private int rows = 10;            // Default rows per page
        private int pageIndex = 0;       // Default page index
        private String cursorMark;
        private final List<String> fieldsToFetch = new ArrayList<>();

        /**
         * Sets the query string for the search.
         *
         * @param queryString The query string to set.
         * @return The builder instance for chaining.
         */
        public Builder queryString(String queryString) {
            this.queryString = queryString;
            return this;
        }

        /**
         * Sets the field to sort by.
         *
         * @param sortField The field name to sort by.
         * @return The builder instance for chaining.
         */
        public Builder sortField(String sortField) {
            this.sortField = sortField;
            return this;
        }

        /**
         * Sets the sorting order.
         *
         * @param ascending {@code true} for ascending order, {@code false} for descending.
         * @return The builder instance for chaining.
         */
        public Builder ascending(boolean ascending) {
            this.ascending = ascending;
            return this;
        }

        /**
         * Sets the number of rows to return in the result set.
         *
         * @param rows The number of rows.
         * @return The builder instance for chaining.
         */
        public Builder rows(int rows) {
            this.rows = rows;
            return this;
        }

        /**
         * Sets the page index for pagination.
         *
         * @param pageIndex The page index.
         * @return The builder instance for chaining.
         */
        public Builder pageIndex(int pageIndex) {
            this.pageIndex = pageIndex;
            return this;
        }

        /**
         * Sets the cursor mark for paginated results.
         *
         * @param cursorMark The cursor mark.
         * @return The builder instance for chaining.
         */
        public Builder cursorMark(String cursorMark) {
            this.cursorMark = cursorMark;
            return this;
        }

        /**
         * Adds a field to the list of fields to fetch.
         *
         * @param field The field name to add.
         * @return The builder instance for chaining.
         */
        public Builder addFieldToFetch(String field) {
            this.fieldsToFetch.add(field);
            return this;
        }

        /**
         * Adds multiple fields to the list of fields to fetch.
         *
         * @param fields The list of field names to add.
         * @return The builder instance for chaining.
         */
        public Builder fieldsToFetch(List<String> fields) {
            this.fieldsToFetch.addAll(fields);
            return this;
        }

        /**
         * Builds and returns an instance of {@link ProcessingIndexQueryParameters}.
         *
         * @return The constructed query parameters.
         */
        public ProcessingIndexQueryParameters build() {
            return new ProcessingIndexQueryParameters(this);
        }
    }
}
