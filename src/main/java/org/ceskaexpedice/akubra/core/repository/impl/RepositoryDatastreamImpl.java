package org.ceskaexpedice.akubra.core.repository.impl;

import org.ceskaexpedice.jaxbmodel.DatastreamType;
import org.ceskaexpedice.akubra.core.repository.RepositoryDatastream;

import java.io.*;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by pstastny on 10/13/2017.
 */
class RepositoryDatastreamImpl implements RepositoryDatastream {

    private static final Logger LOGGER = Logger.getLogger(RepositoryDatastreamImpl.class.getName());

    private final AkubraDOManager manager;
    private final DatastreamType datastream;

    private final String name;
    private final Type type;

    RepositoryDatastreamImpl(DatastreamType datastream, String name, Type type, AkubraDOManager manager) {
        super();
        this.manager = manager;
        this.datastream = datastream;
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Date getLastVersionLastModified() {
        return RepositoryUtils.getLastStreamVersion(datastream).getCREATED().toGregorianCalendar().getTime();
    }

    @Override
    public String getLastVersionMimeType() {
        return RepositoryUtils.getLastStreamVersion(datastream).getMIMETYPE();
    }

    @Override
    public InputStream getLastVersionContent() {
        return RepositoryUtils.getStreamContent(RepositoryUtils.getLastStreamVersion(datastream), manager);
    }

    @Override
    public Type getStreamType() {
        return this.type;
    }


}
