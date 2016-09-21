/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl.strategy;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.google.common.collect.Range;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.applications.frsync.SyncPlanPushStrategy;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.BatchChoice;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailure;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.input.update.grouping.OriginalBatchedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.input.update.grouping.UpdatedBatchedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.input.update.grouping.OriginalBatchedGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.input.update.grouping.UpdatedBatchedGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.input.update.grouping.OriginalBatchedMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.input.update.grouping.UpdatedBatchedMeterBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execute CRUD API for flow + group + meter involving flat-batch strategy.
 */
public class SyncPlanPushStrategyFlatBatchImpl implements SyncPlanPushStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(SyncPlanPushStrategyFlatBatchImpl.class);

    private SalFlatBatchService flatBatchService;
    private TableForwarder tableForwarder;

    @Override
    public ListenableFuture<RpcResult<Void>> executeSyncStrategy(ListenableFuture<RpcResult<Void>> resultVehicle,
                                                                 final SynchronizationDiffInput diffInput,
                                                                 final SyncCrudCounters counters) {
        // prepare default (full) counts
        counters.getGroupCrudCounts().setAdded(ReconcileUtil.countTotalPushed(diffInput.getGroupsToAddOrUpdate()));
        counters.getGroupCrudCounts().setUpdated(ReconcileUtil.countTotalUpdated(diffInput.getGroupsToAddOrUpdate()));
        counters.getGroupCrudCounts().setRemoved(ReconcileUtil.countTotalPushed(diffInput.getGroupsToRemove()));

        counters.getFlowCrudCounts().setAdded(ReconcileUtil.countTotalPushed(diffInput.getFlowsToAddOrUpdate().values()));
        counters.getFlowCrudCounts().setUpdated(ReconcileUtil.countTotalUpdated(diffInput.getFlowsToAddOrUpdate().values()));
        counters.getFlowCrudCounts().setRemoved(ReconcileUtil.countTotalPushed(diffInput.getFlowsToRemove().values()));

        counters.getMeterCrudCounts().setAdded(diffInput.getMetersToAddOrUpdate().getItemsToPush().size());
        counters.getMeterCrudCounts().setUpdated(diffInput.getMetersToAddOrUpdate().getItemsToUpdate().size());
        counters.getMeterCrudCounts().setRemoved(diffInput.getMetersToRemove().getItemsToPush().size());

        /* Tables - have to be pushed before groups */
        // TODO enable table-update when ready
        //resultVehicle = updateTableFeatures(nodeIdent, configTree);

        resultVehicle = Futures.transform(resultVehicle, new AsyncFunction<RpcResult<Void>, RpcResult<Void>>() {
            @Override
            public ListenableFuture<RpcResult<Void>> apply(final RpcResult<Void> input) throws Exception {
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
                        .setNode(new NodeRef(PathUtil.digNodePath(diffInput.getNodeIdent())))
                        // TODO: propagate from input
                        .setExitOnFirstError(false)
                        .setBatch(batchBag)
                        .build();

                final Future<RpcResult<ProcessFlatBatchOutput>> rpcResultFuture = flatBatchService.processFlatBatch(flatBatchInput);

                if (LOG.isDebugEnabled()) {
                    Futures.addCallback(JdkFutureAdapters.listenInPoolThread(rpcResultFuture),
                            createCounterCallback(batchBag, batchOrder, counters));
                }

                return Futures.transform(JdkFutureAdapters.listenInPoolThread(rpcResultFuture),
                        ReconcileUtil.<ProcessFlatBatchOutput>createRpcResultToVoidFunction("flat-batch"));
            }
        });
        return resultVehicle;
    }

    private FutureCallback<RpcResult<ProcessFlatBatchOutput>> createCounterCallback(final List<Batch> inputBatchBag,
                                                                                    final int failureIndexLimit,
                                                                                    final SyncCrudCounters counters) {
        return new FutureCallback<RpcResult<ProcessFlatBatchOutput>>() {
            @Override
            public void onSuccess(@Nullable final RpcResult<ProcessFlatBatchOutput> result) {
                if (!result.isSuccessful() && result.getResult() != null && !result.getResult().getBatchFailure().isEmpty()) {
                    Map<Range<Integer>, Batch> batchMap = mapBatchesToRanges(inputBatchBag, failureIndexLimit);
                    decrementBatchFailuresCounters(result.getResult().getBatchFailure(), batchMap, counters);
                }
            }

            @Override
            public void onFailure(final Throwable t) {
                counters.resetAll();
            }
        };
    }

    private static void decrementBatchFailuresCounters(final List<BatchFailure> batchFailures,
                                                final Map<Range<Integer>, Batch> batchMap,
                                                final SyncCrudCounters counters) {
        for (BatchFailure batchFailure : batchFailures) {
            for (Map.Entry<Range<Integer>, Batch> rangeBatchEntry : batchMap.entrySet()) {
                if (rangeBatchEntry.getKey().contains(batchFailure.getBatchOrder())) {
                    // get type and decrease
                    final BatchChoice batchChoice = rangeBatchEntry.getValue().getBatchChoice();
                    decrementCounters(batchChoice, counters);
                    break;
                }
            }
        }
    }

    static void decrementCounters(final BatchChoice batchChoice, final SyncCrudCounters counters) {
        if (batchChoice instanceof FlatBatchAddFlowCase) {
            counters.getFlowCrudCounts().decAdded();
        } else if (batchChoice instanceof FlatBatchUpdateFlowCase) {
            counters.getFlowCrudCounts().decUpdated();
        } else if (batchChoice instanceof FlatBatchRemoveFlowCase) {
            counters.getFlowCrudCounts().decRemoved();
        } else if (batchChoice instanceof FlatBatchAddGroupCase) {
            counters.getGroupCrudCounts().decAdded();
        } else if (batchChoice instanceof FlatBatchUpdateGroupCase) {
            counters.getGroupCrudCounts().decUpdated();
        } else if (batchChoice instanceof FlatBatchRemoveGroupCase) {
            counters.getGroupCrudCounts().decRemoved();
        } else if (batchChoice instanceof FlatBatchAddMeterCase) {
            counters.getMeterCrudCounts().decAdded();
        } else if (batchChoice instanceof FlatBatchUpdateMeterCase) {
            counters.getMeterCrudCounts().decUpdated();
        } else if (batchChoice instanceof FlatBatchRemoveMeterCase) {
            counters.getMeterCrudCounts().decRemoved();
        }
    }

    static Map<Range<Integer>, Batch> mapBatchesToRanges(final List<Batch> inputBatchBag, final int failureIndexLimit) {
        final Map<Range<Integer>, Batch> batchMap = new LinkedHashMap<>();
        final PeekingIterator<Batch> batchPeekingIterator = Iterators.peekingIterator(inputBatchBag.iterator());
        while (batchPeekingIterator.hasNext()) {
            final Batch batch = batchPeekingIterator.next();
            final int nextBatchOrder = batchPeekingIterator.hasNext()
                    ? batchPeekingIterator.peek().getBatchOrder()
                    : failureIndexLimit;
            batchMap.put(Range.closed(batch.getBatchOrder(), nextBatchOrder - 1), batch);
        }
        return batchMap;
    }

    @VisibleForTesting
    static int assembleRemoveFlows(final List<Batch> batchBag, int batchOrder, final Map<TableKey, ItemSyncBox<Flow>> flowItemSyncTableMap) {
        // process flow remove
        int order = batchOrder;
        if (flowItemSyncTableMap != null) {
            for (Map.Entry<TableKey, ItemSyncBox<Flow>> syncBoxEntry : flowItemSyncTableMap.entrySet()) {
                final ItemSyncBox<Flow> flowItemSyncBox = syncBoxEntry.getValue();

                if (!flowItemSyncBox.getItemsToPush().isEmpty()) {
                    final List<FlatBatchRemoveFlow> flatBatchRemoveFlowBag =
                            new ArrayList<>(flowItemSyncBox.getItemsToUpdate().size());
                    int itemOrder = 0;
                    for (Flow flow : flowItemSyncBox.getItemsToPush()) {
                        flatBatchRemoveFlowBag.add(new FlatBatchRemoveFlowBuilder(flow)
                                .setBatchOrder(itemOrder++)
                                .setFlowId(flow.getId())
                                .build());
                    }
                    final Batch batch = new BatchBuilder()
                            .setBatchChoice(new FlatBatchRemoveFlowCaseBuilder()
                                    .setFlatBatchRemoveFlow(flatBatchRemoveFlowBag)
                                    .build())
                            .setBatchOrder(order)
                            .build();
                    order += itemOrder;
                    batchBag.add(batch);
                }
            }
        }
        return order;
    }

    @VisibleForTesting
    static int assembleAddOrUpdateGroups(final List<Batch> batchBag, int batchOrder, final List<ItemSyncBox<Group>> groupsToAddOrUpdate) {
        // process group add+update
        int order = batchOrder;
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
                            .setBatchOrder(order)
                            .build();
                    order += itemOrder;
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
                            .setBatchOrder(order)
                            .build();
                    order += itemOrder;
                    batchBag.add(batch);
                }
            }
        }
        return order;
    }

    @VisibleForTesting
    static int assembleRemoveGroups(final List<Batch> batchBag, int batchOrder, final List<ItemSyncBox<Group>> groupsToRemoveOrUpdate) {
        // process group add+update
        int order = batchOrder;
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
                            .setBatchOrder(order)
                            .build();
                    order += itemOrder;
                    batchBag.add(batch);
                }
            }
        }
        return order;
    }

    @VisibleForTesting
    static int assembleAddOrUpdateMeters(final List<Batch> batchBag, int batchOrder, final ItemSyncBox<Meter> meterItemSyncBox) {
        // process meter add+update
        int order = batchOrder;
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
                        .setBatchOrder(order)
                        .build();
                order += itemOrder;
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
                        .setBatchOrder(order)
                        .build();
                order += itemOrder;
                batchBag.add(batch);
            }
        }
        return order;
    }

    @VisibleForTesting
    static int assembleRemoveMeters(final List<Batch> batchBag, int batchOrder, final ItemSyncBox<Meter> meterItemSyncBox) {
        // process meter remove
        int order = batchOrder;
        if (meterItemSyncBox != null && !meterItemSyncBox.getItemsToPush().isEmpty()) {
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
                    .setBatchOrder(order)
                    .build();
            order += itemOrder;
            batchBag.add(batch);
        }
        return order;
    }

    @VisibleForTesting
    static int assembleAddOrUpdateFlows(final List<Batch> batchBag, int batchOrder, final Map<TableKey, ItemSyncBox<Flow>> flowItemSyncTableMap) {
        // process flow add+update
        int order = batchOrder;
        if (flowItemSyncTableMap != null) {
            for (Map.Entry<TableKey, ItemSyncBox<Flow>> syncBoxEntry : flowItemSyncTableMap.entrySet()) {
                final ItemSyncBox<Flow> flowItemSyncBox = syncBoxEntry.getValue();

                if (!flowItemSyncBox.getItemsToPush().isEmpty()) {
                    final List<FlatBatchAddFlow> flatBatchAddFlowBag =
                            new ArrayList<>(flowItemSyncBox.getItemsToUpdate().size());
                    int itemOrder = 0;
                    for (Flow flow : flowItemSyncBox.getItemsToPush()) {
                        flatBatchAddFlowBag.add(new FlatBatchAddFlowBuilder(flow)
                                .setBatchOrder(itemOrder++)
                                .setFlowId(flow.getId())
                                .build());
                    }
                    final Batch batch = new BatchBuilder()
                            .setBatchChoice(new FlatBatchAddFlowCaseBuilder()
                                    .setFlatBatchAddFlow(flatBatchAddFlowBag)
                                    .build())
                            .setBatchOrder(order)
                            .build();
                    order += itemOrder;
                    batchBag.add(batch);
                }

                if (!flowItemSyncBox.getItemsToUpdate().isEmpty()) {
                    final List<FlatBatchUpdateFlow> flatBatchUpdateFlowBag =
                            new ArrayList<>(flowItemSyncBox.getItemsToUpdate().size());
                    int itemOrder = 0;
                    for (ItemSyncBox.ItemUpdateTuple<Flow> flowUpdate : flowItemSyncBox.getItemsToUpdate()) {
                        flatBatchUpdateFlowBag.add(new FlatBatchUpdateFlowBuilder()
                                .setBatchOrder(itemOrder++)
                                .setFlowId(flowUpdate.getUpdated().getId())
                                .setOriginalBatchedFlow(new OriginalBatchedFlowBuilder(flowUpdate.getOriginal()).build())
                                .setUpdatedBatchedFlow(new UpdatedBatchedFlowBuilder(flowUpdate.getUpdated()).build())
                                .build());
                    }
                    final Batch batch = new BatchBuilder()
                            .setBatchChoice(new FlatBatchUpdateFlowCaseBuilder()
                                    .setFlatBatchUpdateFlow(flatBatchUpdateFlowBag)
                                    .build())
                            .setBatchOrder(order)
                            .build();
                    order += itemOrder;
                    batchBag.add(batch);
                }
            }
        }
        return order;
    }

    public SyncPlanPushStrategyFlatBatchImpl setFlatBatchService(final SalFlatBatchService flatBatchService) {
        this.flatBatchService = flatBatchService;
        return this;
    }

    public SyncPlanPushStrategyFlatBatchImpl setTableForwarder(final TableForwarder tableForwarder) {
        this.tableForwarder = tableForwarder;
        return this;
    }
}
