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
import org.ceskaexpedice.akubra.core.repository.RepositoryObject;
import org.ceskaexpedice.fedoramodel.DatastreamType;
import org.ceskaexpedice.fedoramodel.DigitalObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;


/**
 * RepositoryObjectImpl
 *
 * @author pavels
 */
class RepositoryObjectImpl implements RepositoryObject {
    private static final Logger LOGGER = Logger.getLogger(RepositoryObjectImpl.class.getName());
    private DigitalObject digitalObject;

    RepositoryObjectImpl(DigitalObject digitalObject) {
        super();
        this.digitalObject = digitalObject;
    }

    @Override
    public DigitalObject getDigitalObject() {
        return digitalObject;
    }

    @Override
    public String getPid() {
        return digitalObject.getPID();
    }

    @Override
    public Date getPropertyLastModified() {
        return RepositoryUtils.getLastModified(digitalObject);
    }

    @Override
    public List<RepositoryDatastream> getStreams() {
        List<RepositoryDatastream> list = new ArrayList<>();
        List<DatastreamType> datastreamList = digitalObject.getDatastream();
        for (DatastreamType datastreamType : datastreamList) {
            list.add(new RepositoryDatastreamImpl(datastreamType, datastreamType.getID(), RepositoryUtils.controlGroup2Type(datastreamType.getCONTROLGROUP())));
        }
        return list;
    }

    @Override
    public RepositoryDatastream getStream(String streamId) {
        List<DatastreamType> datastreamList = digitalObject.getDatastream();
        for (DatastreamType datastreamType : datastreamList) {
            if (streamId.equals(datastreamType.getID())) {
                return new RepositoryDatastreamImpl(datastreamType, datastreamType.getID(), RepositoryUtils.controlGroup2Type(datastreamType.getCONTROLGROUP()));
            }
        }
        return null;
    }

}
