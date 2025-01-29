package org.ceskaexpedice.akubra.access;

import org.ceskaexpedice.akubra.core.repository.RepositoryDatastream;

import java.util.Date;

public interface DatastreamMetadata {
    // TODO - getMimeType , getCreatedData, (typ x,M,....control-group)

    String getMimetype();

    String getName();

    RepositoryDatastream.Type getType();

    Date getLastModified();
}
