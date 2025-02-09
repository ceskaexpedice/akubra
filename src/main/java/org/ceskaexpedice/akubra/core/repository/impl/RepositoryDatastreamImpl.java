/*
 * Copyright (C) 2025 Inovatika
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ceskaexpedice.akubra.core.repository.impl;

import org.ceskaexpedice.akubra.core.repository.RepositoryDatastream;
import org.ceskaexpedice.fedoramodel.DatastreamType;

import java.io.InputStream;
import java.util.Date;
import java.util.logging.Logger;

/**
 * RepositoryDatastreamImpl
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

    @Override
    public DatastreamType getDatastream() {
        return datastream;
    }
}
