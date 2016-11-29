/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.instructions;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadataCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadataCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.write.metadata._case.WriteMetadataBuilder;

public class WriteMetadataInstructionSerializerTest extends AbstractInstructionSerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final long metadata = 10L;
        final long metadataMask = 10L;

        final Instruction instruction = new WriteMetadataCaseBuilder()
                .setWriteMetadata(new WriteMetadataBuilder()
                        .setMetadata(BigInteger.valueOf(metadata))
                        .setMetadataMask(BigInteger.valueOf(metadataMask))
                        .build())
                .build();

        assertInstruction(instruction, out -> {
            out.skipBytes(InstructionConstants.PADDING_IN_WRITE_METADATA);
            assertEquals(out.readLong(), metadata);
            assertEquals(out.readLong(), metadataMask);
        });
    }

    @Override
    protected Class<? extends Instruction> getClazz() {
        return WriteMetadataCase.class;
    }

    @Override
    protected int getType() {
        return InstructionConstants.WRITE_METADATA_TYPE;
    }

    @Override
    protected int getLength() {
        return InstructionConstants.WRITE_METADATA_LENGTH;
    }

}
