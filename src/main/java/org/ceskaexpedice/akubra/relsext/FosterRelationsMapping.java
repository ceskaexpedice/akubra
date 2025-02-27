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

import java.util.Arrays;
import java.util.List;

/**
 * Enum mapping digital objects to their foster relationships based on the parent model.
 * <p>
 * This enumeration defines dynamic relationship mappings where the relation
 * depends on the type of the parent object. For example, a page may have a different
 * relationship depending on whether its parent is an article or an internal part.
 * </p>
 */
public enum FosterRelationsMapping {
    page {
        @Override
        public KnownRelations relation(String parentModel) {
            List<String> parent = Arrays.asList("article", "internalpart");
            if (parent.contains(parentModel)) {
                return KnownRelations.IS_ON_PAGE;
            } else return KnownRelations.CONTAINS;
        }
    },
    anything {
        @Override
        public KnownRelations relation(String parentModel) {
            return KnownRelations.CONTAINS;
        }
    };

    public static FosterRelationsMapping find(String name) {
        FosterRelationsMapping[] values = FosterRelationsMapping.values();
        for (FosterRelationsMapping relMap : values) {
            if (relMap.name().equals(name)) return relMap;
        }
        return anything;

    }

    public abstract KnownRelations relation(String parentModel);

}