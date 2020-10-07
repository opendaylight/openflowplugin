/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import org.opendaylight.openflowplugin.impl.services.batch.BatchPlanStep;
import org.opendaylight.openflowplugin.impl.services.batch.BatchStepJob;
import org.opendaylight.openflowplugin.impl.services.batch.BatchStepType;
import org.opendaylight.openflowplugin.impl.util.FlatBatchUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.Batch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.BatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.BatchKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchAddFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchAddGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchAddMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchRemoveFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchRemoveGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchRemoveMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchUpdateFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchUpdateGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchUpdateMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.add.flow._case.FlatBatchAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.add.flow._case.FlatBatchAddFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.add.flow._case.FlatBatchAddFlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.add.group._case.FlatBatchAddGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.add.meter._case.FlatBatchAddMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.remove.flow._case.FlatBatchRemoveFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.remove.group._case.FlatBatchRemoveGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.remove.meter._case.FlatBatchRemoveMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.update.flow._case.FlatBatchUpdateFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.update.group._case.FlatBatchUpdateGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.update.meter._case.FlatBatchUpdateMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailure;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailureBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.batch.failure.batch.item.id.choice.FlatBatchFailureFlowIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.batch.failure.batch.item.id.choice.FlatBatchFailureFlowIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.SalFlowsBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.output.list.grouping.BatchFailedFlowsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.SalGroupsBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.input.update.grouping.OriginalBatchedGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.input.update.grouping.UpdatedBatchedGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.SalMetersBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.input.update.grouping.OriginalBatchedMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.input.update.grouping.UpdatedBatchedMeterBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Test for {@link org.opendaylight.openflowplugin.impl.services.sal.SalFlatBatchServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SalFlatBatchServiceImplTest {

    private static final NodeId NODE_ID = new NodeId("ut-node-id");
    private static final InstanceIdentifier<Node> NODE_II = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(NODE_ID));
    private static final NodeRef NODE_REF = new NodeRef(NODE_II);

    @Mock
    private SalFlowsBatchService salFlowsBatchService;
    @Mock
    private SalGroupsBatchService salGroupsBatchService;
    @Mock
    private SalMetersBatchService salMetersBatchService;
    @Captor
    private ArgumentCaptor<AddFlowsBatchInput> addFlowsBatchInputCpt;

    private SalFlatBatchServiceImpl salFlatBatchService;

    @Before
    public void setUp() {
        salFlatBatchService =
                new SalFlatBatchServiceImpl(salFlowsBatchService, salGroupsBatchService, salMetersBatchService);

    }

    @After
    public void tearDown() {
        Mockito.verifyNoMoreInteractions(salFlowsBatchService, salGroupsBatchService, salMetersBatchService);
    }

    @Test
    public void testProcessFlatBatch_allSuccessFinished() throws Exception {
        Mockito.when(salFlowsBatchService.addFlowsBatch(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new AddFlowsBatchOutputBuilder().build()).buildFuture());
        Mockito.when(salFlowsBatchService.removeFlowsBatch(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new RemoveFlowsBatchOutputBuilder().build()).buildFuture());
        Mockito.when(salFlowsBatchService.updateFlowsBatch(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new UpdateFlowsBatchOutputBuilder().build()).buildFuture());

        Mockito.when(salGroupsBatchService.addGroupsBatch(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new AddGroupsBatchOutputBuilder().build()).buildFuture());
        Mockito.when(salGroupsBatchService.removeGroupsBatch(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new RemoveGroupsBatchOutputBuilder().build()).buildFuture());
        Mockito.when(salGroupsBatchService.updateGroupsBatch(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new UpdateGroupsBatchOutputBuilder().build()).buildFuture());

        Mockito.when(salMetersBatchService.addMetersBatch(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new AddMetersBatchOutputBuilder().build()).buildFuture());
        Mockito.when(salMetersBatchService.removeMetersBatch(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new RemoveMetersBatchOutputBuilder().build()).buildFuture());
        Mockito.when(salMetersBatchService.updateMetersBatch(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new UpdateMetersBatchOutputBuilder().build()).buildFuture());


        ProcessFlatBatchInput batchInput = new ProcessFlatBatchInputBuilder()
                .setNode(NODE_REF)
                .setBatch(Lists.newArrayList(
                        createFlowAddBatch(0, "f1"),
                        createFlowRemoveBatch(1, "f2"),
                        createFlowUpdateBatch(2, "f3"),

                        createGroupAddBatch(3, 1L),
                        createGroupRemoveBatch(4, 2L),
                        createGroupUpdateBatch(5, 3L),

                        createMeterAddBatch(6, 1L),
                        createMeterRemoveBatch(7, 2L),
                        createMeterUpdateBatch(8, 3L)
                ))
                .setExitOnFirstError(true)
                .build();

        final Future<RpcResult<ProcessFlatBatchOutput>> rpcResultFuture =
                salFlatBatchService.processFlatBatch(batchInput);
        Assert.assertTrue(rpcResultFuture.isDone());
        final RpcResult<ProcessFlatBatchOutput> rpcResult = rpcResultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertTrue(rpcResult.getErrors().isEmpty());
        Assert.assertTrue(rpcResult.getResult().nonnullBatchFailure().isEmpty());

        final InOrder inOrder = Mockito.inOrder(salFlowsBatchService, salGroupsBatchService, salMetersBatchService);
        inOrder.verify(salFlowsBatchService).addFlowsBatch(ArgumentMatchers.any());
        inOrder.verify(salFlowsBatchService).removeFlowsBatch(ArgumentMatchers.any());
        inOrder.verify(salFlowsBatchService).updateFlowsBatch(ArgumentMatchers.any());

        inOrder.verify(salGroupsBatchService).addGroupsBatch(ArgumentMatchers.any());
        inOrder.verify(salGroupsBatchService).removeGroupsBatch(ArgumentMatchers.any());
        inOrder.verify(salGroupsBatchService).updateGroupsBatch(ArgumentMatchers.any());

        inOrder.verify(salMetersBatchService).addMetersBatch(ArgumentMatchers.any());
        inOrder.verify(salMetersBatchService).removeMetersBatch(ArgumentMatchers.any());
        inOrder.verify(salMetersBatchService).updateMetersBatch(ArgumentMatchers.any());
    }

    @Test
    public void testProcessFlatBatch_firstFailedInterrupted() throws Exception {
        prepareFirstFailingMockService();

        int idx = 0;
        ProcessFlatBatchInput batchInput = new ProcessFlatBatchInputBuilder()
                .setNode(NODE_REF)
                .setBatch(Lists.newArrayList(
                        createFlowAddBatch(idx++, "f1", 2),
                        createFlowRemoveBatch(idx++, "f2"),
                        createFlowUpdateBatch(idx++, "f3"),

                        createGroupAddBatch(idx++, 1L),
                        createGroupRemoveBatch(idx++, 2L),
                        createGroupUpdateBatch(idx++, 3L),

                        createMeterAddBatch(idx++, 1L),
                        createMeterRemoveBatch(idx++, 2L),
                        createMeterUpdateBatch(idx++, 3L)
                ))
                .setExitOnFirstError(true)
                .build();

        final Future<RpcResult<ProcessFlatBatchOutput>> rpcResultFuture =
                salFlatBatchService.processFlatBatch(batchInput);
        Assert.assertTrue(rpcResultFuture.isDone());
        final RpcResult<ProcessFlatBatchOutput> rpcResult = rpcResultFuture.get();
        Assert.assertFalse(rpcResult.isSuccessful());
        Assert.assertEquals(1, rpcResult.getErrors().size());
        Assert.assertEquals(1, rpcResult.getResult().nonnullBatchFailure().size());
        Assert.assertEquals(3, rpcResult.getResult().nonnullBatchFailure().values().iterator().next()
                .getBatchOrder().intValue());

        final InOrder inOrder = Mockito.inOrder(salFlowsBatchService, salGroupsBatchService, salMetersBatchService);
        inOrder.verify(salFlowsBatchService).addFlowsBatch(ArgumentMatchers.any());
        inOrder.verify(salFlowsBatchService).removeFlowsBatch(ArgumentMatchers.any());
        inOrder.verify(salFlowsBatchService).updateFlowsBatch(ArgumentMatchers.any());
        inOrder.verify(salGroupsBatchService).addGroupsBatch(ArgumentMatchers.any());
    }

    @Test
    public void testProcessFlatBatch_firstFailedContinue() throws Exception {
        prepareFirstFailingMockService();

        int idx = 0;
        ProcessFlatBatchInput batchInput = new ProcessFlatBatchInputBuilder()
                .setNode(NODE_REF)
                .setBatch(BindingMap.ordered(
                        createFlowAddBatch(idx++, "f1", 2),
                        createFlowRemoveBatch(idx++, "f2"),
                        createFlowUpdateBatch(idx++, "f3"),

                        createGroupAddBatch(idx++, 1L),
                        createGroupRemoveBatch(idx++, 2L),
                        createGroupUpdateBatch(idx++, 3L),

                        createMeterAddBatch(idx++, 1L),
                        createMeterRemoveBatch(idx++, 2L),
                        createMeterUpdateBatch(idx++, 3L)
                ))
                .setExitOnFirstError(false)
                .build();

        final Future<RpcResult<ProcessFlatBatchOutput>> rpcResultFuture =
                salFlatBatchService.processFlatBatch(batchInput);
        Assert.assertTrue(rpcResultFuture.isDone());
        final RpcResult<ProcessFlatBatchOutput> rpcResult = rpcResultFuture.get();
        Assert.assertFalse(rpcResult.isSuccessful());
        Assert.assertEquals(1, rpcResult.getErrors().size());
        Assert.assertEquals(1, rpcResult.getResult().nonnullBatchFailure().size());
        Assert.assertEquals(3, rpcResult.getResult().nonnullBatchFailure().values().iterator().next()
                .getBatchOrder().intValue());

        final InOrder inOrder = Mockito.inOrder(salFlowsBatchService, salGroupsBatchService, salMetersBatchService);
        inOrder.verify(salFlowsBatchService).addFlowsBatch(ArgumentMatchers.any());
        inOrder.verify(salFlowsBatchService).removeFlowsBatch(ArgumentMatchers.any());
        inOrder.verify(salFlowsBatchService).updateFlowsBatch(ArgumentMatchers.any());

        inOrder.verify(salGroupsBatchService).addGroupsBatch(ArgumentMatchers.any());
        inOrder.verify(salGroupsBatchService).removeGroupsBatch(ArgumentMatchers.any());
        inOrder.verify(salGroupsBatchService).updateGroupsBatch(ArgumentMatchers.any());

        inOrder.verify(salMetersBatchService).addMetersBatch(ArgumentMatchers.any());
        inOrder.verify(salMetersBatchService).removeMetersBatch(ArgumentMatchers.any());
        inOrder.verify(salMetersBatchService).updateMetersBatch(ArgumentMatchers.any());
    }

    private void prepareFirstFailingMockService() {
        Mockito.when(salFlowsBatchService.addFlowsBatch(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new AddFlowsBatchOutputBuilder().build()).buildFuture());
        Mockito.when(salFlowsBatchService.removeFlowsBatch(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.<RemoveFlowsBatchOutput>failed()
                        .withResult(new RemoveFlowsBatchOutputBuilder()
                                .setBatchFailedFlowsOutput(Lists.newArrayList(
                                        new BatchFailedFlowsOutputBuilder()
                                                .setBatchOrder(Uint16.ONE)
                                                .setFlowId(new FlowId("123"))
                                                .build()))
                                .build())
                        .withError(RpcError.ErrorType.APPLICATION, "ut-firstFlowAddError")
                        .buildFuture());
        Mockito.when(salFlowsBatchService.updateFlowsBatch(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new UpdateFlowsBatchOutputBuilder().build()).buildFuture());

        Mockito.when(salGroupsBatchService.addGroupsBatch(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new AddGroupsBatchOutputBuilder().build()).buildFuture());
        Mockito.when(salGroupsBatchService.removeGroupsBatch(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new RemoveGroupsBatchOutputBuilder().build()).buildFuture());
        Mockito.when(salGroupsBatchService.updateGroupsBatch(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new UpdateGroupsBatchOutputBuilder().build()).buildFuture());

        Mockito.when(salMetersBatchService.addMetersBatch(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new AddMetersBatchOutputBuilder().build()).buildFuture());
        Mockito.when(salMetersBatchService.removeMetersBatch(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new RemoveMetersBatchOutputBuilder().build()).buildFuture());
        Mockito.when(salMetersBatchService.updateMetersBatch(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new UpdateMetersBatchOutputBuilder().build()).buildFuture());
    }

    private static Batch createFlowAddBatch(final int batchOrder, final String flowIdValue) {
        return createFlowAddBatch(batchOrder, flowIdValue, 1);
    }

    private static Batch createFlowAddBatch(final int batchOrder, final String flowIdValue, final int amount) {
        return new BatchBuilder()
                .setBatchOrder(Uint16.valueOf(batchOrder))
                .setBatchChoice(new FlatBatchAddFlowCaseBuilder()
                        .setFlatBatchAddFlow(repeatFlatBatchAddFlowInList(flowIdValue, amount))
                        .build())
                .build();
    }

    private static Map<FlatBatchAddFlowKey, FlatBatchAddFlow> repeatFlatBatchAddFlowInList(final String flowIdValue,
            final int amount) {
        final Map<FlatBatchAddFlowKey, FlatBatchAddFlow> map = new LinkedHashMap<>();
        for (int i = 0; i < amount; i++) {
            final FlatBatchAddFlowKey key = new FlatBatchAddFlowKey(Uint16.valueOf(i));
            map.put(key, new FlatBatchAddFlowBuilder().withKey(key).setFlowId(new FlowId(flowIdValue + i)).build());
        }
        return map;
    }

    private static <T> List<T> repeatInList(final T item, final int amount) {
        final List<T> list = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            list.add(item);
        }
        return list;
    }

    private static Batch createFlowRemoveBatch(final int batchOrder, final String flowIdValue) {
        return new BatchBuilder()
                .setBatchOrder(Uint16.valueOf(batchOrder))
                .setBatchChoice(new FlatBatchRemoveFlowCaseBuilder()
                        .setFlatBatchRemoveFlow(BindingMap.of(new FlatBatchRemoveFlowBuilder()
                                .setFlowId(new FlowId(flowIdValue))
                                .setBatchOrder(Uint16.valueOf(batchOrder))
                                .build()))
                        .build())
                .build();
    }

    private static Batch createFlowUpdateBatch(final int batchOrder, final String flowIdValue) {
        final Uint16 uint = Uint16.valueOf(batchOrder);
        return new BatchBuilder()
                .setBatchOrder(uint)
                .withKey(new BatchKey(uint))
                .setBatchChoice(new FlatBatchUpdateFlowCaseBuilder()
                        .setFlatBatchUpdateFlow(BindingMap.of(new FlatBatchUpdateFlowBuilder()
                                .setFlowId(new FlowId(flowIdValue))
                                .setBatchOrder(Uint16.valueOf(batchOrder))
                                .build()))
                        .build())
                .build();
    }

    private static Batch createGroupAddBatch(final int batchOrder, final long groupIdValue) {
        return new BatchBuilder()
                .setBatchOrder(Uint16.valueOf(batchOrder))
                .setBatchChoice(new FlatBatchAddGroupCaseBuilder()
                        .setFlatBatchAddGroup(BindingMap.of(new FlatBatchAddGroupBuilder()
                                .setGroupId(new GroupId(Uint32.valueOf(groupIdValue)))
                                .setBatchOrder(Uint16.valueOf(batchOrder))
                                .build()))
                        .build())
                .build();
    }

    private static Batch createGroupRemoveBatch(final int batchOrder, final long groupIdValue) {
        return new BatchBuilder()
                .setBatchOrder(Uint16.valueOf(batchOrder))
                .setBatchChoice(new FlatBatchRemoveGroupCaseBuilder()
                        .setFlatBatchRemoveGroup(BindingMap.of(new FlatBatchRemoveGroupBuilder()
                                .setGroupId(new GroupId(Uint32.valueOf(groupIdValue)))
                                .setBatchOrder(Uint16.valueOf(batchOrder))
                                .build()))
                        .build())
                .build();
    }

    private static Batch createGroupUpdateBatch(final int batchOrder, final long groupIdValue) {
        return new BatchBuilder()
                .setBatchOrder(Uint16.valueOf(batchOrder))
                .setBatchChoice(new FlatBatchUpdateGroupCaseBuilder()
                        .setFlatBatchUpdateGroup(BindingMap.of(new FlatBatchUpdateGroupBuilder()
                                .setOriginalBatchedGroup(new OriginalBatchedGroupBuilder()
                                        .setGroupId(new GroupId(Uint32.valueOf(groupIdValue)))
                                        .build())
                                .setUpdatedBatchedGroup(new UpdatedBatchedGroupBuilder()
                                        .setGroupId(new GroupId(Uint32.valueOf(groupIdValue)))
                                        .build())
                                .setBatchOrder(Uint16.valueOf(batchOrder))
                                .build()))
                        .build())
                .build();
    }

    private static Batch createMeterAddBatch(final int batchOrder, final long groupIdValue) {
        return new BatchBuilder()
                .setBatchOrder(Uint16.valueOf(batchOrder))
                .setBatchChoice(new FlatBatchAddMeterCaseBuilder()
                        .setFlatBatchAddMeter(BindingMap.of(new FlatBatchAddMeterBuilder()
                                .setMeterId(new MeterId(Uint32.valueOf(groupIdValue)))
                                .setBatchOrder(Uint16.valueOf(batchOrder))
                                .build()))
                        .build())
                .build();
    }

    private static Batch createMeterRemoveBatch(final int batchOrder, final long groupIdValue) {
        return new BatchBuilder()
                .setBatchOrder(Uint16.valueOf(batchOrder))
                .setBatchChoice(new FlatBatchRemoveMeterCaseBuilder()
                        .setFlatBatchRemoveMeter(BindingMap.of(new FlatBatchRemoveMeterBuilder()
                                .setMeterId(new MeterId(Uint32.valueOf(groupIdValue)))
                                .setBatchOrder(Uint16.valueOf(batchOrder))
                                .build()))
                        .build())
                .build();
    }

    private static Batch createMeterUpdateBatch(final int batchOrder, final long groupIdValue) {
        return new BatchBuilder()
                .setBatchOrder(Uint16.valueOf(batchOrder))
                .setBatchChoice(new FlatBatchUpdateMeterCaseBuilder()
                        .setFlatBatchUpdateMeter(BindingMap.of(new FlatBatchUpdateMeterBuilder()
                                .setOriginalBatchedMeter(new OriginalBatchedMeterBuilder()
                                        .setMeterId(new MeterId(Uint32.valueOf(groupIdValue)))
                                        .build())
                                .setUpdatedBatchedMeter(new UpdatedBatchedMeterBuilder()
                                        .setMeterId(new MeterId(Uint32.valueOf(groupIdValue)))
                                        .build())
                                .setBatchOrder(Uint16.valueOf(batchOrder))
                                .build()))
                        .build())
                .build();
    }

    @Test
    public void testExecuteBatchPlan() throws Exception {
        BatchStepJob batchStepJob1 = Mockito.mock(BatchStepJob.class);
        BatchStepJob batchStepJob2 = Mockito.mock(BatchStepJob.class);
        AsyncFunction<RpcResult<ProcessFlatBatchOutput>, RpcResult<ProcessFlatBatchOutput>> function1 =
                Mockito.mock(AsyncFunction.class);
        AsyncFunction<RpcResult<ProcessFlatBatchOutput>, RpcResult<ProcessFlatBatchOutput>> function2 =
                Mockito.mock(AsyncFunction.class);
        Mockito.when(batchStepJob1.getStepFunction()).thenReturn(function1);
        Mockito.when(batchStepJob2.getStepFunction()).thenReturn(function2);
        BatchPlanStep batchPlanStep1 = new BatchPlanStep(BatchStepType.GROUP_ADD);
        batchPlanStep1.setBarrierAfter(true);
        BatchPlanStep batchPlanStep2 = new BatchPlanStep(BatchStepType.FLOW_ADD);
        batchPlanStep1.setBarrierAfter(false);
        Mockito.when(batchStepJob1.getPlanStep()).thenReturn(batchPlanStep1);
        Mockito.when(batchStepJob2.getPlanStep()).thenReturn(batchPlanStep2);

        final ListenableFuture<RpcResult<ProcessFlatBatchOutput>> succeededChainOutput =
                FlatBatchUtil.createEmptyRpcBatchResultFuture(true);
        final ListenableFuture<RpcResult<ProcessFlatBatchOutput>> failedChainOutput =
                RpcResultBuilder.<ProcessFlatBatchOutput>failed()
                        .withError(RpcError.ErrorType.APPLICATION, "ut-chainError")
                        .withResult(createFlatBatchOutput(createFlowBatchFailure(Uint16.ZERO, "f1"),
                                    createFlowBatchFailure(Uint16.ONE, "f2")))
                        .buildFuture();

        Mockito.when(batchStepJob1.getStepFunction().apply(ArgumentMatchers.any()))
                .thenReturn(succeededChainOutput);
        Mockito.when(batchStepJob2.getStepFunction().apply(ArgumentMatchers.any()))
                .thenReturn(failedChainOutput);

        final List<BatchStepJob> batchChainElements = Lists.newArrayList(batchStepJob1, batchStepJob2);
        final Future<RpcResult<ProcessFlatBatchOutput>> rpcResultFuture =
                salFlatBatchService.executeBatchPlan(batchChainElements);

        Assert.assertTrue(rpcResultFuture.isDone());
        final RpcResult<ProcessFlatBatchOutput> rpcResult = rpcResultFuture.get();
        Assert.assertFalse(rpcResult.isSuccessful());
        Assert.assertEquals(1, rpcResult.getErrors().size());
        Assert.assertEquals(2, rpcResult.getResult().nonnullBatchFailure().size());
        Iterator<BatchFailure> iterator = rpcResult.getResult().nonnullBatchFailure().values().iterator();
        //Moving iterator two get second element
        iterator.next();
        Assert.assertEquals("f2",
                ((FlatBatchFailureFlowIdCase) iterator.next().getBatchItemIdChoice())
                        .getFlowId().getValue());
    }

    private static BatchFailure createFlowBatchFailure(final Uint16 batchOrder, final String flowIdValue) {
        return new BatchFailureBuilder()
                .setBatchOrder(batchOrder)
                .setBatchItemIdChoice(new FlatBatchFailureFlowIdCaseBuilder()
                        .setFlowId(new FlowId(flowIdValue))
                        .build())
                .build();
    }

    private static ProcessFlatBatchOutput createFlatBatchOutput(final BatchFailure... batchFailures) {
        return new ProcessFlatBatchOutputBuilder()
                .setBatchFailure(Lists.newArrayList(batchFailures))
                .build();
    }

    @Test
    public void testPrepareBatchPlan_success() throws Exception {
        final FlatBatchAddFlow flatBatchAddFlow_1 = new FlatBatchAddFlowBuilder()
                .setFlowId(new FlowId("f1"))
                .setBatchOrder(Uint16.ONE)
                .build();
        final FlatBatchAddFlow flatBatchAddFlow_2 = new FlatBatchAddFlowBuilder()
                .setFlowId(new FlowId("f2"))
                .setBatchOrder(Uint16.TWO)
                .build();
        final BatchPlanStep batchPlanStep = new BatchPlanStep(BatchStepType.FLOW_ADD);
        batchPlanStep.getTaskBag().addAll(Lists.newArrayList(flatBatchAddFlow_1, flatBatchAddFlow_2));
        final List<BatchPlanStep> batchPlan = Lists.newArrayList(batchPlanStep);

        final List<BatchStepJob> batchChain = salFlatBatchService.prepareBatchChain(batchPlan, NODE_REF, true);

        Assert.assertEquals(1, batchChain.size());

        Mockito.when(salFlowsBatchService.addFlowsBatch(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder
                        .success(new AddFlowsBatchOutputBuilder().build())
                        .buildFuture());

        final Future<RpcResult<ProcessFlatBatchOutput>> rpcResultFuture =
                salFlatBatchService.executeBatchPlan(batchChain);
        Assert.assertTrue(rpcResultFuture.isDone());
        final RpcResult<ProcessFlatBatchOutput> rpcResult = rpcResultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(0, rpcResult.getErrors().size());
        Assert.assertEquals(0, rpcResult.getResult().nonnullBatchFailure().size());

        Mockito.verify(salFlowsBatchService).addFlowsBatch(ArgumentMatchers.any());
    }

    @Test
    public void testPrepareBatchPlan_failure() throws Exception {
        final FlatBatchAddFlow flatBatchAddFlow_1 = new FlatBatchAddFlowBuilder()
                .setFlowId(new FlowId("f1"))
                .setBatchOrder(Uint16.ONE)
                .build();
        final FlatBatchAddFlow flatBatchAddFlow_2 = new FlatBatchAddFlowBuilder()
                .setFlowId(new FlowId("f2"))
                .setBatchOrder(Uint16.TWO)
                .build();
        final BatchPlanStep batchPlanStep = new BatchPlanStep(BatchStepType.FLOW_ADD);
        batchPlanStep.getTaskBag().addAll(Lists.newArrayList(flatBatchAddFlow_1, flatBatchAddFlow_2));

        final List<BatchPlanStep> batchPlan = Lists.newArrayList(batchPlanStep, batchPlanStep);

        final List<BatchStepJob> batchChain = salFlatBatchService.prepareBatchChain(batchPlan, NODE_REF, true);

        Assert.assertEquals(2, batchChain.size());

        Mockito.when(salFlowsBatchService.addFlowsBatch(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder
                        .<AddFlowsBatchOutput>failed()
                        .withResult(new AddFlowsBatchOutputBuilder()
                                .setBatchFailedFlowsOutput(Lists.newArrayList(
                                        new BatchFailedFlowsOutputBuilder()
                                                .setBatchOrder(Uint16.ZERO)
                                                .setFlowId(new FlowId("f1"))
                                                .build(),
                                        new BatchFailedFlowsOutputBuilder()
                                                .setBatchOrder(Uint16.ONE)
                                                .setFlowId(new FlowId("f2"))
                                                .build()))
                                .build())
                        .withError(RpcError.ErrorType.APPLICATION, "ut-addFlowBatchError")
                        .buildFuture());

        final Future<RpcResult<ProcessFlatBatchOutput>> rpcResultFuture =
                salFlatBatchService.executeBatchPlan(batchChain);
        Assert.assertTrue(rpcResultFuture.isDone());
        final RpcResult<ProcessFlatBatchOutput> rpcResult = rpcResultFuture.get();
        Assert.assertFalse(rpcResult.isSuccessful());
        Assert.assertEquals(2, rpcResult.getErrors().size());
        Assert.assertEquals(4, rpcResult.getResult().getBatchFailure().size());

        Mockito.verify(salFlowsBatchService, Mockito.times(2)).addFlowsBatch(addFlowsBatchInputCpt.capture());
        Assert.assertEquals(2, addFlowsBatchInputCpt.getValue().getBatchAddFlows().size());
    }
}
