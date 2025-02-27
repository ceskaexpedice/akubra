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
package org.ceskaexpedice.akubra.processingindex;

/**
 * Represents a relationship between entities in a processing index.
 * <p>
 * This class encapsulates a directed relationship between a source, a relation type,
 * and a target. It is used to model connections between digital objects or entities
 * in an indexing or processing system.
 * </p>
 */
public class ProcessingIndexRelation {

    private final String source;
    private final String relation;
    private final String target;

    /**
     * Creates a new {@code ProcessingIndexRelation} with the specified source, relation, and target.
     *
     * @param source   the source entity in the relationship
     * @param relation the type of relationship between source and target
     * @param target   the target entity in the relationship
     */
    public ProcessingIndexRelation(String source, String relation, String target) {
        this.source = source;
        this.relation = relation;
        this.target = target;
    }

    /**
     * Returns the source entity of this relationship.
     *
     * @return the source entity as a {@link String}
     */
    public String getSource() {
        return source;
    }

    /**
     * Returns the relation type of this relationship.
     *
     * @return the relation type as a {@link String}
     */
    public String getRelation() {
        return relation;
    }

    /**
     * Returns the target entity of this relationship.
     *
     * @return the target entity as a {@link String}
     */
    public String getTarget() {
        return target;
    }

    /**
     * Returns a string representation of this relationship in the format:
     * {@code source -relation-> target}.
     *
     * @return a formatted string representing the relationship
     */
    @Override
    public String toString() {
        return String.format("%s -%s-> %s", source, relation, target);
    }
}
