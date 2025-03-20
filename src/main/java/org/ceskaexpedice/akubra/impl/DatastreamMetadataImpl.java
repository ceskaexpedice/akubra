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
import org.ceskaexpedice.akubra.core.repository.impl.RepositoryUtils;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

/**
 * DatastreamMetadataImpl
 */
class DatastreamMetadataImpl implements DatastreamMetadata {
    private static final Logger LOGGER = Logger.getLogger(DatastreamMetadataImpl.class.getName());
    private Map<String, String> metadata;

    DatastreamMetadataImpl(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String getMimetype() {
        return metadata.get("MIMETYPE");
    }

    @Override
    public String getId() {
        return metadata.get("ID");
    }

    @Override
    public long getSize() {
        String sizeStr = metadata.get("SIZE");
        return sizeStr != null ? Long.parseLong(sizeStr) : 0;
    }

    @Override
    public String getControlGroup() {
        return metadata.get("CONTROL_GROUP");
    }

    @Override
    public String getLocation() {
        return metadata.get("LOCATION");
    }

    @Override
    public Date getLastModified() {
        return RepositoryUtils.parseDate(metadata.get("CREATED"));
    }

    @Override
    public Date getCreateDate() {
        return RepositoryUtils.parseDate(metadata.get("CREATED"));
    }

}
