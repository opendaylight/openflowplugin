/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.action;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint16;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.nx.api.NiciraActionDeserializerKey;
import org.opendaylight.openflowjava.nx.api.NiciraActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionOutputReg2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionOutputReg2Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.output.reg2.grouping.NxActionOutputReg2Builder;

public class OutputReg2Codec extends AbstractActionCodec {

    public static final byte SUBTYPE = 32; // NXAST_OUTPUT_REG2
    public static final NiciraActionSerializerKey SERIALIZER_KEY =
            new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionOutputReg2.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, SUBTYPE);

    @Override
    public Action deserialize(ByteBuf message) {
        final int startIndex = message.readerIndex();
        final ActionBuilder actionBuilder = deserializeHeader(message);
        final ActionOutputReg2Builder builder = new ActionOutputReg2Builder();
        NxActionOutputReg2Builder nxActionOutputReg2Builder = new NxActionOutputReg2Builder();
        nxActionOutputReg2Builder.setNBits(readUint16(message));
        nxActionOutputReg2Builder.setMaxLen(readUint16(message));
        nxActionOutputReg2Builder.setSrc(readNxmHeader(message));
        skipPadding(message, startIndex);
        builder.setNxActionOutputReg2(nxActionOutputReg2Builder.build());
        actionBuilder.setActionChoice(builder.build());
        return actionBuilder.build();
    }

    @Override
    public void serialize(Action input, ByteBuf outBuffer) {
        ActionOutputReg2 action = (ActionOutputReg2) input.getActionChoice();
        final int startIndex = outBuffer.writerIndex();
        serializeHeader(EncodeConstants.EMPTY_LENGTH, SUBTYPE, outBuffer);
        outBuffer.writeShort(action.getNxActionOutputReg2().getNBits().shortValue());
        outBuffer.writeShort(action.getNxActionOutputReg2().getMaxLen().shortValue());
        writeNxmHeader(action.getNxActionOutputReg2().getSrc(), outBuffer);
        writePaddingAndSetLength(outBuffer, startIndex);
    }
}
