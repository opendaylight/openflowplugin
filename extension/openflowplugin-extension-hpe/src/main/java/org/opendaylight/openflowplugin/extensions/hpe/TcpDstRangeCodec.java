/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extensions.hpe;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeOfTcpDstRange;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.OfjAugHpeMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.OfjAugHpeMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.ofj.hpe.of.match.tcp.dst.range.grouping.TcpDstRangeValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.ofj.hpe.of.match.tcp.dst.range.grouping.TcpDstRangeValuesBuilder;

public class TcpDstRangeCodec extends HpeAbstractCodec {
    public TcpDstRangeCodec() {
        super((byte) 3, HpeOfTcpDstRange.class, (short) 8);
    }

    @Override
    protected void deserializePayload(ByteBuf message, OfjAugHpeMatchBuilder ofjAugHpeMatchBuilder) {
        TcpDstRangeValuesBuilder tcpDstRangeValuesBuilder = new TcpDstRangeValuesBuilder();
        tcpDstRangeValuesBuilder.setBegin(message.readUnsignedShort());
        tcpDstRangeValuesBuilder.setEnd(message.readUnsignedShort());
        ofjAugHpeMatchBuilder.setTcpDstRangeValues(tcpDstRangeValuesBuilder.build());
    }

    @Override
    protected void serializePayload(ByteBuf outBuffer, OfjAugHpeMatch ofjAugHpeMatch) {
        TcpDstRangeValues tcpDstRangeValues = ofjAugHpeMatch.getTcpDstRangeValues();
        outBuffer.writeShort(tcpDstRangeValues.getBegin());
        outBuffer.writeShort(tcpDstRangeValues.getEnd());
    }
}
