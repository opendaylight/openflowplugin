/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.keys;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Key for an action serializer.
 *
 * @author michal.polkorab
 * @param <T> action type
 */
public class ActionSerializerKey<T extends ActionChoice> extends MessageTypeKey<Action>
        implements ExperimenterSerializerKey {

    private final Class<T> actionType;
    private final Uint32 experimenterId;

    /**
     * Constructor.
     *
     * @param msgVersion protocol wire version
     * @param actionType type of action
     * @param experimenterId experimenter / vendor ID
     */
    public ActionSerializerKey(final short msgVersion, final Class<T> actionType, final Uint32 experimenterId) {
        super(msgVersion, Action.class);
        this.actionType = actionType;
        this.experimenterId = experimenterId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (actionType == null ? 0 : actionType.hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        ActionSerializerKey<?> other = (ActionSerializerKey<?>) obj;
        if (actionType == null) {
            if (other.actionType != null) {
                return false;
            }
        } else if (!actionType.equals(other.actionType)) {
            return false;
        }
        if (experimenterId == null) {
            if (other.experimenterId != null) {
                return false;
            }
        } else if (!experimenterId.equals(other.experimenterId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + " action type: " + actionType.getName() + " experimenterID: " + experimenterId;
    }
}
