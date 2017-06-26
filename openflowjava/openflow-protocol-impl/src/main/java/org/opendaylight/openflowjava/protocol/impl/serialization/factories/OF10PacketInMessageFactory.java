/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public class OF10PacketInMessageFactory implements OFSerializer<PacketInMessage> {
    private static final byte MESSAGE_TYPE = 10;
    private static final byte PADDING = 1;

    @Override
    public void serialize(PacketInMessage message, ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeInt(message.getBufferId().intValue());
        outBuffer.writeShort(message.getTotalLen().intValue());
        outBuffer.writeShort(message.getInPort());
        outBuffer.writeByte(message.getReason().getIntValue());
        outBuffer.writeZero(PADDING);
        byte[] data = message.getData();

        if (data != null) {
            outBuffer.writeBytes(data);
        }
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

}
