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
package org.ceskaexpedice.akubra.impl.utils;


import org.apache.solr.common.SolrDocument;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.core.repository.CoreRepository;
import org.ceskaexpedice.akubra.impl.utils.relsext.RelsExtInternalDomUtils;
import org.ceskaexpedice.akubra.processingindex.*;
import org.ceskaexpedice.akubra.relsext.KnownRelations;
import org.ceskaexpedice.akubra.utils.StringUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Utils for various Processing Index related tasks
 */
public final class ProcessingIndexUtils {
    public static final Logger LOGGER = Logger.getLogger(RelsExtInternalDomUtils.class.getName());

    private static List<KnownRelations> OWN_RELATIONS = Arrays.asList(new KnownRelations[]{
            KnownRelations.HAS_PAGE, KnownRelations.HAS_UNIT, KnownRelations.HAS_VOLUME, KnownRelations.HAS_ITEM,
            KnownRelations.HAS_SOUND_UNIT, KnownRelations.HAS_TRACK, KnownRelations.CONTAINS_TRACK, KnownRelations.HAS_INT_COMP_PART
    });
    private static List<KnownRelations> FOSTER_RELATIONS = Arrays.asList(new KnownRelations[]{
            KnownRelations.IS_ON_PAGE, KnownRelations.CONTAINS
    });

    private ProcessingIndexUtils() {
    }

    // ---------- parents ----------------------------------

    public static List<ProcessingIndexItem>  getParents(String targetPid, CoreRepository coreRepository) {
        final List<ProcessingIndexItem> retvals = new ArrayList<>();
        iterateSectionOfProcessingSortedByFieldWithCursor("targetPid:\"" + targetPid + "\"", "pid", true, ProcessingIndex.CURSOR_MARK_START,
                Integer.MAX_VALUE, (doc) -> {
                    retvals.add(doc);
                }, coreRepository);
        return retvals;
    }

    public static List<ProcessingIndexItem> getParents(String relation, String targetPid, CoreRepository coreRepository) {
        List<ProcessingIndexItem> pids = new ArrayList<>();
        String query = String.format("relation:%s AND targetPid:%s", relation, targetPid.replace(":", "\\:"));
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .sortField("date")
                .ascending(true)
                .cursorMark(ProcessingIndex.CURSOR_MARK_START)
                .rows(Integer.MAX_VALUE)
                .fieldsToFetch(List.of("source"))
                .build();
        coreRepository.getProcessingIndex().iterate(params, processingIndexItem -> {
            pids.add(processingIndexItem);
        });
        return pids;
    }

    public static OwnedAndFosteredParents getParentsRelation(String targetPid, CoreRepository coreRepository) {
        List<ProcessingIndexItem> pseudoparentProcessingIndexRelations = getParentsForTarget(targetPid, coreRepository);
        ProcessingIndexItem ownParentProcessingIndexRelation = null;
        List<ProcessingIndexItem> fosterParentProcessingIndexRelations = new ArrayList<>();
        for (ProcessingIndexItem processingIndexRelation : pseudoparentProcessingIndexRelations) {
            if (isOwnRelation(processingIndexRelation.relation())) {
                if (ownParentProcessingIndexRelation != null) {
                    throw new RepositoryException(String.format("found multiple own parent relations: %s and %s", ownParentProcessingIndexRelation, processingIndexRelation));
                } else {
                    ownParentProcessingIndexRelation = processingIndexRelation;
                }
            } else {
                fosterParentProcessingIndexRelations.add(processingIndexRelation);
            }
        }
        return new OwnedAndFosteredParents(ownParentProcessingIndexRelation, fosterParentProcessingIndexRelations);
    }

    // ------------- children ----------------------------------

    public static List<ProcessingIndexItem> getChildren(String relation, String sourcePid, CoreRepository coreRepository) {
        List<ProcessingIndexItem> processingIndexItems = new ArrayList<>();
        String query = String.format("source:%s AND relation:%s", sourcePid.replace(":", "\\:"), relation);
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .sortField("date")
                .ascending(true)
                .cursorMark(ProcessingIndex.CURSOR_MARK_START)
                .rows(Integer.MAX_VALUE)
                .fieldsToFetch(List.of("targetPid"))
                .build();
        coreRepository.getProcessingIndex().iterate(params, processingIndexItem -> {
            processingIndexItems.add(processingIndexItem);
        });
        return processingIndexItems;
    }

    public static OwnedAndFosteredChildren getChildrenRelation(String sourcePid, CoreRepository coreRepository) {
        List<ProcessingIndexItem> pseudochildrenTriplets = getChildrenForSource(sourcePid, coreRepository);
        List<ProcessingIndexItem> ownChildrenTriplets = new ArrayList<>();
        List<ProcessingIndexItem> fosterChildrenTriplets = new ArrayList<>();
        for (ProcessingIndexItem triplet : pseudochildrenTriplets) {
            if (triplet.targetPid() != null && triplet.targetPid().startsWith("uuid:")) {
                if (isOwnRelation(triplet.relation())) {
                    ownChildrenTriplets.add(triplet);
                } else {
                    fosterChildrenTriplets.add(triplet);
                }
            }
        }
        return new OwnedAndFosteredChildren(ownChildrenTriplets, fosterChildrenTriplets);
    }

    // -------------- model ------------------------------

    public static String getModel(String objectPid, CoreRepository coreRepository) {
        ProcessingIndexItem description = getDescription(objectPid, coreRepository);
        if(description == null) {
            return null;
        }
        String model = description.model();
        return model == null ? null : model.substring("model:".length());
    }

    public static CursorItemsPair getByModelWithCursor(String model, boolean ascendingOrder, String cursor, int limit, CoreRepository coreRepository) {
        List<ProcessingIndexItem> items = new ArrayList<>();
        String query = String.format("type:description AND model:%s", "model\\:" + model); //prvni "model:" je filtr na solr pole, druhy "model:" je hodnota pole, coze je mozna zbytecne (ten prefix)
        String nextCursorMark = iterateSectionOfProcessingSortedByFieldWithCursor(query, "dc.title", ascendingOrder, cursor, limit, (doc) -> {
            items.add(doc);
        }, coreRepository);
        CursorItemsPair result = new CursorItemsPair(nextCursorMark, items);
        return result;
    }

    public static SizeItemsPair getByModel(String model, String titlePrefix, int rows, int pageIndex, CoreRepository coreRepository) {
        String query = String.format("type:description AND model:%s", "model\\:" + model);
        if (StringUtils.isAnyString(titlePrefix)) {
            query = String.format("type:description AND model:%s AND title_edge:%s", "model\\:" + model, titlePrefix); //prvni "model:" je filtr na solr pole, druhy "model:" je hodnota pole, coze  uprime zbytecne
        }
        SizeItemsPair sizeItemsPair = getPageSortedByTitle(query, rows, pageIndex, Arrays.asList("source"), coreRepository);
        return sizeItemsPair;
    }

    public static List<ProcessingIndexItem> getByModel(String model, boolean ascendingOrder, int offset, int limit, CoreRepository coreRepository) {
        List<ProcessingIndexItem> titlePidPairs = new ArrayList<>();
        String query = String.format("type:description AND model:%s", "model\\:" + model); //prvni "model:" je filtr na solr pole, druhy "model:" je hodnota pole, coze je mozna zbytecne (ten prefix)
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .sortField("title")
                .ascending(ascendingOrder)
                .rows(limit)
                .offset(offset)
                .fieldsToFetch(List.of("source", "dc.title"))
                .build();
        coreRepository.getProcessingIndex().iterate(params, processingIndexItem -> {
            titlePidPairs.add(processingIndexItem);
        });
        return titlePidPairs;
    }

    private static ProcessingIndexItem getDescription(String objectPid, CoreRepository coreRepository) {
        final ProcessingIndexItem[] processingIndexItemRetVal = {null};
        String query = String.format("type:description AND source:%s", objectPid.replace(":", "\\:"));
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .sortField("pid")
                .ascending(true)
                .cursorMark("*")
                .rows(100)
                .build();
        coreRepository.getProcessingIndex().iterate(params, processingIndexItem -> {
            processingIndexItemRetVal[0] = processingIndexItem;
        });
        return processingIndexItemRetVal[0];
    }

    private static List<ProcessingIndexItem> getChildrenForSource(String sourcePid, CoreRepository coreRepository) {
        List<ProcessingIndexItem> triplets = new ArrayList<>();
        String query = String.format("source:%s", sourcePid.replace(":", "\\:"));
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .sortField("date")
                .ascending(true)
                .cursorMark("*")
                .rows(100)
                .fieldsToFetch(List.of("targetPid", "relation"))
                .build();
        coreRepository.getProcessingIndex().iterate(params, processingIndexItem -> {
            triplets.add(processingIndexItem);
        });
        return triplets;
    }

    private static List<ProcessingIndexItem> getParentsForTarget(String targetPid, CoreRepository coreRepository) {
        List<ProcessingIndexItem> processingIndexRelations = new ArrayList<>();
        String query = String.format("targetPid:%s", targetPid.replace(":", "\\:"));
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .sortField("date")
                .ascending(true)
                .cursorMark(ProcessingIndex.CURSOR_MARK_START)
                .rows(Integer.MAX_VALUE)
                .fieldsToFetch(List.of("source", "relation"))
                .build();
        coreRepository.getProcessingIndex().iterate(params, processingIndexItem -> {
            processingIndexRelations.add(processingIndexItem);
        });
        return processingIndexRelations;
    }

    private static SizeItemsPair getPageSortedByTitle(String query, int rows, int pageIndex, List<String> fieldList,
                                                                              CoreRepository coreRepository) {
        List<ProcessingIndexItem> docs = new ArrayList<>();
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .sortField("title")
                .ascending(true)
                .rows(rows)
                .pageIndex(pageIndex)
                .fieldsToFetch(fieldList)
                .build();
        coreRepository.getProcessingIndex().iterate(params, processingIndexItem -> {
            docs.add(processingIndexItem);
        });
        return new SizeItemsPair(Long.valueOf(docs.size()), docs); // TODO total size?
    }

    private static String iterateSectionOfProcessingSortedByFieldWithCursor(String query, String sortField, boolean ascending, String cursor,
                                                                            int limit, Consumer<ProcessingIndexItem> action, CoreRepository coreRepository) {
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .sortField(sortField)
                .ascending(ascending)
                .rows(limit)
                .cursorMark(cursor)
                .stopAfterCursorMark(true)
                .build();
        return coreRepository.getProcessingIndex().iterate(params, processingIndexItem -> {
            action.accept(processingIndexItem);
        });
    }

    private static boolean isOwnRelation(String relation) {
        for (KnownRelations knownRelation : OWN_RELATIONS) {
            if (relation.equals(knownRelation.toString())) {
                return true;
            }
        }
        for (KnownRelations knownRelation : FOSTER_RELATIONS) {
            if (relation.equals(knownRelation.toString())) {
                return false;
            }
        }
        throw new IllegalArgumentException(String.format("unknown relation '%s'", relation));
    }

    public static ProcessingIndexItem fromSolrDocument(SolrDocument doc) {
        return new ProcessingIndexItem(
                (String) doc.getFieldValue("source"),
                (String) doc.getFieldValue("type"),
                (String) doc.getFieldValue("model"),
                (String) doc.getFieldValue("dc.title"),
                (String) doc.getFieldValue("title"),
                (String) doc.getFieldValue("ref"),
                (Date) doc.getFieldValue("date"),
                (String) doc.getFieldValue("pid"),
                (String) doc.getFieldValue("relation"),
                (String) doc.getFieldValue("targetPid")
        );
    }

}
