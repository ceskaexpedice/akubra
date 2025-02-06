package org.ceskaexpedice.akubra.impl;

import org.ceskaexpedice.akubra.DatastreamMetadata;
import org.ceskaexpedice.akubra.core.repository.RepositoryDatastream;
import org.ceskaexpedice.fedoramodel.ContentLocationType;

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
    public String getId() {
        return repositoryDatastream.getName();
    }

    @Override
    public RepositoryDatastream.Type getType() {
        return repositoryDatastream.getStreamType();
    }

    @Override
    public long getSize() {
        return repositoryDatastream.getDatastream().getDatastreamVersion().get(0).getSIZE();
    }

    @Override
    public String getControlGroup() {
        return repositoryDatastream.getDatastream().getCONTROLGROUP();
    }

    @Override
    public String getLocation() {
        ContentLocationType contentLocation = repositoryDatastream.getDatastream().getDatastreamVersion().get(0).getContentLocation();
        return contentLocation == null ? null : contentLocation.getREF();
    }

    @Override
    public Date getLastModified() {
        return repositoryDatastream.getLastVersionLastModified();
    }

    @Override
    public Date getCreateDate() {
        return repositoryDatastream.getDatastream().getDatastreamVersion().get(0).getCREATED().toGregorianCalendar().getTime();
    }

}
