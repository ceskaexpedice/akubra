package org.ceskaexpedice.akubra.access;

import java.util.List;

public interface RelsExtWrapper {

    List<RelsExtRelation> getRelations(String namespace);

    List<RelsExtLiteral> getLiterals(String namespace);

}
