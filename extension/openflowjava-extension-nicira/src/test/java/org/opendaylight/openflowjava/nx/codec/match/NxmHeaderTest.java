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

import java.math.BigInteger;
import org.junit.Test;

public class NxmHeaderTest {

    private static final BigInteger NO_MASK_HEADER = new BigInteger("80000496", 16);
    private static final BigInteger MASK_HEADER = new BigInteger("80000596", 16);
    private static final BigInteger EXP_HEADER = new BigInteger("FFFFF4F300F2AB31", 16);

    @Test
    public void nxmHeaderNoMaskTest() {
        NxmHeader nxmHeaderFromBigInt = new NxmHeader(NO_MASK_HEADER);

        assertEquals(0x8000, nxmHeaderFromBigInt.getOxmClass());
        assertEquals(0x2, nxmHeaderFromBigInt.getNxmField());
        assertFalse(nxmHeaderFromBigInt.isHasMask());
        assertEquals(0x96, nxmHeaderFromBigInt.getLength());
        assertEquals(0x80000496L, nxmHeaderFromBigInt.toLong());
        assertFalse(nxmHeaderFromBigInt.isExperimenter());

        NxmHeader nxmHeaderFromFields = new NxmHeader(
                nxmHeaderFromBigInt.getOxmClass(),
                nxmHeaderFromBigInt.getNxmField(),
                nxmHeaderFromBigInt.isHasMask(),
                nxmHeaderFromBigInt.getLength());

        assertEquals(NO_MASK_HEADER, nxmHeaderFromFields.toBigInteger());
        assertEquals(0x80000496L, nxmHeaderFromFields.toLong());
        assertFalse(nxmHeaderFromFields.isExperimenter());

        NxmHeader nxmHeaderFromLong = new NxmHeader(NO_MASK_HEADER.longValue());

        assertEquals(NO_MASK_HEADER, nxmHeaderFromLong.toBigInteger());
        assertEquals(0x80000496L, nxmHeaderFromLong.toLong());
        assertFalse(nxmHeaderFromLong.isExperimenter());

        assertEquals(nxmHeaderFromBigInt, nxmHeaderFromFields);
        assertEquals(nxmHeaderFromBigInt, nxmHeaderFromLong);
        assertEquals(nxmHeaderFromLong, nxmHeaderFromFields);
        assertEquals(nxmHeaderFromBigInt.hashCode(), nxmHeaderFromFields.hashCode());
        assertEquals(nxmHeaderFromBigInt.hashCode(), nxmHeaderFromLong.hashCode());
    }

    @Test
    public void nxmHeaderMaskTest() {
        NxmHeader nxmHeaderFromBigInt = new NxmHeader(MASK_HEADER);

        assertEquals(0x8000, nxmHeaderFromBigInt.getOxmClass());
        assertEquals(0x2, nxmHeaderFromBigInt.getNxmField());
        assertTrue(nxmHeaderFromBigInt.isHasMask());
        assertEquals(0x96, nxmHeaderFromBigInt.getLength());
        assertEquals(0x80000596L, nxmHeaderFromBigInt.toLong());
        assertFalse(nxmHeaderFromBigInt.isExperimenter());

        NxmHeader nxmHeaderFromFields = new NxmHeader(
                nxmHeaderFromBigInt.getOxmClass(),
                nxmHeaderFromBigInt.getNxmField(),
                nxmHeaderFromBigInt.isHasMask(),
                nxmHeaderFromBigInt.getLength());

        assertEquals(MASK_HEADER, nxmHeaderFromFields.toBigInteger());
        assertEquals(0x80000596L, nxmHeaderFromFields.toLong());
        assertFalse(nxmHeaderFromFields.isExperimenter());

        NxmHeader nxmHeaderFromLong = new NxmHeader(MASK_HEADER.longValue());

        assertEquals(MASK_HEADER, nxmHeaderFromLong.toBigInteger());
        assertEquals(0x80000596L, nxmHeaderFromLong.toLong());
        assertFalse(nxmHeaderFromLong.isExperimenter());

        assertEquals(nxmHeaderFromBigInt, nxmHeaderFromFields);
        assertEquals(nxmHeaderFromBigInt, nxmHeaderFromLong);
        assertEquals(nxmHeaderFromLong, nxmHeaderFromFields);
        assertEquals(nxmHeaderFromBigInt.hashCode(), nxmHeaderFromFields.hashCode());
        assertEquals(nxmHeaderFromBigInt.hashCode(), nxmHeaderFromLong.hashCode());
    }

    @Test
    public void nxmHeaderExpTest() {
        NxmHeader nxmHeaderFromBigInt = new NxmHeader(EXP_HEADER);

        assertEquals(0xFFFF, nxmHeaderFromBigInt.getOxmClass());
        assertEquals(0xF4 >>> 1, nxmHeaderFromBigInt.getNxmField());
        assertFalse(nxmHeaderFromBigInt.isHasMask());
        assertEquals(0xF3, nxmHeaderFromBigInt.getLength());
        assertEquals(0xFFFFF4F300F2AB31L, nxmHeaderFromBigInt.toLong());
        assertTrue(nxmHeaderFromBigInt.isExperimenter());
        assertEquals(0x00F2AB31L, nxmHeaderFromBigInt.getExperimenterId());

        NxmHeader nxmHeaderFromFields = new NxmHeader(
                nxmHeaderFromBigInt.getNxmField(),
                nxmHeaderFromBigInt.isHasMask(),
                nxmHeaderFromBigInt.getLength(),
                nxmHeaderFromBigInt.getExperimenterId());

        assertEquals(EXP_HEADER, nxmHeaderFromFields.toBigInteger());
        assertEquals(0xFFFFF4F300F2AB31L, nxmHeaderFromFields.toLong());
        assertTrue(nxmHeaderFromFields.isExperimenter());
        assertEquals(0x00F2AB31L, nxmHeaderFromFields.getExperimenterId());

        NxmHeader nxmHeaderFromLong = new NxmHeader(EXP_HEADER.longValue());

        assertEquals(EXP_HEADER, nxmHeaderFromLong.toBigInteger());
        assertEquals(0xFFFFF4F300F2AB31L, nxmHeaderFromLong.toLong());
        assertTrue(nxmHeaderFromLong.isExperimenter());
        assertEquals(0x00F2AB31L, nxmHeaderFromLong.getExperimenterId());

        assertEquals(nxmHeaderFromBigInt, nxmHeaderFromFields);
        assertEquals(nxmHeaderFromBigInt, nxmHeaderFromLong);
        assertEquals(nxmHeaderFromLong, nxmHeaderFromFields);
        assertEquals(nxmHeaderFromBigInt.hashCode(), nxmHeaderFromFields.hashCode());
        assertEquals(nxmHeaderFromBigInt.hashCode(), nxmHeaderFromLong.hashCode());
    }

}
