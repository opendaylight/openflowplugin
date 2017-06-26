/*
 * Copyright (c) 2016 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.keys;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterMeterBandSubType;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Created by hyy on 2016/9/8.
 */
public class ExperimenterIdMeterSubTypeSerializerKey<T extends DataContainer> extends ExperimenterIdSerializerKey<T> {

    private Class<? extends ExperimenterMeterBandSubType> meterSubType;

    /**
     * @param msgVersion      protocol wire version
     * @param experimenterId  experimenter / vendor ID
     * @param objectClass     class of object to be serialized
     * @param meterSubType    vendor defined subtype
     */
    public ExperimenterIdMeterSubTypeSerializerKey(short msgVersion, long experimenterId,
                                                   Class<T> objectClass, Class<? extends ExperimenterMeterBandSubType> meterSubType) {
        super(msgVersion, experimenterId, objectClass);
        this.meterSubType = meterSubType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((meterSubType == null) ? 0 : meterSubType.hashCode());
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
        ExperimenterIdMeterSubTypeSerializerKey other = (ExperimenterIdMeterSubTypeSerializerKey) obj;
        if (meterSubType == null) {
            if (other.meterSubType != null) {
                return false;
            }
        } else if (!meterSubType.equals(other.meterSubType)) {
            return false;
        }
        return true;
    }

}
