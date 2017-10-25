/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.api.keys;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;

/**
 * @author michal.polkorab
 *
 */
public class InstructionDeserializerKey extends MessageCodeKey {

    private Long experimenterId;
    /**
     * @param version protocol wire version
     * @param type instruction type
     * @param experimenterId experimenter (vendor) identifier
     */
    public InstructionDeserializerKey(short version, int type,
            Long experimenterId) {
        super(version, type, Instruction.class);
        this.experimenterId = experimenterId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((experimenterId == null) ? 0 : experimenterId.hashCode());
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
        if (!(obj instanceof InstructionDeserializerKey)) {
            return false;
        }
        InstructionDeserializerKey other = (InstructionDeserializerKey) obj;
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
        return super.toString() + " experimenterID: " + experimenterId;
    }
}