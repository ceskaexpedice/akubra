package org.ceskaexpedice.akubra.access;

import java.util.List;

public interface RelsExtWrapper {
    //- podavat List<RelstExtRelation>
    // - List<RelsExtRelation> getRelation(String name, String namespace)
    // RelsExtItem - RelsExt
    List<RelsExtRelation> getRelations();

}
