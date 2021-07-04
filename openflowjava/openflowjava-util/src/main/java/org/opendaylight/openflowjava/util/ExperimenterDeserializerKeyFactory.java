/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.util;

import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdTypeDeserializerKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.queue.property.header.QueueProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeatureProperties;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Factory for creating experimenter deserializer keys.
 *
 * @author michal.polkorab
 */
public final class ExperimenterDeserializerKeyFactory {

    private ExperimenterDeserializerKeyFactory() {
        //not called
    }

    /**
     * Creates an experimenter error deserializer key.
     *
     * @param version openflow wire version
     * @param experimenterId experimenter / vendor ID
     * @return key instance
     */
    public static ExperimenterIdDeserializerKey createExperimenterErrorDeserializerKey(
            final Uint8 version, final Uint32 experimenterId) {
        return new ExperimenterIdDeserializerKey(version, experimenterId, ErrorMessage.class);
    }

    /**
     * Creates an experimenter message deserializer key.
     *
     * @param version openflow wire version
     * @param experimenterId experimenter / vendor ID
     * @param type experimenter type according to vendor implementation
     * @return key instance
     */
    public static ExperimenterIdTypeDeserializerKey createExperimenterMessageDeserializerKey(
            final Uint8 version, final Uint32 experimenterId, final long type) {
        return new ExperimenterIdTypeDeserializerKey(version, experimenterId, type, ExperimenterDataOfChoice.class);
    }

    /**
     * Creates a vendor message deserializer key.
     *
     * @param version openflow wire version
     * @param experimenterId experimenter / vendor ID
     * @return key instance
     */
    public static ExperimenterIdDeserializerKey createVendorMessageDeserializerKey(
            final Uint8 version, final Uint32 experimenterId) {
        return new ExperimenterIdDeserializerKey(version, experimenterId, ExperimenterDataOfChoice.class);
    }

    /**
     * Creates a multi-part reply message deserializer key.
     *
     * @param version openflow wire version
     * @param experimenterId experimenter / vendor ID
     * @param type the type
     * @return key instance
     */
    public static ExperimenterIdTypeDeserializerKey createMultipartReplyMessageDeserializerKey(
            final Uint8 version, final Uint32 experimenterId, final long type) {
        return new ExperimenterIdTypeDeserializerKey(version, experimenterId, type, ExperimenterDataOfChoice.class);
    }

    /**
     * Creates a multi-part reply vendor message deserializer key.
     *
     * @param version openflow wire version
     * @param experimenterId experimenter / vendor ID
     * @return key instance
     */
    public static ExperimenterIdDeserializerKey createMultipartReplyVendorMessageDeserializerKey(
            final Uint8 version, final Uint32 experimenterId) {
        return new ExperimenterIdDeserializerKey(version, experimenterId, ExperimenterDataOfChoice.class);
    }

    /**
     * Creates a multi-part reply TF deserializer key.
     *
     * @param version openflow wire version
     * @param experimenterId experimenter / vendor ID
     * @return key instance
     */
    public static ExperimenterIdDeserializerKey createMultipartReplyTFDeserializerKey(
            final Uint8 version, final Uint32 experimenterId) {
        return new ExperimenterIdDeserializerKey(version, experimenterId, TableFeatureProperties.class);
    }

    /**
     * Creates a queue property deserializer key.
     *
     * @param version openflow wire version
     * @param experimenterId experimenter / vendor ID
     * @return key instance
     */
    public static ExperimenterIdDeserializerKey createQueuePropertyDeserializerKey(
            final Uint8 version, final Uint32 experimenterId) {
        return new ExperimenterIdDeserializerKey(version, experimenterId, QueueProperty.class);
    }

    /**
     * Creates a meter band deserializer key.
     *
     * @param version openflow wire version
     * @param experimenterId experimenter / vendor ID
     * @return key instance
     */
    public static ExperimenterIdDeserializerKey createMeterBandDeserializerKey(
            final Uint8 version, final Uint32 experimenterId) {
        return new ExperimenterIdDeserializerKey(version, experimenterId, MeterBandExperimenterCase.class);
    }
}
