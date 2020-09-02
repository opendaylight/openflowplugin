/*
 * Copyright (c) 2016 Hewlett-Packard Enterprise and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.action;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint16;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.copy.field.into.field._case.FlowModCopyFieldIntoField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.copy.field.into.field._case.FlowModCopyFieldIntoFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.copy.value.into.field._case.FlowModCopyValueIntoField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.copy.value.into.field._case.FlowModCopyValueIntoFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.output.to.port._case.FlowModOutputToPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.output.to.port._case.FlowModOutputToPortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.NxActionLearnBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.nx.action.learn.FlowMods;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.nx.action.learn.FlowModsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LearnCodecUtil {

    private static final Logger LOG = LoggerFactory.getLogger(LearnCodecUtil.class);
    public static final int HEADER_LENGTH = 32;
    private static final short SRC_MASK = 0x2000;
    private static final short DST_MASK = 0x1800;
    private static final short NUM_BITS_MASK = 0x07FF;
    private static final int SRC_POS = 13;
    private static final int DST_POS = 11;
    private static final int FROM_FIELD_LENGTH = 14;
    private static final int FROM_VALUE_LENGTH = 10;
    private static final int TO_PORT_LENGTH = 8;
    private static final int EMPTY_FLOW_MOD_LENGTH = 2;
    private static short length;

    private LearnCodecUtil() {
    }

    static short deserializeHeader(ByteBuf message) {
        // size of experimenter type
        message.skipBytes(Short.BYTES);
        // size of length
        short messageLength = message.readShort();
        // vendor id
        message.skipBytes(Integer.BYTES);
        // subtype
        message.skipBytes(Short.BYTES);
        return messageLength;
    }

    /*
     *                                 SERIALIZATION
    */

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
        if (action.getNxActionLearn().getFlowMods() != null) {
            for (FlowMods flowMod : action.getNxActionLearn().getFlowMods()) {
                if (flowMod.getFlowModSpec() instanceof FlowModAddMatchFromFieldCase) {
                    FlowModAddMatchFromField flowModSpecFromField = ((FlowModAddMatchFromFieldCase) flowMod
                            .getFlowModSpec()).getFlowModAddMatchFromField();
                    toFlowModSpecHeader(flowModSpecFromField, outBuffer);
                    outBuffer.writeInt(flowModSpecFromField.getSrcField().intValue());
                    outBuffer.writeShort(flowModSpecFromField.getSrcOfs().shortValue());
                    outBuffer.writeInt(flowModSpecFromField.getDstField().intValue());
                    outBuffer.writeShort(flowModSpecFromField.getDstOfs().shortValue());
                } else if (flowMod.getFlowModSpec() instanceof FlowModAddMatchFromValueCase) {
                    FlowModAddMatchFromValue flowModSpec = ((FlowModAddMatchFromValueCase) flowMod.getFlowModSpec())
                            .getFlowModAddMatchFromValue();
                    toFlowModSpecHeader(flowModSpec, outBuffer);
                    outBuffer.writeShort(flowModSpec.getValue().shortValue());
                    outBuffer.writeInt(flowModSpec.getSrcField().intValue());
                    outBuffer.writeShort(flowModSpec.getSrcOfs().shortValue());

                } else if (flowMod.getFlowModSpec() instanceof FlowModCopyFieldIntoFieldCase) {
                    FlowModCopyFieldIntoField flowModSpec = ((FlowModCopyFieldIntoFieldCase) flowMod.getFlowModSpec())
                            .getFlowModCopyFieldIntoField();
                    toFlowModSpecHeader(flowModSpec, outBuffer);
                    outBuffer.writeInt(flowModSpec.getSrcField().intValue());
                    outBuffer.writeShort(flowModSpec.getSrcOfs().shortValue());
                    outBuffer.writeInt(flowModSpec.getDstField().intValue());
                    outBuffer.writeShort(flowModSpec.getDstOfs().shortValue());

                } else if (flowMod.getFlowModSpec() instanceof FlowModCopyValueIntoFieldCase) {
                    FlowModCopyValueIntoField flowModSpec = ((FlowModCopyValueIntoFieldCase) flowMod.getFlowModSpec())
                            .getFlowModCopyValueIntoField();
                    toFlowModSpecHeader(flowModSpec, outBuffer);
                    outBuffer.writeShort(flowModSpec.getValue().shortValue());
                    outBuffer.writeInt(flowModSpec.getDstField().intValue());
                    outBuffer.writeShort(flowModSpec.getDstOfs().shortValue());

                } else if (flowMod.getFlowModSpec() instanceof FlowModOutputToPortCase) {
                    FlowModOutputToPort flowModSpec = ((FlowModOutputToPortCase) flowMod.getFlowModSpec())
                            .getFlowModOutputToPort();
                    toFlowModSpecHeader(flowModSpec, outBuffer);
                    outBuffer.writeInt(flowModSpec.getSrcField().intValue());
                    outBuffer.writeShort(flowModSpec.getSrcOfs().shortValue());
                }
            }
        }
    }

    private static void toFlowModSpecHeader(FlowModOutputToPort flowModSpec, ByteBuf outBuffer) {
        serializeFlowModSpecHeader(0,2,(short)flowModSpec.getFlowModNumBits().toJava(), outBuffer);
    }

    private static void toFlowModSpecHeader(FlowModCopyValueIntoField flowModSpec, ByteBuf outBuffer) {
        serializeFlowModSpecHeader(1,1,(short)flowModSpec.getFlowModNumBits().toJava(), outBuffer);
    }

    private static void toFlowModSpecHeader(FlowModCopyFieldIntoField flowModSpec, ByteBuf outBuffer) {
        serializeFlowModSpecHeader(0,1,(short)flowModSpec.getFlowModNumBits().toJava(), outBuffer);
    }

    private static void toFlowModSpecHeader(FlowModAddMatchFromValue flowModSpec, ByteBuf outBuffer) {
        serializeFlowModSpecHeader(1,0,(short)flowModSpec.getFlowModNumBits().toJava(), outBuffer);
    }

    private static void toFlowModSpecHeader(FlowModAddMatchFromField flowModSpec, ByteBuf outBuffer) {
        serializeFlowModSpecHeader(0,0,(short)flowModSpec.getFlowModNumBits().toJava(), outBuffer);
    }

    private static void serializeFlowModSpecHeader(int src, int dst, short bitNum, ByteBuf outBuffer) {
        short value = 0;
        value |= src << SRC_POS;
        value |= dst << DST_POS;
        value |= bitNum;
        outBuffer.writeShort(value);
    }

    static int calcLength(ActionLearn action) {
        int actionLength = HEADER_LENGTH;
        if (action.getNxActionLearn().getFlowMods() == null) {
            return actionLength;
        }
        for (FlowMods flowMod : action.getNxActionLearn().getFlowMods()) {
            if (flowMod.getFlowModSpec() instanceof FlowModAddMatchFromFieldCase) {
                actionLength += FROM_FIELD_LENGTH;
            } else if (flowMod.getFlowModSpec() instanceof FlowModAddMatchFromValueCase) {
                actionLength += FROM_VALUE_LENGTH;
            } else if (flowMod.getFlowModSpec() instanceof FlowModCopyFieldIntoFieldCase) {
                actionLength += FROM_FIELD_LENGTH;
            } else if (flowMod.getFlowModSpec() instanceof FlowModCopyValueIntoFieldCase) {
                actionLength += FROM_VALUE_LENGTH;
            } else if (flowMod.getFlowModSpec() instanceof FlowModOutputToPortCase) {
                actionLength += TO_PORT_LENGTH;
            }
        }

        return actionLength;
    }

    /*
     *                                 DESERIALIZATION
    */

    static void deserializeLearnHeader(final ByteBuf message, NxActionLearnBuilder nxActionLearnBuilder) {
        nxActionLearnBuilder.setIdleTimeout(readUint16(message));
        nxActionLearnBuilder.setHardTimeout(readUint16(message));
        nxActionLearnBuilder.setPriority(readUint16(message));
        nxActionLearnBuilder.setCookie(BigInteger.valueOf(message.readLong()));
        nxActionLearnBuilder.setFlags(readUint16(message));
        nxActionLearnBuilder.setTableId(readUint8(message));
        message.skipBytes(1);
        nxActionLearnBuilder.setFinIdleTimeout(readUint16(message));
        nxActionLearnBuilder.setFinHardTimeout(readUint16(message));
    }

    static synchronized void buildFlowModSpecs(NxActionLearnBuilder nxActionLearnBuilder, ByteBuf message,
            short messageLength) {
        LearnCodecUtil.length = messageLength;
        List<FlowMods> flowModeList = new ArrayList<>();

        while (LearnCodecUtil.length > 0) {
            FlowMods flowMod = readFlowMod(message);

            if (flowMod != null) {
                flowModeList.add(flowMod);
            } else {
                LOG.trace("skipping padding bytes");
            }
        }

        if (LearnCodecUtil.length != 0) {
            LOG.error("Learn Codec read {} bytes more than needed from stream. Packet might be corrupted",
                    Math.abs(messageLength));
        }
        nxActionLearnBuilder.setFlowMods(flowModeList);
    }

    private static FlowMods readFlowMod(ByteBuf message) {
        short header = message.readShort();
        length -= Short.BYTES;
        if (header == 0) {
            return null;
        }

        short src = (short) ((header & SRC_MASK) >> SRC_POS);
        short dst = (short) ((header & DST_MASK) >> DST_POS);
        short numBits = (short) (header & NUM_BITS_MASK);

        if (src == 0 && dst == 0 && numBits != 0) {
            return readFlowModAddMatchFromField(message, numBits);
        } else if (src == 0 && dst == 0) {
            message.skipBytes(EMPTY_FLOW_MOD_LENGTH);
            length -= EMPTY_FLOW_MOD_LENGTH;
        } else if (src == 1 && dst == 0) {
            return readFlowModAddMatchFromValue(message, numBits);
        } else if (src == 0 && dst == 1) {
            return readFlowModCopyFromField(message, numBits);
        } else if (src == 1 && dst == 1) {
            return readFlowModCopyFromValue(message, numBits);
        } else if (src == 0 && dst == 2) {
            return readFlowToPort(message, numBits);
        }
        return null;
    }



    private static FlowMods readFlowModAddMatchFromField(ByteBuf message, short numBits) {
        FlowModAddMatchFromFieldBuilder builder = new FlowModAddMatchFromFieldBuilder();
        builder.setSrcField(readUint32(message));
        builder.setSrcOfs((int) message.readShort());
        builder.setDstField(readUint32(message));
        builder.setDstOfs((int) message.readShort());
        builder.setFlowModNumBits((int) numBits);
        length -= FROM_FIELD_LENGTH - Short.BYTES;

        FlowModsBuilder flowModsBuilder = new FlowModsBuilder();
        FlowModAddMatchFromFieldCaseBuilder caseBuilder = new FlowModAddMatchFromFieldCaseBuilder();
        caseBuilder.setFlowModAddMatchFromField(builder.build());
        flowModsBuilder.setFlowModSpec(caseBuilder.build());
        return flowModsBuilder.build();
    }

    private static FlowMods readFlowModAddMatchFromValue(ByteBuf message, short numBits) {
        FlowModAddMatchFromValueBuilder builder = new FlowModAddMatchFromValueBuilder();
        builder.setValue(readUint16(message));
        builder.setSrcField(readUint32(message));
        builder.setSrcOfs((int) message.readShort());
        builder.setFlowModNumBits((int) numBits);
        length -= FROM_VALUE_LENGTH - Short.BYTES;

        FlowModsBuilder flowModsBuilder = new FlowModsBuilder();
        FlowModAddMatchFromValueCaseBuilder caseBuilder = new FlowModAddMatchFromValueCaseBuilder();
        caseBuilder.setFlowModAddMatchFromValue(builder.build());
        flowModsBuilder.setFlowModSpec(caseBuilder.build());
        return flowModsBuilder.build();
    }

    private static FlowMods readFlowModCopyFromField(ByteBuf message, short numBits) {
        FlowModCopyFieldIntoFieldBuilder builder = new FlowModCopyFieldIntoFieldBuilder();
        builder.setSrcField(readUint32(message));
        builder.setSrcOfs((int) message.readShort());
        builder.setDstField(readUint32(message));
        builder.setDstOfs((int) message.readShort());
        builder.setFlowModNumBits((int) numBits);
        length -= FROM_FIELD_LENGTH - Short.BYTES;

        FlowModsBuilder flowModsBuilder = new FlowModsBuilder();
        FlowModCopyFieldIntoFieldCaseBuilder caseBuilder = new FlowModCopyFieldIntoFieldCaseBuilder();
        caseBuilder.setFlowModCopyFieldIntoField(builder.build());
        flowModsBuilder.setFlowModSpec(caseBuilder.build());
        return flowModsBuilder.build();
    }

    private static FlowMods readFlowModCopyFromValue(ByteBuf message, short numBits) {
        FlowModCopyValueIntoFieldBuilder builder = new FlowModCopyValueIntoFieldBuilder();
        builder.setValue(readUint16(message));
        builder.setDstField(readUint32(message));
        builder.setDstOfs((int) message.readShort());
        builder.setFlowModNumBits((int) numBits);
        length -= FROM_VALUE_LENGTH - Short.BYTES;

        FlowModsBuilder flowModsBuilder = new FlowModsBuilder();
        FlowModCopyValueIntoFieldCaseBuilder caseBuilder = new FlowModCopyValueIntoFieldCaseBuilder();
        caseBuilder.setFlowModCopyValueIntoField(builder.build());
        flowModsBuilder.setFlowModSpec(caseBuilder.build());
        return flowModsBuilder.build();
    }

    private static FlowMods readFlowToPort(ByteBuf message, short numBits) {
        FlowModOutputToPortBuilder builder = new FlowModOutputToPortBuilder();
        builder.setSrcField(readUint32(message));
        builder.setSrcOfs((int) message.readShort());
        builder.setFlowModNumBits((int) numBits);
        length -= TO_PORT_LENGTH - Short.BYTES;

        FlowModsBuilder flowModsBuilder = new FlowModsBuilder();
        FlowModOutputToPortCaseBuilder caseBuilder = new FlowModOutputToPortCaseBuilder();
        caseBuilder.setFlowModOutputToPort(builder.build());
        flowModsBuilder.setFlowModSpec(caseBuilder.build());
        return flowModsBuilder.build();
    }
}
