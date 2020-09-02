/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.deserialization.instruction;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteMetadataCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.write.metadata._case.WriteMetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.InstructionBuilder;

/**
 * Deserializer for write metadata instructions.
 *
 * @author michal.polkorab
 */
public class WriteMetadataInstructionDeserializer implements OFDeserializer<Instruction>,
        HeaderDeserializer<Instruction> {

    @Override
    public Instruction deserialize(ByteBuf input) {
        final InstructionBuilder builder = new InstructionBuilder();
        input.skipBytes(2 * Short.BYTES);
        input.skipBytes(InstructionConstants.PADDING_IN_WRITE_METADATA);
        final WriteMetadataCaseBuilder caseBuilder = new WriteMetadataCaseBuilder();
        WriteMetadataBuilder metadataBuilder = new WriteMetadataBuilder();
        byte[] metadata = new byte[Long.BYTES];
        input.readBytes(metadata);
        metadataBuilder.setMetadata(metadata);
        byte[] metadataMask = new byte[Long.BYTES];
        input.readBytes(metadataMask);
        metadataBuilder.setMetadataMask(metadataMask);
        caseBuilder.setWriteMetadata(metadataBuilder.build());
        builder.setInstructionChoice(caseBuilder.build());
        return builder.build();
    }

    @Override
    public Instruction deserializeHeader(ByteBuf input) {
        InstructionBuilder builder = new InstructionBuilder();
        input.skipBytes(2 * Short.BYTES);
        builder.setInstructionChoice(new WriteMetadataCaseBuilder().build());
        return builder.build();
    }
}
