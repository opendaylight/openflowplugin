/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.keys;

import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdSerializerKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandType;
import org.opendaylight.yangtools.yang.binding.DataContainer;

public class ExperimenterIdMeterBandTypeSerializerKey<T extends DataContainer> extends ExperimenterIdSerializerKey<T> {

    private Class<? extends MeterBandType> meterBandType;

    /**
     * @param msgVersion      protocol wire version
     * @param experimenterId  experimenter / vendor ID
     * @param objectClass     class of object to be serialized
     * @param meterBandType   vendor defined subtype
     */
    public ExperimenterIdMeterBandTypeSerializerKey(short msgVersion, long experimenterId,
                                                    Class<T> objectClass, Class<? extends MeterBandType> meterBandType) {
        super(msgVersion, experimenterId, objectClass);
        this.meterBandType = meterBandType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((meterBandType == null) ? 0 : meterBandType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!super.equals(obj)) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final ExperimenterIdMeterBandTypeSerializerKey other = ExperimenterIdMeterBandTypeSerializerKey.class.cast(obj);

        if (meterBandType == null) {
            if (other.meterBandType != null) {
                return false;
            }
        } else if (!meterBandType.equals(other.meterBandType)) {
            return false;
        }

        return true;
    }
}
