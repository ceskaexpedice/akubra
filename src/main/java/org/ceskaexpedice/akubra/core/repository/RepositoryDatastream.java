package org.ceskaexpedice.akubra.core.repository;

import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.Date;

/**
 * Represents datastream
 */
public interface RepositoryDatastream {

    public static enum Type {
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
     * Return metadata document
     * @return
     * @throws
     */
    Document getMetadata();

    /**
     * Return content of the stream
     * @return
     * @throws
     */
    InputStream getContent();

    /**
     * Return mimetype
     * @return
     * @throws
     */
    String getMimeType();

    /**
     * Return last modified flag
     * @return
     * @throws
     */
    Date getLastModified();

    Type getStreamType();
    
}
