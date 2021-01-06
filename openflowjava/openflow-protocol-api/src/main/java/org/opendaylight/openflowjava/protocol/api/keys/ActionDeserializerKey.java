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

/**
 * Key for an action deserializer.
 *
 * @author michal.polkorab
 */
public class ActionDeserializerKey extends MessageCodeKey<Action> {
    private final Long experimenterId;

    /**
     * Constructor.
     *
     * @param version protocol wire version
     * @param type action type
     * @param experimenterId experimenter / vendor ID
     */
    public ActionDeserializerKey(final short version, final int type, final Long experimenterId) {
        super(version, type, Action.class);
        this.experimenterId = experimenterId;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hashCode(experimenterId);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        return super.equals(obj) && obj instanceof ActionDeserializerKey
            && Objects.equals(experimenterId, ((ActionDeserializerKey) obj).experimenterId);
    }

    @Override
    public String toString() {
        return super.toString() + " experimenterID: " + experimenterId;
    }
}
