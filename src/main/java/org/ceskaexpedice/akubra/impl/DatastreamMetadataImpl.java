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
