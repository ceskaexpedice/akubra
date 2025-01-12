/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ceskaexpedice.akubra.utils;

import cz.incad.kramerius.utils.SafeSimpleDateFormat;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.xpath.*;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class FedoraUtils {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(FedoraUtils.class.getName());
    public static final String RELS_EXT_STREAM = "RELS-EXT";
    static final String IMG_THUMB_STREAM = "IMG_THUMB";
    static final String IMG_FULL_STREAM = "IMG_FULL";
    static final String IMG_PREVIEW_STREAM = "IMG_PREVIEW";
    static final String ALTO_STREAM = "ALTO";
    public static final String DC_STREAM = "DC";
    public static final String BIBLIO_MODS_STREAM = "BIBLIO_MODS";
    static final String TEXT_OCR_STREAM = "TEXT_OCR";
    static final String MP3_STREAM = "MP3";
    static final String OGG_STREAM = "OGG";
    static final String WAV_STREAM = "WAV";

    static final String POLICY_STREAM = "POLICY";

    static List<String> DEFAULT_SECURED_STREAM = new ArrayList<String>() {{

        add(IMG_FULL_STREAM);
        add(IMG_PREVIEW_STREAM);

        add(TEXT_OCR_STREAM);
        add(ALTO_STREAM);

        add(MP3_STREAM);
        add(WAV_STREAM);
        add(OGG_STREAM);

        add(ALTO_STREAM);

    }};

    static List<String> getSecuredStreams() {
        String[] securedStreamsExtension = KConfiguration.getInstance().getSecuredAditionalStreams();
        List<String> retvals = new ArrayList<>(DEFAULT_SECURED_STREAM);
        Arrays.stream(securedStreamsExtension).forEach(retvals::add);
        return retvals;
    }

    static List<String> INTERNAL_STREAM = new ArrayList<String>() {{
        add(RELS_EXT_STREAM);
        add(IMG_THUMB_STREAM);
        add(IMG_FULL_STREAM);
        add(IMG_PREVIEW_STREAM);
        add(ALTO_STREAM);
        add(DC_STREAM);
        add(BIBLIO_MODS_STREAM);
    }};

    static List<String> AUDIO_STREAMS = new ArrayList<String>() {{
        add(OGG_STREAM);
        add(MP3_STREAM);
        add(WAV_STREAM);
    }};

    /**
     * Stream for fedora internal use
     */
    static List<String> FEDORA_INTERNAL_STREAMS = new ArrayList<String>() {{
        //add(RELS_EXT_STREAM);
        add(POLICY_STREAM);
    }};

    static final String RELS_EXT_FORMAT_URI = "info:fedora/fedora-system:FedoraRELSExt-1.0";
    static final String BIBLIO_MODS_FORMAT_URI = "http://www.loc.gov/mods/v3";
    static final String DC_FORMAT_URI = "http://www.openarchives.org/OAI/2.0/oai_dc/";

    public static String getFormatUriForDS(String dsID) {
        if (RELS_EXT_STREAM.equals(dsID)) {
            return RELS_EXT_FORMAT_URI;
        }
        if (BIBLIO_MODS_STREAM.equals(dsID)) {
            return BIBLIO_MODS_FORMAT_URI;
        }
        if (DC_STREAM.equals(dsID)) {
            return DC_FORMAT_URI;
        }
        return null;
    }

    /*
    static ArrayList<String> getRdfPids(String pid, String relation) {
        ArrayList<String> pids = new ArrayList<String>();
        try {

            String command = KConfiguration.getInstance().getFedoraHost() + "/get/" + pid + "/" + RELS_EXT_STREAM;
            InputStream is = RESTHelper.inputStream(command, KConfiguration.getInstance().getFedoraUser(), KConfiguration.getInstance().getFedoraPass());
            Document contentDom = XMLUtils.parseDocument(is);
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            String xPathStr = "/RDF/Description/" + relation;
            XPathExpression expr = xpath.compile(xPathStr);
            NodeList nodes = (NodeList) expr.evaluate(contentDom, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node childnode = nodes.item(i);
                if (!childnode.getNodeName().contains("hasModel")) {
                    pids.add(childnode.getNodeName() + " "
                            + childnode.getAttributes().getNamedItem("rdf:resource").getNodeValue().split("/")[1]);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return pids;
    }*/

    /*
    private static String findFirstPagePid(String pid) {
        ArrayList<String> pids = new ArrayList<String>();
        try {
            KConfiguration config = KConfiguration.getInstance();
            String command = config.getFedoraHost() + "/get/" + pid + "/" + RELS_EXT_STREAM;
            InputStream is = RESTHelper.inputStream(command, config.getFedoraUser(), KConfiguration.getInstance().getFedoraPass());
            Document contentDom = XMLUtils.parseDocument(is);
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            XPathExpression expr = xpath.compile("/RDF/Description/*");
            NodeList nodes = (NodeList) expr.evaluate(contentDom, XPathConstants.NODESET);
            List<String> treePredicates = Arrays.asList(config.getPropertyList("fedora.treePredicates"));
            for (int i = 0; i < nodes.getLength(); i++) {
                Node childnode = nodes.item(i);
                String nodeName = childnode.getNodeName();
                String simpleNodeName = nodeName.substring(nodeName.lastIndexOf(":") + 1);
                if (nodeName.contains("hasPage") || nodeName.contains("isOnPage")) {
                    return childnode.getAttributes().getNamedItem("rdf:resource").getNodeValue();
                } else if (!nodeName.contains("hasModel") && childnode.hasAttributes() && treePredicates.contains(simpleNodeName)
                        && childnode.getAttributes().getNamedItem("rdf:resource") != null) {

                    pids.add(childnode.getAttributes().getNamedItem("rdf:resource").getNodeValue().split("/")[1]);
                }
            }
            for (String relpid : pids) {
                return FedoraUtils.findFirstPagePid(relpid);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }
*/
    /**
     * Returns url stream
     *
     * @return
     */
    private static String getDjVuImage(String pid) {
        String imagePath = KConfiguration.getInstance().getFedoraHost() + "/get/" + pid + "/" + IMG_FULL_STREAM;
        return imagePath;
    }

    /**
     * Returns path to fedora stream
     *
     * @param conf   KConfiguraiton
     * @param stream Stream ID
     * @return
     */
    private static String getFedoraStreamPath(String pid, String stream) {
        String imagePath = KConfiguration.getInstance().getFedoraHost() + "/get/" + pid + "/" + stream;
        return imagePath;
    }

    /**
     * Returns path to fedora description
     *
     * @return
     */
    private static String getFedoraDescribe() {
        String describePath = KConfiguration.getInstance().getFedoraHost() + "/describe?xml=true";
        return describePath;
    }


    /**
     * Returns true if given stream (profile of the stream) is referenced stream by URL
     *
     * @param profileDoc Profile document
     */
    private static boolean isFedoraExternalStream(Document profileDoc) throws XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expr = xpath.compile("/datastreamProfile/dsLocationType/text()");
        NodeList nodes = (NodeList) expr.evaluate(profileDoc, XPathConstants.NODESET);
        if (nodes.getLength() > 0) {
            Text text = (Text) nodes.item(0);
            String trimedString = text.getData().trim();
            return trimedString.equals("URL");
        } else {
            return false;
        }
    }

    private static String getLocation(Document profileDoc) throws XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expr = xpath.compile("/datastreamProfile/dsLocation/text()");
        NodeList nodes = (NodeList) expr.evaluate(profileDoc, XPathConstants.NODESET);
        if (nodes.getLength() > 0) {
            Text text = (Text) nodes.item(0);
            String trimedString = text.getData().trim();
            return trimedString;
        } else {
            return null;
        }
    }

    /**
     * Returns thumb stream
     *
     * @return
     */
    private static String getThumbnailFromFedora(String pid) {
        String imagePath = KConfiguration.getInstance().getFedoraHost() + "/get/" + pid + "/" + IMG_THUMB_STREAM;
        return imagePath;
    }

    /**
     * Returns list of fedora streams
     *
     * @return
     */
    private static String getFedoraDatastreamsList(String pid) {
        String datastreamsListPath = KConfiguration.getInstance().getFedoraHost() + "/objects/" + pid + "/datastreams?format=xml";
        return datastreamsListPath;
    }

    private static String getVersionCompatibilityPrefix(String fedoraVersion) {
        return fedoraVersion.substring(0, 3).replace('.', '_');
    }

    private static final DateFormat[] XSD_DATE_FORMATS = {
            new SafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'S'Z'"),
            new SafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"),
            new SafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'S"),
            new SafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
            new SafeSimpleDateFormat("yyyy-MM-dd'Z'"),
            new SafeSimpleDateFormat("yyyy-MM-dd")};

}
