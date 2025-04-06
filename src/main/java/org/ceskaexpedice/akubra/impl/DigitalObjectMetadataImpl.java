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

import org.ceskaexpedice.akubra.DigitalObjectMetadata;
import org.ceskaexpedice.akubra.core.repository.impl.RepositoryUtils;
import org.ceskaexpedice.akubra.impl.utils.ObjectPropertiesSaxParser;

import java.io.File;
import java.util.Date;
import java.util.logging.Logger;

/**
 * ObjectPropertiesImpl
 */
class DigitalObjectMetadataImpl implements DigitalObjectMetadata {
    private static final Logger LOGGER = Logger.getLogger(DigitalObjectMetadataImpl.class.getName());
    private ObjectPropertiesSaxParser objectPropertiesSaxParser;
    private File objectStoragePath;

    DigitalObjectMetadataImpl(ObjectPropertiesSaxParser objectPropertiesSaxParser, File objectStoragePath) {
        this.objectPropertiesSaxParser = objectPropertiesSaxParser;
        this.objectStoragePath = objectStoragePath;
    }

    @Override
    public String getProperty(String propertyName) {
        return objectPropertiesSaxParser.getProperty(propertyName);
    }

    @Override
    public String getPropertyLabel() {
        return getProperty("info:fedora/fedora-system:def/model#label");
    }

    @Override
    public Date getPropertyCreated() {
        String propertyValue = getProperty("info:fedora/fedora-system:def/model#createdDate");
        return RepositoryUtils.parseDate(propertyValue);
    }

    @Override
    public Date getPropertyLastModified() {
        String propertyValue = getProperty("info:fedora/fedora-system:def/view#lastModifiedDate");
        return RepositoryUtils.parseDate(propertyValue);
    }

    @Override
    public File getObjectStoragePath() {
        return objectStoragePath;
    }

}
