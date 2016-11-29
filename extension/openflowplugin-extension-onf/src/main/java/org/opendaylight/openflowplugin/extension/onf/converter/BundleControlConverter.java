/*
  Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.

  This program and the accompanying materials are made available under the
  terms of the Eclipse Public License v1.0 which accompanies this distribution,
  and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf.converter;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageToOFJava;
import org.opendaylight.openflowplugin.extension.api.exception.ConversionException;
import org.opendaylight.openflowplugin.extension.api.path.MessagePath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.BundleControlType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.BundleId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.BundlePropertyType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.bundle.properties.BundleProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.bundle.properties.BundlePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.bundle.properties.bundle.property.bundle.property.entry.BundleExperimenterPropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.bundle.properties.bundle.property.bundle.property.entry.bundle.experimenter.property.BundleExperimenterPropertyData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.experimenter.input.experimenter.data.of.choice.BundleControl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.experimenter.input.experimenter.data.of.choice.BundleControlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.experimenter.core.message.ExperimenterMessageOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.onf.approved.extensions.rev161128.bundle.properties.bundle.property.bundle.property.entry.BundleExperimenterProperty;

/**
 * Converter for BundleControl messages (ONF approved extension #230).
 */
public class BundleControlConverter implements ConvertorMessageToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.onf.approved.extensions.rev161128.send.experimenter.input.experimenter.message.of.choice.BundleControl, BundleControl>,
        ConvertorMessageFromOFJava<BundleControl, MessagePath> {

    private static final ExperimenterId ONF_EXP_ID = new ExperimenterId(0x4F4E4600L);
    private static final long BUNDLE_CONTROL_TYPE = 2300;

    @Override
    public ExperimenterMessageOfChoice convert(final BundleControl input, final MessagePath path) throws ConversionException {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.onf.approved.extensions.rev161128.send.experimenter.input.experimenter.message.of.choice.BundleControlBuilder builder
                = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.onf.approved.extensions.rev161128.send.experimenter.input.experimenter.message.of.choice.BundleControlBuilder();
        builder.setBundleId(new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.onf.approved.extensions.rev161128.BundleId(input.getBundleId().getValue()));
        builder.setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.onf.approved.extensions.rev161128.BundleControlType.forValue(input.getType().getIntValue()));
        builder.setFlags(new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.onf.approved.extensions.rev161128.BundleFlags(input.getFlags().isAtomic(), input.getFlags().isOrdered()));
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.onf.approved.extensions.rev161128.bundle.properties.BundleProperty> properties = new ArrayList<>();
        if (input.getBundleProperty() != null) {
            for (BundleProperty inputProperty : input.getBundleProperty()) {
                final BundlePropertyType type = inputProperty.getType();
                if (type != null && type.equals(BundlePropertyType.ONFETBPTEXPERIMENTER)) {
                    final org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.onf.approved.extensions.rev161128.bundle.properties.BundlePropertyBuilder propertyBuilder
                            = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.onf.approved.extensions.rev161128.bundle.properties.BundlePropertyBuilder();
                    propertyBuilder.setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.onf.approved.extensions.rev161128.BundlePropertyType.forValue(type.getIntValue()));
                    final BundleExperimenterProperty experimenterProperty = (BundleExperimenterProperty) inputProperty.getBundlePropertyEntry();
                    propertyBuilder.setBundlePropertyEntry(new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.onf.approved.extensions.rev161128.bundle.properties.bundle.property.bundle.property.entry.BundleExperimenterPropertyBuilder()
                            .setExperimenter(experimenterProperty.getExperimenter())
                            .setExpType(experimenterProperty.getExpType())
                            .setBundleExperimenterPropertyData(experimenterProperty.getBundleExperimenterPropertyData())
                            .build());
                    properties.add(propertyBuilder.build());
                } else {
                    throw new ConversionException("Bundle property type fail: " + type);
                }
            }
        }
        builder.setBundleProperty(properties);
        return builder.build();
    }

    @Override
    public BundleControl convert(final org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.onf.approved.extensions.rev161128.send.experimenter.input.experimenter.message.of.choice.BundleControl message) throws ConversionException {
        final BundleControlBuilder builder = new BundleControlBuilder();
        builder.setBundleId(new BundleId(message.getBundleId().getValue()));
        builder.setType(BundleControlType.forValue(message.getType().getIntValue()));
        builder.setFlags(new BundleFlags(message.getFlags().isAtomic(), message.getFlags().isOrdered()));
        List<BundleProperty> properties = new ArrayList<>();
        if (message.getBundleProperty() != null ) {
            for (org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.onf.approved.extensions.rev161128.bundle.properties.BundleProperty messageProperty
                    : message.getBundleProperty()) {
                final org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.onf.approved.extensions.rev161128.BundlePropertyType type = messageProperty.getType();
                if (type != null && type.equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.onf.approved.extensions.rev161128.BundlePropertyType.ONFETBPTEXPERIMENTER)) {
                    final BundlePropertyBuilder propertyBuilder = new BundlePropertyBuilder();
                    propertyBuilder.setType(BundlePropertyType.forValue(type.getIntValue()));
                    final org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.onf.approved.extensions.rev161128.bundle.properties.bundle.property.bundle.property.entry.BundleExperimenterProperty experimenterProperty =
                            (org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.onf.approved.extensions.rev161128.bundle.properties.bundle.property.bundle.property.entry.BundleExperimenterProperty) messageProperty.getBundlePropertyEntry();
                    propertyBuilder.setBundlePropertyEntry(new BundleExperimenterPropertyBuilder()
                            .setExperimenter(experimenterProperty.getExperimenter())
                            .setExpType(experimenterProperty.getExpType())
                            .setBundleExperimenterPropertyData((BundleExperimenterPropertyData) experimenterProperty.getBundleExperimenterPropertyData())
                            .build());
                    properties.add(propertyBuilder.build());
                } else {
                    throw new ConversionException("Bundle property type fail: " + type);
                }
            }
        }
        builder.setBundleProperty(properties);
        return builder.build();
    }

    @Override
    public ExperimenterId getExperimenterId() {
        return ONF_EXP_ID;
    }

    @Override
    public long getType() {
        return BUNDLE_CONTROL_TYPE;
    }

}
