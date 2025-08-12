/*
 * Copyright (C) 2025  Inovatika
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
package org.ceskaexpedice.akubra.core.processingindex;

import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.AkubraRepositoryFactory;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.config.RepositoryConfiguration;
import org.ceskaexpedice.akubra.processingindex.ConflictingOwnedAndFosteredParents;
import org.ceskaexpedice.akubra.processingindex.OwnedAndFosteredChildren;
import org.ceskaexpedice.akubra.processingindex.OwnedAndFosteredParents;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndexItem;
import org.ceskaexpedice.testutils.AkubraTestsUtils;
import org.ceskaexpedice.testutils.IntegrationTestsUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

import static org.ceskaexpedice.testutils.AkubraTestsUtils.TEST_REPOSITORY;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ProcessingIndexSolrTest_integration
 * !!! It requires Solr instance running with processing index containing appropriate testing data to pass the tests
 */
public class ProcessingIndexConflictTest_integration {

    private static AkubraRepository akubraRepository;
    private static Properties testsProperties;


    @BeforeAll
    static void beforeAll() {
        testsProperties = IntegrationTestsUtils.loadProperties();
        RepositoryConfiguration config = AkubraTestsUtils.createRepositoryConfig(TEST_REPOSITORY.toFile().getAbsolutePath(), testsProperties, null);
        akubraRepository = AkubraRepositoryFactory.createRepository(config);
    }

    @AfterAll
    static void afterAll() {
        akubraRepository.shutdown();
    }

    @Test
    void testParents() {

        akubraRepository.pi().rebuildProcessingIndex("uuid:c9b6c867-6d60-4a64-9519-95f5e66ea910", null);
        akubraRepository.pi().rebuildProcessingIndex("uuid:463d0452-091e-470f-9af8-fe2f2b9f3a28", null);
        akubraRepository.pi().commit();

        List<ProcessingIndexItem> parents = akubraRepository.pi().getParents("uuid:3ea8b6c6-7273-4e4b-80d0-29db24f11174");
        // Conflict is ok
        Assertions.assertTrue(parents.size() == 2);

        String deletingPage = "uuid:3ea8b6c6-7273-4e4b-80d0-29db24f11174";

        OwnedAndFosteredChildren pidsOfChildren = akubraRepository.pi().getOwnedAndFosteredChildren(deletingPage);
        Assertions.assertTrue(pidsOfChildren.own().size() == 0);
        String model = akubraRepository.pi().getModel(deletingPage);
        Assertions.assertEquals("page", model);


        try {
            OwnedAndFosteredParents pidsOfParents = akubraRepository.pi().getOwnedAndFosteredParents(deletingPage);
            System.out.println(pidsOfParents.own());
            Assertions.fail("Expecting exception due to multiple parents");
        } catch (RepositoryException e) {
            // ok
        }

        ConflictingOwnedAndFosteredParents conflictingOwnerAndFosteredParents = akubraRepository.pi().getConflictingOwnerAndFosteredParents(deletingPage);
        Assertions.assertNotNull(conflictingOwnerAndFosteredParents);
        Assertions.assertTrue(conflictingOwnerAndFosteredParents.ownItems().size() == 2);
    }
}
