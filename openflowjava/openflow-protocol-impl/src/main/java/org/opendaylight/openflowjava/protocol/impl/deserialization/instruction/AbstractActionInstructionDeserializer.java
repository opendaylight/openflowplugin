/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.deserialization.instruction;

import static java.util.Objects.requireNonNull;

import io.netty.buffer.ByteBuf;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.CodeKeyMaker;
import org.opendaylight.openflowjava.protocol.impl.util.CodeKeyMakerFactory;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ListDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;

/** Base class for an action instruction deserializer.
 *
 * @author michal.polkorab
 */
public abstract class AbstractActionInstructionDeserializer implements OFDeserializer<Instruction> {
    private final DeserializerRegistry registry;

    protected AbstractActionInstructionDeserializer(final DeserializerRegistry registry) {
        this.registry = requireNonNull(registry);
    }

    protected List<Action> deserializeActions(final ByteBuf input, final int instructionLength) {
        int length = instructionLength - InstructionConstants.STANDARD_INSTRUCTION_LENGTH;
        CodeKeyMaker keyMaker = CodeKeyMakerFactory.createActionsKeyMaker(EncodeConstants.OF13_VERSION_ID);
        return ListDeserializer.deserializeList(EncodeConstants.OF13_VERSION_ID, length, input, keyMaker, registry);
    }

    protected final DeserializerRegistry getRegistry() {
        return registry;
    }
}
