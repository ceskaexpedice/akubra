/*
 * Copyright (C) 2025 Inovatika
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
package org.ceskaexpedice.akubra.impl;

import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.DatastreamContentWrapper;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.core.repository.CoreRepository;
import org.ceskaexpedice.akubra.core.repository.RepositoryObject;
import org.ceskaexpedice.akubra.relsext.RelsExtHandler;
import org.ceskaexpedice.akubra.relsext.RelsExtWrapper;
import org.ceskaexpedice.akubra.utils.RelsExtUtils;

import java.util.logging.Logger;

/**
 * AkubraRepositoryImpl
 */
public class RelsExtHandlerImpl implements RelsExtHandler {
    private static final Logger LOGGER = Logger.getLogger(RelsExtHandlerImpl.class.getName());

    private AkubraRepository akubraRepository;
    private CoreRepository coreRepository;

    public RelsExtHandlerImpl(AkubraRepository akubraRepository, CoreRepository coreRepository) {
        this.coreRepository = coreRepository;
        this.akubraRepository = akubraRepository;
    }

    @Override
    public RelsExtWrapper get(String pid) {
        DatastreamContentWrapper datastreamContent = akubraRepository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT);
        return new RelsExtWrapperImpl(datastreamContent);
    }

    @Override
    public boolean relationExists(String pid, String relation, String namespace) {
        DatastreamContentWrapper datastreamContent = akubraRepository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT);
        return RelsExtUtils.relsExtRelationsExists(datastreamContent.asDom(true), relation, namespace);
    }

    @Override
    public void addRelation(String pid, String relation, String namespace, String targetRelation) {
        RepositoryObject repositoryObject = coreRepository.getAsRepositoryObject(pid);
        if (repositoryObject == null) {
            return;
        }
        repositoryObject.relsExtAddRelation(relation, namespace, targetRelation);
    }

    @Override
    public void removeRelation(String pid, String relation, String namespace, String targetRelation) {
        RepositoryObject repositoryObject = coreRepository.getAsRepositoryObject(pid);
        if (repositoryObject == null) {
            return;
        }
        repositoryObject.relsExtRemoveRelation(relation, namespace, targetRelation);
    }

    @Override
    public void removeRelationsByNameAndNamespace(String pid, String relation, String namespace) {
        RepositoryObject repositoryObject = coreRepository.getAsRepositoryObject(pid);
        if (repositoryObject == null) {
            return;
        }
        repositoryObject.relsExtRemoveRelationsByNameAndNamespace(relation, namespace);
    }

    @Override
    public void removeRelationsByNamespace(String pid, String namespace) {
        RepositoryObject repositoryObject = coreRepository.getAsRepositoryObject(pid);
        if (repositoryObject == null) {
            return;
        }
        repositoryObject.relsExtRemoveRelationsByNamespace(namespace);
    }

    @Override
    public void addLiteral(String pid, String relation, String namespace, String value) {
        RepositoryObject repositoryObject = coreRepository.getAsRepositoryObject(pid);
        if (repositoryObject == null) {
            return;
        }
        repositoryObject.relsExtAddLiteral(relation, namespace, value);
    }

    @Override
    public void removeLiteral(String pid, String relation, String namespace, String value) {
        RepositoryObject repositoryObject = coreRepository.getAsRepositoryObject(pid);
        if (repositoryObject == null) {
            return;
        }
        repositoryObject.relsExtRemoveLiteral(relation, namespace, value);
    }

}
