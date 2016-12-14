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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFields;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

public class PbbEntryDeserializerTest extends AbstractMatchEntryDeserializerTest {

    @Test
    public void deserializeEntry() throws Exception {
        final ByteBuf in = UnpooledByteBufAllocator.DEFAULT.buffer();
        final long pbb = 6;
        final long pbbMask = 5;

        writeHeader(in, false);
        in.writeMedium((int) pbb);

        ProtocolMatchFields match = deserialize(in).getProtocolMatchFields();
        assertEquals(pbb, match.getPbb().getPbbIsid().longValue());
        assertEquals(0, in.readableBytes());

        writeHeader(in, true);
        in.writeMedium((int) pbb);
        in.writeMedium((int) pbbMask);

        match = deserialize(in).getProtocolMatchFields();
        assertEquals(pbb, match.getPbb().getPbbIsid().longValue());
        assertEquals(pbbMask, match.getPbb().getPbbMask().longValue());
        assertEquals(0, in.readableBytes());
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.PBB_ISID;
    }

    @Override
    protected int getValueLength() {
        return EncodeConstants.SIZE_OF_3_BYTES;
    }

}
