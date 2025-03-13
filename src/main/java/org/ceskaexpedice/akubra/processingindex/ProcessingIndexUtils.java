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


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.impl.utils.relsext.RelsExtInternalDomUtils;
import org.ceskaexpedice.akubra.relsext.KnownRelations;
import org.ceskaexpedice.akubra.utils.StringUtils;
import org.json.JSONObject;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

    private ProcessingIndexUtils() {}

    public static Pair<ProcessingIndexRelation, List<ProcessingIndexRelation>> getParents(String objectPid, AkubraRepository akubraRepository) {
        List<ProcessingIndexRelation> pseudoparentProcessingIndexRelations = getTripletSources(objectPid, akubraRepository);
        ProcessingIndexRelation ownParentProcessingIndexRelation = null;
        List<ProcessingIndexRelation> fosterParentProcessingIndexRelations = new ArrayList<>();
        for (ProcessingIndexRelation processingIndexRelation : pseudoparentProcessingIndexRelations) {
            if (isOwnRelation(processingIndexRelation.getRelation())) {
                if (ownParentProcessingIndexRelation != null) {
                    throw new RepositoryException(String.format("found multiple own parent relations: %s and %s", ownParentProcessingIndexRelation, processingIndexRelation));
                } else {
                    ownParentProcessingIndexRelation = processingIndexRelation;
                }
            } else {
                fosterParentProcessingIndexRelations.add(processingIndexRelation);
            }
        }
        return new ImmutablePair<>(ownParentProcessingIndexRelation, fosterParentProcessingIndexRelations);
    }

    public static Pair<List<ProcessingIndexRelation>, List<ProcessingIndexRelation>> getChildren(String objectPid, AkubraRepository akubraRepository) {
        List<ProcessingIndexRelation> pseudochildrenTriplets = getTripletTargets(objectPid, akubraRepository);
        List<ProcessingIndexRelation> ownChildrenTriplets = new ArrayList<>();
        List<ProcessingIndexRelation> fosterChildrenTriplets = new ArrayList<>();
        for (ProcessingIndexRelation triplet : pseudochildrenTriplets) {
            if (triplet.getTarget().startsWith("uuid:")) { //ignore hasDonator and other indexed relations, that are not binding two objects in repository
                if (isOwnRelation(triplet.getRelation())) {
                    ownChildrenTriplets.add(triplet);
                } else {
                    fosterChildrenTriplets.add(triplet);
                }
            }
        }
        return new ImmutablePair<>(ownChildrenTriplets, fosterChildrenTriplets);
    }

    public static String getModel(String objectPid, AkubraRepository akubraRepository) {
        Map<String, String> description = getDescription(objectPid, akubraRepository);
        String model = description.get("model");
        return model == null ? null : model.substring("model:".length());
    }

    private static Map<String, String> getDescription(String objectPid, AkubraRepository akubraRepository) {
        Map<String, String> description = new HashMap<>();
        String query = String.format("type:description AND source:%s", objectPid.replace(":", "\\:"));
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .sortField("pid")
                .ascending(true)
                .cursorMark("*")
                .rows(100)
                .build();
        akubraRepository.pi().iterate(params, processingIndexItem -> {
            for (String name : processingIndexItem.getFieldNames()) {
                description.put(name, processingIndexItem.getFieldValue(name).toString());
            }
        });
        return description;
    }

    private static List<ProcessingIndexRelation> getTripletTargets(String sourcePid, AkubraRepository akubraRepository) {
        List<ProcessingIndexRelation> triplets = new ArrayList<>();
        String query = String.format("source:%s", sourcePid.replace(":", "\\:"));
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .sortField("date")
                .ascending(true)
                .cursorMark("*")
                .rows(100)
                .fieldsToFetch(List.of("targetPid", "relation"))
                .build();
        akubraRepository.pi().iterate(params, processingIndexItem -> {
            Object targetPid = processingIndexItem.getFieldValue("targetPid");
            Object relation = processingIndexItem.getFieldValue("relation");
            if (targetPid != null && relation != null) {
                triplets.add(new ProcessingIndexRelation(sourcePid, relation.toString(), targetPid.toString()));
            }
        });
        return triplets;
    }

    public static List<String> getTripletTargets(String relation, String sourcePid, AkubraRepository akubraRepository){
        List<String> pids = new ArrayList<>();
        String query = String.format("source:%s AND relation:%s", sourcePid.replace(":", "\\:"), relation);
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .sortField("date")
                .ascending(true)
                .cursorMark("*")
                .rows(100)
                .fieldsToFetch(List.of("targetPid"))
                .build();
        akubraRepository.pi().iterate(params, processingIndexItem -> {
            Object targetPid = processingIndexItem.getFieldValue("targetPid");
            pids.add(targetPid.toString());
        });
        return pids;
    }

    private static List<ProcessingIndexRelation> getTripletSources(String targetPid, AkubraRepository akubraRepository) {
        List<ProcessingIndexRelation> processingIndexRelations = new ArrayList<>();
        String query = String.format("targetPid:%s", targetPid.replace(":", "\\:"));
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .sortField("date")
                .ascending(true)
                .cursorMark("*")
                .rows(100)
                .fieldsToFetch(List.of("source", "relation"))
                .build();
        akubraRepository.pi().iterate(params, processingIndexItem -> {
            Object sourcePid = processingIndexItem.getFieldValue("source");
            Object relation = processingIndexItem.getFieldValue("relation");
            if (sourcePid != null && relation != null) {
                processingIndexRelations.add(new ProcessingIndexRelation(sourcePid.toString(), relation.toString(), targetPid));
            }
        });
        return processingIndexRelations;
    }

    public static List<String> getTripletSources(String relation, String targetPid, AkubraRepository akubraRepository){
        List<String> pids = new ArrayList<>();
        String query = String.format("relation:%s AND targetPid:%s", relation, targetPid.replace(":", "\\:"));
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .sortField("date")
                .ascending(true)
                .cursorMark("*")
                .rows(100)
                .fieldsToFetch(List.of("source"))
                .build();
        akubraRepository.pi().iterate(params, processingIndexItem -> {
            Object sourcePid = processingIndexItem.getFieldValue("source");
            pids.add(sourcePid.toString());
        });
        return pids;
    }

    public static Pair<Long, List<String>> getPidsOfObjectsByModel(String model, String titlePrefix, int rows, int pageIndex,
                                                                   AkubraRepository akubraRepository) {
        String query = String.format("type:description AND model:%s", "model\\:" + model);
        if (StringUtils.isAnyString(titlePrefix)) {
            query = String.format("type:description AND model:%s AND title_edge:%s", "model\\:" + model, titlePrefix); //prvni "model:" je filtr na solr pole, druhy "model:" je hodnota pole, coze  uprime zbytecne
        }
        Pair<Long, List<ProcessingIndexItem>> cp = getPageSortedByTitle(query, rows, pageIndex, Arrays.asList("source"), akubraRepository);
        Long numberOfRecords = cp.getLeft();
        List<String> pids = cp.getRight().stream().map(sd -> {
            Object fieldValue = sd.getFieldValue("source");
            return fieldValue.toString();
        }).collect(Collectors.toList());
        return new ImmutablePair<>(numberOfRecords, pids);
    }

    public static List<String> getPidsOfObjectsByModel(String model, AkubraRepository akubraRepository) {
        String query = String.format("type:description AND model:%s", "model\\:" + model);
        // TODO rows, pageIndex
        Pair<Long, List<ProcessingIndexItem>> cp = getPageSortedByTitle(query, Integer.MAX_VALUE, 0, Arrays.asList("source"), akubraRepository);
        List<String> pids = cp.getRight().stream().map(sd -> {
            Object fieldValue = sd.getFieldValue("source");
            return fieldValue.toString();
        }).collect(Collectors.toList());
        return pids;
    }

    public static List<Pair<String, String>> findByTargetPid(String pid, AkubraRepository akubraRepository) {
        final List<Pair<String, String>> retvals = new ArrayList<>();
        iterateSectionOfProcessingSortedByFieldWithCursor("targetPid:\"" + pid + "\"", "pid", true, "*",
                1000, (doc) -> {
            Pair<String, String> pair = new ImmutablePair<>(doc.getFieldValue("source").toString(), doc.getFieldValue("relation").toString());
            retvals.add(pair);
        }, akubraRepository);
        return retvals;
    }

    public static List<Pair<String, String>> getPidsOfObjectsWithTitlesByModel(String model, boolean ascendingOrder, int offset, int limit, AkubraRepository akubraRepository) {
        List<Pair<String, String>> titlePidPairs = new ArrayList<>();
        String query = String.format("type:description AND model:%s", "model\\:" + model); //prvni "model:" je filtr na solr pole, druhy "model:" je hodnota pole, coze je mozna zbytecne (ten prefix)
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .sortField("title")
                .ascending(ascendingOrder)
                .rows(limit)
                .offset(offset)
                .fieldsToFetch(List.of("source", "dc.title"))
                .build();
        akubraRepository.pi().iterate(params, processingIndexItem -> {
            Object fieldPid = processingIndexItem.getFieldValue("source");
            Object fieldTitle = processingIndexItem.getFieldValue("dc.title");
            String pid = null;
            String title = null;
            if (fieldPid != null) {
                pid = fieldPid.toString();
            }
            if (fieldTitle != null) {
                title = fieldTitle.toString().trim();
            }
            titlePidPairs.add(new ImmutablePair<>(title, pid));
        });
        return titlePidPairs;
    }

    public static Pair getPidsOfObjectsWithTitlesByModelWithCursor(String model, boolean ascendingOrder, String cursor, int limit, AkubraRepository akubraRepository){
        List<Pair<String, String>> titlePidPairs = new ArrayList<>();
        String query = String.format("type:description AND model:%s", "model\\:" + model); //prvni "model:" je filtr na solr pole, druhy "model:" je hodnota pole, coze je mozna zbytecne (ten prefix)
        String nextCursorMark = iterateSectionOfProcessingSortedByFieldWithCursor(query, "dc:title", ascendingOrder, cursor, limit, (doc) -> {
            Object fieldPid = doc.getFieldValue("source");
            Object fieldTitle = doc.getFieldValue("dc.title");
            String pid = null;
            String title = null;
            if (fieldPid != null) {
                pid = fieldPid.toString();
            }
            if (fieldTitle != null) {
                title = fieldTitle.toString().trim();
            }
            titlePidPairs.add(new ImmutablePair(title, pid));
        }, akubraRepository);
        Pair result = new ImmutablePair(titlePidPairs, nextCursorMark);
        return result;
    }

    private static Pair<Long, List<ProcessingIndexItem>> getPageSortedByTitle(String query, int rows, int pageIndex, List<String> fieldList,
                                                                              AkubraRepository akubraRepository){
        List<ProcessingIndexItem> docs = new ArrayList<>();
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .sortField("title")
                .ascending(true)
                .rows(rows)
                .pageIndex(pageIndex)
                .fieldsToFetch(fieldList)
                .build();
        akubraRepository.pi().iterate(params, processingIndexItem -> {
            docs.add(processingIndexItem);
        });
        return new ImmutablePair<>(Long.valueOf(docs.size()), docs);
    }

    private static  String iterateSectionOfProcessingSortedByFieldWithCursor(String query, String sortField, boolean ascending, String cursor,
                                                                             int limit, Consumer<ProcessingIndexItem> action, AkubraRepository akubraRepository) {
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .sortField(sortField)
                .ascending(ascending)
                .rows(limit)
                .cursorMark(cursor)
                .stopAfterCursorMark(true)
                .build();
        return akubraRepository.pi().iterate(params, processingIndexItem -> {
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

    public static Pair<String, Set<String>> getPidsOfParents(String pid, AkubraRepository akubraRepository) {
        JsonObject structure = getStructure(pid, akubraRepository);
        JsonObject parentsJson = structure.getAsJsonObject("parents");
        //own
        String ownParent = null;
        if (parentsJson.has("own")) {
            ownParent = parentsJson.getAsJsonObject("own").get("pid").getAsString();
        }
        //foster
        JsonArray fosterParentsJson = parentsJson.getAsJsonArray("foster");
        Set<String> fosterParents = new HashSet<>();
        Iterator<JsonElement> fosterParentsIt = fosterParentsJson.iterator();
        while (fosterParentsIt.hasNext()) {
            fosterParents.add(fosterParentsIt.next().getAsJsonObject().get("pid").getAsString());
        }
        return new ImmutablePair<>(ownParent, fosterParents);
    }

    public static Pair<List<String>, List<String>> getPidsOfChildren(String pid, AkubraRepository akubraRepository) {
        JsonObject structure = getStructure(pid, akubraRepository);
        if (structure != null) {
            JsonObject childrenJson = structure.getAsJsonObject("children");
            //own
            JsonArray ownChildrenJson = childrenJson.getAsJsonArray("own");
            List<String> ownChildren = new ArrayList<>();
            Iterator<JsonElement> ownChildrenIt = ownChildrenJson.iterator();
            while (ownChildrenIt.hasNext()) {
                ownChildren.add(ownChildrenIt.next().getAsJsonObject().get("pid").getAsString());
            }
            //foster
            JsonArray fosterChildrenJson = childrenJson.getAsJsonArray("foster");
            List<String> fosterChildren = new ArrayList<>();
            Iterator<JsonElement> fosterParentsIt = fosterChildrenJson.iterator();
            while (fosterParentsIt.hasNext()) {
                fosterChildren.add(fosterParentsIt.next().getAsJsonObject().get("pid").getAsString());
            }
            return new ImmutablePair<>(ownChildren, fosterChildren);
        } else return new ImmutablePair<>(new ArrayList<>(), new ArrayList<>());
    }

    private static JsonObject getStructure(String pid, AkubraRepository akubraRepository) {
        return fetchStructure(pid, akubraRepository);
    }

    private static JsonObject fetchStructure(String pid, AkubraRepository akubraRepository) {
        try {
            JSONObject extractStructureInfo = StructureInfoDom4jUtils.extractStructureInfo(akubraRepository, pid);
            return StringUtils.stringToJsonObject(extractStructureInfo.toString());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return null;
        }
    }

    public static void doWithProcessingIndexCommit(AkubraRepository rep, OperationsHandler op) throws RepositoryException {
        try {
            op.operations(rep);
        } finally {
            rep.pi().commit();
        }
    }

    public static interface OperationsHandler {
        public void operations(AkubraRepository rep) throws RepositoryException;
    }

}
