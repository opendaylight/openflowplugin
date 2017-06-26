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

/**
 * @author michal.polkorab
 */
public abstract class ExperimenterSerializerKeyFactory {

    /**
     * @param msgVersion openflow wire version
     * @param experimenterId experimenter / vendor ID
     * @param type experimenter type according to vendor implementation
     * @return key instance
     */
    public static ExperimenterIdSerializerKey<ExperimenterDataOfChoice> createExperimenterMessageSerializerKey(
            short msgVersion, long experimenterId, long type) {
        return new ExperimenterIdTypeSerializerKey<>(msgVersion, experimenterId, type, ExperimenterDataOfChoice.class);
    }

    /**
     * @param msgVersion openflow wire version
     * @param experimenterId experimenter / vendor ID
     * @param type experimenter type according to vendor implementation
     * @return key instance
     */
    public static ExperimenterIdSerializerKey<ExperimenterDataOfChoice> createMultipartRequestSerializerKey(
            short msgVersion, long experimenterId, long type) {
        return new ExperimenterIdTypeSerializerKey<>(msgVersion, experimenterId, type, ExperimenterDataOfChoice.class);
    }

    /**
     * @param msgVersion openflow wire version
     * @param experimenterId experimenter / vendor ID
     * @return key instance
     */
    public static ExperimenterIdSerializerKey<TableFeatureProperties> createMultipartRequestTFSerializerKey(
            short msgVersion, long experimenterId) {
        return new ExperimenterIdSerializerKey<>(msgVersion, experimenterId, TableFeatureProperties.class);
    }

    /**
     * @param msgVersion openflow wire version
     * @param experimenterId experimenter / vendor ID
     * @return key instance
     */
    public static ExperimenterIdSerializerKey<MeterBandExperimenterCase> createMeterBandSerializerKey(
            short msgVersion, long experimenterId) {
        return new ExperimenterIdMeterSubTypeSerializerKey<>(msgVersion, experimenterId, MeterBandExperimenterCase.class, null);
    }

    public static ExperimenterIdSerializerKey<MeterBandExperimenterCase> createMeterBandSerializerKey(
            short msgVersion, long experimenterId, Class<? extends ExperimenterMeterBandSubType> meterSubType) {
        return new ExperimenterIdMeterSubTypeSerializerKey<>(msgVersion, experimenterId, MeterBandExperimenterCase.class, meterSubType);
    }

}