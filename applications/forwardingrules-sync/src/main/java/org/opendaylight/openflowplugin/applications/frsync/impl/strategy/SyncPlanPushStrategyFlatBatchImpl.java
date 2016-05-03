/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl.strategy;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.applications.frsync.SyncPlanPushStrategy;
import org.opendaylight.openflowplugin.applications.frsync.impl.TableForwarder;
import org.opendaylight.openflowplugin.applications.frsync.util.FxChainUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.ItemSyncBox;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconcileUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.SyncCrudCounters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.SalFlatBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.Batch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.BatchBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.add.group._case.FlatBatchAddGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.add.group._case.FlatBatchAddGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.add.meter._case.FlatBatchAddMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.add.meter._case.FlatBatchAddMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.remove.flow._case.FlatBatchRemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.remove.flow._case.FlatBatchRemoveFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.remove.group._case.FlatBatchRemoveGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.remove.group._case.FlatBatchRemoveGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.remove.meter._case.FlatBatchRemoveMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.remove.meter._case.FlatBatchRemoveMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.update.flow._case.FlatBatchUpdateFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.update.flow._case.FlatBatchUpdateFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.update.group._case.FlatBatchUpdateGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.update.group._case.FlatBatchUpdateGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.update.meter._case.FlatBatchUpdateMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.update.meter._case.FlatBatchUpdateMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.input.update.grouping.OriginalBatchedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.input.update.grouping.UpdatedBatchedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.input.update.grouping.OriginalBatchedGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.input.update.grouping.UpdatedBatchedGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.input.update.grouping.OriginalBatchedMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.input.update.grouping.UpdatedBatchedMeterBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * execute CRUD API for flow + group + meter involving one-by-one (incremental) strategy
 */
public class SyncPlanPushStrategyFlatBatchImpl implements SyncPlanPushStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(SyncPlanPushStrategyFlatBatchImpl.class);

    private SalFlatBatchService flatBatchService;
    private TableForwarder tableForwarder;

    @Override
    public ListenableFuture<RpcResult<Void>> executeSyncStrategy(ListenableFuture<RpcResult<Void>> resultVehicle,
                                                                 final SynchronizationDiffInput diffInput,
                                                                 final SyncCrudCounters counters) {
        final InstanceIdentifier<FlowCapableNode> nodeIdent = diffInput.getNodeIdent();
        final NodeId nodeId = PathUtil.digNodeId(nodeIdent);

        /* Tables - have to be pushed before groups */
        // TODO enable table-update when ready
        //resultVehicle = updateTableFeatures(nodeIdent, configTree);

        resultVehicle = Futures.transform(resultVehicle, new AsyncFunction<RpcResult<Void>, RpcResult<Void>>() {
            @Override
            public ListenableFuture<RpcResult<Void>> apply(final RpcResult<Void> input) throws Exception {
                if (!input.isSuccessful()) {
                    //TODO michal.rehak chain errors but not skip processing on first error return Futures.immediateFuture(input);
                    //final ListenableFuture<RpcResult<Void>> singleVoidUpdateResult = Futures.transform(
                    //        Futures.asList Arrays.asList(input, output),
                    //        ReconcileUtil.<UpdateFlowOutput>createRpcResultCondenser("TODO"));
                }

                final List<Batch> batchBag = new ArrayList<>();
                int batchOrder = 0;

                batchOrder = assembleAddOrUpdateGroups(batchBag, batchOrder, diffInput.getGroupsToAddOrUpdate());
                batchOrder = assembleAddOrUpdateMeters(batchBag, batchOrder, diffInput.getMetersToAddOrUpdate());
                batchOrder = assembleAddOrUpdateFlows(batchBag, batchOrder, diffInput.getFlowsToAddOrUpdate());

                batchOrder = assembleRemoveFlows(batchBag, batchOrder, diffInput.getFlowsToRemove());
                batchOrder = assembleRemoveMeters(batchBag, batchOrder, diffInput.getMetersToRemove());
                batchOrder = assembleRemoveGroups(batchBag, batchOrder, diffInput.getGroupsToRemove());

                LOG.trace("Index of last batch step: {}", batchOrder);

                final ProcessFlatBatchInput flatBatchInput = new ProcessFlatBatchInputBuilder()
                        .setNode(new NodeRef(diffInput.getNodeIdent()))
                        .setExitOnFirstError(false) // TODO: propagate from input
                        .setBatch(batchBag)
                        .build();

                final Future<RpcResult<ProcessFlatBatchOutput>> rpcResultFuture = flatBatchService.processFlatBatch(flatBatchInput);

                return Futures.transform(JdkFutureAdapters.listenInPoolThread(rpcResultFuture),
                        ReconcileUtil.<ProcessFlatBatchOutput>createRpcResultToVoidFunction("flat-bulk"));
            }
        });

        Futures.addCallback(resultVehicle, FxChainUtil.logResultCallback(nodeId, "removeRedundantGroups"));
        return resultVehicle;
    }

    @VisibleForTesting
    static int assembleRemoveFlows(final List<Batch> batchBag, int batchOrder, final Map<TableKey, ItemSyncBox<Flow>> flowItemSyncTableMap) {
        // process flow remove
        if (flowItemSyncTableMap != null) {
            for (Map.Entry<TableKey, ItemSyncBox<Flow>> syncBoxEntry : flowItemSyncTableMap.entrySet()) {
                final TableKey tableKey = syncBoxEntry.getKey();
                final ItemSyncBox<Flow> flowItemSyncBox = syncBoxEntry.getValue();

                if (!flowItemSyncBox.getItemsToPush().isEmpty()) {
                    final List<FlatBatchRemoveFlow> flatBatchRemoveFlowBag =
                            new ArrayList<>(flowItemSyncBox.getItemsToUpdate().size());
                    int itemOrder = 0;
                    for (Flow Flow : flowItemSyncBox.getItemsToPush()) {
                        flatBatchRemoveFlowBag.add(new FlatBatchRemoveFlowBuilder(Flow).setBatchOrder(itemOrder++).build());
                    }
                    final Batch batch = new BatchBuilder()
                            .setBatchChoice(new FlatBatchRemoveFlowCaseBuilder()
                                    .setFlatBatchRemoveFlow(flatBatchRemoveFlowBag)
                                    .build())
                            .setBatchOrder(batchOrder++)
                            .build();
                    batchBag.add(batch);
                }
            }
        }
        return batchOrder;
    }

    @VisibleForTesting
    static int assembleAddOrUpdateGroups(final List<Batch> batchBag, int batchOrder, final List<ItemSyncBox<Group>> groupsToAddOrUpdate) {
        // process group add+update
        if (groupsToAddOrUpdate != null) {
            for (ItemSyncBox<Group> groupItemSyncBox : groupsToAddOrUpdate) {
                if (!groupItemSyncBox.getItemsToPush().isEmpty()) {
                    final List<FlatBatchAddGroup> flatBatchAddGroupBag =
                            new ArrayList<>(groupItemSyncBox.getItemsToUpdate().size());
                    int itemOrder = 0;
                    for (Group group : groupItemSyncBox.getItemsToPush()) {
                        flatBatchAddGroupBag.add(new FlatBatchAddGroupBuilder(group).setBatchOrder(itemOrder++).build());
                    }
                    final Batch batch = new BatchBuilder()
                            .setBatchChoice(new FlatBatchAddGroupCaseBuilder()
                                    .setFlatBatchAddGroup(flatBatchAddGroupBag)
                                    .build())
                            .setBatchOrder(batchOrder++)
                            .build();
                    batchBag.add(batch);
                }

                if (!groupItemSyncBox.getItemsToUpdate().isEmpty()) {
                    final List<FlatBatchUpdateGroup> flatBatchUpdateGroupBag =
                            new ArrayList<>(groupItemSyncBox.getItemsToUpdate().size());
                    int itemOrder = 0;
                    for (ItemSyncBox.ItemUpdateTuple<Group> groupUpdate : groupItemSyncBox.getItemsToUpdate()) {
                        flatBatchUpdateGroupBag.add(new FlatBatchUpdateGroupBuilder()
                                .setBatchOrder(itemOrder++)
                                .setOriginalBatchedGroup(new OriginalBatchedGroupBuilder(groupUpdate.getOriginal()).build())
                                .setUpdatedBatchedGroup(new UpdatedBatchedGroupBuilder(groupUpdate.getUpdated()).build())
                                .build());
                    }
                    final Batch batch = new BatchBuilder()
                            .setBatchChoice(new FlatBatchUpdateGroupCaseBuilder()
                                    .setFlatBatchUpdateGroup(flatBatchUpdateGroupBag)
                                    .build())
                            .setBatchOrder(batchOrder++)
                            .build();
                    batchBag.add(batch);
                }
            }
        }
        return batchOrder;
    }

    @VisibleForTesting
    static int assembleRemoveGroups(final List<Batch> batchBag, int batchOrder, final List<ItemSyncBox<Group>> groupsToRemoveOrUpdate) {
        // process group add+update
        if (groupsToRemoveOrUpdate != null) {
            for (ItemSyncBox<Group> groupItemSyncBox : groupsToRemoveOrUpdate) {
                if (!groupItemSyncBox.getItemsToPush().isEmpty()) {
                    final List<FlatBatchRemoveGroup> flatBatchRemoveGroupBag =
                            new ArrayList<>(groupItemSyncBox.getItemsToUpdate().size());
                    int itemOrder = 0;
                    for (Group group : groupItemSyncBox.getItemsToPush()) {
                        flatBatchRemoveGroupBag.add(new FlatBatchRemoveGroupBuilder(group).setBatchOrder(itemOrder++).build());
                    }
                    final Batch batch = new BatchBuilder()
                            .setBatchChoice(new FlatBatchRemoveGroupCaseBuilder()
                                    .setFlatBatchRemoveGroup(flatBatchRemoveGroupBag)
                                    .build())
                            .setBatchOrder(batchOrder++)
                            .build();
                    batchBag.add(batch);
                }

                if (!groupItemSyncBox.getItemsToUpdate().isEmpty()) {
                    final List<FlatBatchUpdateGroup> flatBatchUpdateGroupBag =
                            new ArrayList<>(groupItemSyncBox.getItemsToUpdate().size());
                    int itemOrder = 0;
                    for (ItemSyncBox.ItemUpdateTuple<Group> groupUpdate : groupItemSyncBox.getItemsToUpdate()) {
                        flatBatchUpdateGroupBag.add(new FlatBatchUpdateGroupBuilder()
                                .setBatchOrder(itemOrder++)
                                .setOriginalBatchedGroup(new OriginalBatchedGroupBuilder(groupUpdate.getOriginal()).build())
                                .setUpdatedBatchedGroup(new UpdatedBatchedGroupBuilder(groupUpdate.getUpdated()).build())
                                .build());
                    }
                    final Batch batch = new BatchBuilder()
                            .setBatchChoice(new FlatBatchUpdateGroupCaseBuilder()
                                    .setFlatBatchUpdateGroup(flatBatchUpdateGroupBag)
                                    .build())
                            .setBatchOrder(batchOrder++)
                            .build();
                    batchBag.add(batch);
                }
            }
        }
        return batchOrder;
    }

    @VisibleForTesting
    static int assembleAddOrUpdateMeters(final List<Batch> batchBag, int batchOrder, final ItemSyncBox<Meter> meterItemSyncBox) {
        // process meter add+update
        if (meterItemSyncBox != null) {
            if (!meterItemSyncBox.getItemsToPush().isEmpty()) {
                final List<FlatBatchAddMeter> flatBatchAddMeterBag =
                        new ArrayList<>(meterItemSyncBox.getItemsToUpdate().size());
                int itemOrder = 0;
                for (Meter meter : meterItemSyncBox.getItemsToPush()) {
                    flatBatchAddMeterBag.add(new FlatBatchAddMeterBuilder(meter).setBatchOrder(itemOrder++).build());
                }
                final Batch batch = new BatchBuilder()
                        .setBatchChoice(new FlatBatchAddMeterCaseBuilder()
                                .setFlatBatchAddMeter(flatBatchAddMeterBag)
                                .build())
                        .setBatchOrder(batchOrder++)
                        .build();
                batchBag.add(batch);
            }

            if (!meterItemSyncBox.getItemsToUpdate().isEmpty()) {
                final List<FlatBatchUpdateMeter> flatBatchUpdateMeterBag =
                        new ArrayList<>(meterItemSyncBox.getItemsToUpdate().size());
                int itemOrder = 0;
                for (ItemSyncBox.ItemUpdateTuple<Meter> meterUpdate : meterItemSyncBox.getItemsToUpdate()) {
                    flatBatchUpdateMeterBag.add(new FlatBatchUpdateMeterBuilder()
                            .setBatchOrder(itemOrder++)
                            .setOriginalBatchedMeter(new OriginalBatchedMeterBuilder(meterUpdate.getOriginal()).build())
                            .setUpdatedBatchedMeter(new UpdatedBatchedMeterBuilder(meterUpdate.getUpdated()).build())
                            .build());
                }
                final Batch batch = new BatchBuilder()
                        .setBatchChoice(new FlatBatchUpdateMeterCaseBuilder()
                                .setFlatBatchUpdateMeter(flatBatchUpdateMeterBag)
                                .build())
                        .setBatchOrder(batchOrder++)
                        .build();
                batchBag.add(batch);
            }
        }
        return batchOrder;
    }

    @VisibleForTesting
    static int assembleRemoveMeters(final List<Batch> batchBag, int batchOrder, final ItemSyncBox<Meter> meterItemSyncBox) {
        // process meter remove
        if (meterItemSyncBox != null) {
            if (!meterItemSyncBox.getItemsToPush().isEmpty()) {
                final List<FlatBatchRemoveMeter> flatBatchRemoveMeterBag =
                        new ArrayList<>(meterItemSyncBox.getItemsToUpdate().size());
                int itemOrder = 0;
                for (Meter meter : meterItemSyncBox.getItemsToPush()) {
                    flatBatchRemoveMeterBag.add(new FlatBatchRemoveMeterBuilder(meter).setBatchOrder(itemOrder++).build());
                }
                final Batch batch = new BatchBuilder()
                        .setBatchChoice(new FlatBatchRemoveMeterCaseBuilder()
                                .setFlatBatchRemoveMeter(flatBatchRemoveMeterBag)
                                .build())
                        .setBatchOrder(batchOrder++)
                        .build();
                batchBag.add(batch);
            }
        }
        return batchOrder;
    }

    @VisibleForTesting
    static int assembleAddOrUpdateFlows(final List<Batch> batchBag, int batchOrder, final Map<TableKey, ItemSyncBox<Flow>> flowItemSyncTableMap) {
        // process flow add+update
        if (flowItemSyncTableMap != null) {
            for (Map.Entry<TableKey, ItemSyncBox<Flow>> syncBoxEntry : flowItemSyncTableMap.entrySet()) {
                final TableKey tableKey = syncBoxEntry.getKey();
                final ItemSyncBox<Flow> flowItemSyncBox = syncBoxEntry.getValue();

                if (!flowItemSyncBox.getItemsToPush().isEmpty()) {
                    final List<FlatBatchAddFlow> flatBatchAddFlowBag =
                            new ArrayList<>(flowItemSyncBox.getItemsToUpdate().size());
                    int itemOrder = 0;
                    for (Flow Flow : flowItemSyncBox.getItemsToPush()) {
                        flatBatchAddFlowBag.add(new FlatBatchAddFlowBuilder(Flow).setBatchOrder(itemOrder++).build());
                    }
                    final Batch batch = new BatchBuilder()
                            .setBatchChoice(new FlatBatchAddFlowCaseBuilder()
                                    .setFlatBatchAddFlow(flatBatchAddFlowBag)
                                    .build())
                            .setBatchOrder(batchOrder++)
                            .build();
                    batchBag.add(batch);
                }

                if (!flowItemSyncBox.getItemsToUpdate().isEmpty()) {
                    final List<FlatBatchUpdateFlow> flatBatchUpdateFlowBag =
                            new ArrayList<>(flowItemSyncBox.getItemsToUpdate().size());
                    int itemOrder = 0;
                    for (ItemSyncBox.ItemUpdateTuple<Flow> FlowUpdate : flowItemSyncBox.getItemsToUpdate()) {
                        flatBatchUpdateFlowBag.add(new FlatBatchUpdateFlowBuilder()
                                .setBatchOrder(itemOrder++)
                                .setOriginalBatchedFlow(new OriginalBatchedFlowBuilder(FlowUpdate.getOriginal()).build())
                                .setUpdatedBatchedFlow(new UpdatedBatchedFlowBuilder(FlowUpdate.getUpdated()).build())
                                .build());
                    }
                    final Batch batch = new BatchBuilder()
                            .setBatchChoice(new FlatBatchUpdateFlowCaseBuilder()
                                    .setFlatBatchUpdateFlow(flatBatchUpdateFlowBag)
                                    .build())
                            .setBatchOrder(batchOrder++)
                            .build();
                    batchBag.add(batch);
                }
            }
        }
        return batchOrder;
    }

    public void setFlatBatchService(final SalFlatBatchService flatBatchService) {
        this.flatBatchService = flatBatchService;
    }

    public void setTableForwarder(final TableForwarder tableForwarder) {
        this.tableForwarder = tableForwarder;
    }
}
