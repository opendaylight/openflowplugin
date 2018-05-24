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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsh.mdtype.grouping.NshMdtypeValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsh.mdtype.grouping.NshMdtypeValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.NxExpMatchEntryValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NshMdtypeCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NshMdtypeCaseValueBuilder;

public class NshMdtypeCodecTest {

    private NshMdtypeCodec nshMdtypeCodec;
    private ByteBuf buffer;

    private static final Short MDTYPE_VALUE = (short) 1;

    @Before
    public void setUp() {
        nshMdtypeCodec = new NshMdtypeCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void serializeValueTest() {
        NxExpMatchEntryValue matchEntryValue = createMatchEntryValue(MDTYPE_VALUE);

        nshMdtypeCodec.serializeValue(matchEntryValue, false, buffer);

        assertEquals(MDTYPE_VALUE.shortValue(), buffer.readUnsignedByte());
        assertFalse(buffer.isReadable());
    }

    @Test
    public void deserializeValueTest() {
        writeBuffer(buffer, MDTYPE_VALUE);

        NxExpMatchEntryValue value = nshMdtypeCodec.deserializeValue(buffer, false);

        assertEquals(MDTYPE_VALUE, ((NshMdtypeCaseValue) value).getNshMdtypeValues().getValue());
        assertFalse(buffer.isReadable());
    }

    private NxExpMatchEntryValue createMatchEntryValue(Short value) {
        NshMdtypeValues nshMdtypeValues = new NshMdtypeValuesBuilder().setValue(value).build();
        return new NshMdtypeCaseValueBuilder().setNshMdtypeValues(nshMdtypeValues).build();
    }

    private void writeBuffer(ByteBuf message, Short value) {
        message.writeByte(value.intValue());
    }
}