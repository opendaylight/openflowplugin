/*
 * Copyright © 2016 HPE Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionFinTimeout;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionFinTimeoutBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.fin.timeout.grouping.NxActionFinTimeoutBuilder;

/**
 * Codec for the NX_FIN_TIMEOUT
 */
public class FinTimeoutCodec extends AbstractActionCodec {

    public static final int LENGTH = 16;
    public static final byte NXAST_FIN_TIMEOUT_SUBTYPE = 19;
    public static final NiciraActionSerializerKey SERIALIZER_KEY =
            new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionFinTimeout.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, NXAST_FIN_TIMEOUT_SUBTYPE);

    private static final short OFP_NO_TIMEOUT = (short) 0;
    private static final int PADDING = 2; // nx_action_fin_timeout : uint8_t pad[2];

    @Override
    public void serialize(final Action input, final ByteBuf outBuffer) {
        short idleTimeOut = OFP_NO_TIMEOUT;
        short hardTimeOut = OFP_NO_TIMEOUT;

        ActionFinTimeout action = ((ActionFinTimeout) input.getActionChoice());
        serializeHeader(LENGTH, NXAST_FIN_TIMEOUT_SUBTYPE, outBuffer);

        if (action.getNxActionFinTimeout().getFinIdleTimeout() != null) {
            idleTimeOut = action.getNxActionFinTimeout().getFinIdleTimeout().shortValue();
        }
        if (action.getNxActionFinTimeout().getFinHardTimeout() != null) {
            hardTimeOut = action.getNxActionFinTimeout().getFinHardTimeout().shortValue();
        }
        outBuffer.writeShort(idleTimeOut);
        outBuffer.writeShort(hardTimeOut);
        outBuffer.writeZero(PADDING);
    }

    @Override
    public Action deserialize(final ByteBuf message) {
        ActionBuilder actionBuilder = deserializeHeader(message);

        ActionFinTimeoutBuilder builder = new ActionFinTimeoutBuilder();
        NxActionFinTimeoutBuilder nxActionFinTimeoutBuilder = new NxActionFinTimeoutBuilder();
        nxActionFinTimeoutBuilder.setFinIdleTimeout(message.readUnsignedShort());
        nxActionFinTimeoutBuilder.setFinHardTimeout(message.readUnsignedShort());
        builder.setNxActionFinTimeout(nxActionFinTimeoutBuilder.build());
        message.skipBytes(PADDING);

        actionBuilder.setActionChoice(builder.build());
        return actionBuilder.build();
    }

}
