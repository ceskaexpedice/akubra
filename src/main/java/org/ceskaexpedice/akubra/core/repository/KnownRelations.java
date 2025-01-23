package org.ceskaexpedice.akubra.core.repository;

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