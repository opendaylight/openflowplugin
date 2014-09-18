/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.InstructionBuilder;

/**
 * Created by Martin Bobak mbobak@cisco.com on 9/18/14.
 */
public class OFToMDSalFlowConvertorTest {

    private static final int PRESET_COUNT = 7;

    @Test
    /**
     * Test method for {@link OFToMDSalFlowConvertor#toSALInstruction(java.util.List, org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion)}
     */
    public void testToSALInstruction() {
        List<Instruction> instructions = new ArrayList<>();
        InstructionBuilder instructionBuilder = new InstructionBuilder();
        for (int i = 0; i < PRESET_COUNT; i++) {
            instructions.add(instructionBuilder.build());
        }
        OFToMDSalFlowConvertor.toSALInstruction(instructions, OpenflowVersion.OF13);
    }
}
