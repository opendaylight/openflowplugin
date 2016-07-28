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

import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionLearn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromValueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromValueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModCopyFieldIntoFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModCopyFieldIntoFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModCopyValueIntoFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModCopyValueIntoFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModOutputToPortCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModOutputToPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.field._case.FlowModAddMatchFromField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.field._case.FlowModAddMatchFromFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.value._case.FlowModAddMatchFromValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.value._case.FlowModAddMatchFromValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.copy.field.into.field._case.FlowModCopyFieldIntoFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.copy.value.into.field._case.FlowModCopyValueIntoFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.output.to.port._case.FlowModOutputToPortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.NxActionLearnBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.nx.action.learn.FlowMods;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.nx.action.learn.FlowModsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LearnCodecUtil {

    private static final Logger logger = LoggerFactory.getLogger(LearnCodecUtil.class);
    public static final int HEADER_LENGTH = 32;
    private static final short SRC_MASK = 0x2000;
    private static final short DST_MASK = 0x1800;
    private static final short NUM_BITS_MASK = 0x07FF;
    private static short length;
    
    //////////////////////////////////////////////////////////////////////////////////////////////
    ////                                SERIALIZATION                                    /////////
    //////////////////////////////////////////////////////////////////////////////////////////////

    static void serializeLearnHeader(final ByteBuf outBuffer, ActionLearn action) {
        outBuffer.writeShort(action.getNxActionLearn().getIdleTimeout().shortValue());
        outBuffer.writeShort(action.getNxActionLearn().getHardTimeout().shortValue());
        outBuffer.writeShort(action.getNxActionLearn().getPriority().shortValue());
        outBuffer.writeLong(action.getNxActionLearn().getCookie().longValue());
        outBuffer.writeShort(action.getNxActionLearn().getFlags().shortValue());
        outBuffer.writeByte(action.getNxActionLearn().getTableId().byteValue());
        outBuffer.writeZero(1);
        outBuffer.writeShort(action.getNxActionLearn().getFinIdleTimeout().shortValue());
        outBuffer.writeShort(action.getNxActionLearn().getFinHardTimeout().shortValue());
    }

    static void serializeFlowMods(final ByteBuf outBuffer, ActionLearn action) {
        if(action.getNxActionLearn().getFlowMods() != null){
            for(FlowMods flowMod : action.getNxActionLearn().getFlowMods()){
                if(flowMod.getFlowModSpec() instanceof FlowModAddMatchFromFieldCase){
                    FlowModAddMatchFromField flowModSpecFromField = ((FlowModAddMatchFromFieldCase)flowMod.getFlowModSpec()).getFlowModAddMatchFromField();
                    toFlowModSpecHeader(flowModSpecFromField, outBuffer);
                    outBuffer.writeInt(flowModSpecFromField.getSrcField().intValue());
                    outBuffer.writeShort(flowModSpecFromField.getSrcOfs().shortValue());
                    outBuffer.writeInt(flowModSpecFromField.getDstField().intValue());
                    outBuffer.writeShort(flowModSpecFromField.getDstOfs().shortValue());
                }else if( flowMod.getFlowModSpec() instanceof FlowModAddMatchFromValueCase){
                    FlowModAddMatchFromValue flowModSpec = ((FlowModAddMatchFromValueCase)flowMod.getFlowModSpec()).getFlowModAddMatchFromValue();
                    toFlowModSpecHeader(flowModSpec, outBuffer);
                    outBuffer.writeShort(flowModSpec.getValue().shortValue());
                    outBuffer.writeInt(flowModSpec.getSrcField().intValue());
                    outBuffer.writeShort(flowModSpec.getSrcOfs().shortValue());

                }else if( flowMod.getFlowModSpec() instanceof FlowModCopyFieldIntoFieldCase){

                }else if( flowMod.getFlowModSpec() instanceof FlowModCopyValueIntoFieldCase){

                }else if( flowMod.getFlowModSpec() instanceof FlowModOutputToPortCase){

                }
            }
        }
    }

    private static void toFlowModSpecHeader(FlowModAddMatchFromValue flowModSpec, ByteBuf outBuffer) {
        serializeFlowModSpecHeader(1,0,(short)(int)flowModSpec.getFlowModNumBits(), outBuffer);
    }

    private static void toFlowModSpecHeader(FlowModAddMatchFromField flowModSpec, ByteBuf outBuffer) {
        serializeFlowModSpecHeader(0,0,(short)(int)flowModSpec.getFlowModNumBits(), outBuffer);
    }

    private static void serializeFlowModSpecHeader(int src, int dst, short bitNum, ByteBuf outBuffer) {
        short s = 0;
        s |= (src << 13);
        s |= (dst << 11);
        s |= bitNum;
        outBuffer.writeShort(s);
    }

    static int calcLength(ActionLearn action) {
        int length = HEADER_LENGTH;
        if(action.getNxActionLearn().getFlowMods() == null){
            return length;
        }
        for(FlowMods flowMod : action.getNxActionLearn().getFlowMods()){
            if(flowMod.getFlowModSpec() instanceof FlowModAddMatchFromFieldCase){
                length += 14;
            }else if( flowMod.getFlowModSpec() instanceof FlowModAddMatchFromValueCase){
                length += 10;
            }else if( flowMod.getFlowModSpec() instanceof FlowModCopyFieldIntoFieldCase){
                length += 14;
            }else if( flowMod.getFlowModSpec() instanceof FlowModCopyValueIntoFieldCase){
                length += 10;
            }else if( flowMod.getFlowModSpec() instanceof FlowModOutputToPortCase){
                length += 8;
            }
        }

        return length;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////
    ////                                DESERIALIZATION                                  /////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    
    static void deserializeLearnHeader(final ByteBuf message, NxActionLearnBuilder nxActionLearnBuilder) {
        nxActionLearnBuilder.setIdleTimeout(message.readUnsignedShort());
        nxActionLearnBuilder.setHardTimeout(message.readUnsignedShort());
        nxActionLearnBuilder.setPriority(message.readUnsignedShort());
        nxActionLearnBuilder.setCookie(BigInteger.valueOf(message.readLong()));
        nxActionLearnBuilder.setFlags(message.readUnsignedShort());
        nxActionLearnBuilder.setTableId(message.readUnsignedByte());
        message.skipBytes(1);
        nxActionLearnBuilder.setFinIdleTimeout(message.readUnsignedShort());
        nxActionLearnBuilder.setFinHardTimeout(message.readUnsignedShort());
    }

    static synchronized void buildFlowModSpecs(NxActionLearnBuilder nxActionLearnBuilder, ByteBuf message, short length) {
        LearnCodecUtil.length = length;
        List<FlowMods> flowModeList = new ArrayList<FlowMods>();
        
        while(length > 0){
            FlowMods flowMod = readFlowMod(message);
            
            if(flowMod != null){
                flowModeList.add(flowMod);
            }else{
                logger.warn("couldn't serialize flow mod for learn action");
            }
        }
        
        if(LearnCodecUtil.length != 0){
            logger.error("Learn Codec read " + Math.abs(length) + " bytes more than needed from stream. Packet might be corrupted");
        }
        
        nxActionLearnBuilder.setFlowMods(flowModeList);
        
    }

    private static FlowMods readFlowMod(ByteBuf message) {
        short header = message.readShort();
        length -= EncodeConstants.SIZE_OF_SHORT_IN_BYTES;
        if(header == 0){
            return null;
        }
        
        short src = (short) ((header & SRC_MASK) >> 13);
        short dst = (short) ((header & DST_MASK) >> 11);
        short numBits = (short) (header & NUM_BITS_MASK);
        
        if(src == 0 && dst == 0 && numBits != 0){
            return readFlowModAddMatchFromField(message, numBits);
        } else if(src == 0 && dst == 0){
            message.skipBytes(2);
            length -= 2;
        } else if(src == 1 && dst == 0){
            return readFlowModAddMatchFromValue(message, numBits);
        } else if(src == 0 && dst == 1){
            return readFlowModCopyFromField(message, numBits);
        } else if(src == 1 && dst == 1){
            return readFlowModCopyFromValue(message, numBits);
        } else if(src == 0 && dst == 2){
            return readFlowToPort(message, numBits);
        }
        
        return null;
    }

    

    private static FlowMods readFlowModAddMatchFromField(ByteBuf message, short numBits) {
        FlowModAddMatchFromFieldBuilder builder = new FlowModAddMatchFromFieldBuilder();
        builder.setSrcField((long) message.readInt());
        builder.setSrcOfs((int) message.readShort());
        builder.setDstField((long) message.readInt());
        builder.setDstOfs((int) message.readShort());
        builder.setFlowModNumBits((int) numBits);
        length -= 12;
        
        FlowModsBuilder flowModsBuilder = new FlowModsBuilder();
        FlowModAddMatchFromFieldCaseBuilder caseBuilder = new FlowModAddMatchFromFieldCaseBuilder();
        caseBuilder.setFlowModAddMatchFromField(builder.build());
        flowModsBuilder.setFlowModSpec(caseBuilder.build());
        return flowModsBuilder.build();
    }
    
    private static FlowMods readFlowModAddMatchFromValue(ByteBuf message, short numBits) {
        FlowModAddMatchFromValueBuilder builder = new FlowModAddMatchFromValueBuilder();
        builder.setValue((int) message.readShort());
        builder.setSrcField((long) message.readInt());
        builder.setSrcOfs((int) message.readShort());
        builder.setFlowModNumBits((int) numBits);
        length -= 10;
        
        FlowModsBuilder flowModsBuilder = new FlowModsBuilder();
        FlowModAddMatchFromValueCaseBuilder caseBuilder = new FlowModAddMatchFromValueCaseBuilder();
        caseBuilder.setFlowModAddMatchFromValue(builder.build());
        flowModsBuilder.setFlowModSpec(caseBuilder.build());
        return flowModsBuilder.build();
    }
    
    private static FlowMods readFlowModCopyFromField(ByteBuf message, short numBits) {
        FlowModCopyFieldIntoFieldBuilder builder = new FlowModCopyFieldIntoFieldBuilder();
        builder.setSrcField((long) message.readInt());
        builder.setSrcOfs((int) message.readShort());
        builder.setDstField((long) message.readInt());
        builder.setDstOfs((int) message.readShort());
        builder.setFlowModNumBits((int) numBits);
        length -= 12;
        
        FlowModsBuilder flowModsBuilder = new FlowModsBuilder();
        FlowModCopyFieldIntoFieldCaseBuilder caseBuilder = new FlowModCopyFieldIntoFieldCaseBuilder();
        caseBuilder.setFlowModCopyFieldIntoField(builder.build());
        flowModsBuilder.setFlowModSpec(caseBuilder.build());
        return flowModsBuilder.build();
    }
    
    private static FlowMods readFlowModCopyFromValue(ByteBuf message, short numBits) {
        FlowModCopyValueIntoFieldBuilder builder = new FlowModCopyValueIntoFieldBuilder();
        builder.setValue((int) message.readShort());
        builder.setDstField((long) message.readInt());
        builder.setDstOfs((int) message.readShort());
        builder.setFlowModNumBits((int) numBits);
        length -= 10;
        
        FlowModsBuilder flowModsBuilder = new FlowModsBuilder();
        FlowModCopyValueIntoFieldCaseBuilder caseBuilder = new FlowModCopyValueIntoFieldCaseBuilder();
        caseBuilder.setFlowModCopyValueIntoField(builder.build());
        flowModsBuilder.setFlowModSpec(caseBuilder.build());
        return flowModsBuilder.build();
    }
    
    private static FlowMods readFlowToPort(ByteBuf message, short numBits) {
        FlowModOutputToPortBuilder builder = new FlowModOutputToPortBuilder();
        builder.setSrcField((long) message.readInt());
        builder.setSrcOfs((int) message.readShort());
        builder.setFlowModNumBits((int) numBits);
        length -= 8;
        
        FlowModsBuilder flowModsBuilder = new FlowModsBuilder();
        FlowModOutputToPortCaseBuilder caseBuilder = new FlowModOutputToPortCaseBuilder();
        caseBuilder.setFlowModOutputToPort(builder.build());
        flowModsBuilder.setFlowModSpec(caseBuilder.build());
        return flowModsBuilder.build();
    }

}
