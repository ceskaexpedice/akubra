package org.ceskaexpedice.akubra.access.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.ceskaexpedice.akubra.access.RelsExtRelation;
import org.ceskaexpedice.akubra.access.RelsExtWrapper;
import org.ceskaexpedice.akubra.core.repository.RepositoryNamespaces;
import org.ceskaexpedice.akubra.core.repository.RepositoryObject;
import org.ceskaexpedice.akubra.utils.RelsExtUtils;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class RelsExtWrapperImpl implements RelsExtWrapper {

    private final RepositoryObject repositoryObject;

    RelsExtWrapperImpl(RepositoryObject repositoryObject) {
        this.repositoryObject = repositoryObject;
    }

    @Override
    public List<RelsExtRelation> getRelations() {
        List<RelsExtRelation> rels = new ArrayList<>();
        // TODO
        List<Triple<String, String, String>> triples = repositoryObject.relsExtGetRelations(null);
        List<Triple<String, String, String>> triples1 = repositoryObject.relsExtGetLiterals(null);
//        List<Triple<String, String, String>> triples = repositoryObject.relsExtGetRelations(RepositoryNamespaces.KRAMERIUS_URI);
        for (Triple<String, String, String> triple : triples1) {
            System.out.println(triple.getLeft() + " " + triple.getMiddle() + " " + triple.getRight());
        }

        /* TODO
        List<Pair<String, String>> relations = RelsExtUtils.getRelations(document.getDocumentElement());
        for (Pair<String, String> relation : relations) {
            rels.add(new RelsExtRelationImpl(relation.getLeft(), relation.getRight()));
        }*/
        return rels;
    }
}
