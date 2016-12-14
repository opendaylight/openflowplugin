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
import java.math.BigInteger;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Metadata;

public class MetadataEntryDeserializerTest extends AbstractMatchEntryDeserializerTest {

    @Test
    public void deserializeEntry() throws Exception {
        final ByteBuf in = UnpooledByteBufAllocator.DEFAULT.buffer();
        final BigInteger metadata = BigInteger.valueOf(20);
        final BigInteger metadataMask = BigInteger.valueOf(30);

        writeHeader(in, false);
        in.writeBytes(ByteUtil.convertBigIntegerToNBytes(metadata, getValueLength()));

        assertEquals(metadata, deserialize(in).getMetadata().getMetadata());
        assertEquals(0, in.readableBytes());

        writeHeader(in, true);
        in.writeBytes(ByteUtil.convertBigIntegerToNBytes(metadata, getValueLength()));
        in.writeBytes(ByteUtil.convertBigIntegerToNBytes(metadataMask, getValueLength()));

        final Metadata desMetadata = deserialize(in).getMetadata();
        assertEquals(metadata, desMetadata.getMetadata());
        assertEquals(metadataMask, desMetadata.getMetadataMask());
        assertEquals(0, in.readableBytes());
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.METADATA;
    }

    @Override
    protected int getValueLength() {
        return EncodeConstants.SIZE_OF_LONG_IN_BYTES;
    }

}
