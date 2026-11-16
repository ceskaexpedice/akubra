package org.ceskaexpedice.akubra.reference;

import java.util.ArrayList;
import java.util.List;

public class ReferenceFactory {

    private final List<ReferenceType> types;
    private ReferenceType defaultType;

    public ReferenceFactory(List<ReferenceType> types) {
        this.types = new ArrayList<>(types);
        defaultType = new FedoraReferenceType();
    }

    public Reference create(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Reference must not be null");
        }
        for (ReferenceType type : types) {
            if (type.supports(value)) {
                return type.create(value);
            }
        }
        return defaultType.create(value);
    }
}