/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegMove;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegMoveBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.move.grouping.NxActionRegMoveBuilder;

public class RegMoveCodec extends AbstractActionCodec {

    public static final int LENGTH = 24;
    public static final byte SUBTYPE = 6; // NXAST_REG_MOVE
    public static final NiciraActionSerializerKey SERIALIZER_KEY = new NiciraActionSerializerKey(
            EncodeConstants.OF13_VERSION_ID, ActionRegMove.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY = new NiciraActionDeserializerKey(
            EncodeConstants.OF13_VERSION_ID, SUBTYPE);

    @Override
    public void serialize(final Action input, final ByteBuf outBuffer) {
        ActionRegMove actionRegMove = ((ActionRegMove) input.getActionChoice());
        serializeHeader(LENGTH, SUBTYPE, outBuffer);
        outBuffer.writeShort(actionRegMove.getNxActionRegMove().getNBits());
        outBuffer.writeShort(actionRegMove.getNxActionRegMove().getSrcOfs());
        outBuffer.writeShort(actionRegMove.getNxActionRegMove().getDstOfs());
        outBuffer.writeInt(actionRegMove.getNxActionRegMove().getSrc().intValue());
        outBuffer.writeInt(actionRegMove.getNxActionRegMove().getDst().intValue());
    }

    @Override
    public Action deserialize(final ByteBuf message) {
        ActionBuilder actionBuilder = deserializeHeader(message);
        ActionRegMoveBuilder actionRegMoveBuilder = new ActionRegMoveBuilder();
        NxActionRegMoveBuilder nxActionRegMoveBuilder = new NxActionRegMoveBuilder();
        nxActionRegMoveBuilder.setNBits(message.readUnsignedShort());
        nxActionRegMoveBuilder.setSrcOfs(message.readUnsignedShort());
        nxActionRegMoveBuilder.setDstOfs(message.readUnsignedShort());
        nxActionRegMoveBuilder.setSrc(message.readUnsignedInt());
        nxActionRegMoveBuilder.setDst(message.readUnsignedInt());
        actionRegMoveBuilder.setNxActionRegMove(nxActionRegMoveBuilder.build());
        actionBuilder.setActionChoice(actionRegMoveBuilder.build());
        return actionBuilder.build();
    }

}
