/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl.strategy;

import java.math.BigInteger;
import java.util.Collections;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link FlowForwarder}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FlowForwarderTest {
    private final NodeKey s1Key = new NodeKey(new NodeId("S1"));
    private final TableKey tableKey = new TableKey((short) 2);
    private final FlowId flowId = new FlowId("test_Flow");
    private final FlowKey flowKey = new FlowKey(flowId);
    private final Match emptyMatch = new MatchBuilder().build();
    private final Flow flow = new FlowBuilder()
            .setId(flowId)
            .setTableId((short) 2)
            .setMatch(emptyMatch)
            .build();

    private final KeyedInstanceIdentifier<Node, NodeKey> nodePath = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, s1Key);
    private final InstanceIdentifier<FlowCapableNode> flowCapableNodePath = nodePath
            .augmentation(FlowCapableNode.class);
    private final InstanceIdentifier<Table> tableII = flowCapableNodePath.child(Table.class, tableKey);
    private final InstanceIdentifier<Flow> flowPath = tableII.child(Flow.class, flowKey);

    private FlowForwarder flowForwarder;

    @Mock
    private SalFlowService salFlowService;
    @Captor
    private ArgumentCaptor<AddFlowInput> addFlowInputCpt;
    @Captor
    private ArgumentCaptor<UpdateFlowInput> updateFlowInputCpt;
    @Captor
    private ArgumentCaptor<RemoveFlowInput> removeFlowInputCpt;


    @Before
    public void setUp() throws Exception {
        flowForwarder = new FlowForwarder(salFlowService);
    }

    @Test
    public void addTest() throws Exception {
        Mockito.when(salFlowService.addFlow(addFlowInputCpt.capture())).thenReturn(
                RpcResultBuilder.success(
                        new AddFlowOutputBuilder()
                                .setTransactionId(new TransactionId(BigInteger.ONE))
                                .build()).buildFuture());

        final Future<RpcResult<AddFlowOutput>> addResult = flowForwarder.add(flowPath, flow, flowCapableNodePath);

        Mockito.verify(salFlowService).addFlow(Matchers.<AddFlowInput>any());
        final AddFlowInput flowInput = addFlowInputCpt.getValue();
        Assert.assertEquals(2, flowInput.getTableId().shortValue());
        Assert.assertEquals(emptyMatch, flowInput.getMatch());
        Assert.assertEquals(null, flowInput.getInstructions());
        Assert.assertEquals(nodePath, flowInput.getNode().getValue());
        Assert.assertEquals(flowPath, flowInput.getFlowRef().getValue());
        Assert.assertEquals(null, flowInput.isStrict());


        final RpcResult<AddFlowOutput> addFlowOutputRpcResult = addResult.get(2, TimeUnit.SECONDS);
        Assert.assertTrue(addFlowOutputRpcResult.isSuccessful());
        final AddFlowOutput resultValue = addFlowOutputRpcResult.getResult();
        Assert.assertEquals(1, resultValue.getTransactionId().getValue().intValue());
    }

    @Test
    public void updateTest() throws Exception {
        Mockito.when(salFlowService.updateFlow(updateFlowInputCpt.capture())).thenReturn(
                RpcResultBuilder.success(
                        new UpdateFlowOutputBuilder()
                                .setTransactionId(new TransactionId(BigInteger.ONE))
                                .build()).buildFuture());

        final Instructions originalInstructions = new InstructionsBuilder()
                .setInstruction(Collections.singletonList(new InstructionBuilder()
                        .setInstruction(new ApplyActionsCaseBuilder()
                                .setApplyActions(new ApplyActionsBuilder()
                                        .setAction(Collections.singletonList(new ActionBuilder()
                                                .setAction(new DropActionCaseBuilder()
                                                        .build())
                                                .build())
                                        ).build()
                                ).build())
                        .build())
                ).build();

        final Flow flowUpdated = new FlowBuilder(flow)
                .setInstructions(originalInstructions)
                .setMatch(new MatchBuilder().build())
                .build();

        final Future<RpcResult<UpdateFlowOutput>> updateResult = flowForwarder.update(flowPath, flow, flowUpdated, flowCapableNodePath);

        Mockito.verify(salFlowService).updateFlow(Matchers.<UpdateFlowInput>any());
        final UpdateFlowInput updateFlowInput = updateFlowInputCpt.getValue();
        final OriginalFlow flowOrigInput = updateFlowInput.getOriginalFlow();
        final UpdatedFlow flowInput = updateFlowInput.getUpdatedFlow();

        Assert.assertEquals(nodePath, updateFlowInput.getNode().getValue());
        Assert.assertEquals(flowPath, updateFlowInput.getFlowRef().getValue());

        Assert.assertEquals(2, flowInput.getTableId().shortValue());
        Assert.assertEquals(emptyMatch, flowInput.getMatch());
        Assert.assertEquals(originalInstructions, flowInput.getInstructions());
        Assert.assertEquals(true, flowInput.isStrict());

        Assert.assertEquals(2, flowOrigInput.getTableId().shortValue());
        Assert.assertEquals(emptyMatch, flowOrigInput.getMatch());
        Assert.assertEquals(null, flowOrigInput.getInstructions());
        Assert.assertEquals(true, flowOrigInput.isStrict());


        final RpcResult<UpdateFlowOutput> updateFlowOutputRpcResult = updateResult.get(2, TimeUnit.SECONDS);
        Assert.assertTrue(updateFlowOutputRpcResult.isSuccessful());
        final UpdateFlowOutput resultValue = updateFlowOutputRpcResult.getResult();
        Assert.assertEquals(1, resultValue.getTransactionId().getValue().intValue());
    }

    @Test
    public void removeTest() throws Exception {
        Mockito.when(salFlowService.removeFlow(removeFlowInputCpt.capture())).thenReturn(
                RpcResultBuilder.success(
                        new RemoveFlowOutputBuilder()
                                .setTransactionId(new TransactionId(BigInteger.ONE))
                                .build()).buildFuture());

        final Flow removeFlow = new FlowBuilder(flow).build();
        final Future<RpcResult<RemoveFlowOutput>> removeResult = flowForwarder.remove(flowPath, removeFlow, flowCapableNodePath);

        Mockito.verify(salFlowService).removeFlow(Matchers.<RemoveFlowInput>any());
        final RemoveFlowInput flowInput = removeFlowInputCpt.getValue();
        Assert.assertEquals(2, flowInput.getTableId().shortValue());
        Assert.assertEquals(emptyMatch, flowInput.getMatch());
        Assert.assertEquals(null, flowInput.getInstructions());
        Assert.assertEquals(true, flowInput.isStrict());


        final RpcResult<RemoveFlowOutput> removeFlowOutputRpcResult = removeResult.get(2, TimeUnit.SECONDS);
        Assert.assertTrue(removeFlowOutputRpcResult.isSuccessful());
        final RemoveFlowOutput resultValue = removeFlowOutputRpcResult.getResult();
        Assert.assertEquals(1, resultValue.getTransactionId().getValue().intValue());
    }
}
