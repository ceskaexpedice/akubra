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
package org.ceskaexpedice.akubra.core.repository;

import java.util.Arrays;
import java.util.List;

public enum FosterRelationsMapping {
    page{
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