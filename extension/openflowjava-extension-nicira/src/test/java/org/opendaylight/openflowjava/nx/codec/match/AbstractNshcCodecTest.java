/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.match;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.NxExpMatchEntryValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NshcCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NshcCaseValueBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

public class AbstractNshcCodecTest {

    private class TestCodec extends AbstractNshcCodec {

        @Override
        public int getNxmFieldCode() {
            return 0;
        }

        @Override
        public Class<? extends MatchField> getNxmField() {
            return null;
        }
    }

    private AbstractNshcCodec testCodec;
    private ByteBuf buffer;

    private static final Uint32 NSHC_VALUE = Uint32.TEN;
    private static final Uint32 NSHC_MASK = Uint32.valueOf(0xF);

    @Before
    public void setUp() {
        testCodec = new TestCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void serializeTestNoMask() {
        NxExpMatchEntryValue matchEntryValue = createMatchEntryValue(NSHC_VALUE, null);

        testCodec.serializeValue(matchEntryValue, false, buffer);

        assertEquals(NSHC_VALUE.longValue(), buffer.readUnsignedInt());
        assertFalse(buffer.isReadable());
    }

    @Test
    public void serializeTestMask() {
        NxExpMatchEntryValue matchEntryValue = createMatchEntryValue(NSHC_VALUE, NSHC_MASK);

        testCodec.serializeValue(matchEntryValue, true, buffer);

        assertEquals(NSHC_VALUE.longValue(), buffer.readUnsignedInt());
        assertEquals(NSHC_MASK.longValue(), buffer.readUnsignedInt());
        assertFalse(buffer.isReadable());
    }

    @Test
    public void deserializeTestNoMask() {
        writeBuffer(buffer, NSHC_VALUE, null);

        NxExpMatchEntryValue value = testCodec.deserializeValue(buffer, false);

        assertEquals(NSHC_VALUE, ((NshcCaseValue) value).getNshc());
        assertFalse(buffer.isReadable());
    }

    @Test
    public void deserializeTestMask() {
        writeBuffer(buffer, NSHC_VALUE, NSHC_MASK);

        NxExpMatchEntryValue value = testCodec.deserializeValue(buffer, true);

        assertEquals(NSHC_VALUE, ((NshcCaseValue) value).getNshc());
        assertEquals(NSHC_MASK, ((NshcCaseValue) value).getMask());
        assertFalse(buffer.isReadable());
    }

    private static NxExpMatchEntryValue createMatchEntryValue(final Uint32 value, final Uint32 mask) {
        return new NshcCaseValueBuilder().setNshc(value).setMask(mask).build();
    }

    private static void writeBuffer(final ByteBuf message, final Uint32 value, final Uint32 mask) {
        message.writeInt(value.intValue());
        if (mask != null) {
            message.writeInt(mask.intValue());
        }
    }
}