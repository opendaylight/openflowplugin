/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.messages;

import io.netty.buffer.ByteBuf;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.AsyncConfigMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.FlowRemovedMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.PacketInMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.PortStatusMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowRemovedReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;

public class AsyncConfigMessageSerializer extends AbstractMessageSerializer<AsyncConfigMessage> {
    @Override
    public void serialize(final AsyncConfigMessage message, final ByteBuf outBuffer) {
        final int index = outBuffer.writerIndex();
        super.serialize(message, outBuffer);

        Optional.ofNullable(message.getPacketInMask())
                .ifPresent(mask -> {
                    serializePacketInMask(mask.getMasterMask(), outBuffer);
                    serializePacketInMask(mask.getSlaveMask(), outBuffer);
                });

        Optional.ofNullable(message.getPortStatusMask())
                .ifPresent(mask -> {
                    serializePortStatusMask(mask.getMasterMask(), outBuffer);
                    serializePortStatusMask(mask.getSlaveMask(), outBuffer);
                });

        Optional.ofNullable(message.getFlowRemovedMask())
                .ifPresent(mask -> {
                    serializeFlowRemovedMask(mask.getMasterMask(), outBuffer);
                    serializeFlowRemovedMask(mask.getSlaveMask(), outBuffer);
                });

        outBuffer.setShort(index + 2, outBuffer.writerIndex() - index);
    }

    @Override
    protected byte getMessageType() {
        return 28;
    }

    private static void serializePacketInMask(final PacketInMask mask, final ByteBuf outBuffer) {
        if (mask == null) {
            return;
        }

        final Map<Integer, Boolean> map = new WeakHashMap<>();

        if (mask.getNOMATCH()) {
            map.put(PacketInReason.OFPRNOMATCH.getIntValue(), true);
        }

        if (mask.getACTION()) {
            map.put(PacketInReason.OFPRACTION.getIntValue(), true);
        }

        if (mask.getINVALIDTTL()) {
            map.put(PacketInReason.OFPRINVALIDTTL.getIntValue(), true);
        }

        outBuffer.writeInt(ByteBufUtils.fillBitMaskFromMap(map));
    }

    private static void serializePortStatusMask(final PortStatusMask mask, final ByteBuf outBuffer) {
        if (mask == null) {
            return;
        }

        final Map<Integer, Boolean> map = new WeakHashMap<>();

        if (mask.getADD()) {
            map.put(PortReason.OFPPRADD.getIntValue(), true);
        }

        if (mask.getDELETE()) {
            map.put(PortReason.OFPPRDELETE.getIntValue(), true);
        }

        if (mask.getUPDATE()) {
            map.put(PortReason.OFPPRMODIFY.getIntValue(), true);
        }

        outBuffer.writeInt(ByteBufUtils.fillBitMaskFromMap(map));
    }

    private static void serializeFlowRemovedMask(final FlowRemovedMask mask, final ByteBuf outBuffer) {
        if (mask == null) {
            return;
        }

        final Map<Integer, Boolean> map = new WeakHashMap<>();

        if (mask.getIDLETIMEOUT()) {
            map.put(FlowRemovedReason.OFPRRIDLETIMEOUT.getIntValue(), true);
        }

        if (mask.getHARDTIMEOUT()) {
            map.put(FlowRemovedReason.OFPRRHARDTIMEOUT.getIntValue(), true);
        }

        if (mask.getDELETE()) {
            map.put(FlowRemovedReason.OFPRRDELETE.getIntValue(), true);
        }

        if (mask.getGROUPDELETE()) {
            map.put(FlowRemovedReason.OFPRRGROUPDELETE.getIntValue(), true);
        }

        outBuffer.writeInt(ByteBufUtils.fillBitMaskFromMap(map));
    }
}