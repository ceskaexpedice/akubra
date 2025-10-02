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

public class GetTilesUrlSaxHandlerTest {


    @Test
    public void testOnlyOneVersionOfRELSEXT() throws ParserConfigurationException, SAXException {
        InputStream foxml = GetTilesUrlSaxHandlerTest.class.getClassLoader().getResourceAsStream("foxml/bbae3d9d-ee9f-43a3-9431-024db12de070.xml");
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser saxParser = factory.newSAXParser();
        GetTilesUrlSaxHandler handler = new GetTilesUrlSaxHandler();
        try {
            saxParser.parse(foxml, handler);
        } catch (SAXException e) {
            if (!FOUND.equals(e.getMessage())) {
                throw e;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String tilesUrl = handler.getTilesUrl();
        Assertions.assertEquals(handler.getVersionable(),"false");
        Assertions.assertTrue(handler.getLastAcceptedVersion()==0);
        Assertions.assertTrue("http://imageserver.mzk.cz/mzk01/000/181/899/1949/34/bbae3d9d-ee9f-43a3-9431-024db12de070".equals(tilesUrl));
    }

    @Test
    public void testMultipleVersionOfRELSEXT() throws ParserConfigurationException, SAXException {
        InputStream foxml = GetTilesUrlSaxHandlerTest.class.getClassLoader().getResourceAsStream("foxml/534faa9e-b675-46f3-a4db-e107127a1112.xml");
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser saxParser = factory.newSAXParser();
        GetTilesUrlSaxHandler handler = new GetTilesUrlSaxHandler();
        try {
            saxParser.parse(foxml, handler);
        } catch (SAXException e) {
            if (!FOUND.equals(e.getMessage())) {
                throw e;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String tilesUrl = handler.getTilesUrl();
        Assertions.assertNotNull(tilesUrl);
        Assertions.assertEquals(handler.getVersionable(),"true");
        Assertions.assertTrue(handler.getLastAcceptedVersion() == 12);
        Assertions.assertTrue("http://imageserver.mzk.cz/mzk01/000/181/899/1949/34/534faa9e-b675-46f3-a4db-e107127a1112".equals(tilesUrl));
    }
}
