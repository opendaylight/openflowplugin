/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf.serializer;

import static org.mockito.ArgumentMatchers.any;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowplugin.extension.onf.BundleTestUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleControlType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundlePropertyType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleControlOnf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleControlOnfBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.bundle.control.onf.OnfControlGroupingDataBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Test for {@link org.opendaylight.openflowplugin.extension.onf.serializer.BundleControlFactory}.
 */
@RunWith(MockitoJUnitRunner.class)
public class BundleControlFactoryTest extends AbstractBundleMessageFactoryTest {

    private final OFSerializer<BundleControlOnf> factory = new BundleControlFactory();

    @Test
    public void testSerializeWithoutProperties() {
        testSerialize(false);
    }

    @Test
    public void testSerializeWithExperimenterProperty() {
        testSerialize(true);
    }

    private void testSerialize(final boolean withProperty) {
        final OnfControlGroupingDataBuilder dataBuilder = new OnfControlGroupingDataBuilder();
        dataBuilder.setBundleId(new BundleId(Uint32.ONE));
        dataBuilder.setType(BundleControlType.ONFBCTOPENREQUEST);
        dataBuilder.setFlags(new BundleFlags(true, true));

        if (withProperty) {
            dataBuilder.setBundleProperty(new ArrayList<>(Collections.singleton(
                    BundleTestUtils.createExperimenterProperty(propertyExperimenterData))));
            Mockito.when(registry.getSerializer(any())).thenReturn(propertySerializer);
            ((SerializerRegistryInjector) factory).injectSerializerRegistry(registry);
        }

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(new BundleControlOnfBuilder().setOnfControlGroupingData(dataBuilder.build()).build(), out);

        Assert.assertEquals("Wrong bundle ID", 1L, out.readUnsignedInt());
        Assert.assertEquals("Wrong type", BundleControlType.ONFBCTOPENREQUEST.getIntValue(), out.readUnsignedShort());
        Assert.assertEquals("Wrong flags", 3, out.readUnsignedShort());
        if (withProperty) {
            Assert.assertEquals("Wrong property type", BundlePropertyType.ONFETBPTEXPERIMENTER.getIntValue(),
                    out.readUnsignedShort());
            out.readUnsignedShort(); // length
            Assert.assertEquals("Wrong experimenter ID", 1, out.readUnsignedInt());
            Assert.assertEquals("Wrong experimenter type", 2, out.readUnsignedInt());
            Mockito.verify(propertySerializer, Mockito.times(1)).serialize(propertyExperimenterData, out);

        } else {
            Assert.assertTrue("Unexpected data", out.readableBytes() == 0);
        }
    }
}
