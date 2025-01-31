package org.ceskaexpedice.akubra.access;

import org.ceskaexpedice.akubra.TestsUtilities;
import org.ceskaexpedice.akubra.core.RepositoryConfiguration;
import org.ceskaexpedice.akubra.core.repository.RepositoryDatastream;
import org.ceskaexpedice.akubra.core.repository.RepositoryException;
import org.ceskaexpedice.hazelcast.HazelcastConfiguration;
import org.ceskaexpedice.hazelcast.ServerNode;
import org.ceskaexpedice.akubra.utils.DomUtils;
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
        testsProperties = TestsUtilities.loadProperties();
        HazelcastConfiguration hazelcastConfig = TestsUtilities.createHazelcastConfig(testsProperties);
        ServerNode.ensureHazelcastNode(hazelcastConfig);

        RepositoryConfiguration config = TestsUtilities.createRepositoryConfig(testsProperties, hazelcastConfig);
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
        ContentWrapper resultWrapper = repositoryAccess.getObject(pidTitlePage, FoxmlType.regular);
        assertNotNull(resultWrapper);
        InputStream asStream = resultWrapper.asStream();
        assertNotNull(asStream);
        TestsUtilities.debugPrint(convertUsingBytes(asStream),testsProperties);
    }

    @Test
    void testGetObject_asXmlDom4j() {
        ContentWrapper resultWrapper = repositoryAccess.getObject(pidTitlePage, FoxmlType.regular);
        assertNotNull(resultWrapper);
        Document asXmlDom4j = resultWrapper.asXmlDom4j();
        assertNotNull(asXmlDom4j);
        TestsUtilities.debugPrint(asXmlDom4j.asXML(),testsProperties);
    }

    @Test
    void testGetObject_asXmlDom() {
        ContentWrapper resultWrapper = repositoryAccess.getObject(pidTitlePage, FoxmlType.regular);
        assertNotNull(resultWrapper);
        org.w3c.dom.Document asXmlDom = resultWrapper.asXmlDom();
        assertNotNull(asXmlDom);
        TestsUtilities.debugPrint(DomUtils.toString(asXmlDom.getDocumentElement(), true),testsProperties);
    }

    @Test
    void testGetObject_asString() {
        ContentWrapper resultWrapper = repositoryAccess.getObject(pidTitlePage, FoxmlType.regular);
        assertNotNull(resultWrapper);
        String asString = resultWrapper.asString();
        assertNotNull(asString);
        TestsUtilities.debugPrint(asString,testsProperties);
    }

    @Test
    void testGetObjectArchive_asStream() {
        ContentWrapper resultWrapper = repositoryAccess.getObject(pidTitlePage, FoxmlType.archive);
        assertNotNull(resultWrapper);
        InputStream asStream = resultWrapper.asStream();
        assertNotNull(asStream);
        TestsUtilities.debugPrint(convertUsingBytes(asStream),testsProperties);
    }

    @Test
    void testGetObjectArchive_asXmlDom4j() {
        ContentWrapper resultWrapper = repositoryAccess.getObject(pidTitlePage, FoxmlType.archive);
        assertNotNull(resultWrapper);
        Document asXmlDom4j = resultWrapper.asXmlDom4j();
        assertNotNull(asXmlDom4j);
        TestsUtilities.debugPrint(asXmlDom4j.asXML(),testsProperties);
    }

    @Test
    void testGetObjectArchive_asXmlDom() {
        ContentWrapper resultWrapper = repositoryAccess.getObject(pidTitlePage, FoxmlType.archive);
        assertNotNull(resultWrapper);
        org.w3c.dom.Document asXmlDom = resultWrapper.asXmlDom();
        assertNotNull(asXmlDom);
        TestsUtilities.debugPrint(DomUtils.toString(asXmlDom.getDocumentElement(), true),testsProperties);
    }

    @Test
    void testGetObjectArchive_asString() {
        ContentWrapper resultWrapper = repositoryAccess.getObject(pidTitlePage, FoxmlType.archive);
        assertNotNull(resultWrapper);
        String asString = resultWrapper.asString();
        assertNotNull(asString);
        TestsUtilities.debugPrint(asString,testsProperties);
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
        InputStream imgThumb = repositoryAccess.getDatastreamContent(pidTitlePage, "IMG_THUMB").asStream();
        assertNotNull(imgThumb);
    }

    @Test
    void testGetDatastreamContentBinary_asXmlDom() {
        assertThrows(RepositoryException.class, () -> {
            repositoryAccess.getDatastreamContent(pidTitlePage, "IMG_THUMB").asXmlDom();
        });
    }

    @Test
    void testGetDatastreamContent_asXmlDom() {
        org.w3c.dom.Document xmlDom = repositoryAccess.getDatastreamContent(pidTitlePage, "DC").asXmlDom();
        assertNotNull(xmlDom);
        TestsUtilities.debugPrint(DomUtils.toString(xmlDom.getDocumentElement(), true),testsProperties);
    }

    @Test
    void testGetDatastreamContentBinary_asXmlDom4j() {
        assertThrows(RepositoryException.class, () -> {
            repositoryAccess.getDatastreamContent(pidTitlePage, "IMG_THUMB").asXmlDom4j();
        });
    }

    @Test
    void testGetDatastreamContent_asXmlDom4j() {
        Document xmlDom4j = repositoryAccess.getDatastreamContent(pidTitlePage, "DC").asXmlDom4j();
        assertNotNull(xmlDom4j);
        TestsUtilities.debugPrint(xmlDom4j.asXML(),testsProperties);
    }

    @Test
    void testGetDatastreamContentBinary_asString() {
        String imgThumb = repositoryAccess.getDatastreamContent(pidTitlePage, "IMG_THUMB").asString();
        assertNotNull(imgThumb);
        TestsUtilities.debugPrint(imgThumb,testsProperties);
    }

    @Test
    void testGetDatastreamContent_asString() {
        String dc = repositoryAccess.getDatastreamContent(pidTitlePage, "DC").asString();
        assertNotNull(dc);
        TestsUtilities.debugPrint(dc,testsProperties);
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
        TestsUtilities.debugPrint(String.join(", ", datastreamNames),testsProperties);
    }

    @Test
    void testLocks() {
        // TODO test actual locking with multiple threads
        String pid = pidMonograph;
        String pid1 = pidTitlePage;
        Boolean result = repositoryAccess.doWithWriteLock(pid, () -> {
            Document xmlDom4j = repositoryAccess.getObject(pid, FoxmlType.regular).asXmlDom4j();
            Boolean result1 = repositoryAccess.doWithReadLock(pid1, () -> {
                Document xmlDom4j1 = repositoryAccess.getObject(pid1, FoxmlType.regular).asXmlDom4j();
                return true;
            });
            return result1;
        });
        assertTrue(result);
    }

    private String convertUsingBytes(InputStream inputStream) {
        try {
            byte[] bytes = inputStream.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
