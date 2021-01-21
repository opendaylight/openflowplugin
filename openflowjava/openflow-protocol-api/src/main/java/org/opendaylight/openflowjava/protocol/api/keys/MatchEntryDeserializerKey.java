/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.keys;

import java.util.Objects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Key for a match entry deserializer.
 *
 * @author michal.polkorab
 */
public final class MatchEntryDeserializerKey extends MessageCodeKey
        implements ExperimenterDeserializerKey {

    private final int oxmField;
    private Uint32 experimenterId;

    /**
     * Constructor.
     *
     * @param version protocol wire version
     * @param oxmClass oxm_class (see specification)
     * @param oxmField oxm_field (see specification)
     */
    public MatchEntryDeserializerKey(final short version, final int oxmClass, final int oxmField) {
        super(version, oxmClass, MatchEntry.class);
        this.oxmField = oxmField;
    }

    /**
     * Sets the experimenter id.
     *
     * @param experimenterId experimenter / vendor ID
     */
    public void setExperimenterId(final Uint32 experimenterId) {
        this.experimenterId = experimenterId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (experimenterId == null ? 0 : experimenterId.hashCode());
        result = prime * result + oxmField;
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
        MatchEntryDeserializerKey other = (MatchEntryDeserializerKey) obj;
        if (!Objects.equals(experimenterId, other.experimenterId)) {
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
