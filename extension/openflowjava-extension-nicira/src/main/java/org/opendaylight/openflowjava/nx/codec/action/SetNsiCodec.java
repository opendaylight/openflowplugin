/*
 * Copyright (C) 2014 Red Hat, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Authors : Madhu Venugopal
 */

package org.opendaylight.openflowjava.nx.codec.action;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.nx.api.NiciraActionDeserializerKey;
import org.opendaylight.openflowjava.nx.api.NiciraActionSerializerKey;
import org.opendaylight.openflowjava.nx.codec.action.AbstractActionCodec;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ExperimenterIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.NxmNxSetNsi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.OfjAugNxAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.OfjAugNxActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.set.nsi.grouping.ActionSetNsi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.set.nsi.grouping.ActionSetNsiBuilder;

/**
 * Codec for the NX_SetNsi and NX_SetNsi_TABLE
 */
public class SetNsiCodec extends AbstractActionCodec {
    public static final int LENGTH = 16;
    public static final byte NXAST_SET_NSI_SUBTYPE = 33;
    public static final NiciraActionSerializerKey SERIALIZER_KEY =
            new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, NxmNxSetNsi.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, NXAST_SET_NSI_SUBTYPE);
    private static final int padding = 5; // nx_action_SetNsi : uint8_t pad[3];

    @Override
    public void serialize(Action input, ByteBuf outBuffer) {
        ActionSetNsi action = input.getAugmentation(OfjAugNxAction.class).getActionSetNsi();
        serializeHeader(LENGTH, NXAST_SET_NSI_SUBTYPE, outBuffer);
        outBuffer.writeByte(action.getNsi().byteValue());
        outBuffer.writeZero(padding);
    }

    @Override
    public Action deserialize(ByteBuf message) {
        ActionBuilder actionBuilder = deserializeHeader(message);
        ActionSetNsiBuilder builder = new ActionSetNsiBuilder();
        builder.setNsi(message.readUnsignedByte());
        message.skipBytes(padding);

        OfjAugNxActionBuilder augNxActionBuilder = new OfjAugNxActionBuilder();
        augNxActionBuilder.setActionSetNsi(builder.build());
        actionBuilder.addAugmentation(ExperimenterIdAction.class,
                                      createExperimenterIdAction(NxmNxSetNsi.class));
        actionBuilder.addAugmentation(OfjAugNxAction.class, augNxActionBuilder.build());
        return actionBuilder.build();
    }

}
