/*
 * Copyright (C) 2018 Redhat, Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionCtClear;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionCtClearBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.ct.clear.grouping.NxActionCtClearBuilder;

/**
 * Codec for the ct_Clear.
 */
public class CtClearCodec extends AbstractActionCodec {
    private static final int LENGTH = 16;
    private static final byte NX_CT_CLEAR_SUBTYPE = 43;
    public static final NiciraActionSerializerKey SERIALIZER_KEY =
            new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionCtClear.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, NX_CT_CLEAR_SUBTYPE);
    private static final int PADDING = 6;

    @Override
    public void serialize(Action input, ByteBuf outBuffer) {
        serializeHeader(LENGTH, NX_CT_CLEAR_SUBTYPE, outBuffer);
        outBuffer.writeZero(PADDING);
    }

    @Override
    public Action deserialize(ByteBuf message) {
        ActionBuilder actionBuilder = deserializeHeader(message);
        ActionCtClearBuilder builder = new ActionCtClearBuilder();
        NxActionCtClearBuilder nxActionCtClearBuilder = new NxActionCtClearBuilder();
        message.skipBytes(PADDING);
        builder.setNxActionCtClear(nxActionCtClearBuilder.build());
        actionBuilder.setActionChoice(builder.build());
        return actionBuilder.build();
    }
}
