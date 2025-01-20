package org.ceskaexpedice.akubra.access.impl;

import org.ceskaexpedice.akubra.access.DatastreamContentWrapper;
import org.ceskaexpedice.akubra.core.repository.RepositoryDatastream;
import org.ceskaexpedice.akubra.core.repository.RepositoryException;
import org.ceskaexpedice.akubra.core.repository.impl.RepositoryUtils;
import org.ceskaexpedice.akubra.utils.Utils;
import org.ceskaexpedice.akubra.utils.XMLUtils;
import org.ceskaexpedice.model.DatastreamVersionType;
import org.ceskaexpedice.model.DigitalObject;
import org.dom4j.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DatastreamContentWrapperImpl implements DatastreamContentWrapper {
//    private final RepositoryDatastream content;
    private final InputStream content;
    // TODO
    DigitalObject digitalObject;


    public DatastreamContentWrapperImpl(DigitalObject object, InputStream content) {
        this.content = content;
        // TODO
        digitalObject = object;
    }

    @Override
    public String asString() {
        /*
        if (!supportedFormat.supportsString()) {
            throw new UnsupportedContentFormatException("String format not supported.");
        }
        return new String(content, StandardCharsets.UTF_8);

         */
        return null;
    }

    @Override
    public InputStream asStream() {
        return content;
    }

    @Override
    public Document asXml() {
        try {
            // TODO
            boolean nsAware = true;
            Document document = Utils.inputstreamToDocument(content, nsAware);
            return document;
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    // TODO
    @Override
    public org.w3c.dom.Document asXmlDom() {
        DatastreamVersionType stream = RepositoryUtils.getLastStreamVersion(digitalObject, "RELS-EXT");

        if (stream != null) {
            if (stream.getXmlContent() != null) {
                List<Element> elementList = stream.getXmlContent().getAny();
                if (!elementList.isEmpty()) {
                    return elementList.get(0).getOwnerDocument();
                } else {
                    //throw new IOException("Datastream not found: " + pid + " - " + streamName);
                }

            } else {
                //throw new IOException("Expected XML datastream: " + pid + " - " + streamName);
            }
        }
        return null;

        /*
        try {
            org.w3c.dom.Document document = XMLUtils.parseDocument(content);
            return document;
        } catch (ParserConfigurationException e) {
            throw new RepositoryException(e);
        } catch (SAXException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }*/
    }
}