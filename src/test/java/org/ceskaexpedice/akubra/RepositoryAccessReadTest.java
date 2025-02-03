package org.ceskaexpedice.akubra;

import org.ceskaexpedice.akubra.core.repository.RepositoryDatastream;
import org.ceskaexpedice.akubra.utils.Dom4jUtils;
import org.ceskaexpedice.akubra.testutils.TestUtilities;
import org.ceskaexpedice.akubra.utils.Utils;
import org.ceskaexpedice.hazelcast.HazelcastConfiguration;
import org.ceskaexpedice.hazelcast.ServerNode;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.ceskaexpedice.jaxbmodel.DigitalObject;
import org.dom4j.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class RepositoryAccessReadTest {
    private static final String pidMonograph = "uuid:5035a48a-5e2e-486c-8127-2fa650842e46";
    private static final String pidTitlePage = "uuid:12993b4a-71b4-4f19-8953-0701243cc25d";

    private static RepositoryAccess repositoryAccess;
    private static Properties testsProperties;

    @BeforeAll
    static void beforeAll() {
        testsProperties = TestUtilities.loadProperties();
        HazelcastConfiguration hazelcastConfig = TestUtilities.createHazelcastConfig(testsProperties);
        ServerNode.ensureHazelcastNode(hazelcastConfig);

        URL resource = TestUtilities.class.getClassLoader().getResource("data");
        RepositoryConfiguration config = TestUtilities.createRepositoryConfig(resource.getFile(), testsProperties, hazelcastConfig);
        repositoryAccess = RepositoryAccessFactory.createRepositoryAccess(config);
    }

    @AfterAll
    static void afterAll() {
        repositoryAccess.shutdown();
        ServerNode.shutdown();
    }

    @Test
    void testObjectExists() {
        boolean objectExists = repositoryAccess.objectExists(pidTitlePage);
        assertTrue(objectExists);
    }

    @Test
    void testGetObject_asStream() {
        DigitalObject digitalObject = repositoryAccess.getObject(pidTitlePage, FoxmlType.regular);
        assertNotNull(digitalObject);
        InputStream objectStream = repositoryAccess.marshallObject(digitalObject);
        assertNotNull(objectStream);
     //   TestUtilities.debugPrint(convertUsingBytes(objectStream),testsProperties);
    }

    @Test
    void testGetObject_asXmlDom4j() {
        DigitalObject digitalObject = repositoryAccess.getObject(pidTitlePage, FoxmlType.regular);
        assertNotNull(digitalObject);
        InputStream objectStream = repositoryAccess.marshallObject(digitalObject);
        assertNotNull(objectStream);
        Document asXmlDom4j =  Dom4jUtils.streamToDocument(objectStream, true);
        assertNotNull(asXmlDom4j);
        TestUtilities.debugPrint(asXmlDom4j.asXML(),testsProperties);
    }

    @Test
    void testGetObject_asXmlDom() {
        DigitalObject digitalObject = repositoryAccess.getObject(pidTitlePage, FoxmlType.regular);
        assertNotNull(digitalObject);
        InputStream objectStream = repositoryAccess.marshallObject(digitalObject);
        assertNotNull(objectStream);
        org.w3c.dom.Document asXmlDom = DomUtils.streamToDocument(objectStream);
        assertNotNull(asXmlDom);
        TestUtilities.debugPrint(DomUtils.toString(asXmlDom.getDocumentElement(), true),testsProperties);
    }

    @Test
    void testGetObject_asString() {
        DigitalObject digitalObject = repositoryAccess.getObject(pidTitlePage, FoxmlType.regular);
        assertNotNull(digitalObject);
        InputStream objectStream = repositoryAccess.marshallObject(digitalObject);
        assertNotNull(objectStream);
        String asString = Utils.streamToString(objectStream);;
        assertNotNull(asString);
        TestUtilities.debugPrint(asString,testsProperties);
    }

    @Test
    void testGetObjectArchive_asStream() {
        DigitalObject digitalObject = repositoryAccess.getObject(pidTitlePage, FoxmlType.archive);
        assertNotNull(digitalObject);
        InputStream objectStream = repositoryAccess.marshallObject(digitalObject);
        assertNotNull(objectStream);
        TestUtilities.debugPrint(Utils.streamToString(objectStream),testsProperties);
    }

    @Test
    void testGetObjectProperty() {
        String propertyOwnerId = repositoryAccess.getObjectProperties(pidTitlePage).getProperty("info:fedora/fedora-system:def/model#ownerId");
        assertEquals("fedoraAdmin", propertyOwnerId);
        LocalDateTime propertyCreated = repositoryAccess.getObjectProperties(pidTitlePage).getPropertyCreated();
        assertNull(propertyCreated); // no milliseconds in data
        String propertyLabel = repositoryAccess.getObjectProperties(pidTitlePage).getPropertyLabel();
        assertEquals("- none -", propertyLabel);
        LocalDateTime propertyLastModified = repositoryAccess.getObjectProperties(pidTitlePage).getPropertyLastModified();
        assertEquals("2024-05-20T13:03:27.151", propertyLastModified.toString());
    }

    @Test
    void testDatastreamExists() {
        boolean exists = repositoryAccess.datastreamExists(pidTitlePage, "DC");
        assertTrue(exists);
    }

    @Test
    void testGetDatastreamMetadata() {
        DatastreamMetadata datastreamMetadata = repositoryAccess.getDatastreamMetadata(pidTitlePage, "DC");
        assertNotNull(datastreamMetadata);
        assertEquals("text/xml", datastreamMetadata.getMimetype());
        assertEquals("DC", datastreamMetadata.getId());
        assertEquals(RepositoryDatastream.Type.DIRECT, datastreamMetadata.getType());
        assertEquals("Mon Feb 26 15:40:29 CET 2018", datastreamMetadata.getLastModified().toString());
    }

    @Test
    void testGetDatastreamContent_asStream() {
        InputStream imgThumb = repositoryAccess.getDatastreamContent(pidTitlePage, "IMG_THUMB");
        assertNotNull(imgThumb);
    }

    @Test
    void testGetDatastreamContent_asXmlDom() {
        org.w3c.dom.Document xmlDom = DomUtils.streamToDocument(repositoryAccess.getDatastreamContent(pidTitlePage, "DC"));
        assertNotNull(xmlDom);
        TestUtilities.debugPrint(DomUtils.toString(xmlDom.getDocumentElement(), true),testsProperties);
    }

    @Test
    void testGetDatastreamContent_asXmlDom4j() {
        Document xmlDom4j = Dom4jUtils.streamToDocument(repositoryAccess.getDatastreamContent(pidTitlePage, "DC"), true);
        assertNotNull(xmlDom4j);
        TestUtilities.debugPrint(xmlDom4j.asXML(),testsProperties);
    }

    @Test
    void testGetDatastreamContent_asString() {
        String dc = Utils.streamToString(repositoryAccess.getDatastreamContent(pidTitlePage, "DC"));
        assertNotNull(dc);
        TestUtilities.debugPrint(dc,testsProperties);
    }

    @Test
    void testProcessDatastreamRelsExt() {
        RelsExtWrapper relsExtWrapper = repositoryAccess.processDatastreamRelsExt(pidMonograph);
        assertNotNull(relsExtWrapper);
        List<RelsExtRelation> relations = relsExtWrapper.getRelations(null);
        assertEquals(37, relations.size());
        List<RelsExtLiteral> literals = relsExtWrapper.getLiterals(null);
        assertEquals(5, literals.size());
    }

    @Test
    void testGetDatastreamNames() {
        List<String> datastreamNames = repositoryAccess.getDatastreamNames(pidTitlePage);
        assertEquals(9, datastreamNames.size());
        TestUtilities.debugPrint(String.join(", ", datastreamNames),testsProperties);
    }

    @Test
    void testLocks() {
        // TODO test actual locking with multiple threads
        String pid = pidMonograph;
        String pid1 = pidTitlePage;
        Boolean result = repositoryAccess.doWithWriteLock(pid, () -> {
            repositoryAccess.getObject(pid, FoxmlType.regular);
            Boolean result1 = repositoryAccess.doWithReadLock(pid1, () -> {
                repositoryAccess.getObject(pid1, FoxmlType.regular);
                return true;
            });
            return result1;
        });
        assertTrue(result);
    }

}
