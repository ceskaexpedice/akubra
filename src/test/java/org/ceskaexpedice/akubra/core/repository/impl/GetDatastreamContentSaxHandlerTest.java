package org.ceskaexpedice.akubra.core.repository.impl;

import org.ceskaexpedice.akubra.impl.utils.relsext.GetModelSaxHandler;
import org.ceskaexpedice.akubra.impl.utils.relsext.GetTilesUrlSaxHandler;
import org.ceskaexpedice.akubra.impl.utils.relsext.GetTilesUrlSaxHandlerTest;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import static org.ceskaexpedice.akubra.impl.utils.InternalSaxUtils.FOUND;

public class GetDatastreamContentSaxHandlerTest {

    @Test
    public void testOnlyOneVersionOfRELSEXT() throws ParserConfigurationException, SAXException {
        InputStream foxml = GetTilesUrlSaxHandlerTest.class.getClassLoader().getResourceAsStream("foxml/bbae3d9d-ee9f-43a3-9431-024db12de070.xml");
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser saxParser = factory.newSAXParser();
        GetDatastreamContentSaxHandler handler = new GetDatastreamContentSaxHandler("RELS-EXT");
        try {
            saxParser.parse(foxml, handler);
        } catch (SAXException e) {
            if (!FOUND.equals(e.getMessage())) {
                throw e;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        InputStream is = handler.getXmlContentStream();
        Document document = DomUtils.streamToDocument(is, true);
        Assertions.assertNotNull(document);
        Assertions.assertEquals(handler.getVersionable(), "false");
    }


    @Test
    public void testMultipleVersionOfRELSEXT() throws ParserConfigurationException, SAXException, TransformerException {
        InputStream foxml = GetTilesUrlSaxHandlerTest.class.getClassLoader().getResourceAsStream("foxml/534faa9e-b675-46f3-a4db-e107127a1112.xml");
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser saxParser = factory.newSAXParser();
        GetDatastreamContentSaxHandler handler = new GetDatastreamContentSaxHandler("RELS-EXT");
        try {
            saxParser.parse(foxml, handler);
        } catch (SAXException e) {
            if (!FOUND.equals(e.getMessage())) {
                throw e;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        InputStream is = handler.getXmlContentStream();
        Document document = DomUtils.streamToDocument(is, true);
        Assertions.assertNotNull(document);
        StringWriter writer = new StringWriter();
        DomUtils.print(document.getDocumentElement(),writer);

        Element hasModel = DomUtils.findElement(document.getDocumentElement(), "hasModel");
        Assertions.assertNotNull(hasModel);
        Attr resource = hasModel.getAttributeNodeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource");
        Assertions.assertNotNull(resource);
        Assertions.assertTrue(resource.getValue().endsWith("page12"));

        //System.out.println(hasModel);
        Assertions.assertEquals(handler.getVersionable(),"true");
        Assertions.assertEquals(handler.getLastAcceptedVersion(),12);

    }
}