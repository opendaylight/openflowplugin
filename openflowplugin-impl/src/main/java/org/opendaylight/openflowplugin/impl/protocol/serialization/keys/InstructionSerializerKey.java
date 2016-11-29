/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.keys;


import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;

public class InstructionSerializerKey<T extends Instruction> extends MessageTypeKey<Instruction>
        implements ExperimenterSerializerKey {

    private Class<T> instructionType;
    private Long experimenterId;

    /**
     * @param msgVersion protocol wire version
     * @param instructionType type of instruction
     * @param experimenterId experimenter / vendor ID
     */
    public InstructionSerializerKey(short msgVersion, Class<T> instructionType, Long experimenterId) {
        super(msgVersion, Instruction.class);
        this.instructionType = instructionType;
        this.experimenterId = experimenterId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((instructionType == null) ? 0 : instructionType.hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        InstructionSerializerKey<?> other = (InstructionSerializerKey<?>) obj;
        if (instructionType == null) {
            if (other.instructionType != null) {
                return false;
            }
        } else if (!instructionType.equals(other.instructionType)) {
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
        return super.toString() + " instruction type: " + instructionType.getName() + " experimenterID: " + experimenterId;
    }
}
