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

public enum OwnRelationsMapping {

    page{
        @Override
        public KnownRelations relation() {
            return KnownRelations.HAS_PAGE;
        }
    },

    unit {
        @Override
        public KnownRelations relation() {
            return KnownRelations.HAS_UNIT;
        }
    },
    periodicalvolume {
        @Override
        public KnownRelations relation() {
            return KnownRelations.HAS_VOLUME;
        }
    },
    volume {
        @Override
        public KnownRelations relation() {
            return KnownRelations.HAS_VOLUME;
        }
    },
    periodicalitem {
        @Override
        public KnownRelations relation() {
            return KnownRelations.HAS_ITEM;
        }
    },
    supplement {
        @Override
        public KnownRelations relation() {
            return KnownRelations.HAS_ITEM;
        }
    },
    soundunit {
        @Override
        public KnownRelations relation() {
            return KnownRelations.HAS_SOUND_UNIT;
            //return KnownRelations.CONTAINS_TRACK;
        }
    },
    soundrecording {
        @Override
        public KnownRelations relation() {
            return KnownRelations.HAS_SOUND_UNIT;
        }
    },

    internalpart {
        @Override
        public KnownRelations relation() {
            return KnownRelations.HAS_INT_COMP_PART;
        }
    },
    track {

        @Override
        public KnownRelations relation() {
            return KnownRelations.CONTAINS_TRACK;
        }

    },
    article {
        @Override
        public KnownRelations relation() {
            return KnownRelations.HAS_INT_COMP_PART;
        }
    };

    ;

    public static OwnRelationsMapping find(String name) {
        OwnRelationsMapping[] values = values();
        for (OwnRelationsMapping relMap :  values()) {
            if (relMap.name().equals(name)) {
                return relMap;
            }
        }
        return null;
    }

    public abstract KnownRelations relation();

}