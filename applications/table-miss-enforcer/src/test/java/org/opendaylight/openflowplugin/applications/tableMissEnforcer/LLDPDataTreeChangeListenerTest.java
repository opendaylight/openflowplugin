/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.tableMissEnforcer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Test for {@link LLDPPacketPuntEnforcer}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LLDPDataTreeChangeListenerTest {
    private LLDPPacketPuntEnforcer lldpPacketPuntEnforcer;
    private final static InstanceIdentifier<Node> nodeIID = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId("testnode:1")));
    @Mock
    private SalFlowService flowService;
    @Mock
    private DataTreeModification<FlowCapableNode> dataTreeModification;
    @Captor
    private ArgumentCaptor<AddFlowInput> addFlowInputCaptor;

    @Before
    public void setUp() {
        lldpPacketPuntEnforcer = new LLDPPacketPuntEnforcer(flowService, Mockito.mock(DataBroker.class));
        final DataTreeIdentifier<FlowCapableNode> identifier = new DataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, nodeIID);
        Mockito.when(dataTreeModification.getRootPath()).thenReturn(identifier);
        Mockito.when(dataTreeModification.getRootNode()).thenReturn(Mockito.mock(DataObjectModification.class));
        Mockito.when(dataTreeModification.getRootNode().getModificationType()).thenReturn(ModificationType.WRITE);
    }

    @Test
    public void testOnDataTreeChanged() {
        lldpPacketPuntEnforcer.onDataTreeChanged(Collections.singleton(dataTreeModification));
        Mockito.verify(flowService).addFlow(addFlowInputCaptor.capture());
        AddFlowInput captured = addFlowInputCaptor.getValue();
        Assert.assertEquals(nodeIID, captured.getNode().getValue());
    }

    @Test
    public void testCreateFlow() {
        final Flow flow = lldpPacketPuntEnforcer.createFlow();
        evaluateInstructions(flow.getInstructions());
    }

    @After
    public void tearDown() {
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
