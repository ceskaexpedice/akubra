package org.ceskaexpedice.akubra.access;

import org.ceskaexpedice.akubra.access.impl.ProcessingIndexItemImpl;
import org.ceskaexpedice.akubra.core.Configuration;
import org.ceskaexpedice.akubra.locks.HazelcastServerNode;
import org.dom4j.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RepositoryAccessTest {

    private static HazelcastServerNode hazelcastServerNode;
    private static RepositoryAccess repositoryAccess;

    @BeforeAll
    static void beforeAll() {
        Configuration config = new Configuration.Builder()
                .processingIndexHost("http://localhost:8983/solr/processing")
                .objectStorePath("c:\\Users\\petr\\.kramerius4\\data\\objectStore")
                .objectStorePattern("##/##")
                .datastreamStorePath("c:\\Users\\petr\\.kramerius4\\data\\datastreamStore")
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
    void testGetObject_regular_stream() {
        ResultWrapper repositoryObjectWrapper = repositoryAccess.getObject("uuid:12993b4a-71b4-4f19-8953-0701243cc25d", FoxmlType.regular);
        assertNotNull(repositoryObjectWrapper);
        InputStream repositoryObjectWrapperStream = repositoryObjectWrapper.asStream();
        assertNotNull(repositoryObjectWrapperStream);

        // TODO
        String regularObjectAsString = convertUsingBytes(repositoryObjectWrapperStream);
        System.out.println(regularObjectAsString);
    }

    @Test
    void testGetObject_archive_stream() {
        ResultWrapper repositoryObjectWrapper = repositoryAccess.getObject("uuid:12993b4a-71b4-4f19-8953-0701243cc25d", FoxmlType.archive);
        assertNotNull(repositoryObjectWrapper);
        InputStream repositoryObjectWrapperStream = repositoryObjectWrapper.asStream();
        assertNotNull(repositoryObjectWrapperStream);

        // TODO
        String regularObjectAsString = convertUsingBytes(repositoryObjectWrapperStream);
        System.out.println(regularObjectAsString);
    }

    @Test
    void testGetObject_regular_xmlDom4j() {
        ResultWrapper repositoryObjectWrapper = repositoryAccess.getObject("uuid:12993b4a-71b4-4f19-8953-0701243cc25d", FoxmlType.regular);
        assertNotNull(repositoryObjectWrapper);
        Document objectWrapperXml = repositoryObjectWrapper.asXmlDom4j();
        assertNotNull(objectWrapperXml);

        // TODO
        System.out.println(objectWrapperXml.asXML());
    }

    @Test
    void testGetObject_archive_xmlDom4j() {
        ResultWrapper repositoryObjectWrapper = repositoryAccess.getObject("uuid:12993b4a-71b4-4f19-8953-0701243cc25d", FoxmlType.archive);
        assertNotNull(repositoryObjectWrapper);
        Document objectWrapperXml = repositoryObjectWrapper.asXmlDom4j();
        assertNotNull(objectWrapperXml);

        // TODO
        System.out.println(objectWrapperXml.asXML());
    }

    @Test
    void testGetObjectProperty() {
        String createdDate = repositoryAccess.getObjectProperties("uuid:12993b4a-71b4-4f19-8953-0701243cc25d").getProperty("info:fedora/fedora-system:def/model#createdDate");
        assertNotNull(createdDate);

        // TODO
        System.out.println(createdDate);
        System.out.println(repositoryAccess.getObjectProperties("uuid:12993b4a-71b4-4f19-8953-0701243cc25d").getPropertyCreated());
        System.out.println(repositoryAccess.getObjectProperties("uuid:12993b4a-71b4-4f19-8953-0701243cc25d").getPropertyLabel());
        System.out.println(repositoryAccess.getObjectProperties("uuid:12993b4a-71b4-4f19-8953-0701243cc25d").getPropertyLastModified());
    }

    @Test
    void testGetDatastreamMetadata() {
        DatastreamMetadata datastreamMetadata = repositoryAccess.getDatastreamMetadata("uuid:12993b4a-71b4-4f19-8953-0701243cc25d", "DC");
        assertNotNull(datastreamMetadata);
        System.out.println(datastreamMetadata.getMimetype());
    }

    @Test
    void testGetDatastreamContent() throws IOException {
        InputStream imgThumb = repositoryAccess.getDatastreamContent("uuid:12993b4a-71b4-4f19-8953-0701243cc25d", "IMG_THUMB").asStream();
        assertNotNull(imgThumb);

        // TODO
        Path targetFile = Path.of("c:\\tmp\\output.jpg");
        Files.copy(imgThumb, targetFile, StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    void testGetDatastreamContent_relsExt() throws IOException {
        Document document = repositoryAccess.getDatastreamContent("uuid:5035a48a-5e2e-486c-8127-2fa650842e46", "RELS-EXT").asXmlDom4j();
        assertNotNull(document);
        System.out.println(document.asXML());
    }

    @Test
    void testProcessRelsExt() {
        // TODO
        RelsExtWrapper relsExtWrapper = repositoryAccess.processDatastreamRelsExt("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        assertNotNull(relsExtWrapper);
        relsExtWrapper.getRelations().forEach(System.out::println);
//        String model = RelsExtHelper.getModel(xmlDom.getDocumentElement());
  //      System.out.println(model);
    }

    @Test
    void testProcessingIndex() {
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
                System.out.println(((ProcessingIndexItemImpl)processingIndexItem).getDocument());
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
