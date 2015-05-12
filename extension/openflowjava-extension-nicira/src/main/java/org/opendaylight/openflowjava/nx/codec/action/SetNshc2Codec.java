/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionSetNshc2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionSetNshc2Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.set.nshc._2.grouping.NxActionSetNshc2Builder;

/**
 * Codec for the NX_SetNsp and NX_SetNsp_TABLE
 */
public class SetNshc2Codec extends AbstractActionCodec {
    public static final int LENGTH = 16;
    public static final byte NXAST_SET_NSC_SUBTYPE = 35;
    public static final NiciraActionSerializerKey SERIALIZER_KEY =
            new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionSetNshc2.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, NXAST_SET_NSC_SUBTYPE);
    private static final int padding = 2; // nx_action_SetNsp : uint8_t pad[3];

    @Override
    public void serialize(Action input, ByteBuf outBuffer) {
        ActionSetNshc2 action = ((ActionSetNshc2) input.getActionChoice());
        serializeHeader(LENGTH, NXAST_SET_NSC_SUBTYPE, outBuffer);
        outBuffer.writeZero(padding);
        outBuffer.writeInt(action.getNxActionSetNshc2().getNshc().intValue());
    }

    @Override
    public Action deserialize(ByteBuf message) {
        ActionBuilder actionBuilder = deserializeHeader(message);
        ActionSetNshc2Builder builder = new ActionSetNshc2Builder();
        NxActionSetNshc2Builder nxActionSetNspBuilder = new NxActionSetNshc2Builder();
        message.skipBytes(padding);
        nxActionSetNspBuilder.setNshc(message.readUnsignedInt());
        builder.setNxActionSetNshc2(nxActionSetNspBuilder.build());
        actionBuilder.setActionChoice(builder.build());
        return actionBuilder.build();
    }

}
