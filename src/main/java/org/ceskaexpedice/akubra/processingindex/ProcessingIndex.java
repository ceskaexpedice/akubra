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
package org.ceskaexpedice.akubra.processingindex;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.json.JSONObject;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

/**
 * Interface for interacting with the processing index of Akubra objects.
 * The processing index is an implementation of a Solr index that stores relations between Akubra objects.
 * It also supports descriptions related to the objects. The index allows for efficient querying, updating,
 * and deletion of entries related to object relations and descriptions.
 * @author pavels, petrp
 */
public interface ProcessingIndex {
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
     * Iterates over processing index items based on the provided query parameters.
     * For each item found, the provided action is executed.
     *
     * @param params The query parameters used to filter processing index entries.
     * @param action The action to be performed on each processing index item.
     */
    String iterate(ProcessingIndexQueryParameters params, Consumer<ProcessingIndexItem> action);

    /**
     * Performs a lightweight inspection of processing index items based on the provided query parameters.
     * Unlike {@link #iterate}, this method does not use Solr cursor-based pagination and is intended for
     * quickly retrieving a limited number of matching entries..
     *
     * @param params The query parameters used to filter processing index entries.
     * @param action The action to be performed on each of the initially retrieved processing index items.
     */
    void lookAt(ProcessingIndexQueryParameters params, Consumer<ProcessingIndexItem> action);


        /**
         * Retrieves a list of parent items associated with the given target PID.
         *
         * @param targetPid The unique identifier of the target object.
         * @return A list of parent items associated with the given PID.
         */
    List<ProcessingIndexItem>  getParents(String targetPid);

    /**
     * Retrieves a list of parent items associated with the given target PID and relation type.
     *
     * @param relation The type of relation to filter the parents.
     * @param targetPid The unique identifier of the target object.
     * @return A list of parent items matching the specified relation.
     */
    List<ProcessingIndexItem> getParents(String relation, String targetPid);

    /**
     * Retrieves the parents of the given target PID, categorized into "owned" and "fostered" relationships.
     *
     * @param targetPid The unique identifier of the target object.
     * @return An {@code OwnedAndFosteredParents} object containing lists of parents classified by relation type.
     */
    OwnedAndFosteredParents getOwnedAndFosteredParents(String targetPid);

    /**
     * Retrieves a list of child items associated with the given target PID and relation type.
     *
     * @param relation The type of relation to filter the children.
     * @param targetPid The unique identifier of the target object.
     * @return A list of child items matching the specified relation.
     */
    List<ProcessingIndexItem> getChildren(String relation, String targetPid);

    /**
     * Retrieves the children of the given PID, categorized into "owned" and "fostered" relationships.
     *
     * @param pid The unique identifier of the object whose children are to be retrieved.
     * @return An {@code OwnedAndFosteredChildren} object containing lists of children classified by relation type.
     */
    OwnedAndFosteredChildren getOwnedAndFosteredChildren(String pid);

    /**
     * Retrieves the model associated with the given PID.
     *
     * @param pid The unique identifier of the object.
     * @return The model of the object.
     */
    String getModel(String pid);

    /**
     * Retrieves indexed items by model using a cursor-based pagination approach.
     *
     * @param model The model type to filter results.
     * @param ascendingOrder Whether the results should be sorted in ascending order.
     * @param cursor The cursor for pagination.
     * @param limit The maximum number of results to retrieve.
     * @return A pair containing the retrieved items and cursor for further pagination.
     */
    CursorItemsPair getByModelWithCursor(String model, boolean ascendingOrder, String cursor, int limit);

    /**
     * Retrieves indexed items by model with a paginated response.
     *
     * @param model The model type to filter results.
     * @param titlePrefix The title prefix to filter results.
     * @param rows The number of rows per page.
     * @param pageIndex The index of the page to retrieve.
     * @return A pair containing the size of results and the retrieved items.
     */
    SizeItemsPair getByModel(String model, String titlePrefix, int rows, int pageIndex);

    /**
     * Retrieves indexed items by model with offset-based pagination.
     *
     * @param model The model type to filter results.
     * @param ascendingOrder Whether the results should be sorted in ascending order.
     * @param offset The starting index of the results.
     * @param limit The maximum number of results to retrieve.
     * @return A list of items matching the specified model.
     */
    List<ProcessingIndexItem> getByModel(String model, boolean ascendingOrder, int offset, int limit);

    /**
     * Extracts structured information for the given PID.
     *
     * @param pid The unique identifier of the object.
     * @return A JSON object containing the structured information.
     */
    JSONObject extractStructureInfo(String pid);

    /**
     * Returns count of all models
     * @return
     */
    List<Pair<String,Long>> getModelsCount();

    /**
     * Deletes processing index entries related to the given PID (Persistent Identifier).
     * Specifically targets relations involving the given object as a source.
     *
     * @param pid The unique identifier of the object whose relations are to be deleted.
     */
    void deleteByRelationsForPid(String pid);

    /**
     * Deletes all items in the processing index
     * @return
     */
    void deleteProcessingIndex();

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
     * rebuild processing index for the given pid
     * @param pid  The unique identifier of the object whose index entries to rebuild
     */
    void rebuildProcessingIndex(String pid, Consumer<UpdateRequest> updateRequestCustomizer);

    /**
     * Performs operations on the processing index with an explicit commit.
     *
     * @param op The operations handler defining the operations to be executed.
     */
    void doWithCommit(OperationsHandler op);

    /**
     * Commits the changes to the processing index.
     * This method ensures that any updates or deletions made to the index are finalized.
     */
    void commit();
}
