package org.ceskaexpedice.akubra;

import org.ceskaexpedice.akubra.core.repository.RepositoryDatastream;
import org.ceskaexpedice.akubra.impl.SupportedFormats;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class DatastreamContentWrapper {
    private final RepositoryDatastream content;
    private final SupportedFormats supportedFormat;

    public DatastreamContentWrapper(RepositoryDatastream content, SupportedFormats supportedFormat) {
        this.content = content;
        this.supportedFormat = supportedFormat;
    }

    public String asString() throws UnsupportedContentFormatException {
        /*
        if (!supportedFormat.supportsString()) {
            throw new UnsupportedContentFormatException("String format not supported.");
        }
        return new String(content, StandardCharsets.UTF_8);

         */
        return null;
    }

    public InputStream asStream() throws UnsupportedContentFormatException {
        /*
        if (!supportedFormat.supportsStream()) {
            throw new UnsupportedContentFormatException("InputStream format not supported.");
        }
        return new ByteArrayInputStream(content);

         */return null;
    }

    public Document asXml() throws UnsupportedContentFormatException {
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