/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.keys;

import java.util.Objects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.experimenter.id.action.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.ExperimenterActionSubType;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Key for an experimenter action serializer.
 *
 * @author michal.polkorab
 */
public final class ExperimenterActionSerializerKey extends ActionSerializerKey<ExperimenterIdCase> {
    private final ExperimenterActionSubType actionSubType;

    /**
     * Constructor.
     *
     * @param msgVersion protocol wire version
     * @param experimenterId experimenter / vendor ID
     * @param actionSubType vendor defined subtype
     */
    public ExperimenterActionSerializerKey(final Uint8 msgVersion, final Uint32 experimenterId,
            final ExperimenterActionSubType actionSubType) {
        super(msgVersion, ExperimenterIdCase.class, experimenterId);
        this.actionSubType = actionSubType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (actionSubType == null ? 0 : actionSubType.hashCode());
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
        ExperimenterActionSerializerKey other = (ExperimenterActionSerializerKey) obj;
        if (!Objects.equals(actionSubType, other.actionSubType)) {
            return false;
        }
        return true;
    }
}
