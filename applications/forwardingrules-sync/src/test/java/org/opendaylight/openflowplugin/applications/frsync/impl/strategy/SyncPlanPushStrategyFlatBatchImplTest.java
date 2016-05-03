/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl.strategy;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.applications.frsync.util.ItemSyncBox;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.SalFlatBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.Batch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchAddFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchAddMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchRemoveFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchRemoveMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchUpdateFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchUpdateGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchUpdateMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

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
    private SalFlatBatchService tableUpdateService;

    private List<ItemSyncBox<Group>> groupsToAddOrUpdate;
    private List<ItemSyncBox<Group>> groupsToRemove;
    private ItemSyncBox<Meter> metersToAddOrUpdate;
    private ItemSyncBox<Meter> metersToRemove;
    private Map<TableKey, ItemSyncBox<Flow>> flowsToAddOrUpdate;
    private Map<TableKey, ItemSyncBox<Flow>> flowsToRemove;
    private List<Batch> batchBag;

    private SyncPlanPushStrategyFlatBatchImpl syncPlanPushStrategy;

    public SyncPlanPushStrategyFlatBatchImplTest() {
        groupsToAddOrUpdate = Lists.newArrayList(createGroupSyncBox(1, 2, 3), createGroupSyncBoxWithUpdates(4, 5, 6));
        groupsToRemove = Lists.newArrayList(createGroupSyncBox(1, 2, 3), createGroupSyncBox(4, 5, 6));

        metersToAddOrUpdate = createMeterSyncBoxWithUpdates(1, 2, 3);
        metersToRemove = createMeterSyncBox(1, 2, 3);

        flowsToAddOrUpdate = new HashMap<>();
        flowsToAddOrUpdate.put(new TableKey((short) 0), createFlowSyncBox("1", "2", "3"));
        flowsToAddOrUpdate.put(new TableKey((short) 1), createFlowSyncBoxWithUpdates("4", "5", "6"));
        flowsToRemove = new HashMap<>();
        flowsToRemove.put(new TableKey((short) 0), createFlowSyncBox("1", "2", "3"));
        flowsToRemove.put(new TableKey((short) 1), createFlowSyncBox("4", "5", "6"));
    }

    private ItemSyncBox<Group> createGroupSyncBox(final long... groupIDs) {
        final ItemSyncBox<Group> groupBox = new ItemSyncBox<>();

        for (long gid : groupIDs) {
            groupBox.getItemsToPush().add(createPlainGroup(gid));
        }
        return groupBox;
    }

    private ItemSyncBox<Group> createGroupSyncBoxWithUpdates(final long... groupIDs) {
        final ItemSyncBox<Group> groupBox = new ItemSyncBox<>();

        for (long gid : groupIDs) {
            groupBox.getItemsToPush().add(createPlainGroup(gid));
            groupBox.getItemsToUpdate().add(new ItemSyncBox.ItemUpdateTuple<>(createPlainGroup(gid),
                    createPlainGroup(gid + 100)));
        }
        return groupBox;
    }

    private Group createPlainGroup(final long gid) {
        return new GroupBuilder().setGroupId(new GroupId(gid)).build();
    }

    private ItemSyncBox<Meter> createMeterSyncBox(final long... meterIDs) {
        final ItemSyncBox<Meter> groupBox = new ItemSyncBox<>();

        for (long gid : meterIDs) {
            groupBox.getItemsToPush().add(createPlainMeter(gid));
        }
        return groupBox;
    }

    private ItemSyncBox<Meter> createMeterSyncBoxWithUpdates(final long... meterIDs) {
        final ItemSyncBox<Meter> groupBox = new ItemSyncBox<>();

        for (long mid : meterIDs) {
            groupBox.getItemsToPush().add(createPlainMeter(mid));
            groupBox.getItemsToUpdate().add(new ItemSyncBox.ItemUpdateTuple<>(createPlainMeter(mid),
                    createPlainMeter(mid + 100)));
        }
        return groupBox;
    }

    private Meter createPlainMeter(final long mid) {
        return new MeterBuilder().setMeterId(new MeterId(mid)).build();
    }

    private ItemSyncBox<Flow> createFlowSyncBox(final String... flowIDs) {
        final ItemSyncBox<Flow> flowBox = new ItemSyncBox<>();

        for (String fid : flowIDs) {
            flowBox.getItemsToPush().add(createPlainFlow(fid));
        }
        return flowBox;
    }

    private ItemSyncBox<Flow> createFlowSyncBoxWithUpdates(final String... flowIDs) {
        final ItemSyncBox<Flow> groupBox = new ItemSyncBox<>();

        for (String fid : flowIDs) {
            groupBox.getItemsToPush().add(createPlainFlow(fid));
            groupBox.getItemsToUpdate().add(new ItemSyncBox.ItemUpdateTuple<>(createPlainFlow(fid),
                    createPlainFlow(fid + "upd")));
        }
        return groupBox;
    }

    private Flow createPlainFlow(final String fid) {
        return new FlowBuilder().setId(new FlowId(fid)).build();
    }


    @Before
    public void setUp() throws Exception {
        syncPlanPushStrategy = new SyncPlanPushStrategyFlatBatchImpl();
        syncPlanPushStrategy.setFlatBatchService(flatBatchService);
        syncPlanPushStrategy.setFlatBatchService(tableUpdateService);

        batchBag = new ArrayList<>();
    }

    @Test
    public void testExecuteSyncStrategy() throws Exception {
        final SynchronizationDiffInput diffInput = new SynchronizationDiffInput(NODE_IDENT,
                groupsToAddOrUpdate, null, null, null, null, null);
    }

    @Test
    public void testAssembleRemoveFlows() throws Exception {
        final int lastOrder = SyncPlanPushStrategyFlatBatchImpl.assembleRemoveFlows(batchBag, 0, flowsToRemove);

        Assert.assertEquals(2, lastOrder);
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

        Assert.assertEquals(3, lastOrder);
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

        Assert.assertEquals(2, lastOrder);
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

        Assert.assertEquals(2, lastOrder);
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

        Assert.assertEquals(1, lastOrder);
        Assert.assertEquals(1, batchBag.size());
        Assert.assertEquals(FlatBatchRemoveMeterCase.class, batchBag.get(0).getBatchChoice().getImplementedInterface());
        Assert.assertEquals(3, ((FlatBatchRemoveMeterCase) batchBag.get(0).getBatchChoice())
                .getFlatBatchRemoveMeter().size());
    }

    @Test
    public void testAssembleAddOrUpdateFlows() throws Exception {
        final int lastOrder = SyncPlanPushStrategyFlatBatchImpl.assembleAddOrUpdateFlows(batchBag, 0, flowsToAddOrUpdate);

        Assert.assertEquals(3, lastOrder);
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
}