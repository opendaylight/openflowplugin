/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.util;

import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdMeterSubTypeSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdTypeSerializerKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterMeterBandSubType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeatureProperties;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Factory for creating experimenter serializer keys.
 *
 * @author michal.polkorab
 */
public abstract class ExperimenterSerializerKeyFactory {

    /**
     * Creates an experimenter message serializer key.
     *
     * @param msgVersion openflow wire version
     * @param experimenterId experimenter / vendor ID
     * @param type experimenter type according to vendor implementation
     * @return key instance
     */
    public static ExperimenterIdSerializerKey<ExperimenterDataOfChoice> createExperimenterMessageSerializerKey(
            final Uint8 msgVersion, final Uint32 experimenterId, final long type) {
        return new ExperimenterIdTypeSerializerKey<>(msgVersion, experimenterId, type, ExperimenterDataOfChoice.class);
    }

    /**
     * Creates a multi-part request serializer key.
     *
     * @param msgVersion openflow wire version
     * @param experimenterId experimenter / vendor ID
     * @param type experimenter type according to vendor implementation
     * @return key instance
     */
    public static ExperimenterIdSerializerKey<ExperimenterDataOfChoice> createMultipartRequestSerializerKey(
            final Uint8 msgVersion, final Uint32 experimenterId, final long type) {
        return new ExperimenterIdTypeSerializerKey<>(msgVersion, experimenterId, type, ExperimenterDataOfChoice.class);
    }

    /**
     * Creates a multi-part request TF serializer key.
     *
     * @param msgVersion openflow wire version
     * @param experimenterId experimenter / vendor ID
     * @return key instance
     */
    public static ExperimenterIdSerializerKey<TableFeatureProperties> createMultipartRequestTFSerializerKey(
            final Uint8 msgVersion, final Uint32 experimenterId) {
        return new ExperimenterIdSerializerKey<>(msgVersion, experimenterId, TableFeatureProperties.class);
    }

    /**
     * Creates a meter band serializer key.
     *
     * @param msgVersion openflow wire version
     * @param experimenterId experimenter / vendor ID
     * @return key instance
     */
    public static ExperimenterIdSerializerKey<MeterBandExperimenterCase> createMeterBandSerializerKey(
            final Uint8 msgVersion, final Uint32 experimenterId) {
        return new ExperimenterIdMeterSubTypeSerializerKey<>(msgVersion, experimenterId,
                MeterBandExperimenterCase.class, null);
    }

    public static ExperimenterIdSerializerKey<MeterBandExperimenterCase> createMeterBandSerializerKey(
            final Uint8 msgVersion, final Uint32 experimenterId,
            final ExperimenterMeterBandSubType meterSubType) {
        return new ExperimenterIdMeterSubTypeSerializerKey<>(msgVersion, experimenterId,
                MeterBandExperimenterCase.class, meterSubType);
    }
}
