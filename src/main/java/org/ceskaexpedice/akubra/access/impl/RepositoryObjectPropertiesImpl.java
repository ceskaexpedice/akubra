package org.ceskaexpedice.akubra.access.impl;

import org.ceskaexpedice.akubra.access.FoxmlType;
import org.ceskaexpedice.akubra.access.RepositoryObjectProperties;
import org.ceskaexpedice.akubra.access.RepositoryAccess;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

import static org.ceskaexpedice.akubra.utils.Dom4jUtils.extractProperty;

public class RepositoryObjectPropertiesImpl implements RepositoryObjectProperties {
    private RepositoryAccess repositoryAccess;
    public static final DateTimeFormatter TIMESTAMP_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss.")
            .appendFraction(ChronoField.MILLI_OF_SECOND, 1, 3, false)
            .appendPattern("'Z'")
            .toFormatter();

    RepositoryObjectPropertiesImpl(RepositoryAccess repositoryAccess) {
        this.repositoryAccess = repositoryAccess;
    }

    @Override
    public String getProperty(String pid, String propertyName) {
        org.dom4j.Document objectFoxml = repositoryAccess.getObject(pid, FoxmlType.regular).asXmlDom4j();
        return objectFoxml == null ? null : extractProperty(objectFoxml, propertyName);
    }

    @Override
    public String getPropertyLabel(String pid) {
        return getProperty(pid, "info:fedora/fedora-system:def/model#label");
    }

    @Override
    public LocalDateTime getPropertyCreated(String pid) {
        String propertyValue = getProperty(pid, "info:fedora/fedora-system:def/model#createdDate");
        if (propertyValue != null) {
            try {
                return LocalDateTime.parse(propertyValue, TIMESTAMP_FORMATTER);
            } catch (DateTimeParseException e) {
                // TODO
                System.out.println(String.format("cannot parse createdDate %s from object %s", propertyValue, pid));
            }
        }
        return null;
    }

    @Override
    public LocalDateTime getPropertyLastModified(String pid) {
        String propertyValue = getProperty(pid, "info:fedora/fedora-system:def/view#lastModifiedDate");
        if (propertyValue != null) {
            try {
                return LocalDateTime.parse(propertyValue, TIMESTAMP_FORMATTER);
            } catch (DateTimeParseException e) {
                // TODO
                System.out.println(String.format("cannot parse lastModifiedDate %s from object %s", propertyValue, pid));
            }
        }
        return null;
    }

}
