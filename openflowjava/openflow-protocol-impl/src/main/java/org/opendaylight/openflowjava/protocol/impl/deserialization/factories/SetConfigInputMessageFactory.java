/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.impl.util.VersionAssignableFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.SwitchConfigFlag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInputBuilder;

/**
 * Translates SetConfig messages.
 * OF protocol versions: 1.0, 1.3, 1.4, 1.5.
 * @author giuseppex.petralia@intel.com
 */
public class SetConfigInputMessageFactory extends VersionAssignableFactory implements OFDeserializer<SetConfigInput> {

    @Override
    public SetConfigInput deserialize(ByteBuf rawMessage) {
        SetConfigInputBuilder builder = new SetConfigInputBuilder();
        builder.setVersion(getVersion());
        builder.setXid(rawMessage.readUnsignedInt());
        builder.setFlags(SwitchConfigFlag.forValue(rawMessage.readUnsignedShort()));
        builder.setMissSendLen(rawMessage.readUnsignedShort());
        return builder.build();
    }

}