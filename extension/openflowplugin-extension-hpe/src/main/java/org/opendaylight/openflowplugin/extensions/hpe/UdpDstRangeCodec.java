/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extensions.hpe;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeOfUdpDstRange;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.OfjAugHpeMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.OfjAugHpeMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.ofj.hpe.of.match.udp.dst.range.grouping.UdpDstRangeValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.ofj.hpe.of.match.udp.dst.range.grouping.UdpDstRangeValuesBuilder;

public class UdpDstRangeCodec extends HpeAbstractCodec {
    public UdpDstRangeCodec() {
        super((byte) 1, HpeOfUdpDstRange.class, (short) 8);
    }

    @Override
    protected void deserializePayload(ByteBuf message, OfjAugHpeMatchBuilder ofjAugHpeMatchBuilder) {
        UdpDstRangeValuesBuilder udpDstRangeValuesBuilder = new UdpDstRangeValuesBuilder();
        udpDstRangeValuesBuilder.setBegin(message.readUnsignedShort());
        udpDstRangeValuesBuilder.setEnd(message.readUnsignedShort());
        ofjAugHpeMatchBuilder.setUdpDstRangeValues(udpDstRangeValuesBuilder.build());
    }

    @Override
    protected void serializePayload(ByteBuf outBuffer, OfjAugHpeMatch ofjAugHpeMatch) {
        UdpDstRangeValues udpDstRangeValues = ofjAugHpeMatch.getUdpDstRangeValues();
        outBuffer.writeShort(udpDstRangeValues.getBegin());
        outBuffer.writeShort(udpDstRangeValues.getEnd());
    }
}
