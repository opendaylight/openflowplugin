/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.impl.util.VersionAssignableFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoOutputBuilder;

/**
 * Translates EchoReply messages.
 * OpenFlow protocol versions: 1.0, 1.3, 1.4, 1.5.
 * @author michal.polkorab
 * @author timotej.kubas
 */
public class EchoReplyMessageFactory extends VersionAssignableFactory<EchoOutput> {
    @Override
    public EchoOutput deserialize(final ByteBuf rawMessage) {
        EchoOutputBuilder builder = new EchoOutputBuilder();
        builder.setVersion(getVersion());
        builder.setXid(readUint32(rawMessage));
        int remainingBytes = rawMessage.readableBytes();
        if (remainingBytes > 0) {
            byte[] data = new byte[remainingBytes];
            rawMessage.readBytes(data);
            builder.setData(data);
        }
        return builder.build();
    }
}
