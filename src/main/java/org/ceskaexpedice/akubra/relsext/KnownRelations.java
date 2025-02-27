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
package org.ceskaexpedice.akubra.relsext;

/**
 * Enum representing known relationships between Fedora digital objects.
 * <p>
 * This enumeration defines relationships used to structure digital objects
 * within a Fedora repository, such as hierarchical links between pages,
 * units, and volumes.
 * </p>
 */
public enum KnownRelations {
    //own relations (define object tree)
    HAS_PAGE("hasPage"),
    HAS_UNIT("hasUnit"), //monograph -> monographUnit, convolute -> anything_top-level_but_collection
    HAS_VOLUME("hasVolume"), //periodical -> periodicalVolume
    HAS_ITEM("hasItem"), //periodical -> (periodicalItem, supplement)
    HAS_SOUND_UNIT("hasSoundUnit"), //soundRecording -> soundUnit
    HAS_TRACK("hasTrack"), //(soundRecording, soundUnit) -> track
    CONTAINS_TRACK("containsTrack"), //old version of HAS_TRACK
    HAS_INT_COMP_PART("hasIntCompPart"), //periodicalItem  -> (internalPart, article)
    //foster relations
    IS_ON_PAGE("isOnPage"), //(article, internalPart) -> page
    CONTAINS("contains"); //collection -> (monograph, periodical, ... anything, even other collection)
    //RDF relations, that don't connect two objects are not considered here
    //i.e. hasModel, hasDonator, contract, policy, itemId, handle

    private final String value;

    KnownRelations(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }
}