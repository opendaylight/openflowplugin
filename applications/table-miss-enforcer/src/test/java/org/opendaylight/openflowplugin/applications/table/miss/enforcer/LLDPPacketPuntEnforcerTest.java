/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.table.miss.enforcer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginMastershipChangeServiceProvider;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeRegistration;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeService;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;

/**
 * Test for {@link LLDPPacketPuntEnforcer}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LLDPPacketPuntEnforcerTest {

    private LLDPPacketPuntEnforcer lldpPacketPuntEnforcer;
    @Mock
    private SalFlowService flowService;
    @Mock
    private OpenFlowPluginMastershipChangeServiceProvider openFlowPluginMastershipChangeServiceProvider;
    @Mock
    private MastershipChangeServiceManager mastershipChangeServiceManager;
    @Mock
    private MastershipChangeRegistration registration;

    @Before
    public void setUp() {
        Mockito.when(openFlowPluginMastershipChangeServiceProvider.getMastershipChangeServiceManager())
                .thenReturn(mastershipChangeServiceManager);

        Mockito.when(openFlowPluginMastershipChangeServiceProvider
                .getMastershipChangeServiceManager()
                .register(Matchers.<MastershipChangeService>any())).thenReturn(registration);

        lldpPacketPuntEnforcer = new LLDPPacketPuntEnforcer(
                openFlowPluginMastershipChangeServiceProvider,
                flowService);
    }

    @Test
    public void testClose() throws Exception {
        lldpPacketPuntEnforcer.close();
        Mockito.verify(registration).close();
    }

    @Test
    public void testCreateFlow() {
        final Flow flow = TableMissUtils.createFlow();
        evaluateInstructions(flow.getInstructions());
    }

    @After
    public void tearDown() throws Exception {
        lldpPacketPuntEnforcer.close();
    }

    private static void evaluateInstructions(final Instructions instructions) {
        assertNotNull(instructions.getInstruction());
        assertEquals(1, instructions.getInstruction().size());
        Instruction instruction = instructions.getInstruction().get(0);
        evaluateInstruction(instruction);
    }

    private static void evaluateInstruction(final Instruction instruction) {
        if (instruction.getInstruction() instanceof ApplyActionsCase) {
            ApplyActionsCase applyActionsCase = (ApplyActionsCase) instruction.getInstruction();
            assertNotNull(applyActionsCase.getApplyActions().getAction());
            assertEquals(1, applyActionsCase.getApplyActions().getAction().size());
            Action action = applyActionsCase.getApplyActions().getAction().get(0);
            evaluateAction(action);
        }
    }

    private static void evaluateAction(final Action action) {
        if (action.getAction() instanceof OutputActionCase) {
            OutputActionCase outputActionCase = (OutputActionCase) action.getAction();
            assertEquals("CONTROLLER", outputActionCase.getOutputAction().getOutputNodeConnector().getValue());
            assertEquals(new Integer(0xffff).intValue(), outputActionCase.getOutputAction().getMaxLength().intValue());
        }
    }
}
