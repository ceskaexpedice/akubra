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
package org.ceskaexpedice.akubra;

import org.ceskaexpedice.akubra.config.RepositoryConfiguration;
import org.ceskaexpedice.akubra.reference.*;
import org.ceskaexpedice.testutils.AkubraTestsUtils;
import org.ceskaexpedice.testutils.IntegrationTestsUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.ceskaexpedice.testutils.AkubraTestsUtils.TEST_REPOSITORY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReferenceTest {
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
    void testGetPid() {
        ReferenceFactory referenceFactory = akubraRepository.getReferenceFactory();

        Reference reference = referenceFactory.create("cdk/uuid:123-456");
        assertTrue(reference instanceof CDKReference);
        assertEquals("uuid:123-456", reference.getPid());
        assertEquals("cdk/uuid:123-456", reference.getPrefixedPid());

        reference = referenceFactory.create("info:fedora/uuid:123-456");
        assertTrue(reference instanceof FedoraReference);
        assertEquals("uuid:123-456", reference.getPid());
        assertEquals("info:fedora/uuid:123-456", reference.getPrefixedPid());

        reference = referenceFactory.create("uuid:123-456");
        assertTrue(reference instanceof FedoraReference);
        assertEquals("uuid:123-456", reference.getPid());
        assertEquals("info:fedora/uuid:123-456", reference.getPrefixedPid());

    }

    @Test
    void testGetValueForSPARQLListener() {
        ReferenceFactory referenceFactory = akubraRepository.getReferenceFactory();

        Reference reference = referenceFactory.create("cdk/uuid:123-456");
        assertEquals("cdk/uuid:123-456", reference.getValueForSPARQLListener());

        reference = referenceFactory.create("info:fedora/uuid:123-456");
        assertEquals("uuid:123-456", reference.getValueForSPARQLListener());

        reference = referenceFactory.create("uuid:123-456");
        assertEquals("uuid:123-456", reference.getValueForSPARQLListener());
    }


}
