/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint64;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ControllerRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInputBuilder;

/**
 * Translates RoleRequestInput messages.
 *
 * @author giuseppex.petralia@intel.com
 */
public class RoleRequestInputMessageFactory implements OFDeserializer<RoleRequestInput> {

    private static final byte PADDING = 4;

    @Override
    public RoleRequestInput deserialize(ByteBuf rawMessage) {
        RoleRequestInputBuilder builder = new RoleRequestInputBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_3)
                .setXid(readUint32(rawMessage))
                .setRole(ControllerRole.forValue(rawMessage.readInt()));
        rawMessage.skipBytes(PADDING);
        builder.setGenerationId(readUint64(rawMessage));
        return builder.build();
    }
}
