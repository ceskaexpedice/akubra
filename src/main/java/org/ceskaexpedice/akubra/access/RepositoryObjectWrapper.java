package org.ceskaexpedice.akubra.access;


import org.dom4j.Document;

import java.io.InputStream;

public interface RepositoryObjectWrapper {

    InputStream asStream(FoxmlType foxmlType);

    Document asXml(FoxmlType foxmlType);

    String asString(FoxmlType foxmlType);

}