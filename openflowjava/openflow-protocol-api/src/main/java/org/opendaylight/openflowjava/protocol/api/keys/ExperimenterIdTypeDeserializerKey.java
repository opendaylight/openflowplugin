/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.api.keys;

import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * @author michal.polkorab
 */
public class ExperimenterIdTypeDeserializerKey extends ExperimenterIdDeserializerKey {

    private long type;

    /**
     * @param <T>            type of target experimenter object
     * @param version        protocol wire version
     * @param experimenterId experimenter / vendor ID
     * @param type           data type according to vendor implementation
     * @param objectClass    class of object to be serialized
     */
    public <T extends DataContainer> ExperimenterIdTypeDeserializerKey(final short version, final long experimenterId,
                                                                       final long type, Class<T> objectClass) {
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
    public boolean equals(Object obj) {
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