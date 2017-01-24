/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.openflowplugin.extension.onf.BundleTestUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.PortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.BundleInnerMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleAddFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleAddGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleRemoveFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleRemoveGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleUpdateFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleUpdateGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleUpdatePortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundleFlowModCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundleGroupModCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundlePortModCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.common.grouping.BundleProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.property.grouping.bundle.property.entry.BundlePropertyExperimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.property.grouping.bundle.property.entry.bundle.property.experimenter.BundlePropertyExperimenterData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleAddMessage;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Test for {@link org.opendaylight.openflowplugin.extension.onf.converter.BundleAddMessageConverter}.
 */
public class BundleAddMessageConverterTest {

    private final BundleAddMessageConverter converter = new BundleAddMessageConverter();
    private static final NodeRef NODE_REF = new NodeRef(InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId("openflow:1"))));
    private static final GroupId GROUP_ID = new GroupId(1L);

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
        testConvert(false);
    }

    @Test
    public void testConvertWithProperty() throws Exception {
        testConvert(true);
    }

    @Test
    public void testConvertAddFlowCase() throws Exception {
        testConvert(new BundleAddFlowCaseBuilder().setNode(NODE_REF).build(), BundleFlowModCase.class);
    }

    @Test
    public void testConvertUpdateFlowCase() throws Exception {
        testConvert(new BundleUpdateFlowCaseBuilder().setNode(NODE_REF).build(), BundleFlowModCase.class);
    }

    @Test
    public void testConvertRemoveFlowCase() throws Exception {
        testConvert(new BundleRemoveFlowCaseBuilder().setNode(NODE_REF).build(), BundleFlowModCase.class);
    }

    @Test
    public void testConvertAddGroupCase() throws Exception {
        testConvert(new BundleAddGroupCaseBuilder().setNode(NODE_REF).setGroupId(GROUP_ID).build(), BundleGroupModCase.class);
    }

    @Test
    public void testConvertUpdateGroupCase() throws Exception {
        testConvert(new BundleUpdateGroupCaseBuilder().setNode(NODE_REF).setGroupId(GROUP_ID).build(), BundleGroupModCase.class);
    }

    @Test
    public void testConvertRemoveGroupCase() throws Exception {
        testConvert(new BundleRemoveGroupCaseBuilder().setNode(NODE_REF).setGroupId(GROUP_ID).build(), BundleGroupModCase.class);
    }

    @Test
    public void testConvertUpdatePortCase() throws Exception {
        testConvert(new BundleUpdatePortCaseBuilder()
                .setNode(NODE_REF)
                .setPort(new PortBuilder()
                            .setPort(Collections.singletonList(new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.PortBuilder()
                                    .setConfiguration(Mockito.mock(PortConfig.class))
                                    .setAdvertisedFeatures(Mockito.mock(PortFeatures.class))
                                    .setPortNumber(Mockito.mock(PortNumberUni.class))
                                    .setHardwareAddress(Mockito.mock(MacAddress.class))
                                    .build()))
                            .build())
                .build(), BundlePortModCase.class);
    }

    private void testConvert(final BundleInnerMessage message, final Class clazz) throws Exception {
        testConvert(message, clazz, false);
    }

    private void testConvert(final boolean withProperty) throws Exception {
        final BundleInnerMessage message = new BundleAddFlowCaseBuilder().setNode(NODE_REF).build();
        testConvert(message, BundleFlowModCase.class, withProperty);
    }

    private void testConvert(final BundleInnerMessage message, Class clazz, final boolean withProperty) throws Exception {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleAddMessage
                original = createMessage(withProperty, message);
        final BundleAddMessage converted = converter.convert(original);
        Assert.assertEquals("Wrong BundleId", new BundleId(original.getBundleId().getValue()), converted.getBundleId());
        Assert.assertEquals("Wrong flags", new BundleFlags(original.getFlags().isAtomic(), original.getFlags().isOrdered()), converted.getFlags());
        Assert.assertEquals("Wrong inner message conversion", clazz, converted.getBundleInnerMessage().getImplementedInterface());
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
                createMessage(final boolean withProperty, final BundleInnerMessage innerMessage) {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleAddMessageBuilder builder
                = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleAddMessageBuilder();
        builder.setBundleId(new BundleId(1L));
        builder.setFlags(new BundleFlags(true, false));
        List<BundleProperty> properties = new ArrayList<>();
        if (withProperty) {
            properties.add(BundleTestUtils.createExperimenterProperty(Mockito.mock(BundlePropertyExperimenterData.class)));
        }
        builder.setBundleProperty(properties);

        builder.setBundleInnerMessage(innerMessage);
        return builder.build();
    }

}