package org.ceskaexpedice.akubra.access;

import org.ceskaexpedice.akubra.core.Configuration;
import org.ceskaexpedice.akubra.locks.HazelcastServerNode;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.dom4j.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RepositoryAccessTest {

    private static HazelcastServerNode hazelcastServerNode;
    private static RepositoryAccess repositoryAccess;

    @BeforeAll
    static void beforeAll() {
        URL resource = RepositoryAccessTest.class.getClassLoader().getResource("data");
        String testRepoPath = resource.getFile() + "/";
        //String testRepoPath = "c:\\Users\\petr\\.kramerius4\\data\\";
        Configuration config = new Configuration.Builder()
                .processingIndexHost("http://localhost:8983/solr/processing")
                .objectStorePath(testRepoPath + "objectStore")
                .objectStorePattern("##/##")
                .datastreamStorePath(testRepoPath + "datastreamStore")
                .datastreamStorePattern("##/##")
                .cacheTimeToLiveExpiration(60)
                .hazelcastInstance("akubrasync")
                .hazelcastUser("dev")
                .build();
        hazelcastServerNode = new HazelcastServerNode();
        // TODO hazelcastServerNode.contextInitialized(null);
        HazelcastServerNode.ensureHazelcastNode(config);
        repositoryAccess = RepositoryAccessFactory.createRepositoryAccess(config);
    }

    @AfterAll
    static void afterAll() {
        hazelcastServerNode.contextDestroyed(null);
    }

    @Test
    void testObjectExists() {
        boolean objectExists = repositoryAccess.objectExists("uuid:12993b4a-71b4-4f19-8953-0701243cc25d");
        assertTrue(objectExists);
    }

    @Test
    void testGetObject_asStream() {
        ContentWrapper resultWrapper = repositoryAccess.getObject("uuid:12993b4a-71b4-4f19-8953-0701243cc25d", FoxmlType.regular);
        assertNotNull(resultWrapper);
        InputStream asStream = resultWrapper.asStream();
        assertNotNull(asStream);

        // TODO
        String testString = convertUsingBytes(asStream);
        System.out.println(testString);
    }

    @Test
    void testGetObject_asXmlDom4j() {
        ContentWrapper resultWrapper = repositoryAccess.getObject("uuid:12993b4a-71b4-4f19-8953-0701243cc25d", FoxmlType.regular);
        assertNotNull(resultWrapper);
        Document asXmlDom4j = resultWrapper.asXmlDom4j();
        assertNotNull(asXmlDom4j);

        // TODO
        System.out.println(asXmlDom4j.asXML());
    }

    @Test
    void testGetObject_asXmlDom() {
        ContentWrapper resultWrapper = repositoryAccess.getObject("uuid:12993b4a-71b4-4f19-8953-0701243cc25d", FoxmlType.regular);
        assertNotNull(resultWrapper);
        org.w3c.dom.Document asXmlDom = resultWrapper.asXmlDom();
        assertNotNull(asXmlDom);

        // TODO
        System.out.println(DomUtils.toString(asXmlDom.getDocumentElement(), true));
    }

    @Test
    void testGetObject_asString() {
        ContentWrapper resultWrapper = repositoryAccess.getObject("uuid:12993b4a-71b4-4f19-8953-0701243cc25d", FoxmlType.regular);
        assertNotNull(resultWrapper);
        String asString = resultWrapper.asString();
        assertNotNull(asString);

        // TODO
        System.out.println(asString);
    }

    @Test
    void testGetObjectArchive_asStream() {
        ContentWrapper resultWrapper = repositoryAccess.getObject("uuid:12993b4a-71b4-4f19-8953-0701243cc25d", FoxmlType.archive);
        assertNotNull(resultWrapper);
        InputStream asStream = resultWrapper.asStream();
        assertNotNull(asStream);

        // TODO
        String testString = convertUsingBytes(asStream);
        System.out.println(testString);
    }

    @Test
    void testGetObjectArchive_asXmlDom4j() {
        ContentWrapper resultWrapper = repositoryAccess.getObject("uuid:12993b4a-71b4-4f19-8953-0701243cc25d", FoxmlType.archive);
        assertNotNull(resultWrapper);
        Document asXmlDom4j = resultWrapper.asXmlDom4j();
        assertNotNull(asXmlDom4j);

        // TODO
        System.out.println(asXmlDom4j.asXML());
    }

    @Test
    void testGetObjectArchive_asXmlDom() {
        ContentWrapper resultWrapper = repositoryAccess.getObject("uuid:12993b4a-71b4-4f19-8953-0701243cc25d", FoxmlType.archive);
        assertNotNull(resultWrapper);
        org.w3c.dom.Document asXmlDom = resultWrapper.asXmlDom();
        assertNotNull(asXmlDom);

        // TODO
        System.out.println(DomUtils.toString(asXmlDom.getDocumentElement(), true));
    }

    @Test
    void testGetObjectArchive_asString() {
        ContentWrapper resultWrapper = repositoryAccess.getObject("uuid:12993b4a-71b4-4f19-8953-0701243cc25d", FoxmlType.archive);
        assertNotNull(resultWrapper);
        String asString = resultWrapper.asString();
        assertNotNull(asString);

        // TODO
        System.out.println(asString);
    }

    @Test
    void testGetObjectProperty() {
        String propertyOwnerId = repositoryAccess.getObjectProperties("uuid:12993b4a-71b4-4f19-8953-0701243cc25d").getProperty("info:fedora/fedora-system:def/model#ownerId");
        assertNotNull(propertyOwnerId);

        LocalDateTime propertyCreated = repositoryAccess.getObjectProperties("uuid:12993b4a-71b4-4f19-8953-0701243cc25d").getPropertyCreated();
        String propertyLabel = repositoryAccess.getObjectProperties("uuid:12993b4a-71b4-4f19-8953-0701243cc25d").getPropertyLabel();
        LocalDateTime propertyLastModified = repositoryAccess.getObjectProperties("uuid:12993b4a-71b4-4f19-8953-0701243cc25d").getPropertyLastModified();

        // TODO
        System.out.println(propertyOwnerId);
        System.out.println(propertyCreated);
        System.out.println(propertyLabel);
        System.out.println(propertyLastModified);
    }

    @Test
    void testDatastreamExists() {
        /* TODO
        DatastreamMetadata datastreamMetadata = repositoryAccess.getDatastreamMetadata("uuid:12993b4a-71b4-4f19-8953-0701243cc25d", "DC");
        assertNotNull(datastreamMetadata);
        System.out.println(datastreamMetadata.getMimetype());

         */
    }

    @Test
    void testGetDatastreamMetadata() {
        DatastreamMetadata datastreamMetadata = repositoryAccess.getDatastreamMetadata("uuid:12993b4a-71b4-4f19-8953-0701243cc25d", "DC");
        assertNotNull(datastreamMetadata);
        System.out.println(datastreamMetadata.getMimetype());
    }

    @Test
    void testGetDatastreamContent_asStream() {
        InputStream imgThumb = repositoryAccess.getDatastreamContent("uuid:12993b4a-71b4-4f19-8953-0701243cc25d", "IMG_THUMB").asStream();
        assertNotNull(imgThumb);

        // TODO
        Path targetFile = Path.of("c:\\tmp\\output.jpg");
        try {
            Files.copy(imgThumb, targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetDatastreamContent_asXmlDom() {
        InputStream imgThumb = repositoryAccess.getDatastreamContent("uuid:12993b4a-71b4-4f19-8953-0701243cc25d", "IMG_THUMB").asStream();
        assertNotNull(imgThumb);

        // TODO
        Path targetFile = Path.of("c:\\tmp\\output.jpg");
        try {
            Files.copy(imgThumb, targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetDatastreamContent_asXmlDom4j() {
        InputStream imgThumb = repositoryAccess.getDatastreamContent("uuid:12993b4a-71b4-4f19-8953-0701243cc25d", "IMG_THUMB").asStream();
        assertNotNull(imgThumb);

        // TODO
        Path targetFile = Path.of("c:\\tmp\\output.jpg");
        try {
            Files.copy(imgThumb, targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetDatastreamContent_asString() {
        InputStream imgThumb = repositoryAccess.getDatastreamContent("uuid:12993b4a-71b4-4f19-8953-0701243cc25d", "IMG_THUMB").asStream();
        assertNotNull(imgThumb);

        // TODO
        Path targetFile = Path.of("c:\\tmp\\output.jpg");
        try {
            Files.copy(imgThumb, targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testProcessdatastreamRelsExt() {
        RelsExtWrapper relsExtWrapper = repositoryAccess.processDatastreamRelsExt("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        assertNotNull(relsExtWrapper);

        // TODO
        System.out.println("Relations");
        relsExtWrapper.getRelations(null).forEach(System.out::println);
        System.out.println("Literals");
        relsExtWrapper.getLiterals(null).forEach(System.out::println);
    }

    @Test
    void testGetDatastreamNames() {
        // TODO
    }

    @Test
    void testQueryProcessingIndex() {
        String model = "page";
        String query = String.format("type:description AND model:%s", "model\\:" + model); //prvni "model:" je filtr na solr pole, druhy "model:" je hodnota pole, coze  uprime zbytecne
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .sortField("title")
                .ascending(true)
                .rows(10)
                .pageIndex(0)
                .fieldsToFetch(List.of("source"))
                .build();
        repositoryAccess.queryProcessingIndex(params, new Consumer<ProcessingIndexItem>() {
            @Override
            public void accept(ProcessingIndexItem processingIndexItem) {
                // TODO
               // System.out.println(((ProcessingIndexItemImpl)processingIndexItem).getDocument());
            }
        });

    }

    private static String convertUsingBytes(InputStream inputStream) {
        byte[] bytes = null;
        try {
            bytes = inputStream.readAllBytes();
            return new String(bytes, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
