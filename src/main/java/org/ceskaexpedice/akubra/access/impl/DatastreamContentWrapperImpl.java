package org.ceskaexpedice.akubra.access.impl;

import org.ceskaexpedice.akubra.access.ContentWrapper;
import org.ceskaexpedice.akubra.utils.Dom4jUtils;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.ceskaexpedice.akubra.utils.Utils;
import org.ceskaexpedice.jaxbmodel.DigitalObject;
import org.dom4j.Document;

import java.io.InputStream;

class DatastreamContentWrapperImpl implements ContentWrapper {
    private final InputStream content;

    DatastreamContentWrapperImpl(InputStream content) {
        this.content = content;
    }

    @Override
    public String asString() {
        return Utils.inputstreamToString(content);
    }

    @Override
    public InputStream asStream() {
        return content;
    }

    @Override
    public Document asXmlDom4j() {
        return Dom4jUtils.inputstreamToDocument(content, true);
    }

    @Override
    public org.w3c.dom.Document asXmlDom() {
        return DomUtils.parseDocument(content, true);
    }
}