/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortMod;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.BundleInnerMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.property.grouping.bundle.property.entry.bundle.property.experimenter.BundlePropertyExperimenterData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleAddMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleAddMessageBuilder;

/**
 * Test for {@link org.opendaylight.openflowplugin.extension.onf.serializer.BundleAddMessageFactory}.
 */
@RunWith(MockitoJUnitRunner.class)
public class BundleAddMessageFactoryTest {

    private final OFSerializer<BundleAddMessage> factory = new BundleAddMessageFactory();
    @Mock
    SerializerRegistry registry;
    @Mock
    OFSerializer<PortMod> portModSerializer;
    @Mock
    OFSerializer<BundlePropertyExperimenterData> propertySerializer;

    @Test
    public void testSerializeWithoutProperties() {
        BundleAddMessageBuilder builder = new BundleAddMessageBuilder();
        builder.setBundleId(new BundleId(1L));
        builder.setFlags(new BundleFlags(true, false));

        BundleInnerMessage innerMessage = AbstractBundleMessageFactoryTest.createPortModCase();
        builder.setBundleInnerMessage(innerMessage);

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        Mockito.when(registry.getSerializer(Matchers.any(MessageTypeKey.class))).thenReturn(portModSerializer);
        ((SerializerRegistryInjector) factory).injectSerializerRegistry(registry);
        factory.serialize(builder.build(), out);

        Assert.assertEquals("Wrong bundle ID", 1L, out.readUnsignedInt());
        long padding = out.readUnsignedShort();
        Assert.assertEquals("Wrong flags", 1, out.readUnsignedShort());
        Mockito.verify(portModSerializer, Mockito.times(1)).serialize((PortMod)innerMessage, out);
    }

    @Test
    public void testSerializeWithExperimenterProperty() {
        BundleAddMessageBuilder builder = new BundleAddMessageBuilder();
        builder.setBundleId(new BundleId(2L));
        builder.setFlags(new BundleFlags(true, false));

        BundleInnerMessage innerMessage = AbstractBundleMessageFactoryTest.createPortModCase();
        builder.setBundleInnerMessage(innerMessage);

        BundlePropertyExperimenterData data = AbstractBundleMessageFactoryTest.createBundleExperimenterPropertyData();
        builder.setBundleProperty(AbstractBundleMessageFactoryTest.createListWithBundleExperimenterProperty(data));

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        Mockito.when(registry.getSerializer(Matchers.any(MessageTypeKey.class)))
                .thenReturn(portModSerializer)
                .thenReturn(propertySerializer);
        ((SerializerRegistryInjector) factory).injectSerializerRegistry(registry);
        factory.serialize(builder.build(), out);

        Assert.assertEquals("Wrong bundle ID", 2L, out.readUnsignedInt());
        long padding = out.readUnsignedShort();
        Assert.assertEquals("Wrong flags", 1, out.readUnsignedShort());
        Mockito.verify(portModSerializer, Mockito.times(1)).serialize((PortMod)innerMessage, out);
        Mockito.verify(propertySerializer, Mockito.times(1)).serialize(data, out);
    }

}