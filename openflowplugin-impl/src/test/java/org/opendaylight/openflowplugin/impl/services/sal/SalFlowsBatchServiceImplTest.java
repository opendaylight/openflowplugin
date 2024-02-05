/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.add.flows.batch.input.BatchAddFlows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.add.flows.batch.input.BatchAddFlowsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.input.update.grouping.OriginalBatchedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.input.update.grouping.UpdatedBatchedFlowBuilder;
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
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Test for {@link org.opendaylight.openflowplugin.impl.services.sal.SalFlowsBatchServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SalFlowsBatchServiceImplTest {
    public static final NodeId NODE_ID = new NodeId("ut-dummy-node");
    public static final NodeKey NODE_KEY = new NodeKey(NODE_ID);
    public static final NodeRef NODE_REF = new NodeRef(
        InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY));

    @Mock
    private SalFlowService salFlowService;
    @Mock
    private SendBarrier sendBarrier;
    @Captor
    private ArgumentCaptor<RemoveFlowInput> removeFlowInputCpt;
    @Captor
    private ArgumentCaptor<UpdateFlowInput> updateFlowInputCpt;
    @Captor
    private ArgumentCaptor<AddFlowInput> addFlowInputCpt;

    private SalFlowsBatchServiceImpl salFlowsBatchService;
    public static final String FLOW_ID_VALUE_1 = "ut-dummy-flow1";
    public static final String FLOW_ID_VALUE_2 = "ut-dummy-flow2";

    @Before
    public void setUp() {
        salFlowsBatchService = new SalFlowsBatchServiceImpl(salFlowService, sendBarrier);

        when(sendBarrier.invoke(any())).thenReturn(RpcResultBuilder.<SendBarrierOutput>success().buildFuture());
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(salFlowService, sendBarrier);
    }

    @Test
    public void testRemoveFlowsBatch_success() throws Exception {
        when(salFlowService.removeFlow(any()))
                .thenReturn(RpcResultBuilder.success(new RemoveFlowOutputBuilder().build())
                        .buildFuture());

        final var flow1IdValue = "ut-dummy-flow1";
        final var flow2IdValue = "ut-dummy-flow2";
        final var batchFlow1 = createEmptyBatchRemoveFlow(flow1IdValue, 42);
        final var batchFlow2 = createEmptyBatchRemoveFlow(flow2IdValue, 43);

        final var input = new RemoveFlowsBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchRemoveFlows(BindingMap.ordered(batchFlow1, batchFlow2))
                .build();

        final var resultFuture = salFlowsBatchService.removeFlowsBatch(input);

        assertTrue(resultFuture.isDone());
        final var rpcResult = resultFuture.get();
        assertTrue(rpcResult.isSuccessful());
        final var result = rpcResult.getResult();
        assertEquals(0, result.nonnullBatchFailedFlowsOutput().size());

        final var inOrder = inOrder(salFlowService, sendBarrier);

        inOrder.verify(salFlowService, times(2)).removeFlow(removeFlowInputCpt.capture());
        final var allValues = removeFlowInputCpt.getAllValues();
        assertEquals(2, allValues.size());
        assertEquals(42, allValues.get(0).getPriority().longValue());
        assertEquals(43, allValues.get(1).getPriority().longValue());

        inOrder.verify(sendBarrier).invoke(any());
    }

    @Test
    public void testRemoveFlowsBatch_failed() throws Exception {
        when(salFlowService.removeFlow(any()))
                .thenReturn(RpcResultBuilder.<RemoveFlowOutput>failed()
                        .withError(ErrorType.APPLICATION, "flow-remove-fail-1")
                        .buildFuture());

        final var batchFlow1 = createEmptyBatchRemoveFlow(FLOW_ID_VALUE_1, 42);
        final var batchFlow2 = createEmptyBatchRemoveFlow(FLOW_ID_VALUE_2, 43);

        final var input = new RemoveFlowsBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchRemoveFlows(BindingMap.ordered(batchFlow1, batchFlow2))
                .build();

        final var resultFuture = salFlowsBatchService.removeFlowsBatch(input);

        assertTrue(resultFuture.isDone());
        final var rpcResult = resultFuture.get();
        assertFalse(rpcResult.isSuccessful());
        final var result = rpcResult.getResult();
        var iterator = result.nonnullBatchFailedFlowsOutput().values().iterator();
        assertEquals(2, result.nonnullBatchFailedFlowsOutput().size());
        assertEquals(FLOW_ID_VALUE_1, iterator.next().getFlowId().getValue());
        assertEquals(FLOW_ID_VALUE_2, iterator.next().getFlowId().getValue());

        final var inOrder = inOrder(salFlowService, sendBarrier);

        inOrder.verify(salFlowService, times(2)).removeFlow(removeFlowInputCpt.capture());
        final var allValues = removeFlowInputCpt.getAllValues();
        assertEquals(2, allValues.size());
        assertEquals(42, allValues.get(0).getPriority().longValue());
        assertEquals(43, allValues.get(1).getPriority().longValue());

        inOrder.verify(sendBarrier).invoke(any());
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
        final var emptyOriginalFlow = createEmptyBatchAddFlow(flowIdValue, priority);
        final var emptyUpdatedFlow = createEmptyBatchAddFlow(flowIdValue, priority + 1);
        return new BatchUpdateFlowsBuilder()
                .setFlowId(new FlowId(flowIdValue))
                .setOriginalBatchedFlow(new OriginalBatchedFlowBuilder(emptyOriginalFlow).build())
                .setUpdatedBatchedFlow(new UpdatedBatchedFlowBuilder(emptyUpdatedFlow).build())
                .build();
    }

    @Test
    public void testAddFlowsBatch_success() throws Exception {
        when(salFlowService.addFlow(any()))
                .thenReturn(RpcResultBuilder.success(new AddFlowOutputBuilder().build()).buildFuture());

        final var input = new AddFlowsBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchAddFlows(BindingMap.ordered(
                        createEmptyBatchAddFlow("ut-dummy-flow1", 42),
                        createEmptyBatchAddFlow("ut-dummy-flow2", 43)))
                .build();

        final var resultFuture = salFlowsBatchService.addFlowsBatch(input);

        assertTrue(resultFuture.isDone());
        assertTrue(resultFuture.get().isSuccessful());

        final var inOrder = inOrder(salFlowService, sendBarrier);

        inOrder.verify(salFlowService, times(2)).addFlow(addFlowInputCpt.capture());
        final var allValues = addFlowInputCpt.getAllValues();
        assertEquals(2, allValues.size());
        assertEquals(42, allValues.get(0).getPriority().longValue());
        assertEquals(43, allValues.get(1).getPriority().longValue());

        inOrder.verify(sendBarrier).invoke(any());
    }

    @Test
    public void testAddFlowsBatch_failed() throws Exception {
        when(salFlowService.addFlow(any()))
                .thenReturn(RpcResultBuilder
                        .<AddFlowOutput>failed().withError(ErrorType.APPLICATION, "ut-groupAddError")
                        .buildFuture());

        final var input = new AddFlowsBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchAddFlows(BindingMap.ordered(
                        createEmptyBatchAddFlow(FLOW_ID_VALUE_1, 42),
                        createEmptyBatchAddFlow(FLOW_ID_VALUE_2, 43)))
                .build();

        final var resultFuture = salFlowsBatchService.addFlowsBatch(input);
        var iterator = resultFuture.get().getResult().nonnullBatchFailedFlowsOutput().values().iterator();

        assertTrue(resultFuture.isDone());
        assertFalse(resultFuture.get().isSuccessful());
        assertEquals(2, resultFuture.get().getResult().nonnullBatchFailedFlowsOutput().size());
        assertEquals(FLOW_ID_VALUE_1, iterator.next().getFlowId().getValue());
        assertEquals(FLOW_ID_VALUE_2, iterator.next().getFlowId().getValue());
        assertEquals(2, resultFuture.get().getErrors().size());

        final var inOrder = inOrder(salFlowService, sendBarrier);

        inOrder.verify(salFlowService, times(2)).addFlow(addFlowInputCpt.capture());
        final var allValues = addFlowInputCpt.getAllValues();
        assertEquals(2, allValues.size());
        assertEquals(42, allValues.get(0).getPriority().longValue());
        assertEquals(43, allValues.get(1).getPriority().longValue());

        inOrder.verify(sendBarrier).invoke(any());
    }

    @Test
    public void testUpdateFlowsBatch_success() throws Exception {
        when(salFlowService.updateFlow(any()))
                .thenReturn(RpcResultBuilder.success(new UpdateFlowOutputBuilder().build()).buildFuture());

        final var input = new UpdateFlowsBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchUpdateFlows(BindingMap.ordered(
                        createEmptyBatchUpdateFlow(FLOW_ID_VALUE_1, 42),
                        createEmptyBatchUpdateFlow(FLOW_ID_VALUE_2, 44)))
                .build();

        final var resultFuture = salFlowsBatchService.updateFlowsBatch(input);

        assertTrue(resultFuture.isDone());
        assertTrue(resultFuture.get().isSuccessful());

        final var inOrder = inOrder(salFlowService, sendBarrier);

        inOrder.verify(salFlowService, times(2)).updateFlow(updateFlowInputCpt.capture());
        final var allValues = updateFlowInputCpt.getAllValues();
        assertEquals(2, allValues.size());
        assertEquals(42, allValues.get(0).getOriginalFlow().getPriority().longValue());
        assertEquals(43, allValues.get(0).getUpdatedFlow().getPriority().longValue());
        assertEquals(44, allValues.get(1).getOriginalFlow().getPriority().longValue());
        assertEquals(45, allValues.get(1).getUpdatedFlow().getPriority().longValue());

        inOrder.verify(sendBarrier).invoke(any());
    }

    @Test
    public void testUpdateFlowsBatch_failure() throws Exception {
        when(salFlowService.updateFlow(any()))
                .thenReturn(RpcResultBuilder.<UpdateFlowOutput>failed()
                        .withError(ErrorType.APPLICATION, "ut-flowUpdateError")
                        .buildFuture());

        final var input = new UpdateFlowsBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchUpdateFlows(BindingMap.ordered(
                        createEmptyBatchUpdateFlow(FLOW_ID_VALUE_1, 42),
                        createEmptyBatchUpdateFlow(FLOW_ID_VALUE_2, 44)))
                .build();

        final var resultFuture = salFlowsBatchService.updateFlowsBatch(input);
        var iterator = resultFuture.get().getResult().nonnullBatchFailedFlowsOutput().values().iterator();

        assertTrue(resultFuture.isDone());
        assertFalse(resultFuture.get().isSuccessful());
        assertFalse(resultFuture.get().isSuccessful());
        assertEquals(2, resultFuture.get().getResult().nonnullBatchFailedFlowsOutput().size());
        assertEquals(FLOW_ID_VALUE_1, iterator.next().getFlowId().getValue());
        assertEquals(FLOW_ID_VALUE_2, iterator.next().getFlowId().getValue());
        assertEquals(2, resultFuture.get().getErrors().size());

        final var inOrder = inOrder(salFlowService, sendBarrier);
        inOrder.verify(salFlowService, times(2)).updateFlow(updateFlowInputCpt.capture());
        final var allValues = updateFlowInputCpt.getAllValues();
        assertEquals(2, allValues.size());
        assertEquals(42, allValues.get(0).getOriginalFlow().getPriority().longValue());
        assertEquals(43, allValues.get(0).getUpdatedFlow().getPriority().longValue());
        assertEquals(44, allValues.get(1).getOriginalFlow().getPriority().longValue());
        assertEquals(45, allValues.get(1).getUpdatedFlow().getPriority().longValue());

        inOrder.verify(sendBarrier).invoke(any());
    }
}
