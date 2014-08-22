/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * This JUnit test class tests the DefaultFlags class.
 *
 * @author Simon Hunt
 */
public class DefaultFlagsTest {

    private DefaultDeviceInfo ddi;
    private Flags flags;

    private static final String PROCURVE = "isProCurve";
    private static final String WIRELESS = "isWireless";
    private static final String SPECIAL = "isSpecial";

    private static final Set<String> FLAGS = new HashSet<String>();
    private static DefaultDeviceType TYPE_FIXTURE;

    @BeforeClass
    public static void classSetUp() {
        FLAGS.add(PROCURVE);
        FLAGS.add(WIRELESS);
        // Don't add isSpecial flag
        TYPE_FIXTURE = new DefaultDeviceTypeBuilder("MyType").flags(FLAGS).build();
    }

    @Before
    public void setUp() {
        ddi = new DefaultDeviceInfo(TYPE_FIXTURE);
        flags = new DefaultFlags(ddi);
    }

    // == TESTS GO HERE ==

    @Test
    public void checkFlags() {
        print(EOL + "checkFlags()");
        print(ddi.toDebugString());
        assertTrue(AM_HUH, flags.hasFlag(PROCURVE));
        assertTrue(AM_HUH, flags.hasFlag(WIRELESS));
        assertFalse(AM_HUH, flags.hasFlag(SPECIAL));
    }

    private static final String[] BAD_FLAGS = {
            "",
            ".",
            "fred.flintstone",
            "123",
            "1a",
            ":asdef",
            "$$",
    };

    @Test
    public void checkBadFlags() {
        for (String flag: BAD_FLAGS) {
            try {
                print("testing: [" + flag + "] ");
                flags.hasFlag(flag);
                fail(AM_NOEX);
            } catch (IllegalArgumentException e) {
                print("  caught: " + e);
            }
        }

    }
}
