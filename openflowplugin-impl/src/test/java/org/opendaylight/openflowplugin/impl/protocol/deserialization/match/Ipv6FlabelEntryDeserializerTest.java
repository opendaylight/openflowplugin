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
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;

public class Ipv6FlabelEntryDeserializerTest extends AbstractMatchEntryDeserializerTest {

    @Test
    public void deserializeEntry() throws Exception {
        final ByteBuf in = UnpooledByteBufAllocator.DEFAULT.buffer();
        final int flowLabel = 10;
        final int flowLabelMask = 8;

        writeHeader(in, false);
        in.writeInt(flowLabel);

        Ipv6Match match = Ipv6Match.class.cast(deserialize(in).getLayer3Match());
        assertEquals(flowLabel, match.getIpv6Label().getIpv6Flabel().getValue().intValue());
        assertEquals(0, in.readableBytes());

        writeHeader(in, true);
        in.writeInt(flowLabel);
        in.writeInt(flowLabelMask);

        match = Ipv6Match.class.cast(deserialize(in).getLayer3Match());
        assertEquals(flowLabel, match.getIpv6Label().getIpv6Flabel().getValue().intValue());
        assertEquals(flowLabelMask, match.getIpv6Label().getFlabelMask().getValue().intValue());
        assertEquals(0, in.readableBytes());
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.IPV6_FLABEL;
    }

    @Override
    protected int getValueLength() {
        return EncodeConstants.SIZE_OF_INT_IN_BYTES;
    }

}
