/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frsync.impl.strategy;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.google.common.collect.Range;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.opendaylight.openflowplugin.applications.frsync.SyncPlanPushStrategy;
import org.opendaylight.openflowplugin.applications.frsync.util.ItemSyncBox;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconcileUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.SyncCrudCounters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.flow.crud._case.aug.FlatBatchAddFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.flow.crud._case.aug.FlatBatchAddFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.flow.crud._case.aug.FlatBatchRemoveFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.flow.crud._case.aug.FlatBatchRemoveFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.flow.crud._case.aug.FlatBatchUpdateFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.flow.crud._case.aug.FlatBatchUpdateFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.flow.crud._case.aug.flat.batch.add.flow._case.FlatBatchAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.flow.crud._case.aug.flat.batch.add.flow._case.FlatBatchAddFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.flow.crud._case.aug.flat.batch.add.flow._case.FlatBatchAddFlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.flow.crud._case.aug.flat.batch.remove.flow._case.FlatBatchRemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.flow.crud._case.aug.flat.batch.remove.flow._case.FlatBatchRemoveFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.flow.crud._case.aug.flat.batch.remove.flow._case.FlatBatchRemoveFlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.flow.crud._case.aug.flat.batch.update.flow._case.FlatBatchUpdateFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.flow.crud._case.aug.flat.batch.update.flow._case.FlatBatchUpdateFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.flow.crud._case.aug.flat.batch.update.flow._case.FlatBatchUpdateFlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.group.crud._case.aug.FlatBatchAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.group.crud._case.aug.FlatBatchAddGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.group.crud._case.aug.FlatBatchRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.group.crud._case.aug.FlatBatchRemoveGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.group.crud._case.aug.FlatBatchUpdateGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.group.crud._case.aug.FlatBatchUpdateGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.group.crud._case.aug.flat.batch.add.group._case.FlatBatchAddGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.group.crud._case.aug.flat.batch.add.group._case.FlatBatchAddGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.group.crud._case.aug.flat.batch.add.group._case.FlatBatchAddGroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.group.crud._case.aug.flat.batch.remove.group._case.FlatBatchRemoveGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.group.crud._case.aug.flat.batch.remove.group._case.FlatBatchRemoveGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.group.crud._case.aug.flat.batch.remove.group._case.FlatBatchRemoveGroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.group.crud._case.aug.flat.batch.update.group._case.FlatBatchUpdateGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.group.crud._case.aug.flat.batch.update.group._case.FlatBatchUpdateGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.group.crud._case.aug.flat.batch.update.group._case.FlatBatchUpdateGroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.meter.crud._case.aug.FlatBatchAddMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.meter.crud._case.aug.FlatBatchAddMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.meter.crud._case.aug.FlatBatchRemoveMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.meter.crud._case.aug.FlatBatchRemoveMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.meter.crud._case.aug.FlatBatchUpdateMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.meter.crud._case.aug.FlatBatchUpdateMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.meter.crud._case.aug.flat.batch.add.meter._case.FlatBatchAddMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.meter.crud._case.aug.flat.batch.add.meter._case.FlatBatchAddMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.meter.crud._case.aug.flat.batch.add.meter._case.FlatBatchAddMeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.meter.crud._case.aug.flat.batch.remove.meter._case.FlatBatchRemoveMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.meter.crud._case.aug.flat.batch.remove.meter._case.FlatBatchRemoveMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.meter.crud._case.aug.flat.batch.remove.meter._case.FlatBatchRemoveMeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.meter.crud._case.aug.flat.batch.update.meter._case.FlatBatchUpdateMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.meter.crud._case.aug.flat.batch.update.meter._case.FlatBatchUpdateMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.meter.crud._case.aug.flat.batch.update.meter._case.FlatBatchUpdateMeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.Batch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.BatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.BatchChoice;
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
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.binding.util.BindingMap.Builder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execute CRUD API for flow + group + meter involving flat-batch strategy.
 */
public class SyncPlanPushStrategyFlatBatchImpl implements SyncPlanPushStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(SyncPlanPushStrategyFlatBatchImpl.class);

    private final ProcessFlatBatch processFlatBatch;

    public SyncPlanPushStrategyFlatBatchImpl(final ProcessFlatBatch processFlatBatch) {
        this.processFlatBatch = requireNonNull(processFlatBatch);
    }

    @Override
    public ListenableFuture<RpcResult<Void>> executeSyncStrategy(ListenableFuture<RpcResult<Void>> resultVehicle,
                                                                 final SynchronizationDiffInput diffInput,
                                                                 final SyncCrudCounters counters) {
        // prepare default (full) counts
        counters.getGroupCrudCounts().setAdded(ReconcileUtil.countTotalPushed(diffInput.getGroupsToAddOrUpdate()));
        counters.getGroupCrudCounts().setUpdated(ReconcileUtil.countTotalUpdated(diffInput.getGroupsToAddOrUpdate()));
        counters.getGroupCrudCounts().setRemoved(ReconcileUtil.countTotalPushed(diffInput.getGroupsToRemove()));

        counters.getFlowCrudCounts().setAdded(ReconcileUtil.countTotalPushed(
                diffInput.getFlowsToAddOrUpdate().values()));
        counters.getFlowCrudCounts().setUpdated(ReconcileUtil.countTotalUpdated(
                diffInput.getFlowsToAddOrUpdate().values()));
        counters.getFlowCrudCounts().setRemoved(ReconcileUtil.countTotalPushed(diffInput.getFlowsToRemove().values()));

        counters.getMeterCrudCounts().setAdded(diffInput.getMetersToAddOrUpdate().getItemsToPush().size());
        counters.getMeterCrudCounts().setUpdated(diffInput.getMetersToAddOrUpdate().getItemsToUpdate().size());
        counters.getMeterCrudCounts().setRemoved(diffInput.getMetersToRemove().getItemsToPush().size());

        /* Tables - have to be pushed before groups */
        // TODO enable table-update when ready
        //resultVehicle = updateTableFeatures(nodeIdent, configTree);

        resultVehicle = Futures.transformAsync(resultVehicle, input -> {
            final var batchBag = new ArrayList<Batch>();
            int batchOrder = 0;

            batchOrder = assembleAddOrUpdateGroups(batchBag, batchOrder, diffInput.getGroupsToAddOrUpdate());
            batchOrder = assembleAddOrUpdateMeters(batchBag, batchOrder, diffInput.getMetersToAddOrUpdate());
            batchOrder = assembleAddOrUpdateFlows(batchBag, batchOrder, diffInput.getFlowsToAddOrUpdate());

            batchOrder = assembleRemoveFlows(batchBag, batchOrder, diffInput.getFlowsToRemove());
            batchOrder = assembleRemoveMeters(batchBag, batchOrder, diffInput.getMetersToRemove());
            batchOrder = assembleRemoveGroups(batchBag, batchOrder, diffInput.getGroupsToRemove());

            LOG.trace("Index of last batch step: {}", batchOrder);

            final var rpcResultFuture = processFlatBatch.invoke(new ProcessFlatBatchInputBuilder()
                .setNode(new NodeRef(PathUtil.digNodePath(diffInput.getNodeIdent())))
                // TODO: propagate from input
                .setExitOnFirstError(false)
                .setBatch(BindingMap.ordered(batchBag))
                .build());

            if (LOG.isDebugEnabled()) {
                Futures.addCallback(rpcResultFuture, createCounterCallback(batchBag, batchOrder, counters),
                    MoreExecutors.directExecutor());
            }

            return Futures.transform(rpcResultFuture, ReconcileUtil.createRpcResultToVoidFunction("flat-batch"),
                MoreExecutors.directExecutor());
        }, MoreExecutors.directExecutor());
        return resultVehicle;
    }

    private static FutureCallback<RpcResult<ProcessFlatBatchOutput>> createCounterCallback(
            final List<Batch> inputBatchBag, final int failureIndexLimit, final SyncCrudCounters counters) {
        return new FutureCallback<>() {
            @Override
            public void onSuccess(final RpcResult<ProcessFlatBatchOutput> result) {
                if (!result.isSuccessful() && result.getResult() != null
                        && !result.getResult().nonnullBatchFailure().isEmpty()) {
                    decrementBatchFailuresCounters(result.getResult().nonnullBatchFailure().values(),
                        mapBatchesToRanges(inputBatchBag, failureIndexLimit), counters);
                }
            }

            @Override
            public void onFailure(final Throwable failure) {
                counters.resetAll();
            }
        };
    }

    private static void decrementBatchFailuresCounters(final Collection<BatchFailure> batchFailures,
            final Map<Range<Uint16>, Batch> batchMap, final SyncCrudCounters counters) {
        for (var batchFailure : batchFailures) {
            for (var rangeBatchEntry : batchMap.entrySet()) {
                if (rangeBatchEntry.getKey().contains(batchFailure.getBatchOrder())) {
                    // get type and decrease
                    decrementCounters(rangeBatchEntry.getValue().getBatchChoice(), counters);
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

    static Map<Range<Uint16>, Batch> mapBatchesToRanges(final List<Batch> inputBatchBag, final int failureIndexLimit) {
        final Map<Range<Uint16>, Batch> batchMap = new LinkedHashMap<>();
        final PeekingIterator<Batch> batchPeekingIterator = Iterators.peekingIterator(inputBatchBag.iterator());
        while (batchPeekingIterator.hasNext()) {
            final Batch batch = batchPeekingIterator.next();
            final int nextBatchOrder = batchPeekingIterator.hasNext()
                    ? batchPeekingIterator.peek().getBatchOrder().toJava()
                    : failureIndexLimit;
            batchMap.put(Range.closed(batch.getBatchOrder(), Uint16.valueOf(nextBatchOrder - 1)), batch);
        }
        return batchMap;
    }

    @VisibleForTesting
    static int assembleRemoveFlows(final List<Batch> batchBag, final int batchOrder,
            final Map<TableKey, ItemSyncBox<Flow>> flowItemSyncTableMap) {
        // process flow remove
        int order = batchOrder;
        if (flowItemSyncTableMap != null) {
            for (Map.Entry<TableKey, ItemSyncBox<Flow>> syncBoxEntry : flowItemSyncTableMap.entrySet()) {
                final ItemSyncBox<Flow> flowItemSyncBox = syncBoxEntry.getValue();

                if (!flowItemSyncBox.getItemsToPush().isEmpty()) {
                    final Builder<FlatBatchRemoveFlowKey, FlatBatchRemoveFlow> flatBatchRemoveFlowBag =
                            BindingMap.orderedBuilder(flowItemSyncBox.getItemsToUpdate().size());
                    int itemOrder = 0;
                    for (Flow flow : flowItemSyncBox.getItemsToPush()) {
                        flatBatchRemoveFlowBag.add(new FlatBatchRemoveFlowBuilder(flow)
                                .setBatchOrder(Uint16.valueOf(itemOrder++))
                                .setFlowId(flow.getId())
                                .build());
                    }
                    final Batch batch = new BatchBuilder()
                            .setBatchChoice(new FlatBatchRemoveFlowCaseBuilder()
                                    .setFlatBatchRemoveFlow(flatBatchRemoveFlowBag.build())
                                    .build())
                            .setBatchOrder(Uint16.valueOf(order))
                            .build();
                    order += itemOrder;
                    batchBag.add(batch);
                }
            }
        }
        return order;
    }

    @VisibleForTesting
    static int assembleAddOrUpdateGroups(final List<Batch> batchBag, final int batchOrder,
            final List<ItemSyncBox<Group>> groupsToAddOrUpdate) {
        // process group add+update
        int order = batchOrder;
        if (groupsToAddOrUpdate != null) {
            for (ItemSyncBox<Group> groupItemSyncBox : groupsToAddOrUpdate) {
                if (!groupItemSyncBox.getItemsToPush().isEmpty()) {
                    final Builder<FlatBatchAddGroupKey, FlatBatchAddGroup> flatBatchAddGroupBag =
                        BindingMap.orderedBuilder(groupItemSyncBox.getItemsToPush().size());
                    int itemOrder = 0;
                    for (Group group : groupItemSyncBox.getItemsToPush()) {
                        flatBatchAddGroupBag.add(new FlatBatchAddGroupBuilder(group)
                                .setBatchOrder(Uint16.valueOf(itemOrder++)).build());
                    }
                    final Batch batch = new BatchBuilder()
                            .setBatchChoice(new FlatBatchAddGroupCaseBuilder()
                                    .setFlatBatchAddGroup(flatBatchAddGroupBag.build())
                                    .build())
                            .setBatchOrder(Uint16.valueOf(order))
                            .build();
                    order += itemOrder;
                    batchBag.add(batch);
                }

                if (!groupItemSyncBox.getItemsToUpdate().isEmpty()) {
                    final Builder<FlatBatchUpdateGroupKey, FlatBatchUpdateGroup> flatBatchUpdateGroupBag =
                        BindingMap.orderedBuilder(groupItemSyncBox.getItemsToUpdate().size());
                    int itemOrder = 0;
                    for (ItemSyncBox.ItemUpdateTuple<Group> groupUpdate : groupItemSyncBox.getItemsToUpdate()) {
                        flatBatchUpdateGroupBag.add(new FlatBatchUpdateGroupBuilder()
                            .setBatchOrder(Uint16.valueOf(itemOrder++))
                            .setOriginalBatchedGroup(new OriginalBatchedGroupBuilder(groupUpdate.getOriginal()).build())
                            .setUpdatedBatchedGroup(new UpdatedBatchedGroupBuilder(groupUpdate.getUpdated()).build())
                            .build());
                    }
                    final Batch batch = new BatchBuilder()
                            .setBatchChoice(new FlatBatchUpdateGroupCaseBuilder()
                                    .setFlatBatchUpdateGroup(flatBatchUpdateGroupBag.build())
                                    .build())
                            .setBatchOrder(Uint16.valueOf(order))
                            .build();
                    order += itemOrder;
                    batchBag.add(batch);
                }
            }
        }
        return order;
    }

    @VisibleForTesting
    static int assembleRemoveGroups(final List<Batch> batchBag, final int batchOrder,
            final List<ItemSyncBox<Group>> groupsToRemoveOrUpdate) {
        // process group add+update
        int order = batchOrder;
        if (groupsToRemoveOrUpdate != null) {
            for (ItemSyncBox<Group> groupItemSyncBox : groupsToRemoveOrUpdate) {
                if (!groupItemSyncBox.getItemsToPush().isEmpty()) {
                    final Builder<FlatBatchRemoveGroupKey, FlatBatchRemoveGroup> flatBatchRemoveGroupBag =
                        BindingMap.orderedBuilder(groupItemSyncBox.getItemsToUpdate().size());
                    int itemOrder = 0;
                    for (Group group : groupItemSyncBox.getItemsToPush()) {
                        flatBatchRemoveGroupBag.add(new FlatBatchRemoveGroupBuilder(group)
                                .setBatchOrder(Uint16.valueOf(itemOrder++)).build());
                    }
                    final Batch batch = new BatchBuilder()
                            .setBatchChoice(new FlatBatchRemoveGroupCaseBuilder()
                                    .setFlatBatchRemoveGroup(flatBatchRemoveGroupBag.build())
                                    .build())
                            .setBatchOrder(Uint16.valueOf(order))
                            .build();
                    order += itemOrder;
                    batchBag.add(batch);
                }
            }
        }
        return order;
    }

    @VisibleForTesting
    static int assembleAddOrUpdateMeters(final List<Batch> batchBag, final int batchOrder,
            final ItemSyncBox<Meter> meterItemSyncBox) {
        // process meter add+update
        int order = batchOrder;
        if (meterItemSyncBox != null) {
            if (!meterItemSyncBox.getItemsToPush().isEmpty()) {
                final Builder<FlatBatchAddMeterKey, FlatBatchAddMeter> flatBatchAddMeterBag =
                    BindingMap.orderedBuilder(meterItemSyncBox.getItemsToPush().size());
                int itemOrder = 0;
                for (Meter meter : meterItemSyncBox.getItemsToPush()) {
                    flatBatchAddMeterBag.add(new FlatBatchAddMeterBuilder(meter)
                        .setBatchOrder(Uint16.valueOf(itemOrder++))
                        .build());
                }
                final Batch batch = new BatchBuilder()
                        .setBatchChoice(new FlatBatchAddMeterCaseBuilder()
                                .setFlatBatchAddMeter(flatBatchAddMeterBag.build())
                                .build())
                        .setBatchOrder(Uint16.valueOf(order))
                        .build();
                order += itemOrder;
                batchBag.add(batch);
            }

            if (!meterItemSyncBox.getItemsToUpdate().isEmpty()) {
                final Builder<FlatBatchUpdateMeterKey, FlatBatchUpdateMeter> flatBatchUpdateMeterBag =
                        BindingMap.orderedBuilder(meterItemSyncBox.getItemsToUpdate().size());
                int itemOrder = 0;
                for (ItemSyncBox.ItemUpdateTuple<Meter> meterUpdate : meterItemSyncBox.getItemsToUpdate()) {
                    flatBatchUpdateMeterBag.add(new FlatBatchUpdateMeterBuilder()
                            .setBatchOrder(Uint16.valueOf(itemOrder++))
                            .setOriginalBatchedMeter(new OriginalBatchedMeterBuilder(meterUpdate.getOriginal()).build())
                            .setUpdatedBatchedMeter(new UpdatedBatchedMeterBuilder(meterUpdate.getUpdated()).build())
                            .build());
                }
                final Batch batch = new BatchBuilder()
                        .setBatchChoice(new FlatBatchUpdateMeterCaseBuilder()
                                .setFlatBatchUpdateMeter(flatBatchUpdateMeterBag.build())
                                .build())
                        .setBatchOrder(Uint16.valueOf(order))
                        .build();
                order += itemOrder;
                batchBag.add(batch);
            }
        }
        return order;
    }

    @VisibleForTesting
    static int assembleRemoveMeters(final List<Batch> batchBag, final int batchOrder,
            final ItemSyncBox<Meter> meterItemSyncBox) {
        // process meter remove
        int order = batchOrder;
        if (meterItemSyncBox != null && !meterItemSyncBox.getItemsToPush().isEmpty()) {
            final Builder<FlatBatchRemoveMeterKey, FlatBatchRemoveMeter> flatBatchRemoveMeterBag =
                BindingMap.orderedBuilder(meterItemSyncBox.getItemsToPush().size());
            int itemOrder = 0;
            for (Meter meter : meterItemSyncBox.getItemsToPush()) {
                flatBatchRemoveMeterBag.add(new FlatBatchRemoveMeterBuilder(meter)
                    .setBatchOrder(Uint16.valueOf(itemOrder++))
                    .build());
            }
            final Batch batch = new BatchBuilder()
                    .setBatchChoice(new FlatBatchRemoveMeterCaseBuilder()
                            .setFlatBatchRemoveMeter(flatBatchRemoveMeterBag.build())
                            .build())
                    .setBatchOrder(Uint16.valueOf(order))
                    .build();
            order += itemOrder;
            batchBag.add(batch);
        }
        return order;
    }

    @VisibleForTesting
    static int assembleAddOrUpdateFlows(final List<Batch> batchBag, final int batchOrder,
            final Map<TableKey, ItemSyncBox<Flow>> flowItemSyncTableMap) {
        // process flow add+update
        int order = batchOrder;
        if (flowItemSyncTableMap != null) {
            for (Map.Entry<TableKey, ItemSyncBox<Flow>> syncBoxEntry : flowItemSyncTableMap.entrySet()) {
                final ItemSyncBox<Flow> flowItemSyncBox = syncBoxEntry.getValue();

                if (!flowItemSyncBox.getItemsToPush().isEmpty()) {
                    final Builder<FlatBatchAddFlowKey, FlatBatchAddFlow> flatBatchAddFlowBag =
                        BindingMap.orderedBuilder(flowItemSyncBox.getItemsToPush().size());
                    int itemOrder = 0;
                    for (Flow flow : flowItemSyncBox.getItemsToPush()) {
                        flatBatchAddFlowBag.add(new FlatBatchAddFlowBuilder(flow)
                                .setBatchOrder(Uint16.valueOf(itemOrder++))
                                .setFlowId(flow.getId())
                                .build());
                    }
                    final Batch batch = new BatchBuilder()
                            .setBatchChoice(new FlatBatchAddFlowCaseBuilder()
                                    .setFlatBatchAddFlow(flatBatchAddFlowBag.build())
                                    .build())
                            .setBatchOrder(Uint16.valueOf(order))
                            .build();
                    order += itemOrder;
                    batchBag.add(batch);
                }

                if (!flowItemSyncBox.getItemsToUpdate().isEmpty()) {
                    final Builder<FlatBatchUpdateFlowKey, FlatBatchUpdateFlow> flatBatchUpdateFlowBag =
                        BindingMap.orderedBuilder(flowItemSyncBox.getItemsToUpdate().size());
                    int itemOrder = 0;
                    for (ItemSyncBox.ItemUpdateTuple<Flow> flowUpdate : flowItemSyncBox.getItemsToUpdate()) {
                        flatBatchUpdateFlowBag.add(new FlatBatchUpdateFlowBuilder()
                            .setBatchOrder(Uint16.valueOf(itemOrder++))
                            .setFlowId(flowUpdate.getUpdated().getId())
                            .setOriginalBatchedFlow(new OriginalBatchedFlowBuilder(flowUpdate.getOriginal()).build())
                            .setUpdatedBatchedFlow(new UpdatedBatchedFlowBuilder(flowUpdate.getUpdated()).build())
                            .build());
                    }
                    final Batch batch = new BatchBuilder()
                            .setBatchChoice(new FlatBatchUpdateFlowCaseBuilder()
                                    .setFlatBatchUpdateFlow(flatBatchUpdateFlowBag.build())
                                    .build())
                            .setBatchOrder(Uint16.valueOf(order))
                            .build();
                    order += itemOrder;
                    batchBag.add(batch);
                }
            }
        }
        return order;
    }
}
