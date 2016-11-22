/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.actions;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.function.Consumer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;

public abstract class AbstractSetFieldActionSerializerTest extends AbstractActionSerializerTest {

    protected void assertAction(Action action, final Consumer<ByteBuf> assertBody) {
        // Header serialization
        final ByteBuf bufferHeader = UnpooledByteBufAllocator.DEFAULT.buffer();
        getSerializer().serializeHeader(action, bufferHeader);
        assertEquals(bufferHeader.readUnsignedShort(), getType());
        assertEquals(bufferHeader.readUnsignedShort(), ActionConstants.ACTION_IDS_LENGTH);
        assertEquals(bufferHeader.readableBytes(), 0);

        // Header and body serialization
        final ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        getSerializer().serialize(action, buffer);
        assertEquals(buffer.readUnsignedShort(), getType());
        assertEquals(buffer.readUnsignedShort(),
                EncodeConstants.SIZE_OF_SHORT_IN_BYTES // Size of action type
                        + EncodeConstants.SIZE_OF_SHORT_IN_BYTES // Size of action length
                        + EncodeConstants.SIZE_OF_SHORT_IN_BYTES // Match entry OXM class
                        + EncodeConstants.SIZE_OF_BYTE_IN_BYTES // Match entry field and mask
                        + EncodeConstants.SIZE_OF_BYTE_IN_BYTES // Match entry length
                        + EncodeConstants.PADDING); // Size of set field (match entry)

        // Skip match entry header, we have tests for this elsewhere
        buffer.skipBytes(EncodeConstants.SIZE_OF_INT_IN_BYTES);

        assertBody.accept(buffer);

        int paddingRemainder = buffer.readerIndex() % EncodeConstants.PADDING;

        if (paddingRemainder != 0) {
            buffer.skipBytes(EncodeConstants.PADDING - paddingRemainder);
        }

        assertEquals(buffer.readableBytes(), 0);
    }

    @Override
    protected int getType() {
        return ActionConstants.SET_FIELD_CODE;
    }

    @Override
    protected int getLength() {
        return ActionConstants.GENERAL_ACTION_LENGTH;
    }

}
