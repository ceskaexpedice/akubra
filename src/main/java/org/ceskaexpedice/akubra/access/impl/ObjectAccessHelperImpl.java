package org.ceskaexpedice.akubra.access.impl;

import org.ceskaexpedice.akubra.access.ObjectAccessHelper;
import org.ceskaexpedice.akubra.access.RepositoryAccess;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

public class ObjectAccessHelperImpl implements ObjectAccessHelper {
    private RepositoryAccess repositoryAccess;
    public static final DateTimeFormatter TIMESTAMP_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss.")
            .appendFraction(ChronoField.MILLI_OF_SECOND, 1, 3, false)
            .appendPattern("'Z'")
            .toFormatter();

    ObjectAccessHelperImpl(RepositoryAccess repositoryAccess) {
        this.repositoryAccess = repositoryAccess;
    }

    @Override
    public String getPropertyLabel(String pid) {
        return repositoryAccess.getProperty(pid, "info:fedora/fedora-system:def/model#label");
    }

    @Override
    public LocalDateTime getPropertyCreated(String pid) {
        String propertyValue = repositoryAccess.getProperty(pid, "info:fedora/fedora-system:def/model#createdDate");
        if (propertyValue != null) {
            try {
                return LocalDateTime.parse(propertyValue, TIMESTAMP_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println(String.format("cannot parse createdDate %s from object %s", propertyValue, pid));
            }
        }
        return null;
    }

    @Override
    public LocalDateTime getPropertyLastModified(String pid) {
        String propertyValue = repositoryAccess.getProperty(pid, "info:fedora/fedora-system:def/view#lastModifiedDate");
        if (propertyValue != null) {
            try {
                return LocalDateTime.parse(propertyValue, TIMESTAMP_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println(String.format("cannot parse lastModifiedDate %s from object %s", propertyValue, pid));
            }
        }
        return null;
    }

    /*
    @Override
    public Date getObjectLastmodifiedFlag(String pid) throws IOException {
        DigitalObject object = manager.readObjectFromStorage(pid);
        if (object != null) {
            return AkubraUtils.getLastModified(object);
        }
        throw new IOException("Object not found: " + pid);
    }*/

    /*
        @Override
    public InputStream getFoxml(String pid, boolean archive) throws IOException {
        try {
            if (archive){
                DigitalObject obj = manager.readObjectCloneFromStorage(pid);
                manager.resolveArchivedDatastreams(obj);
                return this.manager.marshallObject(obj);
            }else {
                return this.manager.retrieveObject(pid);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
    @Override
    public org.dom4j.Document getFoxml(String pid) throws RepositoryException, IOException {
        Lock readLock = AkubraDOManager.getReadLock(pid);
        try {
            RepositoryObject object = akubraRepositoryImpl.getObject(pid);
            return Utils.inputstreamToDocument(object.getFoxml(), true);
        } finally {
            readLock.unlock();
        }
    }
    @Override
    public boolean objectExists(String pid) throws RepositoryException {
        Lock readLock = AkubraDOManager.getReadLock(pid);
        try {
            return akubraRepositoryImpl.objectExists(pid);
        } finally {
            readLock.unlock();
        }
    }
    @Override
    public boolean isPidAvailable(String pid) throws IOException, RepositoryException {
        boolean exists = this.repositoryApi.objectExists(pid);
        return exists;
    }

     */

}
