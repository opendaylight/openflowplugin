/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization;

import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.BarrierReplyMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.EchoReplyMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.EchoRequestMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.ErrorMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.ExperimenterMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.FeaturesReplyMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.FlowRemovedMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.GetAsyncReplyMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.GetConfigReplyMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.HelloMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.MultipartReplyMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.OF10ErrorMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.OF10FeaturesReplyMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.OF10FlowRemovedMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.OF10HelloMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.OF10PacketInMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.OF10PortStatusMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.OF10QueueGetConfigReplyMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.OF10StatsReplyMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.PacketInMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.PortStatusMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.QueueGetConfigReplyMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.RoleReplyMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.VendorMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.util.SimpleDeserializerRegistryHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutput;

/**
 * Util class for init registration of deserializers.
 * @author michal.polkorab
 */
public final class MessageDeserializerInitializer {

    private MessageDeserializerInitializer() {
        throw new UnsupportedOperationException("Utility class shouldn't be instantiated");
    }

    /**
     * Registers message deserializers.
     * @param registry registry to be filled with deserializers
     */
    public static void registerMessageDeserializers(final DeserializerRegistry registry) {
        SimpleDeserializerRegistryHelper helper;

        // register OF v1.0 message deserializers
        helper = new SimpleDeserializerRegistryHelper(EncodeConstants.OF10_VERSION_ID, registry);
        helper.registerDeserializer(0, HelloMessage.class, new OF10HelloMessageFactory());
        helper.registerDeserializer(1, ErrorMessage.class, new OF10ErrorMessageFactory());
        helper.registerDeserializer(2, EchoRequestMessage.class, new EchoRequestMessageFactory());
        helper.registerDeserializer(3, EchoOutput.class, new EchoReplyMessageFactory());
        helper.registerDeserializer(4, ExperimenterMessage.class, new VendorMessageFactory(registry));
        helper.registerDeserializer(6, GetFeaturesOutput.class, new OF10FeaturesReplyMessageFactory());
        helper.registerDeserializer(8, GetConfigOutput.class, new GetConfigReplyMessageFactory());
        helper.registerDeserializer(10, PacketInMessage.class, new OF10PacketInMessageFactory());
        helper.registerDeserializer(11, FlowRemovedMessage.class, new OF10FlowRemovedMessageFactory(registry));
        helper.registerDeserializer(12, PortStatusMessage.class, new OF10PortStatusMessageFactory());
        helper.registerDeserializer(17, MultipartReplyMessage.class, new OF10StatsReplyMessageFactory(registry));
        helper.registerDeserializer(19, BarrierOutput.class, new BarrierReplyMessageFactory());
        helper.registerDeserializer(21, GetQueueConfigOutput.class, new OF10QueueGetConfigReplyMessageFactory());

        // register OF v1.3 message deserializers
        helper = new SimpleDeserializerRegistryHelper(EncodeConstants.OF13_VERSION_ID, registry);
        helper.registerDeserializer(0, HelloMessage.class, new HelloMessageFactory());
        helper.registerDeserializer(1, ErrorMessage.class, new ErrorMessageFactory(registry));
        helper.registerDeserializer(2, EchoRequestMessage.class, new EchoRequestMessageFactory());
        helper.registerDeserializer(3, EchoOutput.class, new EchoReplyMessageFactory());
        helper.registerDeserializer(4, ExperimenterMessage.class, new ExperimenterMessageFactory(registry));
        helper.registerDeserializer(6, GetFeaturesOutput.class, new FeaturesReplyMessageFactory());
        helper.registerDeserializer(8, GetConfigOutput.class, new GetConfigReplyMessageFactory());
        helper.registerDeserializer(10, PacketInMessage.class, new PacketInMessageFactory(registry));
        helper.registerDeserializer(11, FlowRemovedMessage.class, new FlowRemovedMessageFactory(registry));
        helper.registerDeserializer(12, PortStatusMessage.class, new PortStatusMessageFactory());
        helper.registerDeserializer(19, MultipartReplyMessage.class, new MultipartReplyMessageFactory(registry));
        helper.registerDeserializer(21, BarrierOutput.class, new BarrierReplyMessageFactory());
        helper.registerDeserializer(23, GetQueueConfigOutput.class, new QueueGetConfigReplyMessageFactory(registry));
        helper.registerDeserializer(25, RoleRequestOutput.class, new RoleReplyMessageFactory());
        helper.registerDeserializer(27, GetAsyncOutput.class, new GetAsyncReplyMessageFactory());

        // register OF v1.4 message deserializers
        helper = new SimpleDeserializerRegistryHelper(EncodeConstants.OF14_VERSION_ID, registry);
        helper.registerDeserializer(0, HelloMessage.class, new HelloMessageFactory());
        helper.registerDeserializer(2, EchoRequestMessage.class, new EchoRequestMessageFactory());
        helper.registerDeserializer(3, EchoOutput.class, new EchoReplyMessageFactory());
        helper.registerDeserializer(8, GetConfigOutput.class, new GetConfigReplyMessageFactory());
        helper.registerDeserializer(21, BarrierOutput.class, new BarrierReplyMessageFactory());

        // register OF v1.5 message deserializers
        helper = new SimpleDeserializerRegistryHelper(EncodeConstants.OF15_VERSION_ID, registry);
        helper.registerDeserializer(0, HelloMessage.class, new HelloMessageFactory());
        helper.registerDeserializer(2, EchoRequestMessage.class, new EchoRequestMessageFactory());
        helper.registerDeserializer(3, EchoOutput.class, new EchoReplyMessageFactory());
        helper.registerDeserializer(8, GetConfigOutput.class, new GetConfigReplyMessageFactory());
        helper.registerDeserializer(21, BarrierOutput.class, new BarrierReplyMessageFactory());
    }
}
