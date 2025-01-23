package org.ceskaexpedice.akubra.core.repository;

public enum KnownDatastreams {
    RELS_EXT("RELS-EXT"),

    BIBLIO_MODS("BIBLIO_MODS"),
    BIBLIO_DC("DC"),

    OCR_ALTO("ALTO"),
    OCR_TEXT("TEXT_OCR"),

    IMG_FULL("IMG_FULL"),
    IMG_THUMB("IMG_THUMB"),
    IMG_PREVIEW("IMG_PREVIEW"),

    AUDIO_MP3("MP3"),
    AUDIO_OGG("OGG"),
    AUDIO_WAV("WAV"),

    // known but not used datastreams
    POLICY("POLICY"),
    MIGRATION("MIGRATION"),
    IMG_FULL_ADM("IMG_FULL_ADM"),
    AUDIT("AUDIT"),
    TEXT_OCR_ADM("TEXT_OCR_ADM"),

    COLLECTION_CLIPPINGS("COLLECTION_CLIPPINGS");


    private final String value;

    KnownDatastreams(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }
}
