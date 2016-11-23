/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.function.Consumer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.AbstractSerializerTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;

public abstract class AbstractMatchEntrySerializerTest extends AbstractSerializerTest {
    private MatchSerializer serializer;

    @Override
    protected void init() {
        serializer = getRegistry().getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, Match.class));
    }

    protected void assertMatch(final Match match, final boolean hasMask, final Consumer<ByteBuf> assertBody) throws Exception {
        final ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(match, buffer);

        final int length = (hasMask ? 2 : 1) * getLength();

        assertEquals(buffer.readShort(), 1); // OXM_MATCH_TYPE
        assertEquals(buffer.readShort(), // Total length of match
                EncodeConstants.SIZE_OF_SHORT_IN_BYTES // OXM_MATCH_TYPE length
                        + EncodeConstants.SIZE_OF_SHORT_IN_BYTES // LENGTH length
                        + EncodeConstants.SIZE_OF_SHORT_IN_BYTES // OXM_CLASS_CODE length
                        + EncodeConstants.SIZE_OF_BYTE_IN_BYTES // OXM field and mask length
                        + EncodeConstants.SIZE_OF_BYTE_IN_BYTES // OXM field and mask length length
                        + length // length of data in match entry
        );

        assertEquals(buffer.readUnsignedShort(), getOxmClassCode());
        final short fieldAndMask = buffer.readUnsignedByte();
        assertEquals(getOxmFieldCode(), fieldAndMask >>> 1);
        assertEquals(hasMask, (fieldAndMask & 1) != 0);
        assertEquals(buffer.readUnsignedByte(), length);
        assertBody.accept(buffer);

        int paddingRemainder = length % EncodeConstants.PADDING;

        if (paddingRemainder != 0) {
            buffer.skipBytes(EncodeConstants.PADDING - paddingRemainder);
        }

        assertEquals(buffer.readableBytes(), 0);
    }

    protected MatchSerializer getSerializer() {
        return serializer;
    }

    protected abstract int getOxmFieldCode();
    protected abstract int getOxmClassCode();
    protected abstract short getLength();

}