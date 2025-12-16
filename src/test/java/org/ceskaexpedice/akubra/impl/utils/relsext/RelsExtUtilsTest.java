package org.ceskaexpedice.akubra.impl.utils.relsext;

import org.ceskaexpedice.akubra.relsext.RelsExtUtils;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RelsExtUtilsTest {

    @Test
    public void testPageRelsExt() {
        InputStream is = RelsExtUtilsTest.class.getResourceAsStream("info%253Afedora%252Fuuid%253A3a288340-a00e-11e8-a81d-5ef3fc9bb22f");
        assertNotNull(is);
        Document document = DomUtils.streamToDocument(is, true);
        Assertions.assertNotNull(document);

        Element relsextFromGivenFOXML = RelsExtUtils.getRELSEXTFromGivenFOXML(document.getDocumentElement());
        Assertions.assertNotNull(relsextFromGivenFOXML);

        String model = RelsExtUtils.getModel(relsextFromGivenFOXML);
        Assertions.assertNotNull(model);
        Assertions.assertEquals("page", model);

        List<String> sortedRelationsPid = RelsExtUtils.getSortedRelationsPid(relsextFromGivenFOXML);
        Assertions.assertNotNull(sortedRelationsPid);
        Assertions.assertEquals(0, sortedRelationsPid.size());

        String tilesUrl = RelsExtUtils.getTilesUrl(relsextFromGivenFOXML);
        Assertions.assertNotNull(tilesUrl);
        Assertions.assertEquals("http://imgserver.nkp.cz/2018/ac0ec160-93d7-11e8-87bd-005056827e52/img/uc_ac0ec160-93d7-11e8-87bd-005056827e52_0147", tilesUrl);
    }

    @Test
    public void testMonographRelsExt() {
        InputStream is = RelsExtUtilsTest.class.getResourceAsStream("info%3Afedora%2Fuuid%3A7e6619ab-e814-4505-a2c1-c94acd8bb3ea");
        assertNotNull(is);
        Document document = DomUtils.streamToDocument(is, true);
        Assertions.assertNotNull(document);

        Element relsextFromGivenFOXML = RelsExtUtils.getRELSEXTFromGivenFOXML(document.getDocumentElement());
        Assertions.assertNotNull(relsextFromGivenFOXML);

        String model = RelsExtUtils.getModel(relsextFromGivenFOXML);
        Assertions.assertNotNull(model);
        Assertions.assertEquals("monograph", model);

        List<String> sortedRelationsPid = RelsExtUtils.getSortedRelationsPid(relsextFromGivenFOXML);
        Assertions.assertNotNull(sortedRelationsPid);
        Assertions.assertEquals(6, sortedRelationsPid.size());
    }

}
