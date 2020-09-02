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
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;

public abstract class AbstractExperimenterMatchEntrySerializerTest extends AbstractMatchEntrySerializerTest {

    protected void assertMatch(final Match match,
                               final boolean hasMask,
                               final Consumer<ByteBuf> assertBody) {
        final ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        getSerializer().serialize(match, buffer);

        final int length = (hasMask ? 2 : 1) * getLength();
        final int lengthExp = length + Integer.BYTES; // add length of Experimenter ID

        assertEquals(buffer.readShort(), 1); // OXM_MATCH_TYPE
        assertEquals(buffer.readShort(), // Total length of match
                Short.BYTES // OXM_MATCH_TYPE length
                        + Short.BYTES // LENGTH length
                        + Short.BYTES // OXM_CLASS_CODE length
                        + Byte.BYTES // OXM field and mask length
                        + Byte.BYTES // OXM field and mask length length
                        + lengthExp // length of data in match entry
        );

        assertEquals(buffer.readUnsignedShort(), getOxmClassCode());
        final short fieldAndMask = buffer.readUnsignedByte();
        assertEquals(getOxmFieldCode(), fieldAndMask >>> 1);
        assertEquals(hasMask, (fieldAndMask & 1) != 0);
        assertEquals(buffer.readUnsignedByte(), lengthExp);
        assertEquals(buffer.readUnsignedInt(), getExperimenterId());
        assertBody.accept(buffer);

        int paddingRemainder = lengthExp % EncodeConstants.PADDING;

        if (paddingRemainder != 0) {
            buffer.skipBytes(EncodeConstants.PADDING - paddingRemainder);
        }

        assertEquals(buffer.readableBytes(), 0);
    }

    protected abstract long getExperimenterId();

}
