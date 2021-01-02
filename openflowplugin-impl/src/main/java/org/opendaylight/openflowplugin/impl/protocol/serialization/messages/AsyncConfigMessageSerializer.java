/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.messages;

import io.netty.buffer.ByteBuf;
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

        final var packetIn = message.getPacketInMask();
        if (packetIn != null) {
            serializePacketInMask(packetIn.getMasterMask(), outBuffer);
            serializePacketInMask(packetIn.getSlaveMask(), outBuffer);
        }
        final var portStatus = message.getPortStatusMask();
        if (portStatus != null) {
            serializePortStatusMask(portStatus.getMasterMask(), outBuffer);
            serializePortStatusMask(portStatus.getSlaveMask(), outBuffer);
        }
        final var flowRemoved = message.getFlowRemovedMask();
        if (flowRemoved != null) {
            serializeFlowRemovedMask(flowRemoved.getMasterMask(), outBuffer);
            serializeFlowRemovedMask(flowRemoved.getSlaveMask(), outBuffer);
        }

        outBuffer.setShort(index + 2, outBuffer.writerIndex() - index);
    }

    @Override
    protected byte getMessageType() {
        return 28;
    }

    private static void serializePacketInMask(final PacketInMask mask, final ByteBuf outBuffer) {
        if (mask != null) {
            int bitmap = 0;
            if (mask.getNOMATCH()) {
                bitmap |= 1 << PacketInReason.OFPRNOMATCH.getIntValue();
            }
            if (mask.getACTION()) {
                bitmap |= 1 << PacketInReason.OFPRACTION.getIntValue();
            }
            if (mask.getINVALIDTTL()) {
                bitmap |= 1 << PacketInReason.OFPRINVALIDTTL.getIntValue();
            }
            outBuffer.writeInt(bitmap);
        }
    }

    private static void serializePortStatusMask(final PortStatusMask mask, final ByteBuf outBuffer) {
        if (mask != null) {
            int bitmap = 0;
            if (mask.getADD()) {
                bitmap |= 1 << PortReason.OFPPRADD.getIntValue();
            }
            if (mask.getDELETE()) {
                bitmap |= 1 << PortReason.OFPPRDELETE.getIntValue();
            }
            if (mask.getUPDATE()) {
                bitmap |= 1 << PortReason.OFPPRMODIFY.getIntValue();
            }
            outBuffer.writeInt(bitmap);
        }
    }

    private static void serializeFlowRemovedMask(final FlowRemovedMask mask, final ByteBuf outBuffer) {
        if (mask != null) {
            int bitmap = 0;
            if (mask.getIDLETIMEOUT()) {
                bitmap |= 1 << FlowRemovedReason.OFPRRIDLETIMEOUT.getIntValue();
            }
            if (mask.getHARDTIMEOUT()) {
                bitmap |= 1 << FlowRemovedReason.OFPRRHARDTIMEOUT.getIntValue();
            }
            if (mask.getDELETE()) {
                bitmap |= 1 << FlowRemovedReason.OFPRRDELETE.getIntValue();
            }
            if (mask.getGROUPDELETE()) {
                bitmap |= 1 << FlowRemovedReason.OFPRRGROUPDELETE.getIntValue();
            }
            outBuffer.writeInt(bitmap);
        }
    }
}