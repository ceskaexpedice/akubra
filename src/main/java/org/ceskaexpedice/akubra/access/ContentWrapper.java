package org.ceskaexpedice.akubra.access;

import org.dom4j.Document;

import java.io.InputStream;

public interface ContentWrapper {

    InputStream asStream();

    Document asXmlDom4j();

    org.w3c.dom.Document asXmlDom();

    String asString();

}