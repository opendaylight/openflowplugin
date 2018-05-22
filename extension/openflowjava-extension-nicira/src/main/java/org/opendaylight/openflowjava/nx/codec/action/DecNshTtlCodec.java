/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionDecNshTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionDecNshTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.dec.nsh.ttl.grouping.NxActionDecNshTtlBuilder;

public class DecNshTtlCodec extends AbstractActionCodec {

    private static final int LENGTH = 16;
    private static final byte NXAST_DEC_NSH_TTL_SUBTYPE = 48;
    public static final NiciraActionSerializerKey SERIALIZER_KEY =
            new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionDecNshTtl.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, NXAST_DEC_NSH_TTL_SUBTYPE);
    private static final int PADDING = 6;

    @Override
    public Action deserialize(ByteBuf message) {
        ActionBuilder actionBuilder = deserializeHeader(message);
        ActionDecNshTtlBuilder builder = new ActionDecNshTtlBuilder();
        NxActionDecNshTtlBuilder nxActionDecNshTtlBuilder = new NxActionDecNshTtlBuilder();
        message.skipBytes(PADDING);
        builder.setNxActionDecNshTtl(nxActionDecNshTtlBuilder.build());
        actionBuilder.setActionChoice(builder.build());
        return actionBuilder.build();
    }

    @Override
    public void serialize(Action input, ByteBuf outBuffer) {
        serializeHeader(LENGTH, NXAST_DEC_NSH_TTL_SUBTYPE, outBuffer);
        outBuffer.writeZero(PADDING);
    }
}
