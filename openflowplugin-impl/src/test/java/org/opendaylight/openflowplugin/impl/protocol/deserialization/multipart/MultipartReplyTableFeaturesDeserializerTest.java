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
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.multipart.reply.multipart.reply.body.MultipartReplyTableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfield;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfieldMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Wildcards;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActionsMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteSetfield;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteSetfieldMiss;

public class MultipartReplyTableFeaturesDeserializerTest extends AbstractMultipartDeserializerTest {

    private static final byte PADDING_IN_MULTIPART_REPLY_TABLE_FEATURES = 5;
    private static final byte MAX_TABLE_NAME_LENGTH = 32;

    private static final byte TABLE_ID = 1;
    private static final long METADATA_MATCH = 2;
    private static final long METADATA_WRITE = 3;
    private static final int  TABLE_CONFIG= 3;
    private static final int  MAX_ENTRIES= 3;

    private static final int OFPTFPT_INSTRUCTIONS = 0;
    private static final int OFPTFPT_INSTRUCTIONS_MISS = 1;
    private static final int OFPTFPT_NEXT_TABLES = 2;
    private static final int OFPTFPT_NEXT_TABLES_MISS = 3;
    private static final int OFPTFPT_WRITE_ACTIONS = 4;
    private static final int OFPTFPT_WRITE_ACTIONS_MISS = 5;
    private static final int OFPTFPT_APPLY_ACTIONS = 6;
    private static final int OFPTFPT_APPLY_ACTIONS_MISS = 7;
    private static final int OFPTFPT_MATCH = 8;
    private static final int OFPTFPT_WILDCARDS = 10;
    private static final int OFPTFPT_WRITE_SETFIELD = 12;
    private static final int OFPTFPT_WRITE_SETFIELD_MISS = 13;
    private static final int OFPTFPT_APPLY_SETFIELD = 14;
    private static final int OFPTFPT_APPLY_SETFIELD_MISS = 15;

    @Test
    public void deserialize() throws Exception {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();

        writeValues(buffer, OFPTFPT_INSTRUCTIONS);
        MultipartReplyTableFeatures reply = (MultipartReplyTableFeatures) deserializeMultipart(buffer);

        assertEquals(TABLE_ID, reply.getTableFeatures().get(0).getTableId().byteValue());
        assertEquals(METADATA_MATCH, reply.getTableFeatures().get(0).getMetadataMatch().longValue());
        assertEquals(METADATA_WRITE, reply.getTableFeatures().get(0).getMetadataWrite().longValue());
        assertEquals(MAX_ENTRIES, reply.getTableFeatures().get(0).getMaxEntries().longValue());
        assertEquals(MAX_ENTRIES, reply.getTableFeatures().get(0).getMaxEntries().longValue());
        assertEquals(Instructions.class, reply.getTableFeatures().get(0).getTableProperties()
                .getTableFeatureProperties().get(0).getTableFeaturePropType().getImplementedInterface());

        writeValues(buffer, OFPTFPT_INSTRUCTIONS_MISS);
        reply = (MultipartReplyTableFeatures) deserializeMultipart(buffer);

        assertEquals(InstructionsMiss.class, reply.getTableFeatures().get(0).getTableProperties()
                .getTableFeatureProperties().get(0).getTableFeaturePropType().getImplementedInterface());

        writeValues(buffer, OFPTFPT_NEXT_TABLES);
        reply = (MultipartReplyTableFeatures) deserializeMultipart(buffer);

        assertEquals(NextTable.class, reply.getTableFeatures().get(0).getTableProperties()
                .getTableFeatureProperties().get(0).getTableFeaturePropType().getImplementedInterface());

        writeValues(buffer, OFPTFPT_NEXT_TABLES_MISS);
        reply = (MultipartReplyTableFeatures) deserializeMultipart(buffer);

        assertEquals(NextTableMiss.class, reply.getTableFeatures().get(0).getTableProperties()
                .getTableFeatureProperties().get(0).getTableFeaturePropType().getImplementedInterface());

        writeValues(buffer, OFPTFPT_WRITE_ACTIONS);
        reply = (MultipartReplyTableFeatures) deserializeMultipart(buffer);

        assertEquals(WriteActions.class, reply.getTableFeatures().get(0).getTableProperties()
                .getTableFeatureProperties().get(0).getTableFeaturePropType().getImplementedInterface());

        writeValues(buffer, OFPTFPT_WRITE_ACTIONS_MISS);
        reply = (MultipartReplyTableFeatures) deserializeMultipart(buffer);

        assertEquals(WriteActionsMiss.class, reply.getTableFeatures().get(0).getTableProperties()
                .getTableFeatureProperties().get(0).getTableFeaturePropType().getImplementedInterface());

        writeValues(buffer, OFPTFPT_APPLY_ACTIONS);
        reply = (MultipartReplyTableFeatures) deserializeMultipart(buffer);

        assertEquals(ApplyActions.class, reply.getTableFeatures().get(0).getTableProperties()
                .getTableFeatureProperties().get(0).getTableFeaturePropType().getImplementedInterface());

        writeValues(buffer, OFPTFPT_APPLY_ACTIONS_MISS);
        reply = (MultipartReplyTableFeatures) deserializeMultipart(buffer);

        assertEquals(ApplyActionsMiss.class, reply.getTableFeatures().get(0).getTableProperties()
                .getTableFeatureProperties().get(0).getTableFeaturePropType().getImplementedInterface());

        writeValues(buffer, OFPTFPT_MATCH);
        reply = (MultipartReplyTableFeatures) deserializeMultipart(buffer);

        assertEquals(Match.class, reply.getTableFeatures().get(0).getTableProperties()
                .getTableFeatureProperties().get(0).getTableFeaturePropType().getImplementedInterface());

        writeValues(buffer, OFPTFPT_WILDCARDS);
        reply = (MultipartReplyTableFeatures) deserializeMultipart(buffer);

        assertEquals(Wildcards.class, reply.getTableFeatures().get(0).getTableProperties()
                .getTableFeatureProperties().get(0).getTableFeaturePropType().getImplementedInterface());

        writeValues(buffer, OFPTFPT_WRITE_SETFIELD);
        reply = (MultipartReplyTableFeatures) deserializeMultipart(buffer);

        assertEquals(WriteSetfield.class, reply.getTableFeatures().get(0).getTableProperties()
                .getTableFeatureProperties().get(0).getTableFeaturePropType().getImplementedInterface());

        writeValues(buffer, OFPTFPT_WRITE_SETFIELD_MISS);
        reply = (MultipartReplyTableFeatures) deserializeMultipart(buffer);

        assertEquals(WriteSetfieldMiss.class, reply.getTableFeatures().get(0).getTableProperties()
                .getTableFeatureProperties().get(0).getTableFeaturePropType().getImplementedInterface());

        writeValues(buffer, OFPTFPT_APPLY_SETFIELD);
        reply = (MultipartReplyTableFeatures) deserializeMultipart(buffer);

        assertEquals(ApplySetfield.class, reply.getTableFeatures().get(0).getTableProperties()
                .getTableFeatureProperties().get(0).getTableFeaturePropType().getImplementedInterface());

        writeValues(buffer, OFPTFPT_APPLY_SETFIELD_MISS);
        reply = (MultipartReplyTableFeatures) deserializeMultipart(buffer);

        assertEquals(ApplySetfieldMiss.class, reply.getTableFeatures().get(0).getTableProperties()
                .getTableFeatureProperties().get(0).getTableFeaturePropType().getImplementedInterface());
        assertEquals(0, buffer.readableBytes());
    }

    private void writeValues(ByteBuf buffer, int propertyType) {
        TableFeaturesPropType propType = TableFeaturesPropType.forValue(propertyType);

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

        switch (propType) {
            case OFPTFPTINSTRUCTIONS:
            case OFPTFPTINSTRUCTIONSMISS:
                int instructionStartIndex = buffer.writerIndex();
                buffer.writeShort(InstructionConstants.APPLY_ACTIONS_TYPE);
                int instructionLengthIndex = buffer.writerIndex();
                buffer.writeShort(EncodeConstants.EMPTY_LENGTH);
                buffer.setShort(instructionLengthIndex, buffer.writerIndex() - instructionStartIndex);
                break;
            case OFPTFPTNEXTTABLES:
            case OFPTFPTNEXTTABLESMISS:
                buffer.writeByte(1);
                buffer.writeByte(2);
                break;
            case OFPTFPTWRITEACTIONS:
            case OFPTFPTWRITEACTIONSMISS:
                buffer.writeShort(InstructionConstants.WRITE_ACTIONS_TYPE);
                buffer.writeShort(EncodeConstants.EMPTY_LENGTH);
                break;
            case OFPTFPTAPPLYACTIONS:
            case OFPTFPTAPPLYACTIONSMISS:
                buffer.writeShort(InstructionConstants.APPLY_ACTIONS_TYPE);
                buffer.writeShort(EncodeConstants.EMPTY_LENGTH);
                break;
            case OFPTFPTMATCH:
                buffer.writeShort(OxmMatchConstants.OPENFLOW_BASIC_CLASS);
                buffer.writeByte(OxmMatchConstants.ARP_OP << 1);
                buffer.writeByte(EncodeConstants.EMPTY_LENGTH);
                break;
            case OFPTFPTWILDCARDS:
                buffer.writeShort(OxmMatchConstants.OPENFLOW_BASIC_CLASS);
                buffer.writeByte(OxmMatchConstants.ARP_SHA << 1);
                buffer.writeByte(EncodeConstants.EMPTY_LENGTH);
                break;
            case OFPTFPTWRITESETFIELD:
                buffer.writeShort(OxmMatchConstants.OPENFLOW_BASIC_CLASS);
                buffer.writeByte(OxmMatchConstants.ARP_SPA << 1);
                buffer.writeByte(EncodeConstants.EMPTY_LENGTH);
                break;
            case OFPTFPTWRITESETFIELDMISS:
                buffer.writeShort(OxmMatchConstants.OPENFLOW_BASIC_CLASS);
                buffer.writeByte(OxmMatchConstants.ARP_THA << 1);
                buffer.writeByte(EncodeConstants.EMPTY_LENGTH);
                break;
            case OFPTFPTAPPLYSETFIELD:
                buffer.writeShort(OxmMatchConstants.OPENFLOW_BASIC_CLASS);
                buffer.writeByte(OxmMatchConstants.ARP_TPA << 1);
                buffer.writeByte(EncodeConstants.EMPTY_LENGTH);
                break;
            case OFPTFPTAPPLYSETFIELDMISS:
                buffer.writeShort(OxmMatchConstants.OPENFLOW_BASIC_CLASS);
                buffer.writeByte(OxmMatchConstants.ETH_TYPE << 1);
                buffer.writeByte(EncodeConstants.EMPTY_LENGTH);
                break;
            default:
                break;
        }

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
