/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsp.grouping.NspValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsp.grouping.NspValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.NxExpMatchEntryValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NspCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NspCaseValueBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

public class NspCodecTest {

    private NspCodec nspCodec;
    private ByteBuf buffer;

    private static final Uint32 NSP_VALUE = Uint32.TEN;
    private static final Uint32 NSP_MASK = Uint32.valueOf(0xF);

    @Before
    public void setUp() {
        nspCodec = new NspCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void serializeTestNoMask() {
        NxExpMatchEntryValue matchEntryValue = createMatchEntryValue(NSP_VALUE, null);

        nspCodec.serializeValue(matchEntryValue, false, buffer);

        assertEquals(NSP_VALUE.longValue(), buffer.readUnsignedInt());
        assertFalse(buffer.isReadable());
    }

    @Test
    public void serializeTestMask() {
        NxExpMatchEntryValue matchEntryValue = createMatchEntryValue(NSP_VALUE, NSP_MASK);

        nspCodec.serializeValue(matchEntryValue, true, buffer);

        assertEquals(NSP_VALUE.longValue(), buffer.readUnsignedInt());
        assertEquals(NSP_MASK.longValue(), buffer.readUnsignedInt());
        assertFalse(buffer.isReadable());
    }

    @Test
    public void deserializeTestNoMask() {
        writeBuffer(buffer, NSP_VALUE, null);

        NxExpMatchEntryValue value = nspCodec.deserializeValue(buffer, false);

        assertEquals(NSP_VALUE, ((NspCaseValue) value).getNspValues().getNsp());
        assertFalse(buffer.isReadable());
    }

    @Test
    public void deserializeTestMask() {
        writeBuffer(buffer, NSP_VALUE, NSP_MASK);

        NxExpMatchEntryValue value = nspCodec.deserializeValue(buffer, true);

        assertEquals(NSP_VALUE, ((NspCaseValue) value).getNspValues().getNsp());
        assertEquals(NSP_MASK, ((NspCaseValue) value).getNspValues().getMask());
        assertFalse(buffer.isReadable());
    }

    private static NxExpMatchEntryValue createMatchEntryValue(final Uint32 value, final Uint32 mask) {
        NspValues nspValues = new NspValuesBuilder().setNsp(value).setMask(mask).build();
        return new NspCaseValueBuilder().setNspValues(nspValues).build();
    }

    private static void writeBuffer(final ByteBuf message, final Uint32 value, final Uint32 mask) {
        message.writeInt(value.intValue());
        if (mask != null) {
            message.writeInt(mask.intValue());
        }
    }
}
