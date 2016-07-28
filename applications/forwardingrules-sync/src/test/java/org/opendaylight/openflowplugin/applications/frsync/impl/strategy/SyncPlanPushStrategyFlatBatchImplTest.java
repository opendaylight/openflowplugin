/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl.strategy;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.opendaylight.openflowplugin.applications.frsync.util.ItemSyncBox;
import org.opendaylight.openflowplugin.applications.frsync.util.SyncCrudCounters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.SalFlatBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.Batch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.BatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchAddFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchAddFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchAddGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchAddMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchAddMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchRemoveFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchRemoveFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchRemoveGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchRemoveMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchRemoveMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchUpdateFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchUpdateFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchUpdateGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchUpdateGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchUpdateMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchUpdateMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link SyncPlanPushStrategyFlatBatchImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SyncPlanPushStrategyFlatBatchImplTest {

    private static final NodeId NODE_ID = new NodeId("ut-node-id");
    private static final InstanceIdentifier<FlowCapableNode> NODE_IDENT = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(NODE_ID))
            .augmentation(FlowCapableNode.class);
    @Mock
    private SalFlatBatchService flatBatchService;
    @Mock
    private TableForwarder tableForwarder;
    @Captor
    private ArgumentCaptor<ProcessFlatBatchInput> processFlatBatchInputCpt;

    private final List<ItemSyncBox<Group>> groupsToAddOrUpdate;
    private final List<ItemSyncBox<Group>> groupsToRemove;
    private final ItemSyncBox<Meter> metersToAddOrUpdate;
    private final ItemSyncBox<Meter> metersToRemove;
    private final Map<TableKey, ItemSyncBox<Flow>> flowsToAddOrUpdate;
    private final Map<TableKey, ItemSyncBox<Flow>> flowsToRemove;
    private List<Batch> batchBag;

    private SyncPlanPushStrategyFlatBatchImpl syncPlanPushStrategy;

    public SyncPlanPushStrategyFlatBatchImplTest() {
        groupsToAddOrUpdate = Lists.newArrayList(DiffInputFactory.createGroupSyncBox(1, 2, 3),
                DiffInputFactory.createGroupSyncBoxWithUpdates(4, 5, 6));
        groupsToRemove = Lists.newArrayList(DiffInputFactory.createGroupSyncBox(1, 2, 3),
                DiffInputFactory.createGroupSyncBox(4, 5, 6));

        metersToAddOrUpdate = DiffInputFactory.createMeterSyncBoxWithUpdates(1, 2, 3);
        metersToRemove = DiffInputFactory.createMeterSyncBox(1, 2, 3);

        flowsToAddOrUpdate = new HashMap<>();
        flowsToAddOrUpdate.put(new TableKey((short) 0), DiffInputFactory.createFlowSyncBox("1", "2", "3"));
        flowsToAddOrUpdate.put(new TableKey((short) 1), DiffInputFactory.createFlowSyncBoxWithUpdates("4", "5", "6"));
        flowsToRemove = new HashMap<>();
        flowsToRemove.put(new TableKey((short) 0), DiffInputFactory.createFlowSyncBox("1", "2", "3"));
        flowsToRemove.put(new TableKey((short) 1), DiffInputFactory.createFlowSyncBox("4", "5", "6"));
    }


    @Before
    public void setUp() throws Exception {
        syncPlanPushStrategy = new SyncPlanPushStrategyFlatBatchImpl();
        syncPlanPushStrategy.setFlatBatchService(flatBatchService);
        syncPlanPushStrategy.setTableForwarder(tableForwarder);

        batchBag = new ArrayList<>();
    }

    @Test
    public void testExecuteSyncStrategy() throws Exception {
        final SynchronizationDiffInput diffInput = new SynchronizationDiffInput(NODE_IDENT,
                groupsToAddOrUpdate, metersToAddOrUpdate, flowsToAddOrUpdate, flowsToRemove, metersToRemove, groupsToRemove);

        Mockito.when(flatBatchService.processFlatBatch(Matchers.<ProcessFlatBatchInput>any()))
                .thenReturn(RpcResultBuilder.success(new ProcessFlatBatchOutputBuilder().build()).buildFuture());

        final SyncCrudCounters counters = new SyncCrudCounters();
        final ListenableFuture<RpcResult<Void>> rpcResult = syncPlanPushStrategy.executeSyncStrategy(
                RpcResultBuilder.<Void>success().buildFuture(), diffInput, counters);

        Mockito.verify(flatBatchService).processFlatBatch(processFlatBatchInputCpt.capture());

        final ProcessFlatBatchInput processFlatBatchInput = processFlatBatchInputCpt.getValue();
        Assert.assertFalse(processFlatBatchInput.isExitOnFirstError());
        Assert.assertEquals(13, processFlatBatchInput.getBatch().size());

        Assert.assertTrue(rpcResult.isDone());
        Assert.assertTrue(rpcResult.get().isSuccessful());

        Assert.assertEquals(6, counters.getFlowCrudCounts().getAdded());
        Assert.assertEquals(3, counters.getFlowCrudCounts().getUpdated());
        Assert.assertEquals(6, counters.getFlowCrudCounts().getRemoved());

        Assert.assertEquals(6, counters.getGroupCrudCounts().getAdded());
        Assert.assertEquals(3, counters.getGroupCrudCounts().getUpdated());
        Assert.assertEquals(6, counters.getGroupCrudCounts().getRemoved());

        Assert.assertEquals(3, counters.getMeterCrudCounts().getAdded());
        Assert.assertEquals(3, counters.getMeterCrudCounts().getUpdated());
        Assert.assertEquals(3, counters.getMeterCrudCounts().getRemoved());
    }

    @Test
    public void testAssembleRemoveFlows() throws Exception {
        final int lastOrder = SyncPlanPushStrategyFlatBatchImpl.assembleRemoveFlows(batchBag, 0, flowsToRemove);

        Assert.assertEquals(6, lastOrder);
        Assert.assertEquals(2, batchBag.size());
        Assert.assertEquals(FlatBatchRemoveFlowCase.class, batchBag.get(0).getBatchChoice().getImplementedInterface());
        Assert.assertEquals(3, ((FlatBatchRemoveFlowCase) batchBag.get(0).getBatchChoice())
                .getFlatBatchRemoveFlow().size());
        Assert.assertEquals(FlatBatchRemoveFlowCase.class, batchBag.get(1).getBatchChoice().getImplementedInterface());
        Assert.assertEquals(3, ((FlatBatchRemoveFlowCase) batchBag.get(1).getBatchChoice())
                .getFlatBatchRemoveFlow().size());
    }

    @Test
    public void testAssembleAddOrUpdateGroups() throws Exception {
        final int lastOrder = SyncPlanPushStrategyFlatBatchImpl.assembleAddOrUpdateGroups(batchBag, 0, groupsToAddOrUpdate);

        Assert.assertEquals(9, lastOrder);
        Assert.assertEquals(3, batchBag.size());
        Assert.assertEquals(FlatBatchAddGroupCase.class, batchBag.get(0).getBatchChoice().getImplementedInterface());
        Assert.assertEquals(3, ((FlatBatchAddGroupCase) batchBag.get(0).getBatchChoice())
                .getFlatBatchAddGroup().size());
        Assert.assertEquals(FlatBatchAddGroupCase.class, batchBag.get(1).getBatchChoice().getImplementedInterface());
        Assert.assertEquals(3, ((FlatBatchAddGroupCase) batchBag.get(1).getBatchChoice())
                .getFlatBatchAddGroup().size());
        Assert.assertEquals(FlatBatchUpdateGroupCase.class, batchBag.get(2).getBatchChoice().getImplementedInterface());
        Assert.assertEquals(3, ((FlatBatchUpdateGroupCase) batchBag.get(2).getBatchChoice())
                .getFlatBatchUpdateGroup().size());
    }

    @Test
    public void testAssembleRemoveGroups() throws Exception {
        final int lastOrder = SyncPlanPushStrategyFlatBatchImpl.assembleRemoveGroups(batchBag, 0, groupsToRemove);

        Assert.assertEquals(6, lastOrder);
        Assert.assertEquals(2, batchBag.size());
        Assert.assertEquals(FlatBatchRemoveGroupCase.class, batchBag.get(0).getBatchChoice().getImplementedInterface());
        Assert.assertEquals(3, ((FlatBatchRemoveGroupCase) batchBag.get(0).getBatchChoice())
                .getFlatBatchRemoveGroup().size());
        Assert.assertEquals(FlatBatchRemoveGroupCase.class, batchBag.get(1).getBatchChoice().getImplementedInterface());
        Assert.assertEquals(3, ((FlatBatchRemoveGroupCase) batchBag.get(1).getBatchChoice())
                .getFlatBatchRemoveGroup().size());
    }

    @Test
    public void testAssembleAddOrUpdateMeters() throws Exception {
        final int lastOrder = SyncPlanPushStrategyFlatBatchImpl.assembleAddOrUpdateMeters(batchBag, 0, metersToAddOrUpdate);

        Assert.assertEquals(6, lastOrder);
        Assert.assertEquals(2, batchBag.size());
        Assert.assertEquals(FlatBatchAddMeterCase.class, batchBag.get(0).getBatchChoice().getImplementedInterface());
        Assert.assertEquals(3, ((FlatBatchAddMeterCase) batchBag.get(0).getBatchChoice())
                .getFlatBatchAddMeter().size());
        Assert.assertEquals(FlatBatchUpdateMeterCase.class, batchBag.get(1).getBatchChoice().getImplementedInterface());
        Assert.assertEquals(3, ((FlatBatchUpdateMeterCase) batchBag.get(1).getBatchChoice())
                .getFlatBatchUpdateMeter().size());
    }

    @Test
    public void testAssembleRemoveMeters() throws Exception {
        final int lastOrder = SyncPlanPushStrategyFlatBatchImpl.assembleRemoveMeters(batchBag, 0, metersToRemove);

        Assert.assertEquals(3, lastOrder);
        Assert.assertEquals(1, batchBag.size());
        Assert.assertEquals(FlatBatchRemoveMeterCase.class, batchBag.get(0).getBatchChoice().getImplementedInterface());
        Assert.assertEquals(3, ((FlatBatchRemoveMeterCase) batchBag.get(0).getBatchChoice())
                .getFlatBatchRemoveMeter().size());
    }

    @Test
    public void testAssembleAddOrUpdateFlows() throws Exception {
        final int lastOrder = SyncPlanPushStrategyFlatBatchImpl.assembleAddOrUpdateFlows(batchBag, 0, flowsToAddOrUpdate);

        Assert.assertEquals(9, lastOrder);
        Assert.assertEquals(3, batchBag.size());
        Assert.assertEquals(FlatBatchAddFlowCase.class, batchBag.get(0).getBatchChoice().getImplementedInterface());
        Assert.assertEquals(3, ((FlatBatchAddFlowCase) batchBag.get(0).getBatchChoice())
                .getFlatBatchAddFlow().size());
        Assert.assertEquals(FlatBatchUpdateFlowCase.class, batchBag.get(1).getBatchChoice().getImplementedInterface());
        Assert.assertEquals(3, ((FlatBatchUpdateFlowCase) batchBag.get(1).getBatchChoice())
                .getFlatBatchUpdateFlow().size());
        Assert.assertEquals(FlatBatchAddFlowCase.class, batchBag.get(2).getBatchChoice().getImplementedInterface());
        Assert.assertEquals(3, ((FlatBatchAddFlowCase) batchBag.get(2).getBatchChoice())
                .getFlatBatchAddFlow().size());
    }

    @Test
    public void testDecrementCounters() throws Exception {
        final SyncCrudCounters counters = new SyncCrudCounters();
        counters.getFlowCrudCounts().setAdded(100);
        counters.getFlowCrudCounts().setUpdated(100);
        counters.getFlowCrudCounts().setRemoved(100);

        counters.getGroupCrudCounts().setAdded(100);
        counters.getGroupCrudCounts().setUpdated(100);
        counters.getGroupCrudCounts().setRemoved(100);

        counters.getMeterCrudCounts().setAdded(100);
        counters.getMeterCrudCounts().setUpdated(100);
        counters.getMeterCrudCounts().setRemoved(100);

        SyncPlanPushStrategyFlatBatchImpl.decrementCounters(new FlatBatchAddFlowCaseBuilder().build(), counters);
        SyncPlanPushStrategyFlatBatchImpl.decrementCounters(new FlatBatchUpdateFlowCaseBuilder().build(), counters);
        SyncPlanPushStrategyFlatBatchImpl.decrementCounters(new FlatBatchRemoveFlowCaseBuilder().build(), counters);

        SyncPlanPushStrategyFlatBatchImpl.decrementCounters(new FlatBatchAddGroupCaseBuilder().build(), counters);
        SyncPlanPushStrategyFlatBatchImpl.decrementCounters(new FlatBatchUpdateGroupCaseBuilder().build(), counters);
        SyncPlanPushStrategyFlatBatchImpl.decrementCounters(new FlatBatchRemoveGroupCaseBuilder().build(), counters);

        SyncPlanPushStrategyFlatBatchImpl.decrementCounters(new FlatBatchAddMeterCaseBuilder().build(), counters);
        SyncPlanPushStrategyFlatBatchImpl.decrementCounters(new FlatBatchUpdateMeterCaseBuilder().build(), counters);
        SyncPlanPushStrategyFlatBatchImpl.decrementCounters(new FlatBatchRemoveMeterCaseBuilder().build(), counters);

        Assert.assertEquals(99, counters.getFlowCrudCounts().getAdded());
        Assert.assertEquals(99, counters.getFlowCrudCounts().getUpdated());
        Assert.assertEquals(99, counters.getFlowCrudCounts().getRemoved());

        Assert.assertEquals(99, counters.getGroupCrudCounts().getAdded());
        Assert.assertEquals(99, counters.getGroupCrudCounts().getUpdated());
        Assert.assertEquals(99, counters.getGroupCrudCounts().getRemoved());

        Assert.assertEquals(99, counters.getMeterCrudCounts().getAdded());
        Assert.assertEquals(99, counters.getMeterCrudCounts().getUpdated());
        Assert.assertEquals(99, counters.getMeterCrudCounts().getRemoved());
    }

    @Test
    public void testMapBatchesToRanges() throws Exception {
        final List<Batch> inputBatchBag = Lists.newArrayList(
                new BatchBuilder().setBatchOrder(0).build(),
                new BatchBuilder().setBatchOrder(5).build(),
                new BatchBuilder().setBatchOrder(9).build(),
                new BatchBuilder().setBatchOrder(15).build()
        );
        final Map<Range<Integer>, Batch> rangeBatchMap = SyncPlanPushStrategyFlatBatchImpl.mapBatchesToRanges(inputBatchBag, 42);

        Assert.assertEquals(4, rangeBatchMap.size());
        int idx = 0;
        final int[] lower = new int[]{0, 5, 9, 15};
        final int[] upper = new int[]{4, 8, 14, 41};
        for (Map.Entry<Range<Integer>, Batch> rangeBatchEntry : rangeBatchMap.entrySet()) {
            Assert.assertEquals(lower[idx], rangeBatchEntry.getKey().lowerEndpoint().intValue());
            Assert.assertEquals(upper[idx], rangeBatchEntry.getKey().upperEndpoint().intValue());
            Assert.assertSame(inputBatchBag.get(idx), rangeBatchEntry.getValue());
            idx++;
        }
    }
}