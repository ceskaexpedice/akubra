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
package org.ceskaexpedice.akubra.impl.utils;


import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.KnownDatastreams;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

/**
 * RepositoryURLConnection
 */
public class RepositoryURLConnection extends URLConnection {

    public static final String IMG_FULL = "IMG_FULL";
    public static final String IMG_THUMB = "IMG_THUMB";

    private AkubraRepository akubraRepository;

    RepositoryURLConnection(URL url, AkubraRepository akubraRepository) {
        super(url);
        this.akubraRepository = akubraRepository;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        String path = getURL().getPath();
        String pid = null;
        String stream = null;
        StringTokenizer tokenizer = new StringTokenizer(path, "/");
        if (tokenizer.hasMoreTokens()) {
            pid = tokenizer.nextToken();
        }
        if (tokenizer.hasMoreTokens()) {
            stream = tokenizer.nextToken();
        }
        if (stream.equals(IMG_FULL)) {
            return this.akubraRepository.getDatastreamContent(pid, KnownDatastreams.IMG_FULL).asInputStream();
        } else if (stream.equals(IMG_THUMB)) {
            return this.akubraRepository.getDatastreamContent(pid, KnownDatastreams.IMG_PREVIEW).asInputStream();
        } else {
            return this.akubraRepository.getDatastreamContent(pid, stream).asInputStream();
        }
    }

    @Override
    public void connect() throws IOException {
    }
}
