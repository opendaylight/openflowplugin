/*
 * Copyright (c) 2016 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.keys;

import java.util.Objects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterMeterBandSubType;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Key for an experimenter id meter subtype serializer.
 *
 * @author hyy on 2016/9/8.
 */
public class ExperimenterIdMeterSubTypeSerializerKey<T extends DataContainer> extends ExperimenterIdSerializerKey<T> {
    private final ExperimenterMeterBandSubType meterSubType;

    /**
     * Constructor.
     *
     * @param msgVersion      protocol wire version
     * @param experimenterId  experimenter / vendor ID
     * @param objectClass     class of object to be serialized
     * @param meterSubType    vendor defined subtype
     */
    public ExperimenterIdMeterSubTypeSerializerKey(final Uint8 msgVersion, final Uint32 experimenterId,
            final Class<T> objectClass, final ExperimenterMeterBandSubType meterSubType) {
        super(msgVersion, experimenterId, objectClass);
        this.meterSubType = meterSubType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (meterSubType == null ? 0 : meterSubType.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ExperimenterIdMeterSubTypeSerializerKey<?> other = (ExperimenterIdMeterSubTypeSerializerKey<?>) obj;
        if (!Objects.equals(meterSubType, other.meterSubType)) {
            return false;
        }
        return true;
    }

}
