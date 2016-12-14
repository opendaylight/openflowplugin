/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TcpFlagsMatch;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

public class TcpFlagsEntryDeserializerTest extends AbstractMatchEntryDeserializerTest {

    @Test
    public void deserializeEntry() throws Exception {
        final ByteBuf in = UnpooledByteBufAllocator.DEFAULT.buffer();
        final int tcpFlags = 6;
        final int tcpFlagsMask = 5;

        writeHeader(in, false);
        in.writeInt((int) EncodeConstants.ONF_EXPERIMENTER_ID);
        in.writeShort(tcpFlags);

        TcpFlagsMatch match = deserialize(in).getTcpFlagsMatch();
        assertEquals(tcpFlags, match.getTcpFlags().intValue());
        assertEquals(0, in.readableBytes());

        writeHeader(in, true);
        in.writeInt((int) EncodeConstants.ONF_EXPERIMENTER_ID);
        in.writeShort(tcpFlags);
        in.writeShort(tcpFlagsMask);

        match = deserialize(in).getTcpFlagsMatch();
        assertEquals(tcpFlags, match.getTcpFlags().intValue());
        assertEquals(tcpFlagsMask, match.getTcpFlagsMask().intValue());
        assertEquals(0, in.readableBytes());
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.EXPERIMENTER_CLASS;
    }

    @Override
    protected int getOxmFieldCode() {
        return EncodeConstants.ONFOXM_ET_TCP_FLAGS;
    }

    @Override
    protected int getValueLength() {
        return EncodeConstants.SIZE_OF_SHORT_IN_BYTES;
    }

}
