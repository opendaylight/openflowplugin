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
import org.opendaylight.yangtools.yang.common.Uint16;
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

    // FIXME: You might wonder, why has a static utility method a static mutable field. Look for FIXMEs and users of
    //        this field for a nice story.
    private static short length;

    private LearnCodecUtil() {
        // Hidden on purpose
    }

    static short deserializeHeader(final ByteBuf message) {
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

    static void serializeLearnHeader(final ByteBuf outBuffer, final ActionLearn action) {
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

    static void serializeFlowMods(final ByteBuf outBuffer, final ActionLearn action) {
        for (FlowMods flowMod : action.getNxActionLearn().nonnullFlowMods()) {
            final var modSpec = flowMod.getFlowModSpec();
            if (modSpec instanceof FlowModAddMatchFromFieldCase) {
                final var flowModSpecFromField = ((FlowModAddMatchFromFieldCase) modSpec).getFlowModAddMatchFromField();
                toFlowModSpecHeader(flowModSpecFromField, outBuffer);
                // TODO: Use ByteBufUtils.writeUint() instead of intValue()/shortValue()?
                outBuffer.writeInt(flowModSpecFromField.getSrcField().intValue());
                outBuffer.writeShort(flowModSpecFromField.getSrcOfs().shortValue());
                outBuffer.writeInt(flowModSpecFromField.getDstField().intValue());
                outBuffer.writeShort(flowModSpecFromField.getDstOfs().shortValue());
            } else if (modSpec instanceof FlowModAddMatchFromValueCase) {
                final var flowModSpec = ((FlowModAddMatchFromValueCase) modSpec).getFlowModAddMatchFromValue();
                toFlowModSpecHeader(flowModSpec, outBuffer);
                outBuffer.writeShort(flowModSpec.getValue().shortValue());
                outBuffer.writeInt(flowModSpec.getSrcField().intValue());
                outBuffer.writeShort(flowModSpec.getSrcOfs().shortValue());

            } else if (modSpec instanceof FlowModCopyFieldIntoFieldCase) {
                final var flowModSpec = ((FlowModCopyFieldIntoFieldCase) modSpec).getFlowModCopyFieldIntoField();
                toFlowModSpecHeader(flowModSpec, outBuffer);
                outBuffer.writeInt(flowModSpec.getSrcField().intValue());
                outBuffer.writeShort(flowModSpec.getSrcOfs().shortValue());
                outBuffer.writeInt(flowModSpec.getDstField().intValue());
                outBuffer.writeShort(flowModSpec.getDstOfs().shortValue());

            } else if (modSpec instanceof FlowModCopyValueIntoFieldCase) {
                final var flowModSpec = ((FlowModCopyValueIntoFieldCase) modSpec).getFlowModCopyValueIntoField();
                toFlowModSpecHeader(flowModSpec, outBuffer);
                outBuffer.writeShort(flowModSpec.getValue().shortValue());
                outBuffer.writeInt(flowModSpec.getDstField().intValue());
                outBuffer.writeShort(flowModSpec.getDstOfs().shortValue());

            } else if (modSpec instanceof FlowModOutputToPortCase) {
                final var flowModSpec = ((FlowModOutputToPortCase) modSpec).getFlowModOutputToPort();
                toFlowModSpecHeader(flowModSpec, outBuffer);
                outBuffer.writeInt(flowModSpec.getSrcField().intValue());
                outBuffer.writeShort(flowModSpec.getSrcOfs().shortValue());
            }
        }
    }

    private static void toFlowModSpecHeader(final FlowModOutputToPort flowModSpec, final ByteBuf outBuffer) {
        serializeFlowModSpecHeader(0, 2, flowModSpec.getFlowModNumBits(), outBuffer);
    }

    private static void toFlowModSpecHeader(final FlowModCopyValueIntoField flowModSpec, final ByteBuf outBuffer) {
        serializeFlowModSpecHeader(1, 1, flowModSpec.getFlowModNumBits(), outBuffer);
    }

    private static void toFlowModSpecHeader(final FlowModCopyFieldIntoField flowModSpec, final ByteBuf outBuffer) {
        serializeFlowModSpecHeader(0, 1, flowModSpec.getFlowModNumBits(), outBuffer);
    }

    private static void toFlowModSpecHeader(final FlowModAddMatchFromValue flowModSpec, final ByteBuf outBuffer) {
        serializeFlowModSpecHeader(1, 0, flowModSpec.getFlowModNumBits(), outBuffer);
    }

    private static void toFlowModSpecHeader(final FlowModAddMatchFromField flowModSpec, final ByteBuf outBuffer) {
        serializeFlowModSpecHeader(0, 0, flowModSpec.getFlowModNumBits(), outBuffer);
    }

    private static void serializeFlowModSpecHeader(final int src, final int dst, final Uint16 bitNum,
            final ByteBuf outBuffer) {
        outBuffer.writeShort(src << SRC_POS | dst << DST_POS | (short) bitNum.toJava());
    }

    static int calcLength(final ActionLearn action) {
        int actionLength = HEADER_LENGTH;

        for (FlowMods flowMod : action.getNxActionLearn().nonnullFlowMods()) {
            final var modSpec = flowMod.getFlowModSpec();
            if (modSpec instanceof FlowModAddMatchFromFieldCase) {
                actionLength += FROM_FIELD_LENGTH;
            } else if (modSpec instanceof FlowModAddMatchFromValueCase) {
                actionLength += FROM_VALUE_LENGTH;
            } else if (modSpec instanceof FlowModCopyFieldIntoFieldCase) {
                actionLength += FROM_FIELD_LENGTH;
            } else if (modSpec instanceof FlowModCopyValueIntoFieldCase) {
                actionLength += FROM_VALUE_LENGTH;
            } else if (modSpec instanceof FlowModOutputToPortCase) {
                actionLength += TO_PORT_LENGTH;
            }
        }

        return actionLength;
    }

    /*
     *                                 DESERIALIZATION
    */

    static void deserializeLearnHeader(final ByteBuf message, final NxActionLearnBuilder nxActionLearnBuilder) {
        nxActionLearnBuilder
            .setIdleTimeout(readUint16(message))
            .setHardTimeout(readUint16(message))
            .setPriority(readUint16(message))
            // FIXME: what conversion are we trying to do here?
            .setCookie(BigInteger.valueOf(message.readLong()))
            .setFlags(readUint16(message))
            .setTableId(readUint8(message));
        message.skipBytes(1);
        nxActionLearnBuilder
            .setFinIdleTimeout(readUint16(message))
            .setFinHardTimeout(readUint16(message));
    }

    // FIXME: OMG: this thing has its synchronized global state ...
    static synchronized void buildFlowModSpecs(final NxActionLearnBuilder nxActionLearnBuilder, final ByteBuf message,
            final short messageLength) {
        // ... which is this integer!
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

        // ... which is used for this warning
        if (LearnCodecUtil.length != 0) {
            LOG.error("Learn Codec read {} bytes more than needed from stream. Packet might be corrupted",
                    Math.abs(messageLength));
        }
        nxActionLearnBuilder.setFlowMods(flowModeList);
    }

    private static FlowMods readFlowMod(final ByteBuf message) {
        final short header = message.readShort();
        length -= Short.BYTES;
        if (header == 0) {
            return null;
        }

        final short src = (short) ((header & SRC_MASK) >> SRC_POS);
        final short dst = (short) ((header & DST_MASK) >> DST_POS);
        final short numBits = (short) (header & NUM_BITS_MASK);

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

    private static FlowMods readFlowModAddMatchFromField(final ByteBuf message, final short numBits) {
        final var builder = new FlowModAddMatchFromFieldBuilder()
            .setSrcField(readUint32(message))
            .setSrcOfs((int) message.readShort())
            .setDstField(readUint32(message))
            .setDstOfs((int) message.readShort())
            .setFlowModNumBits((int) numBits);
        length -= FROM_FIELD_LENGTH - Short.BYTES;

        return new FlowModsBuilder()
            .setFlowModSpec(new FlowModAddMatchFromFieldCaseBuilder()
                .setFlowModAddMatchFromField(builder.build())
                .build())
            .build();
    }

    private static FlowMods readFlowModAddMatchFromValue(final ByteBuf message, final short numBits) {
        final var builder = new FlowModAddMatchFromValueBuilder()
            .setValue(readUint16(message))
            .setSrcField(readUint32(message))
            .setSrcOfs((int) message.readShort())
            .setFlowModNumBits((int) numBits);
        length -= FROM_VALUE_LENGTH - Short.BYTES;

        return new FlowModsBuilder()
            .setFlowModSpec(new FlowModAddMatchFromValueCaseBuilder()
                .setFlowModAddMatchFromValue(builder.build())
                .build())
            .build();
    }

    private static FlowMods readFlowModCopyFromField(final ByteBuf message, final short numBits) {
        final var builder = new FlowModCopyFieldIntoFieldBuilder()
            .setSrcField(readUint32(message))
            .setSrcOfs((int) message.readShort())
            .setDstField(readUint32(message))
            .setDstOfs((int) message.readShort())
            .setFlowModNumBits((int) numBits);
        length -= FROM_FIELD_LENGTH - Short.BYTES;

        return new FlowModsBuilder()
            .setFlowModSpec(new FlowModCopyFieldIntoFieldCaseBuilder()
                .setFlowModCopyFieldIntoField(builder.build())
                .build())
            .build();
    }

    private static FlowMods readFlowModCopyFromValue(final ByteBuf message, final short numBits) {
        final var builder = new FlowModCopyValueIntoFieldBuilder()
            .setValue(readUint16(message))
            .setDstField(readUint32(message))
            .setDstOfs((int) message.readShort())
            .setFlowModNumBits((int) numBits);
        length -= FROM_VALUE_LENGTH - Short.BYTES;

        return new FlowModsBuilder()
            .setFlowModSpec(new FlowModCopyValueIntoFieldCaseBuilder()
                .setFlowModCopyValueIntoField(builder.build())
                .build())
            .build();
    }

    private static FlowMods readFlowToPort(final ByteBuf message, final short numBits) {
        final var builder = new FlowModOutputToPortBuilder()
            .setSrcField(readUint32(message))
            .setSrcOfs((int) message.readShort())
            .setFlowModNumBits((int) numBits);
        length -= TO_PORT_LENGTH - Short.BYTES;

        return new FlowModsBuilder()
            .setFlowModSpec(new FlowModOutputToPortCaseBuilder().setFlowModOutputToPort(builder.build()).build())
            .build();
    }
}
