/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.keys;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Key for an experimenter id serializer.
 *
 * @author michal.polkorab
 * @param <T> class of object to be serialized
 */
public class ExperimenterIdSerializerKey<T extends DataContainer> extends MessageTypeKey<T>
        implements ExperimenterSerializerKey {

    private final Uint32 experimenterId;

    /**
     * Constructor.
     *
     * @param msgVersion protocol wire version
     * @param experimenterId experimenter / vendor ID
     * @param objectClass class of object to be serialized
     */
    public ExperimenterIdSerializerKey(final Uint8 msgVersion,
                                       final Uint32 experimenterId, final Class<T> objectClass) {
        super(msgVersion, objectClass);
        this.experimenterId = requireNonNull(experimenterId);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + experimenterId.hashCode();
        return result;
    }

    protected int hashCodeOfLong(final long longValue) {
        return (int) (longValue ^ longValue >>> 32);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof ExperimenterIdSerializerKey)) {
            return false;
        }
        ExperimenterIdSerializerKey<?> other = (ExperimenterIdSerializerKey<?>) obj;
        if (experimenterId != other.experimenterId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + " experimenterID: " + experimenterId;
    }
}
