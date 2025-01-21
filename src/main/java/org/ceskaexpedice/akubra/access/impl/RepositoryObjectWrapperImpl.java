package org.ceskaexpedice.akubra.access.impl;

import org.ceskaexpedice.akubra.access.FoxmlType;
import org.ceskaexpedice.akubra.access.RepositoryObjectWrapper;
import org.ceskaexpedice.akubra.core.repository.Repository;
import org.ceskaexpedice.akubra.core.repository.RepositoryException;
import org.ceskaexpedice.akubra.core.repository.RepositoryObject;
import org.ceskaexpedice.akubra.utils.Utils;
import org.ceskaexpedice.akubra.utils.XMLUtils;
import org.ceskaexpedice.model.DigitalObject;
import org.dom4j.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.Lock;

public class RepositoryObjectWrapperImpl implements RepositoryObjectWrapper {
    private final Repository repository;
    //private final RepositoryObject content;
    private final String pid;

    RepositoryObjectWrapperImpl(String pid, Repository repository) {
        this.pid = pid;
        this.repository = repository;
    }

    @Override
    public InputStream asStream(FoxmlType foxmlType) {
        if (foxmlType == FoxmlType.archive) {
            DigitalObject obj = repository.getObject(pid, false).getDigitalObject();
            repository.resolveArchivedDatastreams(obj);
            return this.repository.marshallObject(obj);
        } else {
            return this.repository.getObject(pid).getFoxml();
        }
    }

    @Override
    public Document asXml(FoxmlType foxmlType) {
        if (foxmlType == FoxmlType.archive) {
            // TODO
        }
        Lock readLock = repository.getReadLock(pid);
        try {
            RepositoryObject object = repository.getObject(pid);
            try {
                return Utils.inputstreamToDocument(object.getFoxml(), true);
            } catch (IOException e) {
                throw new RepositoryException(e);
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public org.w3c.dom.Document asXmlDom(FoxmlType foxmlType) {
        if (foxmlType == FoxmlType.archive) {
            // TODO
        }
        Lock readLock = repository.getReadLock(pid);
        try {
            RepositoryObject object = repository.getObject(pid, true);
            try {
                return XMLUtils.parseDocument(object.getFoxml());
            } catch (Exception e) {
                throw new RepositoryException(e);
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String asString(FoxmlType foxmlType) {
        return "";
    }

/*
    @Override
    public org.dom4j.Document getFoxml(String pid) throws RepositoryException, IOException {
        Lock readLock = AkubraDOManager.getReadLock(pid);
        try {
            RepositoryObject object = akubraRepositoryImpl.getObject(pid);
            return Utils.inputstreamToDocument(object.getFoxml(), true);
        } finally {
            readLock.unlock();
        }
        //--------------------------------
        try {
            DigitalObject digitalObject = this.manager.readObjectFromStorage(ident);
            if (digitalObject == null) {
                //otherwise later causes NPE at places like AkubraUtils.streamExists(DigitalObject object, String streamID)
                throw new RepositoryException("object not consistently found in storage: " + ident);
            }
            AkubraObject obj = new AkubraObject(this.manager, ident, digitalObject, this.feeder);
            return obj;
        } catch (IOException e) {
            throw new RepositoryException(e);
        }

    }

 */

}