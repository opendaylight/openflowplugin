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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsh.ttl.grouping.NshTtlValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsh.ttl.grouping.NshTtlValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.NxExpMatchEntryValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NshTtlCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NshTtlCaseValueBuilder;
import org.opendaylight.yangtools.yang.common.Uint8;

public class NshTtlCodecTest {

    private NshTtlCodec nshTtlCodec;
    private ByteBuf buffer;

    private static final Uint8 TTL_VALUE = Uint8.valueOf(0xD8);
    private static final Uint8 TTL_MASK = Uint8.MAX_VALUE;

    @Before
    public void setUp() {
        nshTtlCodec = new NshTtlCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void serializeTestNoMask() {
        NxExpMatchEntryValue matchEntryValue = createMatchEntryValue(TTL_VALUE, null);

        nshTtlCodec.serializeValue(matchEntryValue, false, buffer);

        assertEquals(TTL_VALUE.shortValue(), buffer.readUnsignedByte());
        assertFalse(buffer.isReadable());
    }

    @Test
    public void serializeTestMask() {
        NxExpMatchEntryValue matchEntryValue = createMatchEntryValue(TTL_VALUE, TTL_MASK);

        nshTtlCodec.serializeValue(matchEntryValue, true, buffer);

        assertEquals(TTL_VALUE.shortValue(), buffer.readUnsignedByte());
        assertEquals(TTL_MASK.shortValue(), buffer.readUnsignedByte());
        assertFalse(buffer.isReadable());
    }

    @Test
    public void deserializeTestNoMask() {
        writeBuffer(buffer, TTL_VALUE, null);

        NxExpMatchEntryValue value = nshTtlCodec.deserializeValue(buffer, false);

        assertEquals(TTL_VALUE, ((NshTtlCaseValue) value).getNshTtlValues().getNshTtl());
        assertFalse(buffer.isReadable());
    }

    @Test
    public void deserializeTestMask() {
        writeBuffer(buffer, TTL_VALUE, TTL_MASK);

        NxExpMatchEntryValue value = nshTtlCodec.deserializeValue(buffer, true);

        assertEquals(TTL_VALUE, ((NshTtlCaseValue) value).getNshTtlValues().getNshTtl());
        assertEquals(TTL_MASK, ((NshTtlCaseValue) value).getNshTtlValues().getMask());
        assertFalse(buffer.isReadable());
    }

    private static NxExpMatchEntryValue createMatchEntryValue(final Uint8 value, final Uint8 mask) {
        NshTtlValues nshTtlValues = new NshTtlValuesBuilder().setNshTtl(value).setMask(mask).build();
        return new NshTtlCaseValueBuilder().setNshTtlValues(nshTtlValues).build();
    }

    private static void writeBuffer(final ByteBuf message, final Uint8 value, final Uint8 mask) {
        message.writeByte(value.intValue());
        if (mask != null) {
            message.writeByte(mask.intValue());
        }
    }
}