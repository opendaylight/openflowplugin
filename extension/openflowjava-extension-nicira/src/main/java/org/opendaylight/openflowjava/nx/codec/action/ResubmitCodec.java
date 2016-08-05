/*
 * Copyright (c) 2014, 2015 Red Hat, Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionResubmit;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionResubmitBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.resubmit.grouping.NxActionResubmitBuilder;

/**
 * Codec for the NX_RESUBMIT and NX_RESUBMIT_TABLE
 */
public class ResubmitCodec extends AbstractActionCodec {

    public static final int LENGTH = 16;
    public static final byte NXAST_RESUBMIT_SUBTYPE = 1;
    public static final byte NXAST_RESUBMIT_TABLE_SUBTYPE = 14;
    public static final NiciraActionSerializerKey SERIALIZER_KEY =
            new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionResubmit.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, NXAST_RESUBMIT_SUBTYPE);
    public static final NiciraActionDeserializerKey TABLE_DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, NXAST_RESUBMIT_TABLE_SUBTYPE);

    private static final byte OFP_TABLE_ALL = (byte) 255;
    private static final short OFP_IN_PORT = (short) 0xfff8;
    private static final int padding = 3; // nx_action_resubmit : uint8_t pad[3];

    public byte getSubType(final ActionResubmit action) {
        if ((action.getNxActionResubmit().getTable() == null) || (action.getNxActionResubmit().getTable().byteValue() == OFP_TABLE_ALL)) {
            return NXAST_RESUBMIT_SUBTYPE;
        }
        return NXAST_RESUBMIT_TABLE_SUBTYPE;
    }

    @Override
    public void serialize(final Action input, final ByteBuf outBuffer) {
        byte table = OFP_TABLE_ALL;
        short inPort = OFP_IN_PORT;

        ActionResubmit action = ((ActionResubmit) input.getActionChoice());
        serializeHeader(LENGTH, getSubType(action), outBuffer);

        if (action.getNxActionResubmit().getInPort() != null) {
            inPort = action.getNxActionResubmit().getInPort().shortValue();
        }
        if (action.getNxActionResubmit().getTable() != null) {
            table = action.getNxActionResubmit().getTable().byteValue();
        }
        outBuffer.writeShort(inPort);
        outBuffer.writeByte(table);
        outBuffer.writeZero(padding);
    }

    @Override
    public Action deserialize(final ByteBuf message) {
        ActionBuilder actionBuilder = deserializeHeader(message);

        ActionResubmitBuilder builder = new ActionResubmitBuilder();
        NxActionResubmitBuilder nxActionResubmitBuilder = new NxActionResubmitBuilder();
        nxActionResubmitBuilder.setInPort(message.readUnsignedShort());
        nxActionResubmitBuilder.setTable(message.readUnsignedByte());
        builder.setNxActionResubmit(nxActionResubmitBuilder.build());
        message.skipBytes(padding);

        actionBuilder.setActionChoice(builder.build());
        return actionBuilder.build();
    }

}
