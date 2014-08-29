/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.lldp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;

/**
 * Created by Martin Bobak mbobak@cisco.com on 8/29/14.
 */
public class LLDPDataChangeListenerTest {

    @Mock
    private static SalFlowService flowService;

    /**
     * Test method for {@link LLDPPAcketPuntEnforcer#createFlow()}
     * which ensures that LLDPDataChangeListener creates proper flow for
     */
    @Test
    public void testCreateFlow() {
        LLDPPAcketPuntEnforcer lldpDataChangeListener = new LLDPPAcketPuntEnforcer(flowService);
        evaluateFlow(lldpDataChangeListener.createFlow());
    }

    private final void evaluateFlow(Flow flow) {
        evaluateInstructions(flow.getInstructions());
    }

    private final void evaluateInstructions(Instructions instructions) {
        assertNotNull(instructions.getInstruction());
        assertEquals(1, instructions.getInstruction().size());
        Instruction instruction = instructions.getInstruction().get(0);
        evaluateInstruction(instruction);
    }

    private final void evaluateInstruction(Instruction instruction) {
        if (instruction.getInstruction() instanceof ApplyActionsCase) {
            ApplyActionsCase applyActionsCase = (ApplyActionsCase) instruction.getInstruction();
            assertNotNull(applyActionsCase.getApplyActions().getAction());
            assertEquals(1, applyActionsCase.getApplyActions().getAction().size());
            Action action = applyActionsCase.getApplyActions().getAction().get(0);
            evaluateAction(action);
        }
    }

    private final void evaluateAction(Action action) {
        if (action.getAction() instanceof OutputActionCase) {
            OutputActionCase outputActionCase = (OutputActionCase) action.getAction();
            assertEquals("CONTROLLER", outputActionCase.getOutputAction().getOutputNodeConnector().getValue());
            assertEquals(new Integer(0xffff).intValue(), outputActionCase.getOutputAction().getMaxLength().intValue());
        }
    }
}
