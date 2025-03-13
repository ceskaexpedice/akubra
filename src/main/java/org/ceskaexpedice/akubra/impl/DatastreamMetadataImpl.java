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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import static org.ceskaexpedice.akubra.ObjectProperties.TIMESTAMP_FORMATTER;

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
        return parseDate(metadata.get("CREATED"));
    }

    @Override
    public Date getCreateDate() {
        return parseDate(metadata.get("CREATED"));
    }

    private Date parseDate(String dateStr) {
        try {
            return Date.from(LocalDateTime.parse(dateStr, TIMESTAMP_FORMATTER)
                    .atZone(ZoneId.systemDefault())
                    .toInstant());
        } catch (DateTimeParseException e) {
            LOGGER.warning(String.format("cannot parse createdDate %s", dateStr));
        }
        return null;
    }
}
