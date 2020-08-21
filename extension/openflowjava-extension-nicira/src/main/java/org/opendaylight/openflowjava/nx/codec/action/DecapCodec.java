/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.action;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.nx.api.NiciraActionDeserializerKey;
import org.opendaylight.openflowjava.nx.api.NiciraActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionDecap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionDecapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.decap.grouping.NxActionDecapBuilder;

public class DecapCodec extends AbstractActionCodec {

    public static final int LENGTH = 16;
    private static final int NXAST_DECAP_SUBTYPE = 47;
    private static final int PADDING = 2;

    public static final NiciraActionSerializerKey SERIALIZER_KEY =
            new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionDecap.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, NXAST_DECAP_SUBTYPE);

    @Override
    public Action deserialize(ByteBuf message) {
        final ActionBuilder actionBuilder = deserializeHeader(message);
        // skip padding
        message.skipBytes(PADDING);
        return actionBuilder
                .setActionChoice(new ActionDecapBuilder()
                    .setNxActionDecap(new NxActionDecapBuilder().setPacketType(readUint32(message)).build())
                    .build())
                .build();
    }

    @Override
    public void serialize(Action input, ByteBuf outBuffer) {
        serializeHeader(LENGTH, NXAST_DECAP_SUBTYPE, outBuffer);
        // add padding
        outBuffer.writeZero(PADDING);
        ActionDecap actionDecap = (ActionDecap) input.getActionChoice();
        outBuffer.writeInt(actionDecap.getNxActionDecap().getPacketType().intValue());
    }
}
