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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionSetNsi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionSetNsiBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.set.nsi.grouping.NxActionSetNsiBuilder;

/**
 * Codec for the NX_SetNsi and NX_SetNsi_TABLE
 */
public class SetNsiCodec extends AbstractActionCodec {
    public static final int LENGTH = 16;
    public static final byte NXAST_SET_NSI_SUBTYPE = 33;
    public static final NiciraActionSerializerKey SERIALIZER_KEY =
            new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionSetNsi.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, NXAST_SET_NSI_SUBTYPE);
    private static final int padding = 5; // nx_action_SetNsi : uint8_t pad[3];

    @Override
    public void serialize(final Action input, final ByteBuf outBuffer) {
        ActionSetNsi action = ((ActionSetNsi) input.getActionChoice());
        serializeHeader(LENGTH, NXAST_SET_NSI_SUBTYPE, outBuffer);
        outBuffer.writeByte(action.getNxActionSetNsi().getNsi().byteValue());
        outBuffer.writeZero(padding);
    }

    @Override
    public Action deserialize(final ByteBuf message) {
        ActionBuilder actionBuilder = deserializeHeader(message);
        ActionSetNsiBuilder builder = new ActionSetNsiBuilder();
        NxActionSetNsiBuilder nxActionSetNsiBuilder = new NxActionSetNsiBuilder();
        nxActionSetNsiBuilder.setNsi(message.readUnsignedByte());
        message.skipBytes(padding);

        builder.setNxActionSetNsi(nxActionSetNsiBuilder.build());
        actionBuilder.setActionChoice(builder.build());
        return actionBuilder.build();
    }

}
