package org.ceskaexpedice.akubra.access;

import java.time.LocalDateTime;

public interface RepositoryObjectProperties {

    String getProperty(String pid, String propertyName);

    String getPropertyLabel(String pid);

    LocalDateTime getPropertyCreated(String pid);

    LocalDateTime getPropertyLastModified(String pid);

}
