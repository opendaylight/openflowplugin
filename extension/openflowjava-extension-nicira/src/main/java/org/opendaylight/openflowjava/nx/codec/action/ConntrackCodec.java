/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionConntrack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionConntrackBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.conntrack.grouping.NxActionConntrackBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Aswin Suryanarayanan.
 */

public class ConntrackCodec extends AbstractActionCodec {
    private static final Logger logger = LoggerFactory.getLogger(ConntrackCodec.class);

    public static final int LENGTH = 24;
    public static final byte NXAST_CONNTRACK_SUBTYPE = 35;
    public static final NiciraActionSerializerKey SERIALIZER_KEY =
            new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionConntrack.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, NXAST_CONNTRACK_SUBTYPE);

    @Override
    public void serialize(final Action input, final ByteBuf outBuffer) {
        ActionConntrack action = ((ActionConntrack) input.getActionChoice()); //getAugmentation(OfjAugNxAction.class).getActionConntrack();
        serializeHeader(LENGTH, NXAST_CONNTRACK_SUBTYPE, outBuffer);

        outBuffer.writeShort(action.getNxActionConntrack().getFlags().shortValue());
        outBuffer.writeInt(action.getNxActionConntrack().getZoneSrc().intValue());
        outBuffer.writeShort(action.getNxActionConntrack().getConntrackZone().shortValue());
        outBuffer.writeByte(action.getNxActionConntrack().getRecircTable().byteValue());
        outBuffer.writeZero(5);
    }

    @Override
    public Action deserialize(final ByteBuf message) {
        ActionBuilder actionBuilder = deserializeHeader(message);
        ActionConntrackBuilder actionConntrackBuilder = new ActionConntrackBuilder();

        NxActionConntrackBuilder nxActionConntrackBuilder = new NxActionConntrackBuilder();
        nxActionConntrackBuilder.setFlags(message.readUnsignedShort());
        nxActionConntrackBuilder.setZoneSrc(message.readUnsignedInt());
        nxActionConntrackBuilder.setConntrackZone(message.readUnsignedShort());
        nxActionConntrackBuilder.setRecircTable(message.readUnsignedByte());
        message.skipBytes(5);
        actionConntrackBuilder.setNxActionConntrack(nxActionConntrackBuilder.build());
        actionBuilder.setActionChoice(actionConntrackBuilder.build());

        return actionBuilder.build();
    }
}
