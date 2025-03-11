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
import org.ceskaexpedice.akubra.impl.utils.InternalSaxUtils;
import org.ceskaexpedice.akubra.impl.utils.relsext.RelsExtInternalSaxUtils;
import org.ceskaexpedice.akubra.misc.MiscHandler;

import java.util.logging.Logger;

public class MiscHandlerImpl implements MiscHandler {
    private static final Logger LOGGER = Logger.getLogger(MiscHandlerImpl.class.getName());

    private AkubraRepository akubraRepository;

    public MiscHandlerImpl(AkubraRepository akubraRepository) {
        this.akubraRepository = akubraRepository;
    }

    @Override
    public String getModsPartType(String pid) {
        DigitalObjectWrapper digitalObjectWrapper = akubraRepository.get(pid);
        if(digitalObjectWrapper == null) {
            return null;
        }
        return InternalSaxUtils.getModsPartType(digitalObjectWrapper.asInputStream());
    }

}
