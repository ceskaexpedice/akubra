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
package org.ceskaexpedice.akubra.core.repository;

import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexItem;
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexQueryParameters;

import java.io.InputStream;
import java.util.function.Consumer;

/**
 * Interface for interacting with the processing index of Akubra objects.
 * The processing index is an implementation of a Solr index that stores relations between Akubra objects.
 * It also supports descriptions related to the objects. The index allows for efficient querying, updating,
 * and deletion of entries related to object relations and descriptions.
 */
public interface ProcessingIndexFeeder {
    // Constants representing types of index entries (relations and descriptions).
    String TYPE_RELATION = "relation";
    String TYPE_DESC = "description";
    String UNIQUE_KEY = "pid";
    String CURSOR_MARK_PARAM = "cursorMark";
    String CURSOR_MARK_NEXT = "nextCursorMark";
    String CURSOR_MARK_START = "*";

    /**
     * Enumeration of possible title types for indexing.
     * Can be used to define the type of metadata (e.g., Dublin Core (dc) or MODS (mods)).
     */
    enum TitleType {
        dc, mods;
    }

    /**
     * Deletes processing index entries related to the given PID (Persistent Identifier).
     * Specifically targets relations involving the given object as a source.
     *
     * @param pid The unique identifier of the object whose relations are to be deleted.
     */
    void deleteByRelationsForPid(String pid);

    /**
     * Deletes all processing index entries related to the given PID.
     *
     * @param pid The unique identifier of the object whose entries are to be deleted.
     */
    void deleteByPid(String pid);

    /**
     * Deletes processing index entries where the given PID is the target of relations.
     *
     * @param pid The unique identifier of the object whose target relations are to be deleted.
     */
    void deleteByTargetPid(String pid);

    /**
     * Deletes description entries from the processing index for the given PID.
     *
     * @param pid The unique identifier of the object whose description entries are to be deleted.
     */
    void deleteDescriptionByPid(String pid);

    /**
     * Rebuilds the processing index for a given repository object.
     * This method is typically used when the repository object is being updated or ingested.
     *
     * @param repositoryObject The repository object whose index entries need to be rebuilt.
     * @param input The input stream representing the updated object.
     */
    void rebuildProcessingIndex(RepositoryObject repositoryObject, InputStream input);

    /**
     * Iterates over processing index items based on the provided query parameters.
     * For each item found, the provided action is executed.
     *
     * @param params The query parameters used to filter processing index entries.
     * @param action The action to be performed on each processing index item.
     */
    void iterate(ProcessingIndexQueryParameters params, Consumer<ProcessingIndexItem> action);

    /**
     * Commits the changes to the processing index.
     * This method ensures that any updates or deletions made to the index are finalized.
     */
    void commit();
}
