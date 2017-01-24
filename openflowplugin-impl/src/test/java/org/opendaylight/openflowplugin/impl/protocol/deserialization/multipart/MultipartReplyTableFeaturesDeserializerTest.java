/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.multipart.reply.multipart.reply.body.MultipartReplyTableFeatures;

public class MultipartReplyTableFeaturesDeserializerTest extends AbstractMultipartDeserializerTest {

    private static final byte PADDING_IN_MULTIPART_REPLY_TABLE_FEATURES = 5;
    private static final byte MAX_TABLE_NAME_LENGTH = 32;

    private static final byte TABLE_ID = 1;
    private static final long METADATA_MATCH = 2;
    private static final long METADATA_WRITE = 3;
    private static final int  TABLE_CONFIG= 3;
    private static final int  MAX_ENTRIES= 3;

    @Test
    public void deserialize() throws Exception {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();

        writeValues(buffer, 0);
        MultipartReplyTableFeatures reply = (MultipartReplyTableFeatures) deserializeMultipart(buffer);

        assertEquals(TABLE_ID, reply.getTableFeatures().get(0).getTableId().byteValue());
        assertEquals(METADATA_MATCH, reply.getTableFeatures().get(0).getMetadataMatch().longValue());
        assertEquals(METADATA_WRITE, reply.getTableFeatures().get(0).getMetadataWrite().longValue());
        assertEquals(MAX_ENTRIES, reply.getTableFeatures().get(0).getMaxEntries().longValue());
        assertEquals(MAX_ENTRIES, reply.getTableFeatures().get(0).getMaxEntries().longValue());
//        assertEquals(InstructionsBuilder.class, reply.getTableFeatures().get(0).getTableProperties()
//                .getTableFeatureProperties().get(0).getTableFeaturePropType().getClass());

        writeValues(buffer, 1);
        reply = (MultipartReplyTableFeatures) deserializeMultipart(buffer);

        writeValues(buffer, 2);
        reply = (MultipartReplyTableFeatures) deserializeMultipart(buffer);

        writeValues(buffer, 3);
        reply = (MultipartReplyTableFeatures) deserializeMultipart(buffer);

        writeValues(buffer, 4);
        reply = (MultipartReplyTableFeatures) deserializeMultipart(buffer);

        writeValues(buffer, 5);
        reply = (MultipartReplyTableFeatures) deserializeMultipart(buffer);

        writeValues(buffer, 6);
        reply = (MultipartReplyTableFeatures) deserializeMultipart(buffer);

        writeValues(buffer, 7);
        reply = (MultipartReplyTableFeatures) deserializeMultipart(buffer);
    }

    private void writeValues(ByteBuf buffer, int propertyType) {
        buffer.clear();
        int replyIndex = buffer.readerIndex();
        buffer.writeShort(EncodeConstants.EMPTY_LENGTH);
        buffer.writeByte(TABLE_ID);
        buffer.writeZero(PADDING_IN_MULTIPART_REPLY_TABLE_FEATURES);
        buffer.writeZero(MAX_TABLE_NAME_LENGTH);
        buffer.writeLong(METADATA_MATCH);
        buffer.writeLong(METADATA_WRITE);
        buffer.writeInt(TABLE_CONFIG);
        buffer.writeInt(MAX_ENTRIES);

        int propIndex = buffer.writerIndex();
        buffer.writeShort(propertyType);

        int propLengthIndex = buffer.writerIndex();
        buffer.writeShort(EncodeConstants.EMPTY_LENGTH);

        // Instruction POP PBB header
        int instructionStartIndex = buffer.writerIndex();
        buffer.writeShort(InstructionConstants.APPLY_ACTIONS_TYPE);
        int instructionLengthIndex = buffer.writerIndex();
        buffer.writeShort(EncodeConstants.EMPTY_LENGTH);

        // Count total length of instruction
        buffer.setShort(instructionLengthIndex, buffer.writerIndex() - instructionStartIndex);

        int propLength = buffer.writerIndex() - propIndex;
        buffer.setShort(propLengthIndex, buffer.writerIndex() - propIndex);

        int paddingRemainder = propLength % EncodeConstants.PADDING;
        int result = 0;
        if (paddingRemainder != 0) {
            result = EncodeConstants.PADDING - paddingRemainder;
        }
        buffer.writeZero(result);

        buffer.setShort(replyIndex, buffer.writerIndex() - replyIndex);
    }

    @Override
    protected int getType() {
        return MultipartType.OFPMPTABLEFEATURES.getIntValue();
    }
}