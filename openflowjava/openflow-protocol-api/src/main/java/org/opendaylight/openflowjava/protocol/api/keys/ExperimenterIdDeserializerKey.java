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
 * @author michal.polkorab
 */
public class ExperimenterIdDeserializerKey extends MessageCodeKey implements ExperimenterDeserializerKey {

    private long experimenterId;

    /**
     * @param <E> type of target experimenter object
     * @param version protocol wire version
     * @param experimenterId experimenter / vendor ID
     * @param objectClass class of created object
     */
    public <E extends DataContainer> ExperimenterIdDeserializerKey(final short version, final long experimenterId,
                                                                   final Class<E> objectClass) {
        super(version, EncodeConstants.EXPERIMENTER_VALUE, objectClass);
        this.experimenterId = experimenterId;
    }

    protected int hashCodeOfLong(long longValue) {
        return (int) (longValue ^ (longValue >>> 32));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + hashCodeOfLong(experimenterId);
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
        if (!(obj instanceof ExperimenterIdDeserializerKey)) {
            return false;
        }
        ExperimenterIdDeserializerKey other = (ExperimenterIdDeserializerKey) obj;
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