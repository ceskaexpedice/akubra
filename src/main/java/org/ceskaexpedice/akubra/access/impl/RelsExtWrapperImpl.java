package org.ceskaexpedice.akubra.access.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.ceskaexpedice.akubra.access.RelsExtRelation;
import org.ceskaexpedice.akubra.access.RelsExtWrapper;
import org.ceskaexpedice.akubra.utils.RelsExtHelper;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class RelsExtWrapperImpl implements RelsExtWrapper {

    private final Document document;

    RelsExtWrapperImpl(Document document) {
        this.document = document;
    }

    @Override
    public List<RelsExtRelation> getRelations() {
        List<RelsExtRelation> rels = new ArrayList<>();
        List<Pair<String, String>> relations = RelsExtHelper.getRelations(document.getDocumentElement());
        for (Pair<String, String> relation : relations) {
            rels.add(new RelsExtRelationImpl(relation.getLeft(), relation.getRight()));
        }
        return rels;
    }
}
