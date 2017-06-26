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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInput;

/**
 * Translates RoleRequest messages
 * @author michal.polkorab
 * @author timotej.kubas
 */
public class RoleRequestInputMessageFactory implements OFSerializer<RoleRequestInput> {

    /** Code type of RoleRequest message */
    private static final byte MESSAGE_TYPE = 24;
    private static final byte PADDING_IN_ROLE_REQUEST_MESSAGE = 4;

    @Override
    public void serialize(RoleRequestInput message, ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeInt(message.getRole().getIntValue());
        outBuffer.writeZero(PADDING_IN_ROLE_REQUEST_MESSAGE);
        outBuffer.writeLong(message.getGenerationId().longValue());
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

}