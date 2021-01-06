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
 * Key for an experimenter id type deserializer.
 *
 * @param <T> type of target experimenter object
 * @author michal.polkorab
 */
public class ExperimenterIdTypeDeserializerKey<T extends DataContainer> extends ExperimenterIdDeserializerKey<T> {
    private final long type;

    /**
     * Constructor.
     *
     * @param version        protocol wire version
     * @param experimenterId experimenter / vendor ID
     * @param type           data type according to vendor implementation
     * @param objectClass    class of object to be serialized
     */
    public ExperimenterIdTypeDeserializerKey(final short version, final long experimenterId, final long type,
            final Class<T> objectClass) {
        super(version, experimenterId, objectClass);
        this.type = type;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Long.hashCode(type);
    }

    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj) && obj instanceof ExperimenterIdTypeDeserializerKey
            && type == ((ExperimenterIdTypeDeserializerKey<?>) obj).type;
    }

    @Override
    public String toString() {
        return super.toString() + "; type: " + type;
    }
}
