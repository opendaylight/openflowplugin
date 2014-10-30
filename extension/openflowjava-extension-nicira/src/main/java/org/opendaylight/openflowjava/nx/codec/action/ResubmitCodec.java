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
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.openflowjava.nx.codec.action.AbstractActionCodec;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.NxmNxResubmit;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.OfjAugNxAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.OfjAugNxActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.resubmit.grouping.ActionResubmit;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.resubmit.grouping.ActionResubmitBuilder;

/**
 * Codec for the NX_RESUBMIT and NX_RESUBMIT_TABLE
 */
public class ResubmitCodec extends AbstractActionCodec {
    public static final int LENGTH = 16;
    public static final byte NXAST_RESUBMIT_SUBTYPE = 1;
    public static final byte NXAST_RESUBMIT_TABLE_SUBTYPE = 14;
    public static final NiciraActionSerializerKey SERIALIZER_KEY =
            new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, NxmNxResubmit.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, NXAST_RESUBMIT_SUBTYPE);
    public static final NiciraActionDeserializerKey TABLE_DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, NXAST_RESUBMIT_TABLE_SUBTYPE);

    private static final byte OFP_TABLE_ALL = (byte)255;
    private static final short OFP_IN_PORT = (short)0xfff8;
    private static final int padding = 3; // nx_action_resubmit : uint8_t pad[3];

    public byte getSubType(ActionResubmit action) {
        if ((action.getTable() == null) || (action.getTable().byteValue() == OFP_TABLE_ALL)) return NXAST_RESUBMIT_SUBTYPE;
        return NXAST_RESUBMIT_TABLE_SUBTYPE;
    }

    @Override
    public void serialize(Action input, ByteBuf outBuffer) {
        byte table = OFP_TABLE_ALL;
        short inPort = OFP_IN_PORT;

        ActionResubmit action = input.getAugmentation(OfjAugNxAction.class).getActionResubmit();
        serializeHeader(LENGTH, getSubType(action), outBuffer);

        if (action.getInPort() != null) inPort = action.getInPort().shortValue();
        if (action.getTable() != null) table = action.getTable().byteValue();
        outBuffer.writeShort(inPort);
        outBuffer.writeByte(table);
        outBuffer.writeZero(padding);
    }

    @Override
    public Action deserialize(ByteBuf message) {
        ActionBuilder actionBuilder = deserializeHeader(message);
        ActionResubmitBuilder builder = new ActionResubmitBuilder();
        builder.setInPort(message.readUnsignedShort());
        builder.setTable(message.readUnsignedByte());
        message.skipBytes(padding);
        OfjAugNxActionBuilder augNxActionBuilder = new OfjAugNxActionBuilder();
        augNxActionBuilder.setActionResubmit(builder.build());
        actionBuilder.addAugmentation(ExperimenterIdAction.class,
                                      createExperimenterIdAction(NxmNxResubmit.class));
        actionBuilder.addAugmentation(OfjAugNxAction.class, augNxActionBuilder.build());
        return actionBuilder.build();
    }

}
