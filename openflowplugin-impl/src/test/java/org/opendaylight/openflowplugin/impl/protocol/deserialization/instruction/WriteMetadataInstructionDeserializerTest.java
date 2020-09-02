/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.math.BigInteger;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadataCase;

public class WriteMetadataInstructionDeserializerTest extends AbstractInstructionDeserializerTest {
    @Test
    public void testDeserialize() {
        final ByteBuf in = UnpooledByteBufAllocator.DEFAULT.buffer();
        final BigInteger metadata = BigInteger.valueOf(1234L);
        final BigInteger metadataMask = BigInteger.valueOf(9876L);
        writeHeader(in);
        in.writeZero(InstructionConstants.PADDING_IN_WRITE_METADATA);
        in.writeBytes(ByteUtil.convertBigIntegerToNBytes(metadata, Long.BYTES));
        in.writeBytes(ByteUtil.convertBigIntegerToNBytes(metadataMask, Long.BYTES));

        final Instruction instruction = deserializeInstruction(in);
        assertEquals(WriteMetadataCase.class, instruction.implementedInterface());

        assertArrayEquals(
                ByteUtil.convertBigIntegerToNBytes(metadata, Long.BYTES),
                ByteUtil.uint64toBytes(((WriteMetadataCase) instruction).getWriteMetadata().getMetadata()));

        assertArrayEquals(
                ByteUtil.convertBigIntegerToNBytes(metadataMask, Long.BYTES),
                ByteUtil.uint64toBytes(((WriteMetadataCase) instruction).getWriteMetadata().getMetadataMask()));

        assertEquals(0, in.readableBytes());
    }

    @Override
    protected short getType() {
        return InstructionConstants.WRITE_METADATA_TYPE;
    }

    @Override
    protected short getLength() {
        return InstructionConstants.STANDARD_INSTRUCTION_LENGTH;
    }
}
