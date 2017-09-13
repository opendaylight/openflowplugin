/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.messages;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.common.types.rev170913.RawMessage;

public class RawMessageSerializer extends AbstractMessageSerializer<RawMessage> {
    @Override
    public void serializeHeader(final RawMessage message, final ByteBuf outBuffer) {
        outBuffer.writeBytes(message.getPayload());
    }

    @Override
    protected byte getMessageType() {
        return -1;
    }
}