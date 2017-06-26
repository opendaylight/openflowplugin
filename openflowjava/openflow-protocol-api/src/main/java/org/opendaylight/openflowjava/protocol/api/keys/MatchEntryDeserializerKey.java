/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.api.keys;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;

/**
 * @author michal.polkorab
 *
 */
public final class MatchEntryDeserializerKey extends MessageCodeKey
        implements ExperimenterDeserializerKey {

    private int oxmField;
    private Long experimenterId;

    /**
     * @param version protocol wire version
     * @param oxmClass oxm_class (see specification)
     * @param oxmField oxm_field (see specification)
     */
    public MatchEntryDeserializerKey(short version,
            int oxmClass, int oxmField) {
        super(version, oxmClass, MatchEntry.class);
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
        result = prime * result + oxmField;
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
        MatchEntryDeserializerKey other = (MatchEntryDeserializerKey) obj;
        if (experimenterId == null) {
            if (other.experimenterId != null) {
                return false;
            }
        } else if (!experimenterId.equals(other.experimenterId)) {
            return false;
        }
        if (oxmField != other.oxmField) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + " oxm_field: " + oxmField + " experimenterID: " + experimenterId;
    }
}