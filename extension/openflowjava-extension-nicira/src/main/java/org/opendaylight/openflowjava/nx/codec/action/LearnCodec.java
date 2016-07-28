/*
 * Copyright (c) 2016 Hewlett-Packard Enterprise and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.nx.codec.action;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.nx.api.NiciraActionDeserializerKey;
import org.opendaylight.openflowjava.nx.api.NiciraActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionLearn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionLearnBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.FlowModSpec;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.field._case.FlowModAddMatchFromField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.field._case.FlowModAddMatchFromFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.NxActionLearnBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.nx.action.learn.FlowMods;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.nx.action.learn.FlowModsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Slava Radune
 */

public class LearnCodec extends AbstractActionCodec {
    private static final Logger logger = LoggerFactory.getLogger(LearnCodec.class);

    public static final int HEADER_LENGTH = 32;
    public static final byte NXAST_LEARN_SUBTYPE = 16;
    public static final NiciraActionSerializerKey SERIALIZER_KEY =
            new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionLearn.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, NXAST_LEARN_SUBTYPE);

    private static final short SRC_MASK = 0x2000;

    private static final short DST_MASK = 0x1800;
    private static final short NUM_BITS_MASK = 0x07FF;

    private short length;

    @Override
    public void serialize(final Action input, final ByteBuf outBuffer) {
        ActionLearn action = (ActionLearn) input.getActionChoice();
        int length = calcLength(action);
        int lengthMod = length%8;
        
        serializeHeader(length+lengthMod, NXAST_LEARN_SUBTYPE, outBuffer);
        
        

        outBuffer.writeShort(action.getNxActionLearn().getIdleTimeout().shortValue());
        outBuffer.writeShort(action.getNxActionLearn().getHardTimeout().shortValue());
        outBuffer.writeShort(action.getNxActionLearn().getPriority().shortValue());
        outBuffer.writeLong(action.getNxActionLearn().getCookie().longValue());
        outBuffer.writeShort(action.getNxActionLearn().getFlags().shortValue());
        outBuffer.writeByte(action.getNxActionLearn().getTableId().byteValue());
        outBuffer.writeZero(1);
        outBuffer.writeShort(action.getNxActionLearn().getFinIdleTimeout().shortValue());
        outBuffer.writeShort(action.getNxActionLearn().getFinHardTimeout().shortValue());
        
        if(action.getNxActionLearn().getFlowMods() != null){
            for(FlowMods flowMod : action.getNxActionLearn().getFlowMods()){
                FlowModAddMatchFromField flowModSpecFromField = ((FlowModAddMatchFromFieldCase)flowMod.getFlowModSpec()).getFlowModAddMatchFromField();
                toFlowModSpecHeader(flowModSpecFromField, outBuffer);
                outBuffer.writeInt(flowModSpecFromField.getSrcField().intValue());
                outBuffer.writeShort(flowModSpecFromField.getSrcOfs().shortValue());
                outBuffer.writeInt(flowModSpecFromField.getDstField().intValue());
                outBuffer.writeShort(flowModSpecFromField.getDstOfs().shortValue());
            }
        }
        
        if(lengthMod != 0){
            outBuffer.writeZero(lengthMod);
        }
        
    }

    private int calcLength(ActionLearn action) {
        int length = HEADER_LENGTH;
        if(action.getNxActionLearn().getFlowMods() == null){
            return length;
        }
        for(FlowMods flowMod : action.getNxActionLearn().getFlowMods()){
            FlowModAddMatchFromField flowModSpecFromField = ((FlowModAddMatchFromFieldCase)flowMod.getFlowModSpec()).getFlowModAddMatchFromField();
            length += getLength(flowModSpecFromField);
        }
        
        return length;
    }

    private int getLength(FlowModAddMatchFromField flowModSpecFromField) {
        return 14;
    }

    private void toFlowModSpecHeader(FlowModAddMatchFromField flowModAddMatchFromField, ByteBuf outBuffer) {
        short b = 0;
        short src = 0;
        short dst = 0;
        short bitNum = (short)(int)flowModAddMatchFromField.getFlowModHeaderLen();
        b |= (src << 13);
        b |= (dst << 11);
        b |= bitNum;
        
        outBuffer.writeShort(b);
    }

    @Override
    public Action deserialize(final ByteBuf message) {
        ActionBuilder actionBuilder = deserializeHeader(message);
        ActionLearnBuilder actionLearnBuilder = new ActionLearnBuilder();

        NxActionLearnBuilder nxActionLearnBuilder = new NxActionLearnBuilder();
        nxActionLearnBuilder.setIdleTimeout(message.readUnsignedShort());
        nxActionLearnBuilder.setHardTimeout(message.readUnsignedShort());
        nxActionLearnBuilder.setPriority(message.readUnsignedShort());
        nxActionLearnBuilder.setCookie(BigInteger.valueOf(message.readLong()));
        nxActionLearnBuilder.setFlags(message.readUnsignedShort());
        nxActionLearnBuilder.setTableId(message.readUnsignedByte());
        message.skipBytes(1);
        nxActionLearnBuilder.setFinIdleTimeout(message.readUnsignedShort());
        nxActionLearnBuilder.setFinHardTimeout(message.readUnsignedShort());
        
        buildFlowModSpecs(nxActionLearnBuilder, message);
        
        actionLearnBuilder.setNxActionLearn(nxActionLearnBuilder.build());

        actionBuilder.setActionChoice(actionLearnBuilder.build());

        return actionBuilder.build();
    }
    
    private void buildFlowModSpecs(NxActionLearnBuilder nxActionLearnBuilder, ByteBuf message) {
        length -= HEADER_LENGTH;
        List<FlowMods> flowModeList = new ArrayList<FlowMods>();
        
        while(length > 0){
            FlowMods flowMod = readFlowMod(message);
            if(flowMod != null){
                flowModeList.add(flowMod);
            }
        }
        
        if(length != 0){
            logger.error("Learn Codec read " + Math.abs(length) + " bytes more than needed from stream. Packet might be corrupted");
        }
        
        nxActionLearnBuilder.setFlowMods(flowModeList);
        
    }

    private FlowMods readFlowMod(ByteBuf message) {
        short header = message.readShort();
        length -= EncodeConstants.SIZE_OF_SHORT_IN_BYTES;
        if(header == 0){
            return null;
        }
        
        short src = (short) ((header & SRC_MASK) >> 13);
        short dst = (short) ((header & DST_MASK) >> 11);
        short numBits = (short) (header & NUM_BITS_MASK);
        
        if(src == 0 && dst == 0){
            return readFlowModAddMatchFromField(message, numBits);
        }
        
        return null;
    }

    private FlowMods readFlowModAddMatchFromField(ByteBuf message, short numBit) {
        FlowModAddMatchFromFieldBuilder builder = new FlowModAddMatchFromFieldBuilder();
        builder.setSrcField((long) message.readInt());
        builder.setSrcOfs((int) message.readShort());
        builder.setDstField((long) message.readInt());
        builder.setDstOfs((int) message.readShort());
        builder.setFlowModHeaderLen((int) numBit);
        length -= 12;
        
        FlowModsBuilder flowModsBuilder = new FlowModsBuilder();
        FlowModAddMatchFromFieldCaseBuilder caseBuilder = new FlowModAddMatchFromFieldCaseBuilder();
        caseBuilder.setFlowModAddMatchFromField(builder.build());
        flowModsBuilder.setFlowModSpec(caseBuilder.build());
        return flowModsBuilder.build();
    }

    @Override
    protected ActionBuilder deserializeHeader(ByteBuf message) {
        // size of experimenter type
        message.skipBytes(EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        // size of length
        length = message.readShort();
        // vendor id
        message.skipBytes(EncodeConstants.SIZE_OF_INT_IN_BYTES);
        // subtype
        message.skipBytes(EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setExperimenterId(getExperimenterId());
        return actionBuilder;
    }
}
