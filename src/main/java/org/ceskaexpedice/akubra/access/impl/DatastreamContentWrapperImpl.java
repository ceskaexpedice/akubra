package org.ceskaexpedice.akubra.access.impl;

import org.ceskaexpedice.akubra.access.DatastreamContentWrapper;
import org.ceskaexpedice.akubra.core.repository.RepositoryDatastream;
import org.dom4j.Document;

import java.io.InputStream;

public class DatastreamContentWrapperImpl implements DatastreamContentWrapper {
    private final RepositoryDatastream content;

    public DatastreamContentWrapperImpl(RepositoryDatastream content) {
        this.content = content;
    }

    public String asString() {
        /*
        if (!supportedFormat.supportsString()) {
            throw new UnsupportedContentFormatException("String format not supported.");
        }
        return new String(content, StandardCharsets.UTF_8);

         */
        return null;
    }

    public InputStream asStream() {
        /*
        if (!supportedFormat.supportsStream()) {
            throw new UnsupportedContentFormatException("InputStream format not supported.");
        }
        return new ByteArrayInputStream(content);

         */return null;
    }

    public Document asXml() {
        /*
        if (!supportedFormat.supportsXml()) {
            throw new UnsupportedContentFormatException("XML format not supported.");
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            return factory.newDocumentBuilder().parse(new ByteArrayInputStream(content));
        } catch (Exception e) {
            throw new IOException("Failed to parse XML", e);
        }*/return null;
    }
}