/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.api.keys;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.action.container.action.choice.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.ExperimenterActionSubType;

/**
 * @author michal.polkorab
 */
public final class ExperimenterActionSerializerKey extends ActionSerializerKey<ExperimenterIdCase>
        implements ExperimenterSerializerKey {

    private Class<? extends ExperimenterActionSubType> actionSubType;

    /**
     * @param msgVersion protocol wire version
     * @param experimenterId experimenter / vendor ID
     * @param actionSubType vendor defined subtype
     */
    public ExperimenterActionSerializerKey(short msgVersion, Long experimenterId, Class<? extends ExperimenterActionSubType> actionSubType) {
        super(msgVersion, ExperimenterIdCase.class, experimenterId);
        this.actionSubType = actionSubType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((actionSubType == null) ? 0 : actionSubType.hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        ExperimenterActionSerializerKey other = (ExperimenterActionSerializerKey) obj;
        if (actionSubType == null) {
            if (other.actionSubType != null) {
                return false;
            }
        } else if (!actionSubType.equals(other.actionSubType)) {
            return false;
        }
        return true;
    }
}