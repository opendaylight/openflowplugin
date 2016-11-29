/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.instructions;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.function.Consumer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.AbstractSerializerTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;

public abstract class AbstractInstructionSerializerTest extends AbstractSerializerTest {
    private AbstractInstructionSerializer serializer;

    @Override
    protected void init() {
        serializer = getRegistry().getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, getClazz()));
    }

    protected void assertInstruction(final Instruction instruction, final Consumer<ByteBuf> assertBody) {
        // Header serialization
        final ByteBuf bufferHeader = UnpooledByteBufAllocator.DEFAULT.buffer();
        serializer.serializeHeader(instruction, bufferHeader);
        assertEquals(bufferHeader.readUnsignedShort(), getType());
        assertEquals(bufferHeader.readUnsignedShort(), InstructionConstants.INSTRUCTION_IDS_LENGTH);
        assertEquals(bufferHeader.readableBytes(), 0);

        // Header and body serialization
        final ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(instruction, buffer);
        assertEquals(buffer.readUnsignedShort(), getType());
        assertEquals(buffer.readUnsignedShort(), getLength());
        assertBody.accept(buffer);
        assertEquals(buffer.readableBytes(), 0);
    }

    protected AbstractInstructionSerializer getSerializer() {
        return serializer;
    }

    protected abstract Class<? extends Instruction> getClazz();
    protected abstract int getType();
    protected abstract int getLength();
}