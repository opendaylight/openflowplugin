/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.nx.codec.match;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;

public class NxmHeaderTest {

    NxmHeader nxmHeader;

    private static long header;
    private static final int NXM_FIELD_CODE = 4;
    private static final int VALUE_LENGTH = 14;

    @Test
    public void NxmHeaderTest() {
        header = createHeader();
        nxmHeader = new NxmHeader(header);

        assertEquals(OxmMatchConstants.NXM_1_CLASS, nxmHeader.getOxmClass());
        assertEquals(NXM_FIELD_CODE, nxmHeader.getNxmField());
        assertEquals(false, nxmHeader.isHasMask());
        assertEquals(VALUE_LENGTH, nxmHeader.getLength());
    }

    @Test
    public void hashCodeTest() {

    }

    @Test
    public void equalsTest() {
        Object notHeader = new Object();
        header = createHeader();
        nxmHeader = new NxmHeader(header);

        assertFalse(nxmHeader.equals(notHeader));
    }

    @Test
    public void equalsTest1() {
        header = createHeader();
        nxmHeader = new NxmHeader(header);

        assertTrue(nxmHeader.equals(nxmHeader));
    }

    @Test
    public void toStringTest() {
        header = createHeader();
        nxmHeader = new NxmHeader(header);

        String shouldBe = new String("NxmHeader " +
                                        "[headerAsLong=" + header + ", " +
                                        "oxmClass=" + OxmMatchConstants.NXM_1_CLASS + "," +
                                        " nxmField=" + NXM_FIELD_CODE + "," +
                                        " hasMask=" + false + "," +
                                        " length=" + VALUE_LENGTH + "]");

        assertEquals(shouldBe, nxmHeader.toString());
    }


    private long createHeader() {
        long result = 0;
        int oxmClass = 1 << 16;
        result = result | oxmClass;
        int oxmField = NXM_FIELD_CODE << 9;
        result = result | oxmField;
        int mask = 0 << 8;
        result = result | mask;
        int length = VALUE_LENGTH;
        result = result | length;

        return result;
    }
}