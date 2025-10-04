package org.ceskaexpedice.akubra.impl.utils.relsext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

import static org.ceskaexpedice.akubra.impl.utils.InternalSaxUtils.FOUND;

public class GetPidOfFirstChildSaxHandlerTest {

    @Test
    public void testOnlyOneVersionOfRELSEXT() throws ParserConfigurationException, SAXException {
        InputStream foxml = GetPidOfFirstChildSaxHandlerTest.class.getClassLoader().getResourceAsStream("foxml/bbae3d9d-ee9f-43a3-9431-024db12de070.xml");
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser saxParser = factory.newSAXParser();
        GetPidOfFirstChildSaxHandler handler = new GetPidOfFirstChildSaxHandler();
        try {
            saxParser.parse(foxml, handler);
        } catch (SAXException e) {
            if (!FOUND.equals(e.getMessage())) {
                throw e;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String firstPid = handler.getFirstChildPid();
        Assertions.assertNotNull(firstPid);
        Assertions.assertEquals("uuid:c71a7ed0-19ef-11e7-8f17-005056822549",firstPid);
        Assertions.assertTrue(handler.getVersionable().endsWith("false"));
    }

    @Test
    public void testMultipleVersionOfRELSEXT() throws ParserConfigurationException, SAXException {
        InputStream foxml = GetPidOfFirstChildSaxHandlerTest.class.getClassLoader().getResourceAsStream("foxml/534faa9e-b675-46f3-a4db-e107127a1112.xml");
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser saxParser = factory.newSAXParser();
        GetPidOfFirstChildSaxHandler handler = new GetPidOfFirstChildSaxHandler();
        try {
            saxParser.parse(foxml, handler);
        } catch (SAXException e) {
            if (!FOUND.equals(e.getMessage())) {
                throw e;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        String firstPid = handler.getFirstChildPid();
        Assertions.assertNotNull(firstPid);
        Assertions.assertEquals("uuid:c71a7ed0-19ef-11e7-8f17-005056822549",firstPid);
        Assertions.assertTrue(handler.getVersionable().endsWith("true"));
        Assertions.assertTrue(handler.getLastAcceptedVersion() == 12);
    }

}
