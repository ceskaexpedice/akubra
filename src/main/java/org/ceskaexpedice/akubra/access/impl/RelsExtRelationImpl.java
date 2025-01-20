package org.ceskaexpedice.akubra.access.impl;

import org.ceskaexpedice.akubra.access.RelsExtRelation;
import org.ceskaexpedice.akubra.access.RelsExtWrapper;
import org.w3c.dom.Document;

public class RelsExtRelationImpl implements RelsExtRelation {
    private final String name;
    private final String value;

    public RelsExtRelationImpl(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "RelsExtRelationImpl{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
