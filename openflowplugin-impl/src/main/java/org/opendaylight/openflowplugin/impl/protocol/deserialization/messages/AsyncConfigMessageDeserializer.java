/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.messages;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.AsyncConfigMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.AsyncConfigMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.FlowRemovedMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.PacketInMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.PortStatusMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.async.config.FlowRemovedMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.async.config.PacketInMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.async.config.PortStatusMaskBuilder;

public class AsyncConfigMessageDeserializer implements OFDeserializer<AsyncConfigMessage> {
    @Override
    public AsyncConfigMessage deserialize(final ByteBuf message) {
        return new AsyncConfigMessageBuilder()
            .setVersion(EncodeConstants.OF_VERSION_1_3)
            .setXid(readUint32(message))
            .setPacketInMask(new PacketInMaskBuilder()
                .setMasterMask(deserializePacketInMask(message))
                .setSlaveMask(deserializePacketInMask(message))
                .build())
            .setPortStatusMask(new PortStatusMaskBuilder()
                .setMasterMask(deserializePortStatusMask(message))
                .setSlaveMask(deserializePortStatusMask(message))
                .build())
            .setFlowRemovedMask(new FlowRemovedMaskBuilder()
                .setMasterMask(deserializeFlowRemovedMask(message))
                .setSlaveMask(deserializeFlowRemovedMask(message))
                .build())
            .build();
    }

    private static PacketInMask deserializePacketInMask(final ByteBuf byteBuf) {
        final long mask = byteBuf.readUnsignedInt();
        final boolean isNoMatch = (mask & 1) != 0;
        final boolean isAction = (mask & 1 << 1) != 0;
        final boolean isInvalidTtl = (mask & 1 << 2) != 0;
        return new PacketInMask(isAction, isInvalidTtl, isNoMatch);
    }

    private static PortStatusMask deserializePortStatusMask(final ByteBuf byteBuf) {
        final long mask = byteBuf.readUnsignedInt();
        final boolean isAdd = (mask & 1) != 0;
        final boolean isDelete = (mask & 1 << 1) != 0;
        final boolean isModify = (mask & 1 << 2) != 0;
        return new PortStatusMask(isAdd, isDelete, isModify);
    }

    private static FlowRemovedMask deserializeFlowRemovedMask(final ByteBuf byteBuf) {
        final long mask = byteBuf.readUnsignedInt();
        final boolean isIdleTimeout = (mask & 1) != 0;
        final boolean isHardTimeout = (mask & 1 << 1) != 0;
        final boolean isDelete = (mask & 1 << 2) != 0;
        final boolean isGroupDelete = (mask & 1 << 3) != 0;
        return new FlowRemovedMask(isDelete, isGroupDelete, isHardTimeout, isIdleTimeout);
    }
}