package org.ceskaexpedice.akubra.impl;

import org.ceskaexpedice.akubra.*;
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexItem;
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexQueryParameters;
import org.ceskaexpedice.akubra.core.repository.*;
import org.ceskaexpedice.jaxbmodel.DigitalObject;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RepositoryAccessImpl implements RepositoryAccess {
    private static final Logger LOGGER = Logger.getLogger(RepositoryAccessImpl.class.getName());

    private Repository repository;

    public RepositoryAccessImpl(Repository repository) {
        this.repository = repository;
    }


    @Override
    public void ingest(DigitalObject digitalObject) {
        repository.ingestObject(digitalObject);
    }

    @Override
    public boolean objectExists(String pid) {
        return this.repository.objectExists(pid);
    }

    @Override
    public DigitalObject getObject(String pid, FoxmlType foxmlType) {
        Lock readLock = repository.getReadLock(pid);
        try {
            RepositoryObject repositoryObject = repository.getObject(pid);
            if(repositoryObject == null) {
                return null;
            }
            if (foxmlType == FoxmlType.archive) {
                DigitalObject digitalObject = repositoryObject.getDigitalObject();
                repository.resolveArchivedDatastreams(digitalObject);
            }
            return repositoryObject.getDigitalObject();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public ObjectProperties getObjectProperties(String pid) {
        RepositoryObject repositoryObject = repository.getObject(pid);
        return new ObjectPropertiesImpl(repositoryObject);
    }

    @Override
    public void deleteObject(String pid) {

    }

    @Override
    public void deleteObject(String pid, boolean deleteDataOfManagedDatastreams, boolean deleteRelationsWithThisAsTarget) {

    }

    @Override
    public InputStream marshallObject(DigitalObject obj) {
        return repository.marshallObject(obj);
    }

    @Override
    public DigitalObject unmarshallStream(InputStream inputStream) {
        return repository.unmarshallStream(inputStream);
    }

    @Override
    public boolean datastreamExists(String pid, String dsId) {
        RepositoryObject repositoryObject = repository.getObject(pid);
        return repositoryObject.streamExists(dsId);
    }

    @Override
    public DatastreamMetadata getDatastreamMetadata(String pid, String dsId) {
        Lock readLock = repository.getReadLock(pid);
        try {
            RepositoryObject object = repository.getObject(pid);
            RepositoryDatastream stream = object.getStream(dsId);
            return new DatastreamMetadataImpl(stream);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public InputStream getDatastreamContent(String pid, String dsId) {
        Lock readLock = repository.getReadLock(pid);
        try {
            InputStream lastVersionContent = repository.getObject(pid).getStream(dsId).getLastVersionContent();
            return lastVersionContent;
        } finally {
            readLock.unlock();
        }
//        try {
//            /*
//            pid = makeSureObjectPid(pid);
//            if (this.accessLog != null && this.accessLog.isReportingAccess(pid, datastreamName)) {
//                reportAccess(pid, datastreamName);
//            }*/
//
//            DigitalObject object = repository.getObject(pid).getDigitalObject();
//            if (object != null) {
//                InputStream lastVersionContent = repository.getObject(pid).getStream(dsId).getLastVersionContent();
//                return new DatastreamContentWrapperImpl(lastVersionContent);
//                /* TODO
//                DatastreamVersionType stream = RepositoryUtils.getLastStreamVersion(object, dsId);
//                if (stream != null) {
//                    InputStream lastVersionContent = repository.getObject(pid).getStream(dsId).getLastVersionContent();
//                    return new DatastreamContentWrapperImpl(object, lastVersionContent);
//                    // TODO return new DatastreamContentWrapperImpl(object, RepositoryUtils.getStreamContent(stream, repository));
//                    return null;
//                } else {
//                    throw new IOException("cannot find stream '" + dsId + "' for pid '" + pid + "'");
//                }
//
//                 */
//            } else {
//                throw new IOException("cannot find pid '" + pid + "'");
//            }
//        } catch (Exception e) {
//            throw new RepositoryException(e);
//        }
    }

    @Override
    public RelsExtWrapper processDatastreamRelsExt(String pid) {
        RepositoryObject repositoryObject = repository.getObject(pid);
        return new RelsExtWrapperImpl(repositoryObject);
    }

    @Override
    public List<String> getDatastreamNames(String pid) {
        Lock readLock = repository.getReadLock(pid);
        try {
            RepositoryObject object = repository.getObject(pid);
            List<RepositoryDatastream> streams = object.getStreams();
            return streams.stream().map(it -> {
                try {
                    return it.getName();
                } catch (RepositoryException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    return null;
                }
            }).collect(Collectors.toList());
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void iterateProcessingIndex(ProcessingIndexQueryParameters params, Consumer<ProcessingIndexItem> action) {
        repository.getProcessingIndexFeeder().iterate(params, action);
    }

    @Override
    public <T> T doWithReadLock(String pid, LockOperation<T> operation) {
        Lock readLock = repository.getReadLock(pid);
        try {
            return operation.execute();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public <T> T doWithWriteLock(String pid, LockOperation<T> operation) {
        Lock writeLock = repository.getWriteLock(pid);
        try {
            return operation.execute();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void shutdown() {
        repository.shutdown();
    }

    /*
    @Override
    public void ingestObject(org.dom4j.Document foxmlDoc, String pid) {
        DigitalObject digitalObject = foxmlDocToDigitalObject(foxmlDoc);
        Lock writeLock = AkubraDOManager.getWriteLock(pid);
        try {
            akubraRepositoryImpl.ingestObject(digitalObject);
            akubraRepositoryImpl.commitTransaction();
        } finally {
            writeLock.unlock();
        }
    }*/

    /*
    @Override
    public void deleteObject(String pid, boolean deleteDataOfManagedDatastreams) {
        Lock writeLock = AkubraDOManager.getWriteLock(pid);
        try {
            akubraRepositoryImpl.deleteObject(pid, deleteDataOfManagedDatastreams, true);
            akubraRepositoryImpl.commitTransaction();
        } finally {
            writeLock.unlock();
        }
    }*/

    /*
    @Override
    public boolean datastreamExists(String pid, KnownDatastreams dsId) {
        boolean exists = this.repositoryApi.datastreamExists(pid, dsId);
        return exists;
    }*/


    /*
    @Override
    public Pair<Long, List<String>> getPidsOfObjectsByModel(String model, int rows, int pageIndex) throws RepositoryException, IOException, SolrServerException {
        String query = String.format("type:description AND model:%s", "model\\:" + model); //prvni "model:" je filtr na solr pole, druhy "model:" je hodnota pole, coze  uprime zbytecne
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .sortField("title")
                .ascending(true)
                .rows(rows)
                .pageIndex(pageIndex)
                .fieldsToFetch(List.of("source"))
                .build();
        org.apache.commons.lang3.tuple.Pair<Long, List<SolrDocument>> cp =
                akubraRepositoryImpl.getProcessingIndexFeeder().getPageSortedByTitle(
                        params.getQueryString(),
                        params.getRows(),
                        params.getPageIndex(),
                        params.getFieldsToFetch()
                );
        return new ResultMapper<Pair<Long, List<String>>>() {
            @Override
            public Pair<Long, List<String>> map(List<SolrDocument> documents, long totalRecords) {
                List<String> pids = documents.stream().map(sd -> {
                    Object fieldValue = sd.getFieldValue("source");
                    return fieldValue.toString();
                }).collect(Collectors.toList());
                return new Pair<>(totalRecords, pids);
            }
        }.map(cp.getRight(), cp.getLeft());

        // ---------- original---------------------------
        String query = String.format("type:description AND model:%s", "model\\:" + model); //prvni "model:" je filtr na solr pole, druhy "model:" je hodnota pole, coze  uprime zbytecne
        org.apache.commons.lang3.tuple.Pair<Long, List<SolrDocument>> cp = akubraRepositoryImpl.getProcessingIndexFeeder().getPageSortedByTitle(query, rows, pageIndex, Arrays.asList("source"));
        Long numberOfRecords = cp.getLeft();
        List<String> pids = cp.getRight().stream().map(sd -> {
            Object fieldValue = sd.getFieldValue("source");
            return fieldValue.toString();
        }).collect(Collectors.toList());
        return new Pair<>(numberOfRecords, pids);
    }

     */
    /*
    @Override
    public void updateInlineXmlDatastream(String pid, KnownDatastreams dsId, org.dom4j.Document streamDoc, String formatUri) throws RepositoryException, IOException {
        Lock writeLock = AkubraDOManager.getWriteLock(pid);
        try {
            RepositoryObject object = akubraRepositoryImpl.getObject(pid);

            object.deleteStream(dsId);
            object.createStream(dsId, "text/xml", new ByteArrayInputStream(streamDoc.asXML().getBytes(Charset.forName("UTF-8"))));

        } finally {
            writeLock.unlock();
        }
    }*/

    /*
    @Override
    public void updateBinaryDatastream(String pid, KnownDatastreams dsId, String mimeType, byte[] byteArray) throws RepositoryException {
        Lock writeLock = AkubraDOManager.getWriteLock(pid);
        try {
            RepositoryObject object = akubraRepositoryImpl.getObject(pid);
            if (object != null) {
                if (object.streamExists(streamName)) {
                    object.deleteStream(streamName);
                }
                ByteArrayInputStream bos = new ByteArrayInputStream(byteArray);
                object.createManagedStream(streamName, mimeType, bos);
            }
        } finally {
            writeLock.unlock();
        }
    }*/

    /*
    @Override
    public void setDatastreamXml(String pid, KnownDatastreams dsId, org.dom4j.Document ds) {
        Lock writeLock = AkubraDOManager.getWriteLock(pid);
        try {
            org.dom4j.Document foxml = getFoxml(pid);
            org.dom4j.Element originalDsEl = (org.dom4j.Element) Dom4jUtils.buildXpath(String.format("/foxml:digitalObject/foxml:datastream[@ID='%s']", dsId)).selectSingleNode(foxml);
            if (originalDsEl != null) {
                originalDsEl.detach();
            }
            foxml.getRootElement().add(ds.getRootElement().detach());
            updateLastModifiedTimestamp(foxml);
            DigitalObject updatedDigitalObject = foxmlDocToDigitalObject(foxml);
            akubraRepositoryImpl.deleteObject(pid, false, false);
            akubraRepositoryImpl.ingestObject(updatedDigitalObject);
            akubraRepositoryImpl.commitTransaction();
        } finally {
            writeLock.unlock();
        }
    }*/

    /*
    @Override
    public void deleteDatastream(String pid, KnownDatastreams dsId) {
        Lock writeLock = AkubraDOManager.getWriteLock(pid);
        try {
            RepositoryObject object = akubraRepositoryImpl.getObject(pid);
            if (object != null) {
                if (object.streamExists(streamName)) {
                    object.deleteStream(streamName);
                }
            }
        } finally {
            writeLock.unlock();
        }
    }*/

    //------Processing index----------------------------------------------------
    /*
    @Override
    public <T> T queryProcessingIndex(ProcessingIndexQueryParameters params, ProcessingIndexResultMapper<T> mapper) {
        org.apache.commons.lang3.tuple.Pair<Long, List<SolrDocument>> cp =
                akubraRepositoryImpl.getProcessingIndexFeeder().getPageSortedByTitle(
                        params.getQueryString(),
                        params.getRows(),
                        params.getPageIndex(),
                        params.getFieldsToFetch()
                );

        // Use the provided mapper to convert results
        return mapper.map(cp.getRight(), cp.getLeft());
    };
*/
    /*
    private void reportAccess(String pid, String streamName) {
        try {
            this.accessLog.reportAccess(pid, streamName);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Can't write statistic records for " + pid + ", stream name: " + streamName, e);
        }
    }*/
    /*
    private SupportedFormats determineSupportedFormat(KnownDatastreams id) {
        // Example logic to determine supported formats
        if (id == KnownDatastreams.BIBLIO_DC) {
            return new SupportedFormats(false, true, false);
        } else {
            return new SupportedFormats(true, true, true);
        }
    }*/
    /*
    private void updateLastModifiedTimestamp(org.dom4j.Document foxml) {
        Attribute valueAttr = (Attribute) Dom4jUtils.buildXpath("/foxml:digitalObject/foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/view#lastModifiedDate']/@VALUE").selectSingleNode(foxml);
        if (valueAttr != null) {
            valueAttr.setValue(LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        } else {
            org.dom4j.Element objectProperties = (org.dom4j.Element) Dom4jUtils.buildXpath("/foxml:digitalObject/foxml:objectProperties").selectSingleNode(foxml);
            org.dom4j.Element propertyLastModified = objectProperties.addElement(new QName("property", NS_FOXML));
            propertyLastModified.addAttribute("NAME", "info:fedora/fedora-system:def/view#lastModifiedDate");
            propertyLastModified.addAttribute("VALUE", LocalDateTime.now().format(RepositoryApi.TIMESTAMP_FORMATTER));
        }
    }*/
    /*
    private void appendNewInlineXmlDatastreamVersion(org.dom4j.Document foxml, String dsId, org.dom4j.Document streamDoc, String formatUri) {
        org.dom4j.Element datastreamEl = (org.dom4j.Element) Dom4jUtils.buildXpath(String.format("/foxml:digitalObject/foxml:datastream[@ID='%s']", dsId)).selectSingleNode(foxml);
        if (datastreamEl != null) {
            int latestDsIdVersion = extractLatestDsIdVersion(datastreamEl);
            int newDsIdVesion = latestDsIdVersion + 1;
            org.dom4j.Element dsVersionEl = datastreamEl.addElement("datastreamVersion", NAMESPACE_FOXML);
            dsVersionEl.addAttribute("ID", dsId + "." + newDsIdVesion);
            dsVersionEl.addAttribute("CREATED", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
            dsVersionEl.addAttribute("MIMETYPE", "application/xml");
            if (formatUri != null) {
                dsVersionEl.addAttribute("FORMAT_URI", formatUri);
            }
            org.dom4j.Element xmlContentEl = dsVersionEl.addElement("xmlContent", NAMESPACE_FOXML);
            xmlContentEl.add(streamDoc.getRootElement().detach());
        }
    }*/
    /*
    private int extractLatestDsIdVersion(org.dom4j.Element datastreamEl) {
        List<org.dom4j.Node> dsVersionEls = Dom4jUtils.buildXpath("foxml:datastreamVersion").selectNodes(datastreamEl);
        int maxVersion = -1;
        for (org.dom4j.Node node : dsVersionEls) {
            org.dom4j.Element versionEl = (org.dom4j.Element) node;
            String ID = Dom4jUtils.stringOrNullFromAttributeByName(versionEl, "ID");
            int versionNumber = Integer.valueOf(ID.split("\\.")[1]);
            if (versionNumber > maxVersion) {
                maxVersion = versionNumber;
            }
        }
        return maxVersion;
    }*/
    /*
    private AkubraDOManager manager;
    private AkubraRepository repository;
    private ProcessingIndexFeeder feeder;
    private AggregatedAccessLogs accessLog;

     */

    /*
    @Inject
    public RepositoryAccessImpl(ProcessingIndexFeeder feeder, @Nullable AggregatedAccessLogs accessLog, @Named("akubraCacheManager") CacheManager cacheManager) throws IOException {
        super( accessLog);
        try {
            this.manager = new AkubraDOManager(cacheManager);
            this.feeder = feeder;
            this.repository = AkubraRepositoryImpl.build(feeder, this.manager);
            this.accessLog = accessLog;

        } catch (Exception e) {
            throw new IOException(e);
        }
    }*/


}
