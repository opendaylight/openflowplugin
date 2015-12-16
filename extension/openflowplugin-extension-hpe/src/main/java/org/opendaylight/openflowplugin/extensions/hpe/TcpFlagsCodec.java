/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extensions.hpe;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeOfTcpFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.OfjAugHpeMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.OfjAugHpeMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.ofj.hpe.of.match.tcp.flags.grouping.TcpFlagsValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.ofj.hpe.of.match.tcp.flags.grouping.TcpFlagsValuesBuilder;

public class TcpFlagsCodec extends HpeAbstractCodec {
    public TcpFlagsCodec() {
        super((byte) 4, HpeOfTcpFlags.class, (short) 8);
    }

    @Override
    protected void deserializePayload(ByteBuf message, OfjAugHpeMatchBuilder ofjAugHpeMatchBuilder) {
        TcpFlagsValuesBuilder tcpFlagsValuesBuilder = new TcpFlagsValuesBuilder();
        tcpFlagsValuesBuilder.setFlags(message.readUnsignedShort());
        tcpFlagsValuesBuilder.setMask(message.readUnsignedShort());
        ofjAugHpeMatchBuilder.setTcpFlagsValues(tcpFlagsValuesBuilder.build());
    }

    @Override
    protected void serializePayload(ByteBuf outBuffer, OfjAugHpeMatch ofjAugHpeMatch) {
        TcpFlagsValues tcpFlagsValues = ofjAugHpeMatch.getTcpFlagsValues();
        outBuffer.writeShort(tcpFlagsValues.getFlags());
        outBuffer.writeShort(tcpFlagsValues.getMask());
    }
}
