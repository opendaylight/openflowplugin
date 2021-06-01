/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.api.keys;

import java.util.Objects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.InstructionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Key for an instruction serializer.
 *
 * @author michal.polkorab
 * @param <T> instruction type
 */
public class InstructionSerializerKey<T extends InstructionChoice>
        extends MessageTypeKey<Instruction> implements ExperimenterSerializerKey {

    private final Class<T> instructionType;
    private final Long experimenterId;

    /**
     * Constructor.
     *
     * @param msgVersion protocol wire version
     * @param instructionType type of instruction
     * @param experimenterId experimenter / vendor ID
     */
    public InstructionSerializerKey(final Uint8 msgVersion, final Class<T> instructionType, final Long experimenterId) {
        super(msgVersion, Instruction.class);
        this.instructionType = instructionType;
        this.experimenterId = experimenterId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (experimenterId == null ? 0 : experimenterId.hashCode());
        result = prime * result + (instructionType == null ? 0 : instructionType.hashCode());
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
        if (!(obj instanceof InstructionSerializerKey)) {
            return false;
        }
        InstructionSerializerKey<?> other = (InstructionSerializerKey<?>) obj;
        if (!Objects.equals(experimenterId, other.experimenterId)) {
            return false;
        }
        if (!Objects.equals(instructionType, other.instructionType)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + " instructionType type: " + instructionType.getName()
                + " vendorID: " + experimenterId;
    }
}
