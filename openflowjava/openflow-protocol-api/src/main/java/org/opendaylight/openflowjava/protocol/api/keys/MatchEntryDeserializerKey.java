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
public final class MatchEntryDeserializerKey extends MessageCodeKey<MatchEntry>
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
        result = prime * result + Objects.hashCode(experimenterId);
        result = prime * result + oxmField;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }
        final MatchEntryDeserializerKey other = (MatchEntryDeserializerKey) obj;
        return Objects.equals(experimenterId, other.experimenterId) && oxmField == other.oxmField;
    }

    @Override
    public String toString() {
        return super.toString() + " oxm_field: " + oxmField + " experimenterID: " + experimenterId;
    }
}
