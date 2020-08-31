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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsh.flags.grouping.NshFlagsValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsh.flags.grouping.NshFlagsValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.NxExpMatchEntryValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NshFlagsCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NshFlagsCaseValueBuilder;
import org.opendaylight.yangtools.yang.common.Uint8;

public class NshFlagsCodecTest {

    private NshFlagsCodec nshFlagsCodec;
    private ByteBuf buffer;

    private static final Uint8 FLAGS_VALUE = Uint8.valueOf(0xA1);
    private static final Uint8 FLAGS_MASK = Uint8.MAX_VALUE;

    @Before
    public void setUp() {
        nshFlagsCodec = new NshFlagsCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void serializeTestNoMask() {
        NxExpMatchEntryValue matchEntryValue = createMatchEntryValue(FLAGS_VALUE, null);

        nshFlagsCodec.serializeValue(matchEntryValue, false, buffer);

        assertEquals(FLAGS_VALUE.shortValue(), buffer.readUnsignedByte());
        assertFalse(buffer.isReadable());
    }

    @Test
    public void serializeTestMask() {
        NxExpMatchEntryValue matchEntryValue = createMatchEntryValue(FLAGS_VALUE, FLAGS_MASK);

        nshFlagsCodec.serializeValue(matchEntryValue, true, buffer);

        assertEquals(FLAGS_VALUE.shortValue(), buffer.readUnsignedByte());
        assertEquals(FLAGS_MASK.shortValue(), buffer.readUnsignedByte());
        assertFalse(buffer.isReadable());
    }

    @Test
    public void deserializeTestNoMask() {
        writeBuffer(buffer, FLAGS_VALUE, null);

        NxExpMatchEntryValue value = nshFlagsCodec.deserializeValue(buffer, false);

        assertEquals(FLAGS_VALUE, ((NshFlagsCaseValue) value).getNshFlagsValues().getNshFlags());
        assertFalse(buffer.isReadable());
    }

    @Test
    public void deserializeTestMask() {
        writeBuffer(buffer, FLAGS_VALUE, FLAGS_MASK);

        NxExpMatchEntryValue value = nshFlagsCodec.deserializeValue(buffer, true);

        assertEquals(FLAGS_VALUE, ((NshFlagsCaseValue) value).getNshFlagsValues().getNshFlags());
        assertEquals(FLAGS_MASK, ((NshFlagsCaseValue) value).getNshFlagsValues().getMask());
        assertFalse(buffer.isReadable());
    }

    private static NxExpMatchEntryValue createMatchEntryValue(Uint8 value, Uint8 mask) {
        NshFlagsValues nshFlagsValues = new NshFlagsValuesBuilder().setNshFlags(value).setMask(mask).build();
        return new NshFlagsCaseValueBuilder().setNshFlagsValues(nshFlagsValues).build();
    }

    private static void writeBuffer(ByteBuf message, Uint8 value, Uint8 mask) {
        message.writeByte(value.intValue());
        if (mask != null) {
            message.writeByte(mask.intValue());
        }
    }
}