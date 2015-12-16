/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extensions.hpe;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeOfUdpSrcRange;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.OfjAugHpeMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.OfjAugHpeMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.ofj.hpe.of.match.udp.src.range.grouping.UdpSrcRangeValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.ofj.hpe.of.match.udp.src.range.grouping.UdpSrcRangeValuesBuilder;

public class UdpSrcRangeCodec extends HpeAbstractCodec {
    public UdpSrcRangeCodec() {
        super((byte) 0, HpeOfUdpSrcRange.class, (short) 8);
    }

    @Override
    protected void deserializePayload(ByteBuf message, OfjAugHpeMatchBuilder ofjAugHpeMatchBuilder) {
        UdpSrcRangeValuesBuilder udpSrcRangeValuesBuilder = new UdpSrcRangeValuesBuilder();
        udpSrcRangeValuesBuilder.setBegin(message.readUnsignedShort());
        udpSrcRangeValuesBuilder.setEnd(message.readUnsignedShort());
        ofjAugHpeMatchBuilder.setUdpSrcRangeValues(udpSrcRangeValuesBuilder.build());
    }

    @Override
    protected void serializePayload(ByteBuf outBuffer, OfjAugHpeMatch ofjAugHpeMatch) {
        UdpSrcRangeValues udpSrcRangeValues = ofjAugHpeMatch.getUdpSrcRangeValues();
        outBuffer.writeShort(udpSrcRangeValues.getBegin());
        outBuffer.writeShort(udpSrcRangeValues.getEnd());
    }
}
