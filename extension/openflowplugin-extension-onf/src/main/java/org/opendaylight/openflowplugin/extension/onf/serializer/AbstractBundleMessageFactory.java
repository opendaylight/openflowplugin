/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.onf.serializer;

import io.netty.buffer.ByteBuf;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdTypeSerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundlePropertyType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.common.grouping.BundleProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.property.grouping.bundle.property.entry.BundlePropertyExperimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.property.grouping.bundle.property.entry.bundle.property.experimenter.BundlePropertyExperimenterData;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for common stuff of bundle messages.
 */
public abstract class AbstractBundleMessageFactory<T extends DataContainer> implements OFSerializer<T>,
        SerializerRegistryInjector {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractBundleMessageFactory.class);
    protected SerializerRegistry serializerRegistry;

    @Override
    public void injectSerializerRegistry(final SerializerRegistry registry) {
        this.serializerRegistry = registry;
    }

    static void writeBundleFlags(final BundleFlags bundleFlags, final ByteBuf outBuffer) {
        short flagsBitMap = fillBitMask(bundleFlags.getAtomic(), bundleFlags.getOrdered());
        outBuffer.writeShort(flagsBitMap);
    }

    void writeBundleProperties(final List<BundleProperty> properties, final ByteBuf outBuffer) {
        for (BundleProperty property : properties) {
            BundlePropertyType type = property.getType();
            if (type != null && type.equals(BundlePropertyType.ONFETBPTEXPERIMENTER)) {
                final int startIndex = outBuffer.writerIndex();
                outBuffer.writeShort(type.getIntValue());
                int lengthIndex = outBuffer.writerIndex();
                outBuffer.writeShort(EncodeConstants.EMPTY_LENGTH);
                writeBundleExperimenterProperty((BundlePropertyExperimenter)property.getBundlePropertyEntry(),
                        outBuffer);
                outBuffer.setShort(lengthIndex, outBuffer.writerIndex() - startIndex);
            } else {
                LOG.warn("Trying to serialize unknown bundle property (type: {}), skipping",
                        type != null ? type.getIntValue() : 0);
            }
        }
    }

    private void writeBundleExperimenterProperty(final BundlePropertyExperimenter property, final ByteBuf outBuffer) {
        int experimenterId = property.getExperimenter().getValue().intValue();
        int expType = property.getExpType().intValue();
        outBuffer.writeInt(experimenterId);
        outBuffer.writeInt(expType);
        OFSerializer<BundlePropertyExperimenterData> serializer = serializerRegistry.getSerializer(
                new ExperimenterIdTypeSerializerKey<>(EncodeConstants.OF13_VERSION_ID, experimenterId, expType,
                        BundlePropertyExperimenterData.class));
        serializer.serialize(property.getBundlePropertyExperimenterData(), outBuffer);
    }

    private static short fillBitMask(final boolean... values) {
        short bitmask = 0;
        short index = 0;
        for (boolean v : values) {
            if (v) {
                bitmask |= 1 << index;
            }
            ++index;
        }
        return bitmask;
    }
}
