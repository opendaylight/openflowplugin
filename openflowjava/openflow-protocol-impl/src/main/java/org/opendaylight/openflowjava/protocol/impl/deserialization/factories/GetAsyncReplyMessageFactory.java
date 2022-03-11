/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import com.google.common.collect.ImmutableSet;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowRemovedReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.FlowRemovedMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.FlowRemovedMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PacketInMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PacketInMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PortStatusMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PortStatusMaskBuilder;

/**
 * Translates GetAsyncReply messages.
 *
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class GetAsyncReplyMessageFactory implements OFDeserializer<GetAsyncOutput> {
    private static final byte SEPARATE_ROLES = 2;

    @Override
    public GetAsyncOutput deserialize(ByteBuf rawMessage) {
        return new GetAsyncOutputBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_3)
                .setXid(readUint32(rawMessage))
                .setPacketInMask(decodePacketInMask(rawMessage))
                .setPortStatusMask(decodePortStatusMask(rawMessage))
                .setFlowRemovedMask(decodeFlowRemovedMask(rawMessage))
                .build();
    }

    private static List<PacketInMask> decodePacketInMask(ByteBuf input) {
        List<PacketInMask> inMasks = new ArrayList<>();
        PacketInMaskBuilder maskBuilder;
        for (int i = 0; i < SEPARATE_ROLES; i++) {
            maskBuilder = new PacketInMaskBuilder();
            maskBuilder.setMask(decodePacketInReasons(input.readUnsignedInt()));
            inMasks.add(maskBuilder.build());
        }
        return inMasks;
    }

    private static List<PortStatusMask> decodePortStatusMask(ByteBuf input) {
        List<PortStatusMask> inMasks = new ArrayList<>();
        PortStatusMaskBuilder maskBuilder;
        for (int i = 0; i < SEPARATE_ROLES; i++) {
            maskBuilder = new PortStatusMaskBuilder();
            maskBuilder.setMask(decodePortReasons(input.readUnsignedInt()));
            inMasks.add(maskBuilder.build());
        }
        return inMasks;
    }

    private static List<FlowRemovedMask> decodeFlowRemovedMask(ByteBuf input) {
        List<FlowRemovedMask> inMasks = new ArrayList<>();
        FlowRemovedMaskBuilder maskBuilder;
        for (int i = 0; i < SEPARATE_ROLES; i++) {
            maskBuilder = new FlowRemovedMaskBuilder();
            maskBuilder.setMask(decodeFlowRemovedReasons(input.readUnsignedInt()));
            inMasks.add(maskBuilder.build());
        }
        return inMasks;
    }

    private static Set<PacketInReason> decodePacketInReasons(long input) {
        final var builder = ImmutableSet.<PacketInReason>builder();
        if ((input & 1 << 0) != 0) {
            builder.add(PacketInReason.OFPRNOMATCH);
        }
        if ((input & 1 << 1) != 0) {
            builder.add(PacketInReason.OFPRACTION);
        }
        if ((input & 1 << 2) != 0) {
            builder.add(PacketInReason.OFPRINVALIDTTL);
        }
        return builder.build();
    }

    private static Set<PortReason> decodePortReasons(long input) {
        final var builder = ImmutableSet.<PortReason>builder();
        if ((input & 1 << 0) != 0) {
            builder.add(PortReason.OFPPRADD);
        }
        if ((input & 1 << 1) != 0) {
            builder.add(PortReason.OFPPRDELETE);
        }
        if ((input & 1 << 2) != 0) {
            builder.add(PortReason.OFPPRMODIFY);
        }
        return builder.build();
    }

    private static Set<FlowRemovedReason> decodeFlowRemovedReasons(long input) {
        final var builder = ImmutableSet.<FlowRemovedReason>builder();
        if ((input & 1 << 0) != 0) {
            builder.add(FlowRemovedReason.OFPRRIDLETIMEOUT);
        }
        if ((input & 1 << 1) != 0) {
            builder.add(FlowRemovedReason.OFPRRHARDTIMEOUT);
        }
        if ((input & 1 << 2) != 0) {
            builder.add(FlowRemovedReason.OFPRRDELETE);
        }
        if ((input & 1 << 3) != 0) {
            builder.add(FlowRemovedReason.OFPRRGROUPDELETE);
        }
        return builder.build();
    }
}
