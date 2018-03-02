/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.instructions;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadataCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.write.metadata._case.WriteMetadata;

public class WriteMetadataInstructionSerializer extends AbstractInstructionSerializer<WriteMetadataCase> {

    @Override
    public void serialize(WriteMetadataCase input, ByteBuf outBuffer) {
        super.serialize(input, outBuffer);
        final WriteMetadata writeMetadata = input.getWriteMetadata();
        outBuffer.writeZero(InstructionConstants.PADDING_IN_WRITE_METADATA);
        outBuffer.writeBytes(ByteUtil
                .convertBigIntegerToNBytes(writeMetadata.getMetadata(), EncodeConstants.SIZE_OF_LONG_IN_BYTES));
        outBuffer.writeBytes(ByteUtil
                .convertBigIntegerToNBytes(writeMetadata.getMetadataMask(), EncodeConstants.SIZE_OF_LONG_IN_BYTES));
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
