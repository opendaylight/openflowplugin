/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.eric.codec.match;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.openflowjava.eric.api.EricConstants;

public class EricHeaderTest {

    private static final int ERIC_FIELD_CODE = 4;
    private static final int VALUE_LENGTH = 14;
    private static long header;

    private EricHeader ericHeader;

    @Test
    public void ericHeaderTest() {
        header = createHeader();
        ericHeader = new EricHeader(header);
        assertEquals(EricConstants.ERICOXM_OF_EXPERIMENTER_ID, ericHeader.getOxmClass());
        assertEquals(ERIC_FIELD_CODE, ericHeader.getEricField());
        assertEquals(false, ericHeader.isHasMask());
        assertEquals(VALUE_LENGTH, ericHeader.getLength());
    }

    @Test
    public void equalsTest() {
        Object notHeader = new Object();
        header = createHeader();
        ericHeader = new EricHeader(header);
        assertFalse(ericHeader.equals(notHeader));
    }

    @Test
    public void equalsTest1() {
        header = createHeader();
        ericHeader = new EricHeader(header);

        assertTrue(ericHeader.equals(ericHeader));
    }

    @Test
    public void toStringTest() {
        header = createHeader();
        ericHeader = new EricHeader(header);

        String shouldBe = new String("EricHeader " + "[headerAsLong=" + header + ", " + "oxmClass="
               + EricConstants.ERICOXM_OF_EXPERIMENTER_ID + "," + " ericField=" + ERIC_FIELD_CODE + "," + " hasMask="
                + false + "," + " length=" + VALUE_LENGTH + "]");
        assertEquals(shouldBe, ericHeader.toString());
    }

    private long createHeader() {
        long result = 0;
        int oxmClass = 4096 << 16;
        result = result | oxmClass;
        int oxmField = ERIC_FIELD_CODE << 9;
        result = result | oxmField;
        int mask = 0 << 8;
        result = result | mask;
        int length = VALUE_LENGTH;
        result = result | length;
        return result;
     }

}