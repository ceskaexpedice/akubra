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
package org.ceskaexpedice.akubra.core.processingindex;

import org.ceskaexpedice.akubra.RepositoryException;

/**
 * Listener is able to receive information about processing RELS-EXT
 * @see RelsExtSPARQLBuilder
 */
interface RelsExtSPARQLBuilderListener {

    /**
     * Returns changed path
     * @param path Path parsed from RELS-EXT
     * @param localName Local name
     * @return
     * @throws RepositoryException
     */
    String inform(String path, String localName) throws RepositoryException;

}
