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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionEncap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionEncapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.encap.grouping.NxActionEncap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.encap.grouping.NxActionEncapBuilder;

public class EncapCodec extends AbstractActionCodec {

    public static final int LENGTH = 16;
    private static final int NXAST_ENCAP_SUBTYPE = 46;
    private static final int HEADER_SIZE_NOT_SPECIFIED = 0;

    public static final NiciraActionSerializerKey SERIALIZER_KEY =
            new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionEncap.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, NXAST_ENCAP_SUBTYPE);

    @Override
    public Action deserialize(ByteBuf message) {
        final ActionBuilder actionBuilder = deserializeHeader(message);
        // skip header size, not used
        message.skipBytes(Short.BYTES);
        NxActionEncap nxActionEncap = new NxActionEncapBuilder().setPacketType(readUint32(message)).build();
        ActionEncap actionEncap = new ActionEncapBuilder().setNxActionEncap(nxActionEncap).build();
        actionBuilder.setActionChoice(actionEncap);
        return actionBuilder.build();
    }

    @Override
    public void serialize(Action input, ByteBuf outBuffer) {
        serializeHeader(LENGTH, NXAST_ENCAP_SUBTYPE, outBuffer);
        outBuffer.writeShort(HEADER_SIZE_NOT_SPECIFIED);
        ActionEncap actionEncap = (ActionEncap) input.getActionChoice();
        outBuffer.writeInt(actionEncap.getNxActionEncap().getPacketType().intValue());
    }
}
