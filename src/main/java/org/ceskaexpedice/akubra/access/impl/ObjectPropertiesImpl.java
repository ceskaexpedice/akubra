package org.ceskaexpedice.akubra.access.impl;

import org.ceskaexpedice.akubra.access.ObjectProperties;
import org.ceskaexpedice.akubra.core.repository.RepositoryObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

import static org.ceskaexpedice.akubra.utils.Dom4jUtils.extractProperty;

class ObjectPropertiesImpl implements ObjectProperties {
    private RepositoryObject repositoryObject;
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss.")
            .appendFraction(ChronoField.MILLI_OF_SECOND, 1, 3, false)
            .appendPattern("'Z'")
            .toFormatter();

    ObjectPropertiesImpl(RepositoryObject repositoryObject) {
        this.repositoryObject = repositoryObject;
    }

    @Override
    public String getProperty(String propertyName) {
        org.dom4j.Document objectFoxml = new RepositoryObjectWrapperImpl(repositoryObject.getFoxml()).asXmlDom4j();
        return objectFoxml == null ? null : extractProperty(objectFoxml, propertyName);
    }

    @Override
    public String getPropertyLabel() {
        return getProperty("info:fedora/fedora-system:def/model#label");
    }

    @Override
    public LocalDateTime getPropertyCreated() {
        String propertyValue = getProperty("info:fedora/fedora-system:def/model#createdDate");
        if (propertyValue != null) {
            try {
                return LocalDateTime.parse(propertyValue, TIMESTAMP_FORMATTER);
            } catch (DateTimeParseException e) {
                // TODO
                System.out.println(String.format("cannot parse createdDate %s from object %s", propertyValue, repositoryObject.getPid()));
            }
        }
        return null;
    }

    @Override
    public LocalDateTime getPropertyLastModified() {
        String propertyValue = getProperty("info:fedora/fedora-system:def/view#lastModifiedDate");
        if (propertyValue != null) {
            try {
                return LocalDateTime.parse(propertyValue, TIMESTAMP_FORMATTER);
            } catch (DateTimeParseException e) {
                // TODO
                System.out.println(String.format("cannot parse lastModifiedDate %s from object %s", propertyValue, repositoryObject.getPid()));
            }
        }
        return null;
    }

}
