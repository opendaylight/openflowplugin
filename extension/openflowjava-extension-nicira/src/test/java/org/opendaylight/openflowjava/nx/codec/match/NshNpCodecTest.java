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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsh.np.grouping.NshNpValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsh.np.grouping.NshNpValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.NxExpMatchEntryValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NshNpCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NshNpCaseValueBuilder;
import org.opendaylight.yangtools.yang.common.Uint8;

public class NshNpCodecTest {

    private NshNpCodec nshNpCodec;
    private ByteBuf buffer;

    private static final Uint8 NP_VALUE = Uint8.valueOf(3);

    @Before
    public void setUp() {
        nshNpCodec = new NshNpCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void serializeValueTest() {
        NxExpMatchEntryValue matchEntryValue = createMatchEntryValue(NP_VALUE);

        nshNpCodec.serializeValue(matchEntryValue, false, buffer);

        assertEquals(NP_VALUE.shortValue(), buffer.readUnsignedByte());
        assertFalse(buffer.isReadable());
    }

    @Test
    public void deserializeValueTest() {
        writeBuffer(buffer, NP_VALUE);

        NxExpMatchEntryValue value = nshNpCodec.deserializeValue(buffer, false);

        assertEquals(NP_VALUE, ((NshNpCaseValue) value).getNshNpValues().getValue());
        assertFalse(buffer.isReadable());
    }

    private static NxExpMatchEntryValue createMatchEntryValue(Uint8 value) {
        NshNpValues nshNpValues = new NshNpValuesBuilder().setValue(value).build();
        return new NshNpCaseValueBuilder().setNshNpValues(nshNpValues).build();
    }

    private static void writeBuffer(ByteBuf message, Uint8 value) {
        message.writeByte(value.intValue());
    }
}