/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.junit.Test;
import org.opendaylight.of.lib.AbstractTest;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for OxmVlanId.
 *
 * @author Simon Hunt
 */
public class OxmVlanIdTest extends AbstractTest {

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (OxmVlanId mode: OxmVlanId.values())
            print(mode);
        assertEquals(AM_UXCC, 3, OxmVlanId.values().length);
    }

    @Test
    public void values() {
        print(EOL + "values()");
        assertEquals(AM_NEQ, 0x0000, OxmVlanId.NONE.getValue());
        assertEquals(AM_NEQ, 0x1000, OxmVlanId.PRESENT.getValue());
        assertEquals(AM_NEQ, 0xabcd, OxmVlanId.EXACT.getValue());
    }
}
