/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.translator;

import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializationFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.of.message.service.rev160511.OfMessageReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.of.message.service.rev160511.OfMessageReceivedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

public class OfMessageReceivedTranslator implements MessageTranslator<OfHeader, OfMessageReceived> {

    private static final Logger LOG = LoggerFactory.getLogger(OfMessageReceivedTranslator.class);

    @Override
    public OfMessageReceived translate(OfHeader input, DeviceState deviceState,
            Object connectionDistinguisher) {
        OfMessageReceivedBuilder builder = new OfMessageReceivedBuilder();
        NodeRef nodeRef = new NodeRef(deviceState.getNodeInstanceIdentifier());
        short ofVersion = deviceState.getVersion();
        builder.setIngress(nodeRef);
        builder.setMessage(serialize(input, ofVersion));
        return builder.build();
    }

    private byte[] serialize(DataObject msg, short ofVersion) {
        SerializationFactory factory = new SerializationFactory();
        SerializerRegistry registry = new SerializerRegistryImpl();
        registry.init();
        ByteBuf output = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.setSerializerTable(registry);
        try {
            factory.messageToBuffer(ofVersion, output, msg);
            byte[] bytes = new byte[output.readableBytes()];
            output.readBytes(bytes);
            return bytes;
        } catch (NullPointerException ex) {
            LOG.debug("Error in serializing message. OpenFlow message is malformed.");
            return new byte[0];
        }
    }
}
