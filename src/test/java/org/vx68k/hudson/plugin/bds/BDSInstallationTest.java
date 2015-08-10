/*
 * BDSInstallationTest
 * Copyright (C) 2015 Nishimura Software Studio
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.vx68k.hudson.plugin.bds;

import java.util.Collections;
import hudson.tools.ToolProperty;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Collection of unit tests for {@link BDSInstallation}.
 *
 * @author Kaz Nishimura
 * @since 4.0
 */
public class BDSInstallationTest {

    private static final String TEST_NAME = "RAD Studio XE";
    private static final String TEST_HOME = "";

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testInstance() {
        BDSInstallation bds = new BDSInstallation(
                TEST_NAME, TEST_HOME,
                Collections.<ToolProperty<?>>emptyList());
        assertEquals(TEST_NAME, bds.getName());
        assertEquals(TEST_HOME, bds.getHome());
        assertTrue(bds.getProperties().isEmpty());
    }
}
