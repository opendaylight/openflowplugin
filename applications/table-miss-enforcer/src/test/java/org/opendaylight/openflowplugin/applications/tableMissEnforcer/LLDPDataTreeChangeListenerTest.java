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
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Test for {@link LLDPPacketPuntEnforcer}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LLDPDataTreeChangeListenerTest {
    private LLDPPacketPuntEnforcer lldpPacketPuntEnforcer;

    private static final InstanceIdentifier<Node> nodeIID = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId("testnode:1")));

    private static final InstanceIdentifier<FlowCapableNode> nodeII =
            InstanceIdentifier.create(Nodes.class)
            .child(Node.class)
                    .augmentation(FlowCapableNode.class);
    @Mock
    private SalFlowService flowService;
    @Mock
    private DataTreeModification<FlowCapableNode> dataTreeModification;
    @Mock
    private DataObjectModification<FlowCapableNode> operationalModification;
    @Mock
    private ClusterSingletonServiceProvider clusterSingletonService;
    @Mock
    private BindingAwareBroker bindingAwareBroker;
    @Mock
    private DataBroker dataBroker;
    @Mock
    private FlowCapableNode operationalNode;
    @Mock
    private ProviderContext providerContext;
    @Mock
    private ClusterSingletonServiceRegistration registration;
    @Mock
    private ListenerRegistration<LLDPPacketPuntEnforcer> dataTreeChangeRegistration;

    @Before
    public void setUp() {
        lldpPacketPuntEnforcer = new LLDPPacketPuntEnforcer(
                bindingAwareBroker,
                dataBroker,
                clusterSingletonService,
                flowService);
        final DataTreeIdentifier<FlowCapableNode> identifier = new DataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, nodeIID);
        Mockito.when(dataTreeModification.getRootPath()).thenReturn(identifier);
        Mockito.when(dataTreeModification.getRootNode()).thenReturn(operationalModification);


        final DataTreeIdentifier<FlowCapableNode> nodeIdentifier = new DataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, nodeII);
        Mockito.when(dataBroker.registerDataTreeChangeListener(nodeIdentifier, lldpPacketPuntEnforcer)).thenReturn(dataTreeChangeRegistration);

        lldpPacketPuntEnforcer.onSessionInitiated(providerContext);

        Mockito.when(clusterSingletonService.registerClusterSingletonService(Matchers.<ClusterSingletonService>any()))
                .thenReturn(registration);
    }

    @Test
    public void testDataTreeChangeListenerRegistration() {
        lldpPacketPuntEnforcer.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verify(dataBroker).registerDataTreeChangeListener(
                Matchers.<DataTreeIdentifier<FlowCapableNode>>any(),
                Matchers.<DataTreeChangeListener<FlowCapableNode>>any());
        Mockito.verify(bindingAwareBroker).registerProvider(lldpPacketPuntEnforcer);
    }

    @Test
    public void testOnDataTreeChanged() {
        // add
        Mockito.when(dataTreeModification.getRootNode().getModificationType()).thenReturn(ModificationType.WRITE);
        Mockito.when(operationalModification.getDataBefore()).thenReturn(null);
        Mockito.when(operationalModification.getDataAfter()).thenReturn(operationalNode);

        lldpPacketPuntEnforcer.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verify(clusterSingletonService).registerClusterSingletonService(Matchers.any());
        Assert.assertEquals(1, lldpPacketPuntEnforcer.getTableMissEnforcerManager().getTableMissEnforcers().size());

        // delete
        Mockito.when(dataTreeModification.getRootNode().getModificationType()).thenReturn(ModificationType.DELETE);
        Mockito.when(operationalModification.getDataBefore()).thenReturn(operationalNode);
        Mockito.when(operationalModification.getDataAfter()).thenReturn(null);

        lldpPacketPuntEnforcer.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Assert.assertEquals(0, lldpPacketPuntEnforcer.getTableMissEnforcerManager().getTableMissEnforcers().size());
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
