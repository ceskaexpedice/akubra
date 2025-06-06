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
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Builder is able to prepare SPARQL update script
 * @see RelsExtSPARQLBuilderListener
 */
interface RelsExtSPARQLBuilder {

    /**
     * Generate update sparql
     *
     * @param relsExt  processing RELS-EXT stream
     * @param listener Listener Listener
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws RepositoryException
     */
    void sparqlProps(String relsExt, RelsExtSPARQLBuilderListener listener) throws IOException, SAXException, ParserConfigurationException, RepositoryException;
}
