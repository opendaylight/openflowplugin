/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.keys;

import java.util.Objects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Key for a match entry serializer.
 *
 * @author michal.polkorab
 * @param <C> oxm_class (see specification)
 * @param <F> oxm_field (see specification)
 */
public final class MatchEntrySerializerKey<C extends OxmClassBase, F extends MatchField>
        extends MessageTypeKey<MatchEntry> implements ExperimenterSerializerKey {

    private final Class<C> oxmClass;
    private final Class<F> oxmField;

    private Uint32 experimenterId;

    /**
     * Constructor.
     *
     * @param msgVersion protocol wire version
     * @param oxmClass oxm_class (see specification)
     * @param oxmField oxm_field (see specification)
     */
    public MatchEntrySerializerKey(final Uint8 msgVersion, final Class<C> oxmClass, final Class<F> oxmField) {
        super(msgVersion, MatchEntry.class);
        this.oxmClass = oxmClass;
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
        result = prime * result + (oxmClass == null ? 0 : oxmClass.hashCode());
        result = prime * result + (oxmField == null ? 0 : oxmField.hashCode());
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
        MatchEntrySerializerKey<?, ?> other = (MatchEntrySerializerKey<?, ?>) obj;
        if (!Objects.equals(experimenterId, other.experimenterId)) {
            return false;
        }
        if (!Objects.equals(oxmClass, other.oxmClass)) {
            return false;
        }
        if (!Objects.equals(oxmField, other.oxmField)) {
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
