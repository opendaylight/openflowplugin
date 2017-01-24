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
import org.mockito.Mockito;
import org.opendaylight.openflowplugin.extension.api.path.MessagePath;
import org.opendaylight.openflowplugin.extension.onf.BundleTestUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleControlType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.common.grouping.BundleProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.property.grouping.bundle.property.entry.BundlePropertyExperimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.property.grouping.bundle.property.entry.bundle.property.experimenter.BundlePropertyExperimenterData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleControl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleControlBuilder;

/**
 * Test for {@link org.opendaylight.openflowplugin.extension.onf.converter.BundleControlConverter}.
 */
public class BundleControlConverterTest {

    private final BundleControlConverter converter = new BundleControlConverter();

    @Test
    public void testGetExperimenterId() {
        Assert.assertEquals("Wrong ExperimenterId.", new ExperimenterId(0x4F4E4600L), converter.getExperimenterId());
    }

    @Test
    public void testGetType() {
        Assert.assertEquals("Wrong type.", 2300, converter.getType());
    }

    @Test
    public void testConvertDownWithProperty() {
        testConvertDown(true);
    }

    @Test
    public void testConvertDownWithoutProperty() {
        testConvertDown(false);
    }

    @Test
    public void testConvertUpWithProperty() {
        testConvertUp(true);
    }

    @Test
    public void testConvertUpWithoutProperty() {
        testConvertUp(true);
    }

    private void testConvertDown(final boolean withProperty) {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleControl original
                = createOFPMessage(withProperty);
        final BundleControl converted = converter.convert(original);
        testConvert(original, converted, withProperty);
    }

    private void testConvertUp(final boolean withProperty) {
        final BundleControl original = createOFJMessage(withProperty);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleControl converted
                = converter.convert(original, MessagePath.MESSAGE_NOTIFICATION);
        testConvert(converted, original, withProperty);
    }

    private static void testConvert(final org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleControl ofpMessage,
                                 final BundleControl ofjMessage,
                                 final boolean withProperty) {
        Assert.assertEquals("Wrong BundleId", new BundleId(ofpMessage.getBundleId().getValue()), ofjMessage.getBundleId());
        Assert.assertEquals("Wrong type", BundleControlType.forValue(ofpMessage.getType().getIntValue()), ofjMessage.getType());
        Assert.assertEquals("Wrong flags", new BundleFlags(ofpMessage.getFlags().isAtomic(), ofpMessage.getFlags().isOrdered()), ofjMessage.getFlags());
        if (withProperty) {
            final BundlePropertyExperimenter originalProperty = (BundlePropertyExperimenter) ofpMessage.getBundleProperty().get(0).getBundlePropertyEntry();
            final BundlePropertyExperimenter convertedProperty = ((BundlePropertyExperimenter) ofjMessage.getBundleProperty().get(0).getBundlePropertyEntry());
            Assert.assertEquals("Wrong property ExperimenterId", new ExperimenterId(originalProperty.getExperimenter()), convertedProperty.getExperimenter());
            Assert.assertEquals("Wrong property experimenter type", originalProperty.getExpType(), convertedProperty.getExpType());
            Assert.assertEquals("Wrong property data", originalProperty.getBundlePropertyExperimenterData(), convertedProperty.getBundlePropertyExperimenterData());
        } else {
            Assert.assertTrue("Properties not empty", ofjMessage.getBundleProperty().isEmpty());
        }
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleControl
                    createOFPMessage(final boolean withProperty) {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleControlBuilder builder
                = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleControlBuilder();
        builder.setBundleId(new BundleId(1L));
        builder.setType(BundleControlType.ONFBCTOPENREQUEST);
        builder.setFlags(new BundleFlags(true, false));
        List<BundleProperty> properties = new ArrayList<>();
        if (withProperty) {
            properties.add(BundleTestUtils.createExperimenterProperty(Mockito.mock(BundlePropertyExperimenterData.class)));
        }
        builder.setBundleProperty(properties);
        return builder.build();
    }

    private static BundleControl createOFJMessage(final boolean withProperty) {
        final BundleControlBuilder builder = new BundleControlBuilder();
        builder.setBundleId(new BundleId(1L));
        builder.setType(BundleControlType.ONFBCTOPENREPLY);
        builder.setFlags(new BundleFlags(false, false));
        List<BundleProperty> properties = new ArrayList<>();
        if (withProperty) {
            properties.add(BundleTestUtils.createExperimenterProperty(Mockito.mock(BundlePropertyExperimenterData.class)));
        }
        builder.setBundleProperty(properties);
        return builder.build();
    }

}
