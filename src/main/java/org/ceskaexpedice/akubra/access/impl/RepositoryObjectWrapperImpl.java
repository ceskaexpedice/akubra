package org.ceskaexpedice.akubra.access.impl;

import org.ceskaexpedice.akubra.access.ContentWrapper;
import org.ceskaexpedice.akubra.utils.Dom4jUtils;
import org.ceskaexpedice.akubra.utils.Utils;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.dom4j.Document;

import java.io.InputStream;

class RepositoryObjectWrapperImpl implements ContentWrapper {
    private final InputStream objectStream;

    RepositoryObjectWrapperImpl(InputStream objectStream) {
        this.objectStream = objectStream;
    }

    @Override
    public InputStream asStream() {
        return objectStream;
    }

    @Override
    public Document asXmlDom4j() {
        return Dom4jUtils.inputstreamToDocument(objectStream, true);
    }

    @Override
    public org.w3c.dom.Document asXmlDom() {
        return DomUtils.parseDocument(objectStream);
    }

    @Override
    public String asString() {
        return Utils.inputstreamToString(objectStream);
    }

}