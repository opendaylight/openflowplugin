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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutputBuilder;

/**
 * Translates BarrierReply messages.
 * OF protocol versions: 1.0, 1.3, 1.4, 1.5.
 * @author michal.polkorab
 * @author timotej.kubas
 */
public class BarrierReplyMessageFactory extends VersionAssignableFactory<BarrierOutput> {
    @Override
    public BarrierOutput deserialize(final ByteBuf rawMessage) {
        return new BarrierOutputBuilder()
                .setVersion(getVersion())
                .setXid(readUint32(rawMessage))
                .build();
    }
}
