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
import java.util.ArrayList;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowplugin.extension.onf.BundleTestUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.BundleInnerMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundleFlowModCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundleGroupModCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundlePortModCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleAddMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleAddMessageBuilder;

/**
 * Test for {@link org.opendaylight.openflowplugin.extension.onf.serializer.BundleAddMessageFactory}.
 */
@RunWith(MockitoJUnitRunner.class)
public class BundleAddMessageFactoryTest extends AbstractBundleMessageFactoryTest {

    private final OFSerializer<BundleAddMessage> factory = new BundleAddMessageFactory();
    @Mock
    private OFSerializer caseSerializer;

    @Test
    public void testSerializeWithoutProperties() {
        testSerialize(false);
    }

    @Test
    public void testSerializeWithExperimenterProperty() {
        testSerialize(true);
    }

    @Test
    public void testSerializeFlowModCase() {
        testSerialize(new BundleFlowModCaseBuilder().build());
    }

    @Test
    public void testSerializeGroupModCase() {
        testSerialize(new BundleGroupModCaseBuilder().build());
    }

    @Test
    public void testSerializePortModCase() {
        testSerialize(new BundlePortModCaseBuilder().build());
    }

    private void testSerialize(final BundleInnerMessage innerMessage) {
        testSerialize(false, innerMessage);
    }

    private void testSerialize(final boolean withProperty) {
        testSerialize(withProperty, new BundleFlowModCaseBuilder().build());
    }

    private void testSerialize(final boolean withProperty, final BundleInnerMessage innerMessage) {
        final BundleAddMessageBuilder builder = new BundleAddMessageBuilder();
        builder.setBundleId(new BundleId(1L));
        builder.setFlags(new BundleFlags(true, false));

        builder.setBundleInnerMessage(innerMessage);

        if (withProperty) {
            builder.setBundleProperty(new ArrayList<>(Collections.singleton(
                    BundleTestUtils.createExperimenterProperty(propertyExperimenterData))));
            Mockito.when(registry.getSerializer(Matchers.any()))
                    .thenReturn(caseSerializer)
                    .thenReturn(propertySerializer);
        } else {
            Mockito.when(registry.getSerializer(Matchers.any())).thenReturn(caseSerializer);
        }

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        ((SerializerRegistryInjector) factory).injectSerializerRegistry(registry);
        factory.serialize(builder.build(), out);

        Assert.assertEquals("Wrong bundle ID", 1L, out.readUnsignedInt());
        long padding = out.readUnsignedShort();
        Assert.assertEquals("Wrong flags", 1, out.readUnsignedShort());
        Mockito.verify(caseSerializer).serialize(innerMessage, out);

        if (withProperty) {
            Mockito.verify(propertySerializer).serialize(propertyExperimenterData, out);
        }
    }

}