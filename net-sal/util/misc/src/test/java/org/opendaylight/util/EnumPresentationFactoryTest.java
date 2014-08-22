/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;

import static org.junit.Assert.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * This JUnit test class tests the EnumPresentationFactory class.
 *
 * @author Simon Hunt
 */
public class EnumPresentationFactoryTest {

    private EnumPresentationFactory factory;

    @Before
    public void setUp() {
        factory = new EnumPresentationFactory();
        assertEquals(AM_UXS, 0, factory.getBindingsCount());
    }

    // == TESTS GO HERE ==
    @Test
    public void basic() {
        print(EOL + "basic()");
        print(factory);
    }

    private static enum Cavemen { FRED, BARNEY, WILMA }

    @Test
    public void cavemen() {
        print(EOL + "cavemen()");
        factory.register(Cavemen.class);
        assertEquals(AM_UXS, 1, factory.getBindingsCount());
        print(factory.toDebugString());
    }

    private static enum StarWars { ANAKIN, LUKE, LEIA, HAN }

    @Test
    public void starWars() {
        print (EOL + "starWars()");
        try {
            factory.register(StarWars.class);
            fail(AM_NOEX);
        } catch (ValidationException v) {
            print (v);
            List<String> issues = v.getIssues();
            assertEquals(AM_UXS, 5, issues.size());
            // easier to assert, if we re-couch as a set
            Set<String> iset = new HashSet<>(issues);
            assertTrue(AM_HUH, iset.contains("Error: Missing resource: LUKE-name : StarWars.LUKE"));
            assertTrue(AM_HUH, iset.contains("Error: Missing resource: LEIA-desc : StarWars.LEIA"));
            assertTrue(AM_HUH, iset.contains("Error: Empty resource: HAN-name : StarWars.HAN"));
            assertTrue(AM_HUH, iset.contains("Error: Empty resource: HAN-desc : StarWars.HAN"));
            assertTrue(AM_HUH, iset.contains("Info: Validating: org.opendaylight.util.EnumPresentationFactoryTest$StarWars"));
        }
        assertEquals(AM_UXS, 0, factory.getBindingsCount());
        print(factory.toDebugString());
    }

    // NOTE: For the following test to work there must NOT be a corresponding properties file in the resources dir 
    private static enum Planets { EARTH, MARS, VENUS, JUPITER }

    @Test(expected = MissingResourceException.class)
    public void planets() {
        print (EOL + "planets()");
        factory.register(Planets.class);
    }
}
