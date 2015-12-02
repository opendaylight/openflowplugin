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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionPopEth;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionPopEthBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.pop.eth.grouping.NxActionPopEthBuilder;

/**
 * Codec for the pop_eth
 */
public class PopEthCodec extends AbstractActionCodec {
    public static final int LENGTH = 16;
    public static final byte NXAST_POP_ETH_SUBTYPE = 38;
    public static final NiciraActionSerializerKey SERIALIZER_KEY =
            new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionPopEth.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, NXAST_POP_ETH_SUBTYPE);
    private static final int padding = 6;

    @Override
    public void serialize(Action input, ByteBuf outBuffer) {
        ActionPopEth action = ((ActionPopEth) input.getActionChoice());
        serializeHeader(LENGTH, NXAST_POP_ETH_SUBTYPE, outBuffer);
        outBuffer.writeZero(padding);
    }

    @Override
    public Action deserialize(ByteBuf message) {
        ActionBuilder actionBuilder = deserializeHeader(message);
        ActionPopEthBuilder builder = new ActionPopEthBuilder();
        NxActionPopEthBuilder nxActionPopEthBuilder = new NxActionPopEthBuilder();
        message.skipBytes(padding);
        builder.setNxActionPopEth(nxActionPopEthBuilder.build());
        actionBuilder.setActionChoice(builder.build());
        return actionBuilder.build();
    }

}
