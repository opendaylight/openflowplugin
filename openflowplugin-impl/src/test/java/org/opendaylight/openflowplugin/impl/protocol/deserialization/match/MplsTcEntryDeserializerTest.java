/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFields;

public class MplsTcEntryDeserializerTest extends AbstractMatchEntryDeserializerTest {
    @Test
    public void deserializeEntry() {
        final ByteBuf in = UnpooledByteBufAllocator.DEFAULT.buffer();
        final short mplsTc = 6;

        writeHeader(in, false);
        in.writeByte(mplsTc);

        ProtocolMatchFields match = deserialize(in).getProtocolMatchFields();
        assertEquals(mplsTc, match.getMplsTc().shortValue());
        assertEquals(0, in.readableBytes());
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.MPLS_TC;
    }

    @Override
    protected int getValueLength() {
        return Byte.BYTES;
    }
}
