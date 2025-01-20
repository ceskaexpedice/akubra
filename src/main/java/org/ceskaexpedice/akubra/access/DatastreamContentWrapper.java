package org.ceskaexpedice.akubra.access;

import org.dom4j.Document;

import java.io.InputStream;

public interface DatastreamContentWrapper {

    String asString();

    InputStream asStream();

    Document asXml();

    org.w3c.dom.Document asXmlDom();
}