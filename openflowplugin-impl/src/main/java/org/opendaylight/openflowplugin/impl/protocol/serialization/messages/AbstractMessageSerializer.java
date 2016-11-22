/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.messages;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

public abstract class AbstractMessageSerializer<T extends OfHeader> implements OFSerializer<T>, HeaderSerializer<T> {

    @Override
    public void serialize(T message, ByteBuf outBuffer) {
        serializeHeader(message, outBuffer);
    }

    @Override
    public void serializeHeader(T message, ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(getMessageType(), message, outBuffer, EncodeConstants.EMPTY_LENGTH);
    }

    /**
     * @return of message type
     */
    protected abstract byte getMessageType();
}
