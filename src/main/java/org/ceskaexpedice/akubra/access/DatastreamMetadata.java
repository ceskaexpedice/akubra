package org.ceskaexpedice.akubra.access;

import org.ceskaexpedice.akubra.core.repository.RepositoryDatastream;

import java.util.Date;

public interface DatastreamMetadata {
    String getId();

    String getMimetype();

    RepositoryDatastream.Type getType();

    int getSize();

    String getControlGroup();

    String getLocation();

    Date getLastModified();

    Date getCreateDate();
}
