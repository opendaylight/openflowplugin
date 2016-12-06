/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowplugin.extension.onf.BundleTestUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.BundleControlType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.BundleId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.BundlePropertyType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.experimenter.input.experimenter.data.of.choice.BundleControl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.experimenter.input.experimenter.data.of.choice.BundleControlBuilder;

/**
 * Test for {@link org.opendaylight.openflowplugin.extension.onf.serializer.BundleControlFactory}.
 */
@RunWith(MockitoJUnitRunner.class)
public class BundleControlFactoryTest extends AbstractBundleMessageFactoryTest {

    private final OFSerializer<BundleControl> factory = new BundleControlFactory();

    @Test
    public void testSerializeWithoutProperties() {
        testSerialize(false);
    }

    @Test
    public void testSerializeWithExperimenterProperty() {
        testSerialize(true);
    }

    private void testSerialize(final boolean withProperty) {
        final BundleControlBuilder builder = new BundleControlBuilder();
        builder.setBundleId(new BundleId(1L));
        builder.setType(BundleControlType.ONFBCTOPENREQUEST);
        builder.setFlags(new BundleFlags(true, true));

        if (withProperty) {
            builder.setBundleProperty((new ArrayList<>(Collections.singleton(
                    BundleTestUtils.createExperimenterProperty(propertyExperimenterData)))));
            Mockito.when(registry.getSerializer(Matchers.any(MessageTypeKey.class))).thenReturn(propertySerializer);
            ((SerializerRegistryInjector) factory).injectSerializerRegistry(registry);
        }

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(builder.build(), out);

        Assert.assertEquals("Wrong bundle ID", 1L, out.readUnsignedInt());
        Assert.assertEquals("Wrong type", BundleControlType.ONFBCTOPENREQUEST.getIntValue(), out.readUnsignedShort());
        Assert.assertEquals("Wrong flags", 3, out.readUnsignedShort());
        if (withProperty) {
            Assert.assertEquals("Wrong property type", BundlePropertyType.ONFETBPTEXPERIMENTER.getIntValue(), out.readUnsignedShort());
            int length = out.readUnsignedShort();
            Assert.assertEquals("Wrong experimenter ID", 1, out.readUnsignedInt());
            Assert.assertEquals("Wrong experimenter type", 2, out.readUnsignedInt());
            Mockito.verify(propertySerializer, Mockito.times(1)).serialize(propertyExperimenterData, out);

        } else {
            Assert.assertTrue("Unexpected data", out.readableBytes() == 0);
        }
    }

}