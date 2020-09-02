/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization;

import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.BarrierInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.FlowModInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.GetAsyncRequestMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.GetConfigInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.GetFeaturesInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.GetQueueConfigInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.GroupModInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.MeterModInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.MultipartRequestInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.OF10FeaturesRequestMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.OF10FlowModInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.OF10GetQueueConfigInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.OF10PacketOutInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.OF10PortModInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.OF10StatsRequestInputFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.PacketOutInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.PortModInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.RoleRequestInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.SetAsyncInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.SetConfigInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.TableModInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.util.SimpleDeserializerRegistryHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.TableModInput;

/**
 * Util class for init registration of additional deserializers.
 *
 * @author giuseppex.petralia@intel.com
 */
public final class AdditionalMessageDeserializerInitializer {
    private AdditionalMessageDeserializerInitializer() {
        throw new UnsupportedOperationException("Utility class shouldn't be instantiated");
    }

    /**
     * Registers additional message deserializers.
     * @param registry registry to be filled with deserializers
     */
    public static void registerMessageDeserializers(final DeserializerRegistry registry) {
        SimpleDeserializerRegistryHelper helper;

        // register OF v1.0 message deserializers
        helper = new SimpleDeserializerRegistryHelper(EncodeConstants.OF10_VERSION_ID, registry);
        helper.registerDeserializer(5, GetFeaturesInput.class, new OF10FeaturesRequestMessageFactory());
        helper.registerDeserializer(7, GetConfigInput.class, new GetConfigInputMessageFactory());
        helper.registerDeserializer(9, SetConfigInput.class, new SetConfigInputMessageFactory());
        helper.registerDeserializer(13, PacketOutInput.class, new OF10PacketOutInputMessageFactory(registry));
        helper.registerDeserializer(14, FlowModInput.class, new OF10FlowModInputMessageFactory(registry));
        helper.registerDeserializer(15, PortModInput.class, new OF10PortModInputMessageFactory());
        helper.registerDeserializer(16, MultipartRequestInput.class, new OF10StatsRequestInputFactory(registry));
        helper.registerDeserializer(18, BarrierInput.class, new BarrierInputMessageFactory());
        helper.registerDeserializer(20, GetQueueConfigInput.class, new OF10GetQueueConfigInputMessageFactory());

        // register OF v1.3 message deserializers
        helper = new SimpleDeserializerRegistryHelper(EncodeConstants.OF13_VERSION_ID, registry);
        helper.registerDeserializer(5, GetFeaturesInput.class, new GetFeaturesInputMessageFactory());
        helper.registerDeserializer(7, GetConfigInput.class, new GetConfigInputMessageFactory());
        helper.registerDeserializer(9, SetConfigInput.class, new SetConfigInputMessageFactory());
        helper.registerDeserializer(13, PacketOutInput.class, new PacketOutInputMessageFactory(registry));
        helper.registerDeserializer(14, FlowModInput.class, new FlowModInputMessageFactory(registry));
        helper.registerDeserializer(15, GroupModInput.class, new GroupModInputMessageFactory(registry));
        helper.registerDeserializer(16, PortModInput.class, new PortModInputMessageFactory());
        helper.registerDeserializer(17, TableModInput.class, new TableModInputMessageFactory());
        helper.registerDeserializer(18, MultipartRequestInput.class, new MultipartRequestInputMessageFactory(registry));
        helper.registerDeserializer(20, BarrierInput.class, new BarrierInputMessageFactory());
        helper.registerDeserializer(22, GetQueueConfigInput.class, new GetQueueConfigInputMessageFactory());
        helper.registerDeserializer(24, RoleRequestInput.class, new RoleRequestInputMessageFactory());
        helper.registerDeserializer(26, GetAsyncInput.class, new GetAsyncRequestMessageFactory());
        helper.registerDeserializer(28, SetAsyncInput.class, new SetAsyncInputMessageFactory());
        helper.registerDeserializer(29, MeterModInput.class, new MeterModInputMessageFactory(registry));

        // register OF v1.4 message deserializers
        helper = new SimpleDeserializerRegistryHelper(EncodeConstants.OF14_VERSION_ID, registry);
        helper.registerDeserializer(7, GetConfigInput.class, new GetConfigInputMessageFactory());
        helper.registerDeserializer(9, SetConfigInput.class, new SetConfigInputMessageFactory());
        helper.registerDeserializer(20, BarrierInput.class, new BarrierInputMessageFactory());

        // register OF v1.5 message deserializers
        helper = new SimpleDeserializerRegistryHelper(EncodeConstants.OF15_VERSION_ID, registry);
        helper.registerDeserializer(7, GetConfigInput.class, new GetConfigInputMessageFactory());
        helper.registerDeserializer(9, SetConfigInput.class, new SetConfigInputMessageFactory());
        helper.registerDeserializer(20, BarrierInput.class, new BarrierInputMessageFactory());
    }

}
