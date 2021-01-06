/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.keys;

import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Key for an experimenter id deserializer.
 *
 * @param <E> type of target experimenter object
 * @author michal.polkorab
 */
public class ExperimenterIdDeserializerKey<E extends DataContainer> extends MessageCodeKey<E>
        implements ExperimenterDeserializerKey {
    private final long experimenterId;

    /**
     * Constructor.
     *
     * @param version protocol wire version
     * @param experimenterId experimenter / vendor ID
     * @param objectClass class of created object
     */
    public ExperimenterIdDeserializerKey(final short version, final long experimenterId, final Class<E> objectClass) {
        super(version, EncodeConstants.EXPERIMENTER_VALUE, objectClass);
        this.experimenterId = experimenterId;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Long.hashCode(experimenterId);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        return super.equals(obj) && obj instanceof ExperimenterIdDeserializerKey
            && experimenterId == ((ExperimenterIdDeserializerKey<?>) obj).experimenterId;
    }

    @Override
    public String toString() {
        return super.toString() + " experimenterID: " + experimenterId;
    }
}
