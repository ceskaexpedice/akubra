package org.ceskaexpedice.akubra.access.impl;

import org.ceskaexpedice.akubra.access.DatastreamMetadata;
import org.ceskaexpedice.akubra.core.repository.RepositoryDatastream;

import java.util.Date;

class DatastreamMetadataImpl implements DatastreamMetadata {
    private RepositoryDatastream repositoryDatastream;

    DatastreamMetadataImpl(RepositoryDatastream repositoryDatastream) {
        this.repositoryDatastream = repositoryDatastream;
    }

    @Override
    public String getMimetype() {
        return repositoryDatastream.getLastVersionMimeType();
    }

    @Override
    public String getName() {
        return repositoryDatastream.getName();
    }

    @Override
    public RepositoryDatastream.Type getType() {
        return repositoryDatastream.getStreamType();
    }

    @Override
    public Date getLastModified() {
        return repositoryDatastream.getLastVersionLastModified();
    }

}
