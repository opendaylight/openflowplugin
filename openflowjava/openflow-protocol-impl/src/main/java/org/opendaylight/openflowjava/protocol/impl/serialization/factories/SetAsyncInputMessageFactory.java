/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.FlowRemovedMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PacketInMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PortStatusMask;
import org.opendaylight.yangtools.yang.binding.Enumeration;

/**
 * Translates SetAsync messages.
 *
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class SetAsyncInputMessageFactory implements OFSerializer<SetAsyncInput> {
    private static final byte MESSAGE_TYPE = 28;

    @Override
    public void serialize(final SetAsyncInput message, final ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        serializePacketInMask(message.getPacketInMask(), outBuffer);
        serializePortStatusMask(message.getPortStatusMask(), outBuffer);
        serializerFlowRemovedMask(message.getFlowRemovedMask(), outBuffer);
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

    private static void serializePacketInMask(final List<PacketInMask> packetInMask, final ByteBuf outBuffer) {
        if (packetInMask != null) {
            for (PacketInMask currentPacketMask : packetInMask) {
                serializeReasons(currentPacketMask.getMask(), outBuffer);
            }
        }
    }

    private static void serializePortStatusMask(final List<PortStatusMask> portStatusMask, final ByteBuf outBuffer) {
        if (portStatusMask != null) {
            for (PortStatusMask currentPortStatusMask : portStatusMask) {
                serializeReasons(currentPortStatusMask.getMask(), outBuffer);
            }
        }
    }

    private static void serializerFlowRemovedMask(final List<FlowRemovedMask> flowRemovedMask,
            final ByteBuf outBuffer) {
        if (flowRemovedMask != null) {
            for (FlowRemovedMask currentFlowRemovedMask : flowRemovedMask) {
                serializeReasons(currentFlowRemovedMask.getMask(), outBuffer);
            }
        }
    }

    private static void serializeReasons(final List<? extends Enumeration> reasons, final ByteBuf outBuffer) {
        if (reasons != null) {
            int bitmap = 0;
            for (Enumeration reason : reasons) {
                bitmap |= 1 << reason.getIntValue();
            }
            outBuffer.writeInt(bitmap);
        }
    }
}
