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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutput;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public class RoleReplyMessageFactory implements OFSerializer<RoleRequestOutput> {
    private static final byte MESSAGE_TYPE = 25;
    private static final byte PADDING = 4;

    @Override
    public void serialize(RoleRequestOutput message, ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeInt(message.getRole().getIntValue());
        outBuffer.writeZero(PADDING);
        outBuffer.writeLong(message.getGenerationId().longValue());
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

}
