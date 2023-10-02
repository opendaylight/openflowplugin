/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.sal;

import com.google.common.collect.ImmutableClassToInstanceMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.add.flows.batch.input.BatchAddFlows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.add.flows.batch.input.BatchAddFlowsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.input.update.grouping.OriginalBatchedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.input.update.grouping.UpdatedBatchedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.output.list.grouping.BatchFailedFlowsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.remove.flows.batch.input.BatchRemoveFlows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.remove.flows.batch.input.BatchRemoveFlowsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.update.flows.batch.input.BatchUpdateFlows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.update.flows.batch.input.BatchUpdateFlowsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link SalFlowsBatchRpcs}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SalFlowsBatchRpcsTest {

    private static final Logger LOG = LoggerFactory.getLogger(SalFlowsBatchRpcsTest.class);

    public static final NodeId NODE_ID = new NodeId("ut-dummy-node");
    public static final NodeKey NODE_KEY = new NodeKey(NODE_ID);
    public static final NodeRef NODE_REF =
            new NodeRef(InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY));

    @Mock
    private SalFlowRpcs salFlowRpcs;
    @Mock
    private FlowCapableTransactionRpc transactionService;
    @Mock
    private SendBarrier sendBarrier;
    @Mock
    private RemoveFlow removeFlow;
    @Mock
    private AddFlow addFlow;
    @Mock
    private UpdateFlow updateFlow;
    @Captor
    private ArgumentCaptor<RemoveFlowInput> removeFlowInputCpt;
    @Captor
    private ArgumentCaptor<UpdateFlowInput> updateFlowInputCpt;
    @Captor
    private ArgumentCaptor<AddFlowInput> addFlowInputCpt;

    private SalFlowsBatchRpcs salFlowsBatchRpcs;

    public static final String FLOW_ID_VALUE_1 = "ut-dummy-flow1";
    public static final String FLOW_ID_VALUE_2 = "ut-dummy-flow2";

    @Before
    public void setUp() {
        salFlowsBatchRpcs = new SalFlowsBatchRpcs(salFlowRpcs, transactionService);

        Mockito.when(transactionService.getRpcClassToInstanceMap())
                .thenReturn(ImmutableClassToInstanceMap.of(SendBarrier.class, sendBarrier));
        Mockito.when(sendBarrier.invoke(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.<SendBarrierOutput>success().buildFuture());
    }

    @After
    public void tearDown() {
        Mockito.verifyNoMoreInteractions(removeFlow, addFlow, updateFlow, sendBarrier);
    }

    @Test
    public void testRemoveFlowsBatch_success() throws Exception {
        Mockito.when(salFlowRpcs.getRpcClassToInstanceMap())
                .thenReturn(ImmutableClassToInstanceMap.of(RemoveFlow.class, removeFlow));
        Mockito.when(removeFlow.invoke(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new RemoveFlowOutputBuilder().build())
                        .buildFuture());

        final String flow1IdValue = "ut-dummy-flow1";
        final String flow2IdValue = "ut-dummy-flow2";
        final BatchRemoveFlows batchFlow1 = createEmptyBatchRemoveFlow(flow1IdValue, 42);
        final BatchRemoveFlows batchFlow2 = createEmptyBatchRemoveFlow(flow2IdValue, 43);

        final RemoveFlowsBatchInput input = new RemoveFlowsBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchRemoveFlows(BindingMap.ordered(batchFlow1, batchFlow2))
                .build();

        final Future<RpcResult<RemoveFlowsBatchOutput>> resultFuture = salFlowsBatchRpcs.removeFlowsBatch(input);

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<RemoveFlowsBatchOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        final RemoveFlowsBatchOutput result = rpcResult.getResult();
        Assert.assertEquals(0, result.nonnullBatchFailedFlowsOutput().size());

        final InOrder inOrder = Mockito.inOrder(removeFlow, sendBarrier);

        inOrder.verify(removeFlow, Mockito.times(2)).invoke(removeFlowInputCpt.capture());
        final List<RemoveFlowInput> allValues = removeFlowInputCpt.getAllValues();
        Assert.assertEquals(2, allValues.size());
        Assert.assertEquals(42, allValues.get(0).getPriority().longValue());
        Assert.assertEquals(43, allValues.get(1).getPriority().longValue());

        inOrder.verify(sendBarrier).invoke(ArgumentMatchers.any());
    }

    @Test
    public void testRemoveFlowsBatch_failed() throws Exception {
        Mockito.when(salFlowRpcs.getRpcClassToInstanceMap())
                .thenReturn(ImmutableClassToInstanceMap.of(RemoveFlow.class, removeFlow));
        Mockito.when(removeFlow.invoke(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.<RemoveFlowOutput>failed()
                        .withError(ErrorType.APPLICATION, "flow-remove-fail-1")
                        .buildFuture());

        final BatchRemoveFlows batchFlow1 = createEmptyBatchRemoveFlow(FLOW_ID_VALUE_1, 42);
        final BatchRemoveFlows batchFlow2 = createEmptyBatchRemoveFlow(FLOW_ID_VALUE_2, 43);

        final RemoveFlowsBatchInput input = new RemoveFlowsBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchRemoveFlows(BindingMap.ordered(batchFlow1, batchFlow2))
                .build();

        final Future<RpcResult<RemoveFlowsBatchOutput>> resultFuture = salFlowsBatchRpcs.removeFlowsBatch(input);

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<RemoveFlowsBatchOutput> rpcResult = resultFuture.get();
        Assert.assertFalse(rpcResult.isSuccessful());
        final RemoveFlowsBatchOutput result = rpcResult.getResult();
        Iterator<BatchFailedFlowsOutput> iterator = result.nonnullBatchFailedFlowsOutput().values().iterator();
        Assert.assertEquals(2, result.nonnullBatchFailedFlowsOutput().size());
        Assert.assertEquals(FLOW_ID_VALUE_1, iterator.next().getFlowId().getValue());
        Assert.assertEquals(FLOW_ID_VALUE_2, iterator.next().getFlowId().getValue());

        final InOrder inOrder = Mockito.inOrder(removeFlow, sendBarrier);

        inOrder.verify(removeFlow, Mockito.times(2)).invoke(removeFlowInputCpt.capture());
        final List<RemoveFlowInput> allValues = removeFlowInputCpt.getAllValues();
        Assert.assertEquals(2, allValues.size());
        Assert.assertEquals(42, allValues.get(0).getPriority().longValue());
        Assert.assertEquals(43, allValues.get(1).getPriority().longValue());

        inOrder.verify(sendBarrier).invoke(ArgumentMatchers.any());
    }

    private static BatchAddFlows createEmptyBatchAddFlow(final String flowIdValue, final int priority) {
        return new BatchAddFlowsBuilder()
                .setFlowId(new FlowId(flowIdValue))
                .setPriority(Uint16.valueOf(priority))
                .setMatch(new MatchBuilder().build())
                .setTableId(Uint8.ZERO)
                .build();
    }

    private static BatchRemoveFlows createEmptyBatchRemoveFlow(final String flowIdValue, final int priority) {
        return new BatchRemoveFlowsBuilder()
                .setFlowId(new FlowId(flowIdValue))
                .setPriority(Uint16.valueOf(priority))
                .setMatch(new MatchBuilder().build())
                .setTableId(Uint8.ZERO)
                .build();
    }

    private static BatchUpdateFlows createEmptyBatchUpdateFlow(final String flowIdValue, final int priority) {
        final BatchAddFlows emptyOriginalFlow = createEmptyBatchAddFlow(flowIdValue, priority);
        final BatchAddFlows emptyUpdatedFlow = createEmptyBatchAddFlow(flowIdValue, priority + 1);
        return new BatchUpdateFlowsBuilder()
                .setFlowId(new FlowId(flowIdValue))
                .setOriginalBatchedFlow(new OriginalBatchedFlowBuilder(emptyOriginalFlow).build())
                .setUpdatedBatchedFlow(new UpdatedBatchedFlowBuilder(emptyUpdatedFlow).build())
                .build();
    }

    @Test
    public void testAddFlowsBatch_success() throws Exception {
        Mockito.when(salFlowRpcs.getRpcClassToInstanceMap())
                .thenReturn(ImmutableClassToInstanceMap.of(AddFlow.class, addFlow));
        Mockito.when(addFlow.invoke(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new AddFlowOutputBuilder().build()).buildFuture());

        final AddFlowsBatchInput input = new AddFlowsBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchAddFlows(BindingMap.ordered(
                        createEmptyBatchAddFlow("ut-dummy-flow1", 42),
                        createEmptyBatchAddFlow("ut-dummy-flow2", 43)))
                .build();

        final Future<RpcResult<AddFlowsBatchOutput>> resultFuture = salFlowsBatchRpcs.addFlowsBatch(input);

        Assert.assertTrue(resultFuture.isDone());
        Assert.assertTrue(resultFuture.get().isSuccessful());

        final InOrder inOrder = Mockito.inOrder(addFlow, sendBarrier);

        inOrder.verify(addFlow, Mockito.times(2)).invoke(addFlowInputCpt.capture());
        final List<AddFlowInput> allValues = addFlowInputCpt.getAllValues();
        Assert.assertEquals(2, allValues.size());
        Assert.assertEquals(42, allValues.get(0).getPriority().longValue());
        Assert.assertEquals(43, allValues.get(1).getPriority().longValue());

        inOrder.verify(sendBarrier).invoke(ArgumentMatchers.any());
    }

    @Test
    public void testAddFlowsBatch_failed() throws Exception {
        Mockito.when(salFlowRpcs.getRpcClassToInstanceMap())
                .thenReturn(ImmutableClassToInstanceMap.of(AddFlow.class, addFlow));
        Mockito.when(addFlow.invoke(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder
                        .<AddFlowOutput>failed().withError(ErrorType.APPLICATION, "ut-groupAddError")
                        .buildFuture());

        final AddFlowsBatchInput input = new AddFlowsBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchAddFlows(BindingMap.ordered(
                        createEmptyBatchAddFlow(FLOW_ID_VALUE_1, 42),
                        createEmptyBatchAddFlow(FLOW_ID_VALUE_2, 43)))
                .build();

        final Future<RpcResult<AddFlowsBatchOutput>> resultFuture = salFlowsBatchRpcs.addFlowsBatch(input);
        Iterator<BatchFailedFlowsOutput> iterator = resultFuture.get().getResult().nonnullBatchFailedFlowsOutput()
                .values().iterator();

        Assert.assertTrue(resultFuture.isDone());
        Assert.assertFalse(resultFuture.get().isSuccessful());
        Assert.assertEquals(2, resultFuture.get().getResult().nonnullBatchFailedFlowsOutput().size());
        Assert.assertEquals(FLOW_ID_VALUE_1, iterator.next().getFlowId().getValue());
        Assert.assertEquals(FLOW_ID_VALUE_2, iterator.next().getFlowId().getValue());
        Assert.assertEquals(2, resultFuture.get().getErrors().size());

        final InOrder inOrder = Mockito.inOrder(addFlow, sendBarrier);

        inOrder.verify(addFlow, Mockito.times(2)).invoke(addFlowInputCpt.capture());
        final List<AddFlowInput> allValues = addFlowInputCpt.getAllValues();
        Assert.assertEquals(2, allValues.size());
        Assert.assertEquals(42, allValues.get(0).getPriority().longValue());
        Assert.assertEquals(43, allValues.get(1).getPriority().longValue());

        inOrder.verify(sendBarrier).invoke(ArgumentMatchers.any());
    }

    @Test
    public void testUpdateFlowsBatch_success() throws Exception {
        Mockito.when(salFlowRpcs.getRpcClassToInstanceMap())
                .thenReturn(ImmutableClassToInstanceMap.of(UpdateFlow.class, updateFlow));
        Mockito.when(updateFlow.invoke(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new UpdateFlowOutputBuilder().build()).buildFuture());

        final UpdateFlowsBatchInput input = new UpdateFlowsBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchUpdateFlows(BindingMap.ordered(
                        createEmptyBatchUpdateFlow(FLOW_ID_VALUE_1, 42),
                        createEmptyBatchUpdateFlow(FLOW_ID_VALUE_2, 44)))
                .build();

        final Future<RpcResult<UpdateFlowsBatchOutput>> resultFuture = salFlowsBatchRpcs.updateFlowsBatch(input);

        Assert.assertTrue(resultFuture.isDone());
        Assert.assertTrue(resultFuture.get().isSuccessful());

        final InOrder inOrder = Mockito.inOrder(updateFlow, sendBarrier);

        inOrder.verify(updateFlow, Mockito.times(2)).invoke(updateFlowInputCpt.capture());
        final List<UpdateFlowInput> allValues = updateFlowInputCpt.getAllValues();
        Assert.assertEquals(2, allValues.size());
        Assert.assertEquals(42, allValues.get(0).getOriginalFlow().getPriority().longValue());
        Assert.assertEquals(43, allValues.get(0).getUpdatedFlow().getPriority().longValue());
        Assert.assertEquals(44, allValues.get(1).getOriginalFlow().getPriority().longValue());
        Assert.assertEquals(45, allValues.get(1).getUpdatedFlow().getPriority().longValue());

        inOrder.verify(sendBarrier).invoke(ArgumentMatchers.any());
    }

    @Test
    public void testUpdateFlowsBatch_failure() throws Exception {
        Mockito.when(salFlowRpcs.getRpcClassToInstanceMap())
            .thenReturn(ImmutableClassToInstanceMap.of(UpdateFlow.class, updateFlow));
        Mockito.when(updateFlow.invoke(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.<UpdateFlowOutput>failed()
                        .withError(ErrorType.APPLICATION, "ut-flowUpdateError")
                        .buildFuture());

        final UpdateFlowsBatchInput input = new UpdateFlowsBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchUpdateFlows(BindingMap.ordered(
                        createEmptyBatchUpdateFlow(FLOW_ID_VALUE_1, 42),
                        createEmptyBatchUpdateFlow(FLOW_ID_VALUE_2, 44)))
                .build();

        final Future<RpcResult<UpdateFlowsBatchOutput>> resultFuture = salFlowsBatchRpcs.updateFlowsBatch(input);
        Iterator<BatchFailedFlowsOutput> iterator = resultFuture.get().getResult().nonnullBatchFailedFlowsOutput()
                .values().iterator();

        Assert.assertTrue(resultFuture.isDone());
        Assert.assertFalse(resultFuture.get().isSuccessful());
        Assert.assertFalse(resultFuture.get().isSuccessful());
        Assert.assertEquals(2, resultFuture.get().getResult().nonnullBatchFailedFlowsOutput().size());
        Assert.assertEquals(FLOW_ID_VALUE_1, iterator.next().getFlowId().getValue());
        Assert.assertEquals(FLOW_ID_VALUE_2, iterator.next().getFlowId().getValue());
        Assert.assertEquals(2, resultFuture.get().getErrors().size());

        final InOrder inOrder = Mockito.inOrder(updateFlow, sendBarrier);
        inOrder.verify(updateFlow, Mockito.times(2)).invoke(updateFlowInputCpt.capture());
        final List<UpdateFlowInput> allValues = updateFlowInputCpt.getAllValues();
        Assert.assertEquals(2, allValues.size());
        Assert.assertEquals(42, allValues.get(0).getOriginalFlow().getPriority().longValue());
        Assert.assertEquals(43, allValues.get(0).getUpdatedFlow().getPriority().longValue());
        Assert.assertEquals(44, allValues.get(1).getOriginalFlow().getPriority().longValue());
        Assert.assertEquals(45, allValues.get(1).getUpdatedFlow().getPriority().longValue());

        inOrder.verify(sendBarrier).invoke(ArgumentMatchers.any());
    }
}