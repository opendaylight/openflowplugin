/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.api.keys;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;

/**
 * @author michal.polkorab
 * @param <C> oxm_class (see specification)
 * @param <F> oxm_field (see specification)
 */
public final class MatchEntrySerializerKey<C extends OxmClassBase, F extends MatchField>
        extends MessageTypeKey<MatchEntry> implements ExperimenterSerializerKey {

    private Class<C> oxmClass;
    private Class<F> oxmField;
    private Long experimenterId;

    /**
     * @param msgVersion protocol wire version
     * @param oxmClass oxm_class (see specification)
     * @param oxmField oxm_field (see specification)
     */
    public MatchEntrySerializerKey(short msgVersion, Class<C> oxmClass,
            Class<F> oxmField) {
        super(msgVersion, MatchEntry.class);
        this.oxmClass = oxmClass;
        this.oxmField = oxmField;
    }

    /**
     * @param experimenterId experimenter / vendor ID
     */
    public void setExperimenterId(Long experimenterId) {
        this.experimenterId = experimenterId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((experimenterId == null) ? 0 : experimenterId.hashCode());
        result = prime * result + ((oxmClass == null) ? 0 : oxmClass.hashCode());
        result = prime * result + ((oxmField == null) ? 0 : oxmField.hashCode());
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
        MatchEntrySerializerKey<?, ?> other = (MatchEntrySerializerKey<?, ?>) obj;
        if (experimenterId == null) {
            if (other.experimenterId != null) {
                return false;
            }
        } else if (!experimenterId.equals(other.experimenterId)) {
            return false;
        }
        if (oxmClass == null) {
            if (other.oxmClass != null) {
                return false;
            }
        } else if (!oxmClass.equals(other.oxmClass)) {
            return false;
        }
        if (oxmField == null) {
            if (other.oxmField != null) {
                return false;
            }
        } else if (!oxmField.equals(other.oxmField)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + " oxm_class: " + oxmClass.getName() + " oxm_field: "
                + oxmField.getName() + " experimenterID: " + experimenterId;
    }
}
