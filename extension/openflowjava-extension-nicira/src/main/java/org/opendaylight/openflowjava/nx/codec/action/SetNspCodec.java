/*
 * Copyright (C) 2014, 2015 Red Hat, Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionSetNsp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionSetNspBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.set.nsp.grouping.NxActionSetNspBuilder;

/**
 * Codec for the NX_SetNsp and NX_SetNsp_TABLE
 */
public class SetNspCodec extends AbstractActionCodec {
    public static final int LENGTH = 16;
    public static final byte NXAST_SET_NSP_SUBTYPE = 32;
    public static final NiciraActionSerializerKey SERIALIZER_KEY =
            new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionSetNsp.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, NXAST_SET_NSP_SUBTYPE);
    private static final int padding = 2; // nx_action_SetNsp : uint8_t pad[3];

    @Override
    public void serialize(Action input, ByteBuf outBuffer) {
        ActionSetNsp action = ((ActionSetNsp) input.getActionChoice());
        serializeHeader(LENGTH, NXAST_SET_NSP_SUBTYPE, outBuffer);
        outBuffer.writeZero(padding);
        outBuffer.writeInt(action.getNxActionSetNsp().getNsp().intValue());
    }

    @Override
    public Action deserialize(ByteBuf message) {
        ActionBuilder actionBuilder = deserializeHeader(message);
        ActionSetNspBuilder builder = new ActionSetNspBuilder();
        NxActionSetNspBuilder nxActionSetNspBuilder = new NxActionSetNspBuilder();
        message.skipBytes(padding);
        nxActionSetNspBuilder.setNsp(message.readUnsignedInt());
        builder.setNxActionSetNsp(nxActionSetNspBuilder.build());
        actionBuilder.setActionChoice(builder.build());
        return actionBuilder.build();
    }

}
