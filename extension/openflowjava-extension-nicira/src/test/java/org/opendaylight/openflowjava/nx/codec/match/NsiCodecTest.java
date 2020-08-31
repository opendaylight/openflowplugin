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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsi.grouping.NsiValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsi.grouping.NsiValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.NxExpMatchEntryValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NsiCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NsiCaseValueBuilder;
import org.opendaylight.yangtools.yang.common.Uint8;

public class NsiCodecTest {

    NsiCodec nsiCodec;
    ByteBuf buffer;

    private static final Uint8 NSI_VALUE = Uint8.MAX_VALUE;
    private static final Uint8 NSI_MASK = Uint8.valueOf(0xF0);

    @Before
    public void setUp() {
        nsiCodec = new NsiCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void serializeTestNoMask() {
        NxExpMatchEntryValue matchEntryValue = createMatchEntryValue(NSI_VALUE, null);

        nsiCodec.serializeValue(matchEntryValue, false, buffer);

        assertEquals(NSI_VALUE.shortValue(), buffer.readUnsignedByte());
        assertFalse(buffer.isReadable());
    }

    @Test
    public void serializeTestMask() {
        NxExpMatchEntryValue matchEntryValue = createMatchEntryValue(NSI_VALUE, NSI_MASK);

        nsiCodec.serializeValue(matchEntryValue, true, buffer);

        assertEquals(NSI_VALUE.shortValue(), buffer.readUnsignedByte());
        assertEquals(NSI_MASK.shortValue(), buffer.readUnsignedByte());
        assertFalse(buffer.isReadable());
    }

    @Test
    public void deserializeTestNoMask() {
        writeBuffer(buffer, NSI_VALUE, null);

        NxExpMatchEntryValue value = nsiCodec.deserializeValue(buffer, false);

        assertEquals(NSI_VALUE, ((NsiCaseValue) value).getNsiValues().getNsi());
        assertFalse(buffer.isReadable());
    }

    @Test
    public void deserializeTestMask() {
        writeBuffer(buffer, NSI_VALUE, NSI_MASK);

        NxExpMatchEntryValue value = nsiCodec.deserializeValue(buffer, true);

        assertEquals(NSI_VALUE, ((NsiCaseValue) value).getNsiValues().getNsi());
        assertEquals(NSI_MASK, ((NsiCaseValue) value).getNsiValues().getMask());
        assertFalse(buffer.isReadable());
    }

    private static NxExpMatchEntryValue createMatchEntryValue(Uint8 value, Uint8 mask) {
        NsiValues nsiValues = new NsiValuesBuilder().setNsi(value).setMask(mask).build();
        return new NsiCaseValueBuilder().setNsiValues(nsiValues).build();
    }

    private static void writeBuffer(ByteBuf message, Uint8 value, Uint8 mask) {
        message.writeByte(value.intValue());
        if (mask != null) {
            message.writeByte(mask.intValue());
        }
    }
}
