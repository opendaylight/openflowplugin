/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.BundleControlType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.BundleId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.BundlePropertyType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.bundle.common.grouping.BundleProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.bundle.common.grouping.BundlePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.bundle.property.grouping.bundle.property.entry.BundlePropertyExperimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.bundle.property.grouping.bundle.property.entry.BundlePropertyExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.experimenter.input.experimenter.data.of.choice.BundleControl;

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
    public void testConvertWithProperty() {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev161201.send.experimenter.input.experimenter.message.of.choice.BundleControl original
                = createMessage(true);
        final BundleControl converted = converter.convert(original);
        Assert.assertEquals("Wrong BundleId", new BundleId(original.getBundleId().getValue()), converted.getBundleId());
        Assert.assertEquals("Wrong type", BundleControlType.forValue(original.getType().getIntValue()), converted.getType());
        Assert.assertEquals("Wrong flags", new BundleFlags(original.getFlags().isAtomic(), original.getFlags().isOrdered()), converted.getFlags());
        Assert.assertEquals("Wrong property type", BundlePropertyType.ONFETBPTEXPERIMENTER, converted.getBundleProperty().get(0).getType());
        final BundlePropertyExperimenter originalProperty = (BundlePropertyExperimenter) original.getBundleProperty().get(0).getBundlePropertyEntry();
        final BundlePropertyExperimenter convertedProperty = ((BundlePropertyExperimenter) converted.getBundleProperty().get(0).getBundlePropertyEntry());
        Assert.assertEquals("Wrong property ExperimenterId", new ExperimenterId(originalProperty.getExperimenter()), convertedProperty.getExperimenter());
        Assert.assertEquals("Wrong property experimenter type", originalProperty.getExpType(), convertedProperty.getExpType());
        Assert.assertEquals("Wrong property data", originalProperty.getBundlePropertyExperimenterData(), convertedProperty.getBundlePropertyExperimenterData());
    }

    @Test
    public void testConvertWithoutProperty() {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev161201.send.experimenter.input.experimenter.message.of.choice.BundleControl original
                = createMessage(false);
        final BundleControl converted = converter.convert(original);
        Assert.assertEquals("Wrong BundleId", new BundleId(original.getBundleId().getValue()), converted.getBundleId());
        Assert.assertEquals("Wrong type", BundleControlType.forValue(original.getType().getIntValue()), converted.getType());
        Assert.assertEquals("Wrong flags", new BundleFlags(original.getFlags().isAtomic(), original.getFlags().isOrdered()), converted.getFlags());
        Assert.assertTrue("Properties not empty", converted.getBundleProperty().isEmpty());
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev161201.send.experimenter.input.experimenter.message.of.choice.BundleControl
                    createMessage(final boolean withProperty) {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev161201.send.experimenter.input.experimenter.message.of.choice.BundleControlBuilder builder
                = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev161201.send.experimenter.input.experimenter.message.of.choice.BundleControlBuilder();
        builder.setBundleId(new BundleId(1L));
        builder.setType(BundleControlType.ONFBCTOPENREQUEST);
        builder.setFlags(new BundleFlags(true, false));
        List<BundleProperty> properties = new ArrayList<>();
        if (withProperty) {
            final BundlePropertyBuilder propertyBuilder = new BundlePropertyBuilder();
            propertyBuilder.setType(BundlePropertyType.ONFETBPTEXPERIMENTER);
            propertyBuilder.setBundlePropertyEntry(new BundlePropertyExperimenterBuilder()
                    .setExperimenter(new ExperimenterId(1L))
                    .setExpType(1L)
                    .setBundlePropertyExperimenterData(null)
                    .build());
            properties.add(propertyBuilder.build());
        }
        builder.setBundleProperty(properties);
        return builder.build();
    }

}
