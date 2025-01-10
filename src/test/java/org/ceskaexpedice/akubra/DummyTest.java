package org.ceskaexpedice.akubra;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DummyTest {
    @Test
    void testIsInputValid() {
        // Valid input should return true
        assertTrue(Module1.isInputValid("pepo"), "Non-blank input should be valid");

        // Blank input should return false
        assertFalse(Module1.isInputValid(" "), "Blank input should not be valid");

        // Null input should return false
        assertFalse(Module1.isInputValid(null), "Null input should not be valid");
    }
}
