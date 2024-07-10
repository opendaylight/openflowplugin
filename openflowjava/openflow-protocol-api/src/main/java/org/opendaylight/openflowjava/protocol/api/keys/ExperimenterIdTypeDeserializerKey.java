/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.keys;

import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Key for an experimenter id type deserializer.
 *
 * @author michal.polkorab
 */
public class ExperimenterIdTypeDeserializerKey extends ExperimenterIdDeserializerKey {
    private final long type;

    /**
     * Constructor.
     *
     * @param <T>            type of target experimenter object
     * @param version        protocol wire version
     * @param experimenterId experimenter / vendor ID
     * @param type           data type according to vendor implementation
     * @param objectClass    class of object to be serialized
     */
    public <T extends DataContainer> ExperimenterIdTypeDeserializerKey(final Uint8 version, final Uint32 experimenterId,
                                                                       final long type, final Class<T> objectClass) {
        super(version, experimenterId, objectClass);
        this.type = type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + hashCodeOfLong(type);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof ExperimenterIdTypeDeserializerKey)) {
            return false;
        }
        ExperimenterIdTypeDeserializerKey other = (ExperimenterIdTypeDeserializerKey) obj;
        if (type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + "; type: " + type;
    }
}
