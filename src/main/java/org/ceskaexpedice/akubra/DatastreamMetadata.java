package org.ceskaexpedice.akubra;

import org.ceskaexpedice.akubra.core.repository.RepositoryDatastream;

import java.util.Date;

public interface DatastreamMetadata {
    String getId();

    String getMimetype();

    RepositoryDatastream.Type getType();

    long getSize();

    String getControlGroup();

    String getLocation();

    Date getLastModified();

    Date getCreateDate();
}
