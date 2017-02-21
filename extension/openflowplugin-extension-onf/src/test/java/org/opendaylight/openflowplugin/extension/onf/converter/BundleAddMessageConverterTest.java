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
import org.junit.Ignore;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.add.flow._case.AddFlowCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.add.group._case.AddGroupCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.remove.flow._case.RemoveFlowCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.remove.group._case.RemoveGroupCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.update.flow._case.UpdateFlowCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.update.group._case.UpdateGroupCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.update.port._case.UpdatePortCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleAddMessageSal;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleAddMessageSalBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.bundle.add.message.sal.SalAddMessageDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundleFlowModCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundleGroupModCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundlePortModCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.common.grouping.BundleProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.property.grouping.bundle.property.entry.BundlePropertyExperimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.property.grouping.bundle.property.entry.bundle.property.experimenter.BundlePropertyExperimenterData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleAddMessageOnf;
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
    @Ignore
    public void testConvertWithoutProperty() throws Exception {
        testConvert(false);
    }

    @Test
    @Ignore
    public void testConvertWithProperty() throws Exception {
        testConvert(true);
    }

    @Test
    @Ignore
    public void testConvertAddFlowCase() throws Exception {
        testConvert(new BundleAddFlowCaseBuilder().setAddFlowCaseData(new AddFlowCaseDataBuilder().build()).build(), BundleFlowModCase.class);
    }

    @Test
    @Ignore
    public void testConvertUpdateFlowCase() throws Exception {
        testConvert(new BundleUpdateFlowCaseBuilder().setUpdateFlowCaseData(new UpdateFlowCaseDataBuilder().build()).build(), BundleFlowModCase.class);
    }

    @Test
    @Ignore
    public void testConvertRemoveFlowCase() throws Exception {
        testConvert(new BundleRemoveFlowCaseBuilder().setRemoveFlowCaseData(new RemoveFlowCaseDataBuilder().build()).build(), BundleFlowModCase.class);
    }

    @Test
    @Ignore
    public void testConvertAddGroupCase() throws Exception {
        testConvert(new BundleAddGroupCaseBuilder().setAddGroupCaseData(new AddGroupCaseDataBuilder().setGroupId(GROUP_ID).build()).build(), BundleGroupModCase.class);
    }

    @Test
    @Ignore
    public void testConvertUpdateGroupCase() throws Exception {
        testConvert(new BundleUpdateGroupCaseBuilder().setUpdateGroupCaseData(new UpdateGroupCaseDataBuilder().setGroupId(GROUP_ID).build()).build(), BundleGroupModCase.class);
    }

    @Test
    @Ignore
    public void testConvertRemoveGroupCase() throws Exception {
        testConvert(new BundleRemoveGroupCaseBuilder().setRemoveGroupCaseData(new RemoveGroupCaseDataBuilder().setGroupId(GROUP_ID).build()).build(), BundleGroupModCase.class);
    }

    @Test
    @Ignore
    public void testConvertUpdatePortCase() throws Exception {
        testConvert(new BundleUpdatePortCaseBuilder()
                .setUpdatePortCaseData(new UpdatePortCaseDataBuilder()
                .setPort(new PortBuilder()
                            .setPort(Collections.singletonList(new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.PortBuilder()
                                    .setConfiguration(Mockito.mock(PortConfig.class))
                                    .setAdvertisedFeatures(Mockito.mock(PortFeatures.class))
                                    .setPortNumber(Mockito.mock(PortNumberUni.class))
                                    .setHardwareAddress(Mockito.mock(MacAddress.class))
                                    .build()))
                            .build()).build())
                .build(), BundlePortModCase.class);
    }

    private void testConvert(final BundleInnerMessage message, final Class clazz) throws Exception {
        testConvert(message, clazz, false);
    }

    private void testConvert(final boolean withProperty) throws Exception {
        final BundleInnerMessage message = new BundleAddFlowCaseBuilder().build();
        testConvert(message, BundleFlowModCase.class, withProperty);
    }

    private void testConvert(final BundleInnerMessage message, Class clazz, final boolean withProperty) throws Exception {
        final BundleAddMessageSal original = createMessage(withProperty, message);
        final BundleAddMessageOnf converted = converter.convert(original);
        Assert.assertEquals("Wrong BundleId", new BundleId(original.getSalAddMessageData().getBundleId().getValue()), converted.getOnfAddMessageGroupingData().getBundleId());
        Assert.assertEquals("Wrong flags", new BundleFlags(original.getSalAddMessageData().getFlags().isAtomic(), original.getSalAddMessageData().getFlags().isOrdered()), converted.getOnfAddMessageGroupingData().getFlags());
        Assert.assertEquals("Wrong inner message conversion", clazz, converted.getOnfAddMessageGroupingData().getBundleInnerMessage().getImplementedInterface());
        if (withProperty) {
            final BundlePropertyExperimenter originalProperty = (BundlePropertyExperimenter) original.getSalAddMessageData().getBundleProperty().get(0).getBundlePropertyEntry();
            final BundlePropertyExperimenter convertedProperty = ((BundlePropertyExperimenter) converted.getOnfAddMessageGroupingData().getBundleProperty().get(0).getBundlePropertyEntry());
            Assert.assertEquals("Wrong property ExperimenterId", new ExperimenterId(originalProperty.getExperimenter()), convertedProperty.getExperimenter());
            Assert.assertEquals("Wrong property experimenter type", originalProperty.getExpType(), convertedProperty.getExpType());
            Assert.assertEquals("Wrong property data", originalProperty.getBundlePropertyExperimenterData(), convertedProperty.getBundlePropertyExperimenterData());
        } else {
            Assert.assertTrue("Properties not empty", converted.getOnfAddMessageGroupingData().getBundleProperty().isEmpty());
        }
    }

    private static BundleAddMessageSal createMessage(final boolean withProperty, final BundleInnerMessage innerMessage) {
        final SalAddMessageDataBuilder dataBuilder = new SalAddMessageDataBuilder();
        dataBuilder.setBundleId(new BundleId(1L));
        dataBuilder.setFlags(new BundleFlags(true, false));
        List<BundleProperty> properties = new ArrayList<>();
        if (withProperty) {
            properties.add(BundleTestUtils.createExperimenterProperty(Mockito.mock(BundlePropertyExperimenterData.class)));
        }
        dataBuilder.setBundleProperty(properties);
        dataBuilder.setBundleInnerMessage(innerMessage);

        return new BundleAddMessageSalBuilder().setSalAddMessageData(dataBuilder.build()).build();
    }

}