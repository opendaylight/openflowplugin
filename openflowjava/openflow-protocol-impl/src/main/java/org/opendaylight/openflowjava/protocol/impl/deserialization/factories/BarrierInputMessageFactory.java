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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInputBuilder;

/**
 * Translates BarrierRequest messages.
 * OF protocol versions: 1.0, 1.3, 1.4, 1.5.
 * @author giuseppex.petralia@intel.com
 */
public class BarrierInputMessageFactory extends VersionAssignableFactory implements OFDeserializer<BarrierInput>{

    @Override
    public BarrierInput deserialize(ByteBuf rawMessage) {
        BarrierInputBuilder builder = new BarrierInputBuilder();
        builder.setVersion(getVersion());
        builder.setXid(rawMessage.readUnsignedInt());
        return builder.build();
    }

}
