package org.ceskaexpedice.akubra.utils;

import org.ceskaexpedice.akubra.access.RepositoryAccess;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class RepositoryURLStreamHandler extends URLStreamHandler {

    private RepositoryAccess fedoraAccess;

    /*
    @Inject
    public Handler(RepositoryAccess fedoraAccess) {
        super();
        this.fedoraAccess = fedoraAccess;
    }*/

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return new RepositoryURLConnection(u, fedoraAccess);
    }

    @Override
    protected URLConnection openConnection(URL u, Proxy p) throws IOException {
        // TODO Auto-generated method stub
        return super.openConnection(u, p);
    }

}
