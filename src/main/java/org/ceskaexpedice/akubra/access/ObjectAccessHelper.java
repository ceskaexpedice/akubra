package org.ceskaexpedice.akubra.access;

import org.ceskaexpedice.akubra.core.repository.RepositoryException;

import java.io.IOException;
import java.time.LocalDateTime;

public interface ObjectAccessHelper {

    public String getPropertyLabel(String pid);

    public LocalDateTime getPropertyCreated(String pid);

    public LocalDateTime getPropertyLastModified(String pid);


}
