/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf.converter;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowplugin.extension.onf.ConverterTestUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleAddFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleAddFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundleFlowModCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.common.grouping.BundleProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.property.grouping.bundle.property.entry.BundlePropertyExperimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleAddMessage;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Test for {@link org.opendaylight.openflowplugin.extension.onf.converter.BundleAddMessageConverter}.
 */
public class BundleAddMessageConverterTest {

    private final BundleAddMessageConverter converter = new BundleAddMessageConverter();

    @Test
    public void testGetExperimenterId() {
        Assert.assertEquals("Wrong ExperimenterId.", new ExperimenterId(0x4F4E4600L), converter.getExperimenterId());
    }

    @Test
    public void testGetType() {
        Assert.assertEquals("Wrong type.", 2301, converter.getType());
    }

    @Test
    public void testConvertWithoutProperty() throws Exception {
        final boolean withProperty = false;
        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleAddMessage
            original = createMessage(withProperty);
        final BundleAddMessage converted = converter.convert(original);
        testConvert(original, converted, withProperty);
    }

    @Test
    public void testConvertWithProperty() throws Exception {
        final boolean withProperty = true;
        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleAddMessage
                original = createMessage(withProperty);
        final BundleAddMessage converted = converter.convert(original);
        testConvert(original, converted, withProperty);
    }

    private void testConvert(final org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleAddMessage original,
                             final BundleAddMessage converted, final boolean withProperty) {
        Assert.assertEquals("Wrong BundleId", new BundleId(original.getBundleId().getValue()), converted.getBundleId());
        Assert.assertEquals("Wrong flags", new BundleFlags(original.getFlags().isAtomic(), original.getFlags().isOrdered()), converted.getFlags());
        Assert.assertEquals("Wrong inner message conversion", BundleFlowModCase.class, converted.getBundleInnerMessage().getImplementedInterface());
        if (withProperty) {
            final BundlePropertyExperimenter originalProperty = (BundlePropertyExperimenter) original.getBundleProperty().get(0).getBundlePropertyEntry();
            final BundlePropertyExperimenter convertedProperty = ((BundlePropertyExperimenter) converted.getBundleProperty().get(0).getBundlePropertyEntry());
            Assert.assertEquals("Wrong property ExperimenterId", new ExperimenterId(originalProperty.getExperimenter()), convertedProperty.getExperimenter());
            Assert.assertEquals("Wrong property experimenter type", originalProperty.getExpType(), convertedProperty.getExpType());
            Assert.assertEquals("Wrong property data", originalProperty.getBundlePropertyExperimenterData(), convertedProperty.getBundlePropertyExperimenterData());
        } else {
            Assert.assertTrue("Properties not empty", converted.getBundleProperty().isEmpty());
        }
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleAddMessage
                createMessage(final boolean withProperty) {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleAddMessageBuilder builder
                = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleAddMessageBuilder();
        builder.setBundleId(new BundleId(1L));
        builder.setFlags(new BundleFlags(true, false));
        List<BundleProperty> properties = new ArrayList<>();
        if (withProperty) {
            properties.add(ConverterTestUtils.createExperimenterProperty());
        }
        builder.setBundleProperty(properties);

        builder.setBundleInnerMessage(createaddFlowCase());
        return builder.build();
    }

    private static BundleAddFlowCase createaddFlowCase() {
        return new BundleAddFlowCaseBuilder()
                .setNode(new NodeRef(InstanceIdentifier.create(Nodes.class)
                        .child(Node.class, new NodeKey(new NodeId("openflow:1")))))
                .build();
    }

}