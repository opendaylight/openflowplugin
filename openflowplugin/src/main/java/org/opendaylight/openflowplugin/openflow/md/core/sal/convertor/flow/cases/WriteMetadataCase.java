/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow.cases;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.write.metadata._case.WriteMetadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteMetadataCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.write.metadata._case.WriteMetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.InstructionBuilder;

public class WriteMetadataCase extends ConvertorCase<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadataCase, Instruction, ActionConvertorData> {
    public WriteMetadataCase() {
        super(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadataCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<Instruction> process(final @Nonnull org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadataCase source, final ActionConvertorData data, ConvertorExecutor convertorExecutor) {
        WriteMetadata writeMetadata = source.getWriteMetadata();
        WriteMetadataCaseBuilder writeMetadataCaseBuilder = new WriteMetadataCaseBuilder();
        WriteMetadataBuilder writeMetadataBuilder = new WriteMetadataBuilder();
        writeMetadataBuilder.setMetadata(ByteUtil.convertBigIntegerToNBytes(writeMetadata.getMetadata(), OFConstants.SIZE_OF_LONG_IN_BYTES));
        writeMetadataBuilder.setMetadataMask(ByteUtil.convertBigIntegerToNBytes(writeMetadata.getMetadataMask(), OFConstants.SIZE_OF_LONG_IN_BYTES));
        writeMetadataCaseBuilder.setWriteMetadata(writeMetadataBuilder.build());
        InstructionBuilder instructionBuilder = new InstructionBuilder();
        instructionBuilder.setInstructionChoice(writeMetadataCaseBuilder.build());
        return Optional.of(instructionBuilder.build());
    }
}
