package org.ceskaexpedice.akubra.access.impl;

import org.ceskaexpedice.akubra.access.ResultWrapper;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.ceskaexpedice.jaxbmodel.DigitalObject;
import org.dom4j.Document;

import java.io.InputStream;

public class DatastreamContentWrapperImpl implements ResultWrapper {
//    private final RepositoryDatastream content;
    private final InputStream content;
    // TODO
    DigitalObject digitalObject;


    public DatastreamContentWrapperImpl(InputStream content) {
        this.content = content;
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
    public Document asXmlDom4j() {
        /* TODO
        try {
            // TODO
            boolean nsAware = true;
            Document document = Utils.inputstreamToDocument(content, nsAware);
            return document;
        } catch (IOException e) {
            throw new RepositoryException(e);
        }*/return null;
    }

    // TODO
    @Override
    public org.w3c.dom.Document asXmlDom() {
        return DomUtils.parseDocument(content, true);
        /* TODO
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

         */

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