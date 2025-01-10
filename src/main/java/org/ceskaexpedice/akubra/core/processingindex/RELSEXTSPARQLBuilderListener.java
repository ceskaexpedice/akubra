package org.ceskaexpedice.akubra.core.processingindex;

import org.ceskaexpedice.akubra.RepositoryException;

/**
 * Listener is able to receive information about processing RELS-EXT
 * @see RELSEXTSPARQLBuilder
 */
interface RELSEXTSPARQLBuilderListener {

    /**
     * Returns changed path
     * @param path Path parsed from RELS-EXT
     * @param localName Local name
     * @return
     * @throws RepositoryException
     */
    String inform(String path, String localName) throws RepositoryException;

}
