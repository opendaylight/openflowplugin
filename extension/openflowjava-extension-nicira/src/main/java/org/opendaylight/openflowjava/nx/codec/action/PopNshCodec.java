/*
 * Copyright (C) 2015 Intel, Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionPopNsh;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionPopNshBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.pop.nsh.grouping.NxActionPopNshBuilder;

/**
 * Codec for the pop_nsh
 */
public class PopNshCodec extends AbstractActionCodec {
    public static final int LENGTH = 16;
    public static final byte NXAST_POP_NSH_SUBTYPE = 36;
    public static final NiciraActionSerializerKey SERIALIZER_KEY =
            new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionPopNsh.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, NXAST_POP_NSH_SUBTYPE);
    private static final int padding = 6;

    @Override
    public void serialize(Action input, ByteBuf outBuffer) {
        ActionPopNsh action = ((ActionPopNsh) input.getActionChoice());
        serializeHeader(LENGTH, NXAST_POP_NSH_SUBTYPE, outBuffer);
        outBuffer.writeZero(padding);
    }

    @Override
    public Action deserialize(ByteBuf message) {
        ActionBuilder actionBuilder = deserializeHeader(message);
        ActionPopNshBuilder builder = new ActionPopNshBuilder();
        NxActionPopNshBuilder nxActionPopNshBuilder = new NxActionPopNshBuilder();
        message.skipBytes(padding);
        builder.setNxActionPopNsh(nxActionPopNshBuilder.build());
        actionBuilder.setActionChoice(builder.build());
        return actionBuilder.build();
    }

}
