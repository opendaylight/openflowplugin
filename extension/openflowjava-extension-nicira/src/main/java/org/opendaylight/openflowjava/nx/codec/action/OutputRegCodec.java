/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.nx.codec.action;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.nx.api.NiciraActionDeserializerKey;
import org.opendaylight.openflowjava.nx.api.NiciraActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionOutputReg;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionOutputRegBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.output.reg.grouping.NxActionOutputRegBuilder;

/**
 * Codec for the Nicira OutputRegAction
 *
 * @author readams
 */
public class OutputRegCodec extends AbstractActionCodec {
    public static final int LENGTH = 24;
    public static final byte SUBTYPE = 15; // NXAST_OUTPUT_REG
    public static final byte PADDING_IN_OUTPUT_REG_ACTION = 6;
    public static final NiciraActionSerializerKey SERIALIZER_KEY =
            new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionOutputReg.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, SUBTYPE);

    @Override
    public void serialize(final Action input, final ByteBuf outBuffer) {
        ActionOutputReg action = ((ActionOutputReg) input.getActionChoice());
        serializeHeader(LENGTH, SUBTYPE, outBuffer);
        outBuffer.writeShort(action.getNxActionOutputReg().getNBits().shortValue());
        outBuffer.writeInt(action.getNxActionOutputReg().getSrc().intValue());
        outBuffer.writeShort(action.getNxActionOutputReg().getMaxLen().shortValue());
        outBuffer.writeZero(6);
    }

    @Override
    public Action deserialize(final ByteBuf message) {
        ActionBuilder actionBuilder = deserializeHeader(message);
        ActionOutputRegBuilder builder = new ActionOutputRegBuilder();
        NxActionOutputRegBuilder nxActionOutputRegBuilder = new NxActionOutputRegBuilder();
        nxActionOutputRegBuilder.setNBits(message.readUnsignedShort());
        nxActionOutputRegBuilder.setSrc(message.readUnsignedInt());
        nxActionOutputRegBuilder.setMaxLen(message.readUnsignedShort());
        message.skipBytes(PADDING_IN_OUTPUT_REG_ACTION);
        builder.setNxActionOutputReg(nxActionOutputRegBuilder.build());
        actionBuilder.setActionChoice(builder.build());
        return actionBuilder.build();
    }

}
