package org.ceskaexpedice.akubra.access.impl;

import org.ceskaexpedice.akubra.access.DatastreamMetadata;
import org.ceskaexpedice.akubra.core.repository.RepositoryDatastream;

class DatastreamMetadataImpl implements DatastreamMetadata {
    private RepositoryDatastream repositoryDatastream;

    DatastreamMetadataImpl(RepositoryDatastream repositoryDatastream) {
        this.repositoryDatastream = repositoryDatastream;
    }

    @Override
    public String getMimetype() {
        return repositoryDatastream.getLastVersionMimeType();
    }

}
