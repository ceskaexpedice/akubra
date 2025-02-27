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

import java.util.List;

/**
 * A wrapper interface for handling RelsExt relations and literals in the Akubra Fefora model stream.
 * This interface provides methods to retrieve collections of relations and literals for a given namespace.
 */
public interface RelsExtWrapper {

    /**
     * Retrieves a list of {@link RelsExtRelation} objects associated with the specified namespace.
     *
     * @param namespace The namespace for which to retrieve the relations.
     * @return A list of {@link RelsExtRelation} objects that belong to the specified namespace.
     */
    List<RelsExtRelation> getRelations(String namespace);

    /**
     * Retrieves a list of {@link RelsExtLiteral} objects associated with the specified namespace.
     *
     * @param namespace The namespace for which to retrieve the literals.
     * @return A list of {@link RelsExtLiteral} objects that belong to the specified namespace.
     */
    List<RelsExtLiteral> getLiterals(String namespace);
}

