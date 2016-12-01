/**
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.BundleControlType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.BundleId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.BundlePropertyType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.bundle.properties.BundleProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.bundle.properties.BundlePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.bundle.properties.bundle.property.bundle.property.entry.BundleExperimenterPropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.bundle.properties.bundle.property.bundle.property.entry.bundle.experimenter.property.BundleExperimenterPropertyData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.experimenter.input.experimenter.data.of.choice.BundleControl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.experimenter.input.experimenter.data.of.choice.BundleControlBuilder;

/**
 * Translates BundleControl messages (OpenFlow v1.3 extension #230).
 */
public class BundleControlFactory implements OFDeserializer<BundleControl>, DeserializerRegistryInjector {

    private DeserializerRegistry deserializerRegistry;

    @Override
    public BundleControl deserialize(ByteBuf message) {
        BundleId bundleId = new BundleId(message.readUnsignedInt());
        BundleControlType type = BundleControlType.forValue(message.readUnsignedShort());
        BundleFlags flags = createBundleFlags(message.readUnsignedShort());
        BundleControlBuilder builder = new BundleControlBuilder();
        List<BundleProperty> properties = createBundleProperties(message);
        return builder.setBundleId(bundleId)
                .setType(type)
                .setFlags(flags)
                .setBundleProperty(properties)
                .build();
    }

    private static BundleFlags createBundleFlags(final int flags) {
        Boolean isAtomic = (flags & (1 << 0)) != 0;
        Boolean isOrdered = (flags & (1 << 1)) != 0;
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
        BundleExperimenterPropertyBuilder experimenterProperty = new BundleExperimenterPropertyBuilder();
        long experimenterId = message.readUnsignedInt();
        long expType = message.readUnsignedInt();
        experimenterProperty.setExperimenter(new ExperimenterId(experimenterId));
        experimenterProperty.setExpType(expType);

        OFDeserializer<BundleExperimenterPropertyData> deserializer = deserializerRegistry.getDeserializer(
                new ExperimenterIdTypeDeserializerKey(EncodeConstants.OF13_VERSION_ID, experimenterId, expType,
                        BundleExperimenterPropertyData.class));
        experimenterProperty.setBundleExperimenterPropertyData(deserializer.deserialize(message.readBytes(length - 12)));

        return new BundlePropertyBuilder().setType(BundlePropertyType.ONFETBPTEXPERIMENTER)
                .setBundlePropertyEntry(experimenterProperty.build())
                .build();
    }

    @Override
    public void injectDeserializerRegistry(DeserializerRegistry deserializerRegistry) {
        this.deserializerRegistry = deserializerRegistry;
    }

}
