/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadataCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.write.metadata._case.WriteMetadataBuilder;

public class WriteMetadataInstructionDeserializer extends AbstractInstructionDeserializer {
    @Override
    public Instruction deserialize(ByteBuf message) {
        processHeader(message);
        message.skipBytes(InstructionConstants.PADDING_IN_WRITE_METADATA);

        final byte[] meta = new byte[Long.BYTES];
        message.readBytes(meta);
        final byte[] metaMask = new byte[Long.BYTES];
        message.readBytes(metaMask);

        return new WriteMetadataCaseBuilder()
                .setWriteMetadata(new WriteMetadataBuilder()
                        .setMetadata(new BigInteger(1, meta))
                        .setMetadataMask(new BigInteger(1, metaMask))
                        .build())
                .build();
    }

    @Override
    public Instruction deserializeHeader(ByteBuf message) {
        processHeader(message);
        return new WriteMetadataCaseBuilder().build();
    }
}
