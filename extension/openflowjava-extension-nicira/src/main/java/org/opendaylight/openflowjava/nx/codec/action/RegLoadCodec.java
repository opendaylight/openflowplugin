/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.nx.codec.action;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import org.opendaylight.openflowjava.nx.api.NiciraActionDeserializerKey;
import org.opendaylight.openflowjava.nx.api.NiciraActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegLoad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegLoadBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.load.grouping.NxActionRegLoadBuilder;

public class RegLoadCodec extends AbstractActionCodec {

    public static final int LENGTH = 24;
    public static final byte SUBTYPE = 7; // NXAST_REG_LOAD
    public static final NiciraActionSerializerKey SERIALIZER_KEY = new NiciraActionSerializerKey(
            EncodeConstants.OF13_VERSION_ID, ActionRegLoad.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY = new NiciraActionDeserializerKey(
            EncodeConstants.OF13_VERSION_ID, SUBTYPE);

    @Override
    public void serialize(final Action input, final ByteBuf outBuffer) {
        ActionRegLoad actionRegLoad = ((ActionRegLoad) input.getActionChoice());
        serializeHeader(LENGTH, SUBTYPE, outBuffer);
        outBuffer.writeShort(actionRegLoad.getNxActionRegLoad().getOfsNbits());
        outBuffer.writeInt(actionRegLoad.getNxActionRegLoad().getDst().intValue());
        outBuffer.writeLong(actionRegLoad.getNxActionRegLoad().getValue().longValue());
    }

    @Override
    public Action deserialize(final ByteBuf message) {
        ActionBuilder actionBuilder = deserializeHeader(message);
        NxActionRegLoadBuilder nxActionRegLoadBuilder = new NxActionRegLoadBuilder();
        ActionRegLoadBuilder actionRegLoadBuilder = new ActionRegLoadBuilder();
        nxActionRegLoadBuilder.setOfsNbits(message.readUnsignedShort());
        nxActionRegLoadBuilder.setDst(message.readUnsignedInt());
        nxActionRegLoadBuilder.setValue(BigInteger.valueOf(message.readLong()));
        actionRegLoadBuilder.setNxActionRegLoad(nxActionRegLoadBuilder.build());
        actionBuilder.setActionChoice(actionRegLoadBuilder.build());
        return actionBuilder.build();
    }

}
