/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessageBuilder;

/**
 * Translates Hello messages (OpenFlow v1.0).
 *
 * @author michal.polkorab
 */
public class OF10HelloMessageFactory implements OFDeserializer<HelloMessage> {

    @Override
    public HelloMessage deserialize(ByteBuf rawMessage) {
        HelloMessageBuilder builder = new HelloMessageBuilder();
        builder.setVersion((short) EncodeConstants.OF10_VERSION_ID);
        builder.setXid(rawMessage.readUnsignedInt());
        if (rawMessage.readableBytes() > 0) {
            rawMessage.skipBytes(rawMessage.readableBytes());
        }
        return builder.build();
    }

}
