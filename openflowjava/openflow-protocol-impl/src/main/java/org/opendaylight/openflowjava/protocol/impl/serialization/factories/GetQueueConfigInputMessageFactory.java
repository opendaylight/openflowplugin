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
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigInput;

/**
 * Translates QueueGetConfigRequest messages
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class GetQueueConfigInputMessageFactory implements OFSerializer<GetQueueConfigInput> {

    private static final byte MESSAGE_TYPE = 22;
    private static final byte PADDING_IN_GET_QUEUE_CONFIG_MESSAGE = 4;

    @Override
    public void serialize(GetQueueConfigInput message, ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeInt(message.getPort().getValue().intValue());
        outBuffer.writeZero(PADDING_IN_GET_QUEUE_CONFIG_MESSAGE);
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

}
