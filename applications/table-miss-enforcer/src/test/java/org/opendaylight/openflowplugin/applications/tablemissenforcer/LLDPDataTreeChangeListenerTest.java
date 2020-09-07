/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.tablemissenforcer;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.deviceownershipservice.DeviceOwnershipService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;

/**
 * Test for {@link LLDPPacketPuntEnforcer}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LLDPDataTreeChangeListenerTest {
    private LLDPPacketPuntEnforcer lldpPacketPuntEnforcer;
    private static final InstanceIdentifier<Node> NODE_IID = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId("testnode:1")));
    @Mock
    private SalFlowService flowService;
    @Mock
    private DataTreeModification<FlowCapableNode> dataTreeModification;
    @Mock
    private DeviceOwnershipService deviceOwnershipService;
    @Captor
    private ArgumentCaptor<AddFlowInput> addFlowInputCaptor;

    @Before
    public void setUp() {
        doReturn(RpcResultBuilder.success().buildFuture()).when(flowService).addFlow(any());
        lldpPacketPuntEnforcer = new LLDPPacketPuntEnforcer(flowService, mock(DataBroker.class),
                deviceOwnershipService);
        final DataTreeIdentifier<FlowCapableNode> identifier = DataTreeIdentifier.create(
                LogicalDatastoreType.OPERATIONAL, NODE_IID.augmentation(FlowCapableNode.class));
        when(dataTreeModification.getRootPath()).thenReturn(identifier);
        when(dataTreeModification.getRootNode()).thenReturn(mock(DataObjectModification.class));
        when(dataTreeModification.getRootNode().getModificationType()).thenReturn(ModificationType.WRITE);
        when(deviceOwnershipService.isEntityOwned(any())).thenReturn(true);
    }

    @After
    public void tearDown() {
        lldpPacketPuntEnforcer.close();
    }

    @Test
    public void testOnDataTreeChanged() {
        lldpPacketPuntEnforcer.onDataTreeChanged(Collections.singleton(dataTreeModification));
        verify(flowService).addFlow(addFlowInputCaptor.capture());
        AddFlowInput captured = addFlowInputCaptor.getValue();
        assertEquals(NODE_IID, captured.getNode().getValue());
    }

    @Test
    public void testCreateFlow() {
        final Flow flow = LLDPPacketPuntEnforcer.createFlow();
        final Instructions instructions = flow.getInstructions();
        final Map<InstructionKey, Instruction> insns = instructions.getInstruction();
        assertNotNull(insns);
        assertEquals(1, insns.size());

        final Instruction instruction = insns.values().iterator().next();
        assertNotNull(instruction);

        final var insn = instruction.getInstruction();
        assertThat(insn, instanceOf(ApplyActionsCase.class));
        final ApplyActionsCase applyActionsCase = (ApplyActionsCase) insn;
        assertNotNull(applyActionsCase.getApplyActions().getAction());
        assertEquals(1, applyActionsCase.getApplyActions().getAction().size());

        final Action action = applyActionsCase.getApplyActions().getAction().values().iterator().next().getAction();
        assertThat(action, instanceOf(OutputActionCase.class));
        final OutputActionCase outputActionCase = (OutputActionCase) action;
        assertEquals("CONTROLLER", outputActionCase.getOutputAction().getOutputNodeConnector().getValue());
        assertEquals(Uint16.MAX_VALUE, outputActionCase.getOutputAction().getMaxLength());
    }
}
