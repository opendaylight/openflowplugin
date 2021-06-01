/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.protocol.deserialization;

import java.util.Objects;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.yangtools.yang.common.Uint8;

public class MessageCodeExperimenterKey extends MessageCodeKey implements ExperimenterDeserializerKey {
    private final Long experimenterId;

    /**
     * Constructor.
     * @param version wire protocol version
     * @param value used as distinguisher (read from binary data / buffer)
     * @param clazz class of object that is going to be deserialized
     * @param experimenterId experimenter id
     */
    public MessageCodeExperimenterKey(final Uint8 version, final int value, final Class<?> clazz,
            final Long experimenterId) {
        super(version, value, clazz);
        this.experimenterId = experimenterId;
    }

    public Long getExperimenterId() {
        return experimenterId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime * super.hashCode() + (experimenterId == null ? 0 : experimenterId.intValue());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MessageCodeExperimenterKey)) {
            return false;
        }
        MessageCodeExperimenterKey other = (MessageCodeExperimenterKey) obj;
        return Objects.equals(experimenterId, other.experimenterId) && super.equals(obj);
    }

    @Override
    public String toString() {
        return super.toString() + " experimenterId: " + experimenterId;
    }
}
