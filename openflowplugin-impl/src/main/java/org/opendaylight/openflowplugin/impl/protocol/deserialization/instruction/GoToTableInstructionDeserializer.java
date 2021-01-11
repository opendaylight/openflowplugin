/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.go.to.table._case.GoToTableBuilder;
import org.opendaylight.yangtools.yang.common.Uint8;

public class GoToTableInstructionDeserializer extends AbstractInstructionDeserializer {

    @Override
    public Instruction deserialize(final ByteBuf message) {
        processHeader(message);
        final Uint8 tableId = readUint8(message);
        message.skipBytes(InstructionConstants.PADDING_IN_GOTO_TABLE);

        return new GoToTableCaseBuilder()
                .setGoToTable(new GoToTableBuilder()
                        .setTableId(tableId)
                        .build())
                .build();
    }

    @Override
    public Instruction deserializeHeader(final ByteBuf message) {
        processHeader(message);
        return new GoToTableCaseBuilder().build();
    }

}
