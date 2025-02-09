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
package org.ceskaexpedice.akubra.utils;

import org.ceskaexpedice.akubra.AkubraRepository;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class RepositoryURLStreamHandler extends URLStreamHandler {

    private AkubraRepository akubraRepository;

    /*
    @Inject
    public Handler(RepositoryAccess fedoraAccess) {
        super();
        this.fedoraAccess = fedoraAccess;
    }*/

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return new RepositoryURLConnection(u, akubraRepository);
    }

    @Override
    protected URLConnection openConnection(URL u, Proxy p) throws IOException {
        return super.openConnection(u, p);
    }

}
