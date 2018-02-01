/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf.deserializer;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdTypeDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleControlType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundlePropertyType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.common.grouping.BundleProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.common.grouping.BundlePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.property.grouping.bundle.property.entry.BundlePropertyExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.property.grouping.bundle.property.entry.bundle.property.experimenter.BundlePropertyExperimenterData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleControlOnf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleControlOnfBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.bundle.control.onf.OnfControlGroupingDataBuilder;

/**
 * Translates BundleControl messages (OpenFlow v1.3 extension #230).
 */
public class BundleControlFactory implements OFDeserializer<BundleControlOnf>, DeserializerRegistryInjector {

    private DeserializerRegistry deserializerRegistry;

    @Override
    public BundleControlOnf deserialize(ByteBuf message) {
        BundleId bundleId = new BundleId(message.readUnsignedInt());
        BundleControlType type = BundleControlType.forValue(message.readUnsignedShort());
        BundleFlags flags = createBundleFlags(message.readUnsignedShort());
        OnfControlGroupingDataBuilder builder = new OnfControlGroupingDataBuilder();
        List<BundleProperty> properties = createBundleProperties(message);
        builder.setBundleId(bundleId)
               .setType(type)
               .setFlags(flags)
               .setBundleProperty(properties);
        return new BundleControlOnfBuilder().setOnfControlGroupingData(builder.build()).build();
    }

    private static BundleFlags createBundleFlags(final int flags) {
        Boolean isAtomic = (flags & 1 << 0) != 0;
        Boolean isOrdered = (flags & 1 << 1) != 0;
        return new BundleFlags(isAtomic, isOrdered);
    }

    private List<BundleProperty> createBundleProperties(final ByteBuf message) {
        List<BundleProperty> properties = new ArrayList<>();
        while (message.readableBytes() > 0) {
            BundlePropertyType type = BundlePropertyType.forValue(message.readUnsignedShort());
            int length = message.readUnsignedShort();
            if (type != null && type.equals(BundlePropertyType.ONFETBPTEXPERIMENTER)) {
                properties.add(createExperimenterBundleProperty(length, message));
            } else {
                message.skipBytes(length);
            }
        }
        return properties;
    }

    private BundleProperty createExperimenterBundleProperty(final int length, final ByteBuf message) {
        BundlePropertyExperimenterBuilder experimenterProperty = new BundlePropertyExperimenterBuilder();
        long experimenterId = message.readUnsignedInt();
        long expType = message.readUnsignedInt();
        experimenterProperty.setExperimenter(new ExperimenterId(experimenterId));
        experimenterProperty.setExpType(expType);

        OFDeserializer<BundlePropertyExperimenterData> deserializer = deserializerRegistry.getDeserializer(
                new ExperimenterIdTypeDeserializerKey(EncodeConstants.OF13_VERSION_ID, experimenterId, expType,
                        BundlePropertyExperimenterData.class));
        experimenterProperty.setBundlePropertyExperimenterData(
                deserializer.deserialize(message.readBytes(length - 12)));

        return new BundlePropertyBuilder().setType(BundlePropertyType.ONFETBPTEXPERIMENTER)
                .setBundlePropertyEntry(experimenterProperty.build())
                .build();
    }

    @Override
    public void injectDeserializerRegistry(DeserializerRegistry registry) {
        this.deserializerRegistry = registry;
    }

}
