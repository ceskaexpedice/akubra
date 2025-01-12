package org.ceskaexpedice.akubra.utils;

import org.ceskaexpedice.akubra.RepositoryAccess;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler {

    private RepositoryAccess fedoraAccess;

    /*
    @Inject
    public Handler(RepositoryAccess fedoraAccess) {
        super();
        this.fedoraAccess = fedoraAccess;
    }*/

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return new FedoraURLConnection(u, fedoraAccess);
    }

    @Override
    protected URLConnection openConnection(URL u, Proxy p) throws IOException {
        // TODO Auto-generated method stub
        return super.openConnection(u, p);
    }

}
