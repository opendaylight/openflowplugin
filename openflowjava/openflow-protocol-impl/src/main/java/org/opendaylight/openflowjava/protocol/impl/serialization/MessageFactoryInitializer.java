/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization;

import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.BarrierInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.EchoInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.EchoReplyInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.ExperimenterInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.FlowModInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.GetAsyncRequestMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.GetConfigInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.GetFeaturesInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.GetQueueConfigInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.GroupModInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.HelloInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.MeterModInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.MultipartRequestInputFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.OF10BarrierInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.OF10FlowModInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.OF10HelloInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.OF10PacketOutInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.OF10PortModInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.OF10QueueGetConfigInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.OF10StatsRequestInputFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.PacketOutInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.PortModInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.RoleRequestInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.SetAsyncInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.SetConfigMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.TableModInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.VendorInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.util.CommonMessageRegistryHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.TableModInput;

/**
 * Util class for init registration of serializers.
 * @author michal.polkorab
 */
public final class MessageFactoryInitializer {
    private MessageFactoryInitializer() {
        throw new UnsupportedOperationException("Utility class shouldn't be instantiated");
    }

    /**
     * Registers message serializers into provided registry.
     * @param serializerRegistry registry to be initialized with message serializers
     */
    public static void registerMessageSerializers(final SerializerRegistry serializerRegistry) {
        CommonMessageRegistryHelper registryHelper;

        // register OF v1.0 message serializers
        registryHelper = new CommonMessageRegistryHelper(EncodeConstants.OF10_VERSION_ID, serializerRegistry);
        registryHelper.registerSerializer(BarrierInput.class, new OF10BarrierInputMessageFactory());
        registryHelper.registerSerializer(EchoInput.class, new EchoInputMessageFactory());
        registryHelper.registerSerializer(EchoReplyInput.class, new EchoReplyInputMessageFactory());
        registryHelper.registerSerializer(ExperimenterInput.class,
            new VendorInputMessageFactory(serializerRegistry));
        registryHelper.registerSerializer(FlowModInput.class, new OF10FlowModInputMessageFactory(serializerRegistry));
        registryHelper.registerSerializer(GetConfigInput.class, new GetConfigInputMessageFactory());
        registryHelper.registerSerializer(GetFeaturesInput.class, new GetFeaturesInputMessageFactory());
        registryHelper.registerSerializer(GetQueueConfigInput.class, new OF10QueueGetConfigInputMessageFactory());
        registryHelper.registerSerializer(HelloInput.class, new OF10HelloInputMessageFactory());
        registryHelper.registerSerializer(MultipartRequestInput.class,
            new OF10StatsRequestInputFactory(serializerRegistry));
        registryHelper.registerSerializer(PacketOutInput.class,
            new OF10PacketOutInputMessageFactory(serializerRegistry));
        registryHelper.registerSerializer(PortModInput.class, new OF10PortModInputMessageFactory());
        registryHelper.registerSerializer(SetConfigInput.class, new SetConfigMessageFactory());

        // register OF v1.3 message serializers
        registryHelper = new CommonMessageRegistryHelper(EncodeConstants.OF13_VERSION_ID, serializerRegistry);
        registryHelper.registerSerializer(BarrierInput.class, new BarrierInputMessageFactory());
        registryHelper.registerSerializer(EchoInput.class, new EchoInputMessageFactory());
        registryHelper.registerSerializer(EchoReplyInput.class, new EchoReplyInputMessageFactory());
        registryHelper.registerSerializer(ExperimenterInput.class,
            new ExperimenterInputMessageFactory(serializerRegistry));
        registryHelper.registerSerializer(FlowModInput.class, new FlowModInputMessageFactory(serializerRegistry));
        registryHelper.registerSerializer(GetAsyncInput.class, new GetAsyncRequestMessageFactory());
        registryHelper.registerSerializer(GetConfigInput.class, new GetConfigInputMessageFactory());
        registryHelper.registerSerializer(GetFeaturesInput.class, new GetFeaturesInputMessageFactory());
        registryHelper.registerSerializer(GetQueueConfigInput.class, new GetQueueConfigInputMessageFactory());
        registryHelper.registerSerializer(GroupModInput.class,
                new GroupModInputMessageFactory(serializerRegistry, serializerRegistry.isGroupAddModEnabled()));
        registryHelper.registerSerializer(HelloInput.class, new HelloInputMessageFactory());
        registryHelper.registerSerializer(MeterModInput.class, new MeterModInputMessageFactory(serializerRegistry));
        registryHelper.registerSerializer(MultipartRequestInput.class,
            new MultipartRequestInputFactory(serializerRegistry));
        registryHelper.registerSerializer(PacketOutInput.class, new PacketOutInputMessageFactory(serializerRegistry));
        registryHelper.registerSerializer(PortModInput.class, new PortModInputMessageFactory());
        registryHelper.registerSerializer(RoleRequestInput.class, new RoleRequestInputMessageFactory());
        registryHelper.registerSerializer(SetAsyncInput.class, new SetAsyncInputMessageFactory());
        registryHelper.registerSerializer(SetConfigInput.class, new SetConfigMessageFactory());
        registryHelper.registerSerializer(TableModInput.class, new TableModInputMessageFactory());
    }
}
