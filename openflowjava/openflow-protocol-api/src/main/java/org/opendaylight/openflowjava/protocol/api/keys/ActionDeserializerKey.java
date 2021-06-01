/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.keys;

import java.util.Objects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Key for an action deserializer.
 *
 * @author michal.polkorab
 */
public class ActionDeserializerKey extends MessageCodeKey {
    private final Long experimenterId;

    /**
     * Constructor.
     *
     * @param version protocol wire version
     * @param type action type
     * @param experimenterId experimenter / vendor ID
     */
    public ActionDeserializerKey(final Uint8 version, final int type, final Long experimenterId) {
        super(version, type, Action.class);
        this.experimenterId = experimenterId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (experimenterId == null ? 0 : experimenterId.hashCode());
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
        if (!(obj instanceof ActionDeserializerKey)) {
            return false;
        }
        ActionDeserializerKey other = (ActionDeserializerKey) obj;
        return Objects.equals(experimenterId, other.experimenterId);
    }

    @Override
    public String toString() {
        return super.toString() + " experimenterID: " + experimenterId;
    }
}
