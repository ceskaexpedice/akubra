package org.ceskaexpedice.akubra.core.repository;

import java.io.InputStream;
import java.util.Date;

/**
 * Represents datastream
 */
public interface RepositoryDatastream {

    enum Type {
        DIRECT,
        INDIRECT;
    }

    /**
     * Return name
     * @return
     * @throws
     */
    String getName();

    /**
     * Return mimetype
     * @return
     * @throws
     */
    String getLastVersionMimeType();

    /**
     * Return last modified flag
     * @return
     * @throws
     */
    Date getLastVersionLastModified();

    Type getStreamType();

    /**
     * Return content of the stream
     * @return
     * @throws
     */
    InputStream getLastVersionContent();

}
