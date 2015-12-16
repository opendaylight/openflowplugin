/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extensions.hpe;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeOfTcpSrcRange;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.OfjAugHpeMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.OfjAugHpeMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.ofj.hpe.of.match.tcp.src.range.grouping.TcpSrcRangeValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.ofj.hpe.of.match.tcp.src.range.grouping.TcpSrcRangeValuesBuilder;

public class TcpSrcRangeCodec extends HpeAbstractCodec {
    public TcpSrcRangeCodec() {
        super((byte) 2, HpeOfTcpSrcRange.class, (short) 8);
    }

    @Override
    protected void deserializePayload(ByteBuf message, OfjAugHpeMatchBuilder ofjAugHpeMatchBuilder) {
        TcpSrcRangeValuesBuilder tcpSrcRangeValuesBuilder = new TcpSrcRangeValuesBuilder();
        tcpSrcRangeValuesBuilder.setBegin(message.readUnsignedShort());
        tcpSrcRangeValuesBuilder.setEnd(message.readUnsignedShort());
        ofjAugHpeMatchBuilder.setTcpSrcRangeValues(tcpSrcRangeValuesBuilder.build());
    }

    @Override
    protected void serializePayload(ByteBuf outBuffer, OfjAugHpeMatch ofjAugHpeMatch) {
        TcpSrcRangeValues tcpSrcRangeValues = ofjAugHpeMatch.getTcpSrcRangeValues();
        outBuffer.writeShort(tcpSrcRangeValues.getBegin());
        outBuffer.writeShort(tcpSrcRangeValues.getEnd());
    }
}
