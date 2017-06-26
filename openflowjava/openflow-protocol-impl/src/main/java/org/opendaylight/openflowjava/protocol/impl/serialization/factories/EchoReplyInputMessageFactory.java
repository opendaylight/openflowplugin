/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInput;

/**
 * Translates EchoReply messages (both OpenFlow v1.0 and OpenFlow v1.3)
 * @author michal.polkorab
 * @author timotej.kubas
 */
public class EchoReplyInputMessageFactory implements OFSerializer<EchoReplyInput>{

    /** Code type of EchoReply message */
    private static final byte MESSAGE_TYPE = 3;

    @Override
    public void serialize(EchoReplyInput message, ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.OFHEADER_SIZE);
        byte[] data = message.getData();
        if (data != null) {
            outBuffer.writeBytes(data);
            ByteBufUtils.updateOFHeaderLength(outBuffer);
        }
    }

}
