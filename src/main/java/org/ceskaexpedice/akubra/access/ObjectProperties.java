package org.ceskaexpedice.akubra.access;

import java.time.LocalDateTime;

public interface ObjectProperties {

    String getProperty(String propertyName);

    String getPropertyLabel();

    LocalDateTime getPropertyCreated();

    LocalDateTime getPropertyLastModified();

}
