package org.ceskaexpedice.akubra;

import org.apache.commons.lang3.StringUtils;
import org.ceskaexpedice.akubra.core.RepositoryFactory;
import org.ceskaexpedice.akubra.core.repository.Repository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DummyTest {

    @Test
    void testIsInputValid() {
        assertTrue(1 == 1);
        Repository akubraRepository = RepositoryFactory.createAkubraRepository();

    }

}
