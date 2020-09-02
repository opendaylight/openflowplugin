/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.instruction;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.GotoTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice._goto.table._case.GotoTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.InstructionBuilder;

/**
 * Deserializer for goto table instructions.
 *
 * @author michal.polkorab
 */
public class GoToTableInstructionDeserializer  implements OFDeserializer<Instruction>,
        HeaderDeserializer<Instruction> {

    @Override
    public Instruction deserialize(ByteBuf input) {
        input.skipBytes(2 * Short.BYTES);
        final InstructionBuilder builder = new InstructionBuilder()
                .setInstructionChoice(new GotoTableCaseBuilder()
                    .setGotoTable(new GotoTableBuilder().setTableId(readUint8(input)).build())
                    .build());
        input.skipBytes(InstructionConstants.PADDING_IN_GOTO_TABLE);
        return builder.build();
    }

    @Override
    public Instruction deserializeHeader(ByteBuf input) {
        input.skipBytes(2 * Short.BYTES);
        return new InstructionBuilder().setInstructionChoice(new GotoTableCaseBuilder().build()).build();
    }
}
