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
package org.ceskaexpedice.akubra.utils;


import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.ProcessingIndexRelation;
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexQueryParameters;
import org.ceskaexpedice.akubra.core.repository.KnownRelations;
import org.ceskaexpedice.akubra.core.repository.RepositoryException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ProcessingIndexUtils
 */
public final class ProcessingIndexUtils {

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
            if (isOwnRelation(processingIndexRelation.relation)) {
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
        akubraRepository.iterateProcessingIndex(params, processingIndexItem -> {
            Object sourcePid = processingIndexItem.getFieldValue("source");
            Object relation = processingIndexItem.getFieldValue("relation");
            if (sourcePid != null && relation != null) {
                processingIndexRelations.add(new ProcessingIndexRelation(sourcePid.toString(), relation.toString(), targetPid));
            }
        });
        return processingIndexRelations;
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

}
