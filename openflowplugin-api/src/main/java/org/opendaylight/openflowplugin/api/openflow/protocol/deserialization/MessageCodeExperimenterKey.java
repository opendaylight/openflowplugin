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

public class MessageCodeExperimenterKey extends MessageCodeKey implements ExperimenterDeserializerKey {

    private Long experimenterId;

    /**
     * Constructor.
     * @param version wire protocol version
     * @param value used as distinguisher (read from binary data / buffer)
     * @param clazz class of object that is going to be deserialized
     * @param experimenterId experimenter id
     */
    public MessageCodeExperimenterKey(short version, int value, Class<?> clazz, Long experimenterId) {
        super(version, value, clazz);
        this.experimenterId = experimenterId;
    }

    public Long getExperimenterId() {
        return experimenterId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime * super.hashCode() + (Objects.isNull(experimenterId) ? 0 : experimenterId.intValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MessageCodeExperimenterKey)) {
            return false;
        }
        MessageCodeExperimenterKey other = (MessageCodeExperimenterKey) obj;

        if (experimenterId == null) {
            if (other.experimenterId != null) {
                return false;
            }
        } else if (!experimenterId.equals(other.experimenterId)) {
            return false;
        }

        return super.equals(obj);
    }

    @Override
    public String toString() {
        return super.toString() + " experimenterId: " + experimenterId;
    }
}
