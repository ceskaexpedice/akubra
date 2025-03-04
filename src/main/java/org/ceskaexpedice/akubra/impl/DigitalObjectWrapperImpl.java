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

import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.DigitalObjectWrapper;
import org.ceskaexpedice.akubra.utils.Dom4jUtils;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.ceskaexpedice.akubra.utils.StringUtils;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class DigitalObjectWrapperImpl implements DigitalObjectWrapper {
    private AkubraRepository akubraRepository;
    private byte[] digitalObjectBytes;

    public DigitalObjectWrapperImpl(byte[] digitalObjectBytes, AkubraRepository akubraRepository) {
        this.digitalObjectBytes = digitalObjectBytes;
        this.akubraRepository = akubraRepository;
    }

    @Override
    public DigitalObject asDigitalObject() {
        if(digitalObjectBytes == null || digitalObjectBytes.length == 0) {
            return null;
        }
        return akubraRepository.unmarshall(new ByteArrayInputStream(digitalObjectBytes));
    }

    @Override
    public InputStream asInputStream() {
        if(digitalObjectBytes == null || digitalObjectBytes.length == 0) {
            return null;
        }
        return new ByteArrayInputStream(digitalObjectBytes);
    }

    @Override
    public byte[] asBytes() {
        return digitalObjectBytes;
    }

    @Override
    public Document asDom(boolean nsAware) {
        if(digitalObjectBytes == null || digitalObjectBytes.length == 0) {
            return null;
        }
        return DomUtils.streamToDocument(asInputStream(), nsAware);
    }

    @Override
    public org.dom4j.Document asDom4j(boolean nsAware) {
        if(digitalObjectBytes == null || digitalObjectBytes.length == 0) {
            return null;
        }
        return Dom4jUtils.streamToDocument(asInputStream(), nsAware);
    }

    @Override
    public String asString() {
        if(digitalObjectBytes == null || digitalObjectBytes.length == 0) {
            return null;
        }
        return StringUtils.streamToString(asInputStream());
    }
}
