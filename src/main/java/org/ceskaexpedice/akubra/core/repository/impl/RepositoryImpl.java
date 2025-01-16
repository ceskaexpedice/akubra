/*
 * Copyright (C) 2016 Pavel Stastny
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.ceskaexpedice.akubra.core.repository.impl;

import org.ceskaexpedice.akubra.access.RepositoryObjectWrapper;
import org.ceskaexpedice.model.DigitalObject;
import org.ceskaexpedice.model.ObjectPropertiesType;
import org.ceskaexpedice.model.PropertyType;
import org.ceskaexpedice.akubra.core.repository.Repository;
import org.ceskaexpedice.akubra.core.repository.RepositoryException;
import org.ceskaexpedice.akubra.core.repository.RepositoryObject;
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexFeeder;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author pavels
 */
public class RepositoryImpl implements Repository {

    private static final Logger LOGGER = Logger.getLogger(RepositoryImpl.class.getName());

    private AkubraDOManager manager;
    private ProcessingIndexFeeder feeder;

    public RepositoryImpl(ProcessingIndexFeeder feeder, AkubraDOManager manager) {
        super();
        this.feeder = feeder;
        this.manager = manager;
    }

    /* (non-Javadoc)
     * @see cz.incad.fcrepo.Repository#createOrFindObject(java.lang.String)
     */
    @Override
    public RepositoryObject createOrFindObject(String ident) {
        if (objectExists(ident)) {
            try {
                RepositoryObjectImpl obj = new RepositoryObjectImpl(this.manager.readObjectFromStorage(ident), this.manager, this.feeder);
                return obj;
            } catch (IOException e) {
                throw new RepositoryException(e);
            }
        } else {
            try {
                DigitalObject emptyDigitalObject = createEmptyDigitalObject(ident);
                manager.commit(emptyDigitalObject, null);
                try {
                    feeder.deleteByPid(emptyDigitalObject.getPID());
                } catch (Throwable th) {
                    LOGGER.log(Level.SEVERE, "Cannot update processing index for " + ident + " - reindex manually.", th);
                }
                RepositoryObjectImpl obj = new RepositoryObjectImpl(emptyDigitalObject, this.manager, this.feeder);
                return obj;
            } catch (IOException e) {
                throw new RepositoryException(e);
            }
        }
    }

    @Override
    public RepositoryObject ingestObject(DigitalObject contents) {
        if (objectExists(contents.getPID())) {
            throw new RepositoryException("Ingested object exists:" + contents.getPID());
        } else {
            try {
                RepositoryObjectImpl obj = new RepositoryObjectImpl(contents, this.manager, this.feeder);
                manager.commit(obj.getDigitalObject(), null);
                obj.rebuildProcessingIndex();
                return obj;
            } catch (IOException e) {
                throw new RepositoryException(e);
            }
        }

    }

    @Override
    public boolean objectExists(String ident) {
        try {
            return manager.readObjectFromStorage(ident) != null;
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public RepositoryObject getObject(String ident) {
        try {
            DigitalObject digitalObject = this.manager.readObjectFromStorage(ident);
            if (digitalObject == null) {
                //otherwise later causes NPE at places like AkubraUtils.streamExists(DigitalObject object, String streamID)
                throw new RepositoryException("object not consistently found in storage: " + ident);
            }
            RepositoryObjectImpl obj = new RepositoryObjectImpl(digitalObject, this.manager, this.feeder);
            return obj;
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public DigitalObject readObjectCloneFromStorage(String pid) {
        return manager.readObjectCloneFromStorage(pid);
    }

    @Override
    public InputStream retrieveObject(String pid) {
        try {
            return manager.retrieveObject(pid);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }


    @Override
    public void deleteObject(String pid, boolean deleteDataOfManagedDatastreams, boolean deleteRelationsWithThisAsTarget) {
        try {
            this.manager.deleteObject(pid, deleteDataOfManagedDatastreams);
            try {
                // delete relations with this object as a source
                this.feeder.deleteByRelationsForPid(pid);
                // possibly delete relations with this object as a target
                if (deleteRelationsWithThisAsTarget) {
                    this.feeder.deleteByTargetPid(pid);
                }
                // delete this object's description
                this.feeder.deleteDescriptionByPid(pid);
            } catch (Throwable th) {
                LOGGER.log(Level.SEVERE, "Cannot update processing index for " + pid + " - reindex manually.", th);
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        } finally {
            try {
                this.feeder.commit();
                LOGGER.info("CALLED PROCESSING INDEX COMMIT AFTER DELETE " + pid);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SolrServerException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void deleteObject(String pid) {
        deleteObject(pid, true, true);
    }

    /* (non-Javadoc)
     * @see cz.incad.fcrepo.Repository#commitTransaction()
     */
    @Override
    public void commitTransaction() {
        try {
            //to avoid temporary inconsistency between Akubra and Processing index
            this.feeder.commit();
        } catch (IOException | SolrServerException e) {
            throw new RepositoryException(e);
        }
    }

    /* (non-Javadoc)
     * @see cz.incad.fcrepo.Repository#rollbackTransaction()
     */
    @Override
    public void rollbackTransaction() {
        throw new RepositoryException("Transactions not supported in Akubra");
    }

    private DigitalObject createEmptyDigitalObject(String pid) {
        DigitalObject retval = new DigitalObject();
        retval.setPID(pid);
        retval.setVERSION("1.1");
        ObjectPropertiesType objectPropertiesType = new ObjectPropertiesType();
        List<PropertyType> propertyTypeList = objectPropertiesType.getProperty();
        propertyTypeList.add(RepositoryUtils.createProperty("info:fedora/fedora-system:def/model#state", "Active"));
        propertyTypeList.add(RepositoryUtils.createProperty("info:fedora/fedora-system:def/model#ownerId", "fedoraAdmin"));
        String currentTime = RepositoryUtils.currentTimeString();
        propertyTypeList.add(RepositoryUtils.createProperty("info:fedora/fedora-system:def/model#createdDate", currentTime));
        propertyTypeList.add(RepositoryUtils.createProperty("info:fedora/fedora-system:def/view#lastModifiedDate", currentTime));
        retval.setObjectProperties(objectPropertiesType);
        return retval;
    }

    @Override
    public ProcessingIndexFeeder getProcessingIndexFeeder() {
        return this.feeder;
    }

    @Override
    public void resolveArchivedDatastreams(DigitalObject obj) {
        manager.resolveArchivedDatastreams(obj);
    }

    @Override
    public InputStream marshallObject(DigitalObject obj) {
        return manager.marshallObject(obj);
    }

    @Override
    public Lock getReadLock(String pid) {
        Lock readLock = AkubraDOManager.getReadLock(pid);
        return readLock;
    }

    /* TODO

    public static final String DELETE_LITERAL( String relation,String namespace, String value) throws IOException {
        StringTemplate deleteRelation = RELSEXTSPARQLBuilderImpl.SPARQL_TEMPLATES().getInstanceOf("deleteliteral_sparql");
        deleteRelation.setAttribute("namespace", namespace);
        deleteRelation.setAttribute("relation",relation);
        deleteRelation.setAttribute("value",value);
        return deleteRelation.toString();
    }

    public static final String DELETE_RELATION( String relation,String namespace, String target) throws IOException {
        StringTemplate deleteRelation = RELSEXTSPARQLBuilderImpl.SPARQL_TEMPLATES().getInstanceOf("deleterelation_sparql");
        deleteRelation.setAttribute("namespace", namespace);
        deleteRelation.setAttribute("relation",relation);
        deleteRelation.setAttribute("target",target);
        return deleteRelation.toString();
    }

    public static final String DELETE_RELATIONS(Collection<Triple<String,String,String>> triples) throws IOException {
        StringTemplate deleteRelation = RELSEXTSPARQLBuilderImpl.SPARQL_TEMPLATES().getInstanceOf("delete_general");
        deleteRelation.setAttribute("triples", triples);
        return deleteRelation.toString();
    }

    public static final String UPDATE_PID(String pid ) throws IOException {
        StringTemplate updatePid = RELSEXTSPARQLBuilderImpl.SPARQL_TEMPLATES().getInstanceOf("updatepid_sparql");
        updatePid.setAttribute("pid",pid);
        return updatePid.toString();
    }

    public static final String UPDATE_INDEXING_SPARQL() throws IOException {
        StringTemplate indexPid = RELSEXTSPARQLBuilderImpl.SPARQL_TEMPLATES().getInstanceOf("indexable_sparql");
        return indexPid.toString();
    }
    */

    /* TODO
    @Override
    public String getBoundContext() throws RepositoryException {
        throw new RepositoryException("BOUND CONTEXT not supported in Akubra");
    }*/

}
