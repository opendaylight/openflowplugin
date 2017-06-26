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
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigInputBuilder;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public class GetQueueConfigInputMessageFactory implements OFDeserializer<GetQueueConfigInput> {

    @Override
    public GetQueueConfigInput deserialize(ByteBuf rawMessage) {
        GetQueueConfigInputBuilder builder = new GetQueueConfigInputBuilder();
        builder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        builder.setXid((rawMessage.readUnsignedInt()));
        builder.setPort(new PortNumber(rawMessage.readUnsignedInt()));
        return builder.build();
    }

}
