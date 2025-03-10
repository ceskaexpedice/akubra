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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ceskaexpedice.akubra.ObjectProperties;
import org.ceskaexpedice.akubra.core.repository.RepositoryObject;
import org.ceskaexpedice.akubra.utils.Dom4jUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.logging.Logger;

import static org.ceskaexpedice.akubra.utils.Dom4jUtils.extractProperty;

/**
 * ObjectPropertiesImpl
 */
class ObjectPropertiesImpl implements ObjectProperties {
    private static final Logger LOGGER = Logger.getLogger(ObjectPropertiesImpl.class.getName());
    private static final Log log = LogFactory.getLog(ObjectPropertiesImpl.class);
    private RepositoryObject repositoryObject;

    ObjectPropertiesImpl(RepositoryObject repositoryObject) {
        this.repositoryObject = repositoryObject;
    }

    @Override
    public String getProperty(String propertyName) {
        org.dom4j.Document objectFoxml = Dom4jUtils.streamToDocument(repositoryObject.getFoxml(), true);
        return objectFoxml == null ? null : extractProperty(objectFoxml, propertyName);
    }

    @Override
    public String getPropertyLabel() {
        return getProperty("info:fedora/fedora-system:def/model#label");
    }

    @Override
    public Date getPropertyCreated() {
        String propertyValue = getProperty("info:fedora/fedora-system:def/model#createdDate");
        if (propertyValue != null) {
            try {
                return Date.from(LocalDateTime.parse(propertyValue, TIMESTAMP_FORMATTER)
                        .atZone(ZoneId.systemDefault())
                        .toInstant());
            } catch (DateTimeParseException e) {
                LOGGER.warning(String.format("cannot parse createdDate %s from object %s", propertyValue, repositoryObject.getPid()));
            }
        }
        return null;
    }

    @Override
    public Date getPropertyLastModified() {
        String propertyValue = getProperty("info:fedora/fedora-system:def/view#lastModifiedDate");
        if (propertyValue != null) {
            try {
                return Date.from(LocalDateTime.parse(propertyValue, TIMESTAMP_FORMATTER)
                        .atZone(ZoneId.systemDefault())
                        .toInstant());
            } catch (DateTimeParseException e) {
                LOGGER.warning(String.format("cannot parse lastModifiedDate %s from object %s", propertyValue, repositoryObject.getPid()));
            }
        }
        return null;
    }

}
