package org.ceskaexpedice.akubra.access.impl;

import org.apache.commons.lang3.tuple.Triple;
import org.ceskaexpedice.akubra.access.RelsExtLiteral;
import org.ceskaexpedice.akubra.access.RelsExtRelation;
import org.ceskaexpedice.akubra.access.RelsExtWrapper;
import org.ceskaexpedice.akubra.core.repository.RepositoryObject;

import java.util.ArrayList;
import java.util.List;

class RelsExtWrapperImpl implements RelsExtWrapper {

    private final RepositoryObject repositoryObject;

    RelsExtWrapperImpl(RepositoryObject repositoryObject) {
        this.repositoryObject = repositoryObject;
    }

    @Override
    public List<RelsExtRelation> getRelations(String namespace) {
        List<RelsExtRelation> rels = new ArrayList<>();
        List<Triple<String, String, String>> triples = repositoryObject.relsExtGetRelations(namespace);
        for (Triple<String, String, String> triple : triples) {
            RelsExtRelation relsExtRelation = new RelsExtRelation(triple.getLeft(), triple.getMiddle(), triple.getRight());
            rels.add(relsExtRelation);
        }
        return rels;
    }

    @Override
    public List<RelsExtLiteral> getLiterals(String namespace) {
        List<RelsExtLiteral> rels = new ArrayList<>();
        List<Triple<String, String, String>> triples = repositoryObject.relsExtGetLiterals(namespace);
        for (Triple<String, String, String> triple : triples) {
            RelsExtLiteral relsExtLiteral = new RelsExtLiteral(triple.getLeft(), triple.getMiddle(), triple.getRight());
            rels.add(relsExtLiteral);
        }
        return rels;
    }

}
