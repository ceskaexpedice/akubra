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
package org.ceskaexpedice.akubra;

/**
 * ProcessingIndexRelation
 */
public class ProcessingIndexRelation {
    private final String source;
    private final String relation;
    private final String target;

    public ProcessingIndexRelation(String source, String relation, String target) {
        this.source = source;
        this.relation = relation;
        this.target = target;
    }

    public String getSource() {
        return source;
    }

    public String getRelation() {
        return relation;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return String.format("%s -%s-> %s", source, relation, target);
    }
}
