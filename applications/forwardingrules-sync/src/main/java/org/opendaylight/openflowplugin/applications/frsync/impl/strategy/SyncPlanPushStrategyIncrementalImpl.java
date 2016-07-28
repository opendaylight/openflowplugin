/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl.strategy;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.opendaylight.openflowplugin.applications.frsync.SyncPlanPushStrategy;
import org.opendaylight.openflowplugin.applications.frsync.util.CrudCounts;
import org.opendaylight.openflowplugin.applications.frsync.util.FxChainUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.ItemSyncBox;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconcileUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.SyncCrudCounters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execute CRUD API for flow + group + meter involving one-by-one (incremental) strategy.
 */
public class SyncPlanPushStrategyIncrementalImpl implements SyncPlanPushStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(SyncPlanPushStrategyIncrementalImpl.class);

    private FlowForwarder flowForwarder;
    private MeterForwarder meterForwarder;
    private GroupForwarder groupForwarder;
    private TableForwarder tableForwarder;
    private FlowCapableTransactionService transactionService;

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
                    //TODO chain errors but not skip processing on first error return Futures.immediateFuture(input);
                    //final ListenableFuture<RpcResult<Void>> singleVoidUpdateResult = Futures.transform(
                    //        Futures.asList Arrays.asList(input, output),
                    //        ReconcileUtil.<UpdateFlowOutput>createRpcResultCondenser("TODO"));
                }
                return addMissingGroups(nodeId, nodeIdent, diffInput.getGroupsToAddOrUpdate(), counters);
            }
        });
        Futures.addCallback(resultVehicle, FxChainUtil.logResultCallback(nodeId, "addMissingGroups"));
        resultVehicle = Futures.transform(resultVehicle, new AsyncFunction<RpcResult<Void>, RpcResult<Void>>() {
            @Override
            public ListenableFuture<RpcResult<Void>> apply(final RpcResult<Void> input) throws Exception {
                if (!input.isSuccessful()) {
                    //TODO chain errors but not skip processing on first error return Futures.immediateFuture(input);
                }
                return addMissingMeters(nodeId, nodeIdent, diffInput.getMetersToAddOrUpdate(), counters);
            }
        });
        Futures.addCallback(resultVehicle, FxChainUtil.logResultCallback(nodeId, "addMissingMeters"));
        resultVehicle = Futures.transform(resultVehicle, new AsyncFunction<RpcResult<Void>, RpcResult<Void>>() {
            @Override
            public ListenableFuture<RpcResult<Void>> apply(final RpcResult<Void> input) throws Exception {
                if (!input.isSuccessful()) {
                    //TODO chain errors but not skip processing on first error return Futures.immediateFuture(input);
                }
                return addMissingFlows(nodeId, nodeIdent, diffInput.getFlowsToAddOrUpdate(), counters);
            }
        });
        Futures.addCallback(resultVehicle, FxChainUtil.logResultCallback(nodeId, "addMissingFlows"));


        resultVehicle = Futures.transform(resultVehicle, new AsyncFunction<RpcResult<Void>, RpcResult<Void>>() {
            @Override
            public ListenableFuture<RpcResult<Void>> apply(final RpcResult<Void> input) throws Exception {
                if (!input.isSuccessful()) {
                    //TODO chain errors but not skip processing on first error return Futures.immediateFuture(input);
                }
                return removeRedundantFlows(nodeId, nodeIdent, diffInput.getFlowsToRemove(), counters);
            }
        });
        Futures.addCallback(resultVehicle, FxChainUtil.logResultCallback(nodeId, "removeRedundantFlows"));
        resultVehicle = Futures.transform(resultVehicle, new AsyncFunction<RpcResult<Void>, RpcResult<Void>>() {
            @Override
            public ListenableFuture<RpcResult<Void>> apply(final RpcResult<Void> input) throws Exception {
                if (!input.isSuccessful()) {
                    //TODO chain errors but not skip processing on first error return Futures.immediateFuture(input);
                }
                return removeRedundantMeters(nodeId, nodeIdent, diffInput.getMetersToRemove(), counters);
            }
        });
        Futures.addCallback(resultVehicle, FxChainUtil.logResultCallback(nodeId, "removeRedundantMeters"));
        resultVehicle = Futures.transform(resultVehicle, new AsyncFunction<RpcResult<Void>, RpcResult<Void>>() {
            @Override
            public ListenableFuture<RpcResult<Void>> apply(final RpcResult<Void> input) throws Exception {
                if (!input.isSuccessful()) {
                    //TODO chain errors but not skip processing on first error return Futures.immediateFuture(input);
                }
                return removeRedundantGroups(nodeId, nodeIdent, diffInput.getGroupsToRemove(), counters);
            }
        });
        Futures.addCallback(resultVehicle, FxChainUtil.logResultCallback(nodeId, "removeRedundantGroups"));
        return resultVehicle;
    }


    ListenableFuture<RpcResult<Void>> addMissingFlows(final NodeId nodeId,
                                                      final InstanceIdentifier<FlowCapableNode> nodeIdent,
                                                      final Map<TableKey, ItemSyncBox<Flow>> flowsInTablesSyncBox,
                                                      final SyncCrudCounters counters) {
        if (flowsInTablesSyncBox.isEmpty()) {
            LOG.trace("no tables in config for node: {} -> SKIPPING", nodeId.getValue());
            return RpcResultBuilder.<Void>success().buildFuture();
        }

        final List<ListenableFuture<RpcResult<AddFlowOutput>>> allResults = new ArrayList<>();
        final List<ListenableFuture<RpcResult<UpdateFlowOutput>>> allUpdateResults = new ArrayList<>();
        final CrudCounts flowCrudCounts = counters.getFlowCrudCounts();

        for (Map.Entry<TableKey, ItemSyncBox<Flow>> flowsInTableBoxEntry : flowsInTablesSyncBox.entrySet()) {
            final TableKey tableKey = flowsInTableBoxEntry.getKey();
            final ItemSyncBox<Flow> flowSyncBox = flowsInTableBoxEntry.getValue();

            final KeyedInstanceIdentifier<Table, TableKey> tableIdent = nodeIdent.child(Table.class, tableKey);

            for (final Flow flow : flowSyncBox.getItemsToPush()) {
                final KeyedInstanceIdentifier<Flow, FlowKey> flowIdent = tableIdent.child(Flow.class, flow.getKey());

                LOG.trace("adding flow {} in table {} - absent on device {} match{}",
                        flow.getId(), tableKey, nodeId, flow.getMatch());

                allResults.add(JdkFutureAdapters.listenInPoolThread(
                        flowForwarder.add(flowIdent, flow, nodeIdent)));
                flowCrudCounts.incAdded();
            }

            for (final ItemSyncBox.ItemUpdateTuple<Flow> flowUpdate : flowSyncBox.getItemsToUpdate()) {
                final Flow existingFlow = flowUpdate.getOriginal();
                final Flow updatedFlow = flowUpdate.getUpdated();

                final KeyedInstanceIdentifier<Flow, FlowKey> flowIdent = tableIdent.child(Flow.class, updatedFlow.getKey());
                LOG.trace("flow {} in table {} - needs update on device {} match{}",
                        updatedFlow.getId(), tableKey, nodeId, updatedFlow.getMatch());

                allUpdateResults.add(JdkFutureAdapters.listenInPoolThread(
                        flowForwarder.update(flowIdent, existingFlow, updatedFlow, nodeIdent)));
                flowCrudCounts.incUpdated();
            }
        }

        final ListenableFuture<RpcResult<Void>> singleVoidAddResult = Futures.transform(
                Futures.allAsList(allResults),
                ReconcileUtil.<AddFlowOutput>createRpcResultCondenser("flow adding"));

        final ListenableFuture<RpcResult<Void>> singleVoidUpdateResult = Futures.transform(
                Futures.allAsList(allUpdateResults),
                ReconcileUtil.<UpdateFlowOutput>createRpcResultCondenser("flow updating"));

        return Futures.transform(Futures.allAsList(singleVoidAddResult, singleVoidUpdateResult),
                ReconcileUtil.<Void>createRpcResultCondenser("flow add/update"));
    }

    ListenableFuture<RpcResult<Void>> removeRedundantFlows(final NodeId nodeId,
                                                           final InstanceIdentifier<FlowCapableNode> nodeIdent,
                                                           final Map<TableKey, ItemSyncBox<Flow>> removalPlan,
                                                           final SyncCrudCounters counters) {
        if (removalPlan.isEmpty()) {
            LOG.trace("no tables in operational for node: {} -> SKIPPING", nodeId.getValue());
            return RpcResultBuilder.<Void>success().buildFuture();
        }

        final List<ListenableFuture<RpcResult<RemoveFlowOutput>>> allResults = new ArrayList<>();
        final CrudCounts flowCrudCounts = counters.getFlowCrudCounts();

        for (final Map.Entry<TableKey, ItemSyncBox<Flow>> flowsPerTable : removalPlan.entrySet()) {
            final KeyedInstanceIdentifier<Table, TableKey> tableIdent =
                    nodeIdent.child(Table.class, flowsPerTable.getKey());

            // loop flows on device and check if the are configured
            for (final Flow flow : flowsPerTable.getValue().getItemsToPush()) {
                final KeyedInstanceIdentifier<Flow, FlowKey> flowIdent =
                        tableIdent.child(Flow.class, flow.getKey());
                allResults.add(JdkFutureAdapters.listenInPoolThread(
                        flowForwarder.remove(flowIdent, flow, nodeIdent)));
                flowCrudCounts.incRemoved();
            }
        }

        final ListenableFuture<RpcResult<Void>> singleVoidResult = Futures.transform(
                Futures.allAsList(allResults), ReconcileUtil.<RemoveFlowOutput>createRpcResultCondenser("flow remove"));
        return Futures.transform(singleVoidResult,
                ReconcileUtil.chainBarrierFlush(PathUtil.digNodePath(nodeIdent), transactionService));

    }

    ListenableFuture<RpcResult<Void>> removeRedundantMeters(final NodeId nodeId,
                                                            final InstanceIdentifier<FlowCapableNode> nodeIdent,
                                                            final ItemSyncBox<Meter> meterRemovalPlan,
                                                            final SyncCrudCounters counters) {
        if (meterRemovalPlan.isEmpty()) {
            LOG.trace("no meters on device for node: {} -> SKIPPING", nodeId.getValue());
            return RpcResultBuilder.<Void>success().buildFuture();
        }

        final CrudCounts meterCrudCounts = counters.getMeterCrudCounts();

        final List<ListenableFuture<RpcResult<RemoveMeterOutput>>> allResults = new ArrayList<>();
        for (Meter meter : meterRemovalPlan.getItemsToPush()) {
            LOG.trace("removing meter {} - absent in config {}",
                    meter.getMeterId(), nodeId);
            final KeyedInstanceIdentifier<Meter, MeterKey> meterIdent =
                    nodeIdent.child(Meter.class, meter.getKey());
            allResults.add(JdkFutureAdapters.listenInPoolThread(
                    meterForwarder.remove(meterIdent, meter, nodeIdent)));
            meterCrudCounts.incRemoved();
        }

        return Futures.transform(Futures.allAsList(allResults),
                ReconcileUtil.<RemoveMeterOutput>createRpcResultCondenser("meter remove"));
    }

    ListenableFuture<RpcResult<Void>> removeRedundantGroups(final NodeId nodeId,
                                                            final InstanceIdentifier<FlowCapableNode> nodeIdent,
                                                            final List<ItemSyncBox<Group>> groupsRemovalPlan,
                                                            final SyncCrudCounters counters) {
        if (groupsRemovalPlan.isEmpty()) {
            LOG.trace("no groups on device for node: {} -> SKIPPING", nodeId.getValue());
            return RpcResultBuilder.<Void>success().buildFuture();
        }

        final CrudCounts groupCrudCounts = counters.getGroupCrudCounts();

        ListenableFuture<RpcResult<Void>> chainedResult = RpcResultBuilder.<Void>success().buildFuture();
        try {
            groupCrudCounts.setRemoved(ReconcileUtil.countTotalPushed(groupsRemovalPlan));
            if (LOG.isDebugEnabled()) {
                LOG.debug("removing groups: planSteps={}, toRemoveTotal={}",
                        groupsRemovalPlan.size(), groupCrudCounts.getRemoved());
            }
            Collections.reverse(groupsRemovalPlan);
            for (final ItemSyncBox<Group> groupsPortion : groupsRemovalPlan) {
                chainedResult =
                        Futures.transform(chainedResult, new AsyncFunction<RpcResult<Void>, RpcResult<Void>>() {
                            @Override
                            public ListenableFuture<RpcResult<Void>> apply(final RpcResult<Void> input)
                                    throws Exception {
                                final ListenableFuture<RpcResult<Void>> result;
                                if (input.isSuccessful()) {
                                    result = flushRemoveGroupPortionAndBarrier(nodeIdent, groupsPortion);
                                } else {
                                    // pass through original unsuccessful rpcResult
                                    result = Futures.immediateFuture(input);
                                }

                                return result;
                            }
                        });
            }
        } catch (IllegalStateException e) {
            chainedResult = RpcResultBuilder.<Void>failed()
                    .withError(RpcError.ErrorType.APPLICATION, "failed to add missing groups", e)
                    .buildFuture();
        }

        return chainedResult;
    }

    private ListenableFuture<RpcResult<Void>> flushRemoveGroupPortionAndBarrier(
            final InstanceIdentifier<FlowCapableNode> nodeIdent,
            final ItemSyncBox<Group> groupsPortion) {
        List<ListenableFuture<RpcResult<RemoveGroupOutput>>> allResults = new ArrayList<>();
        for (Group group : groupsPortion.getItemsToPush()) {
            final KeyedInstanceIdentifier<Group, GroupKey> groupIdent = nodeIdent.child(Group.class, group.getKey());
            allResults.add(JdkFutureAdapters.listenInPoolThread(groupForwarder.remove(groupIdent, group, nodeIdent)));
        }

        final ListenableFuture<RpcResult<Void>> singleVoidResult = Futures.transform(
                Futures.allAsList(allResults),
                ReconcileUtil.<RemoveGroupOutput>createRpcResultCondenser("group remove"));

        return Futures.transform(singleVoidResult,
                ReconcileUtil.chainBarrierFlush(PathUtil.digNodePath(nodeIdent), transactionService));
    }

    ListenableFuture<RpcResult<Void>> updateTableFeatures(final InstanceIdentifier<FlowCapableNode> nodeIdent,
                                                          final FlowCapableNode flowCapableNodeConfigured) {
        // CHECK if while pushing the update, updateTableInput can be null to emulate a table add
        final List<Table> tableList = ReconcileUtil.safeTables(flowCapableNodeConfigured);

        final List<ListenableFuture<RpcResult<UpdateTableOutput>>> allResults = new ArrayList<>();
        for (Table table : tableList) {
            TableKey tableKey = table.getKey();
            KeyedInstanceIdentifier<TableFeatures, TableFeaturesKey> tableFeaturesII = nodeIdent
                    .child(TableFeatures.class, new TableFeaturesKey(tableKey.getId()));
            List<TableFeatures> tableFeatures = flowCapableNodeConfigured.getTableFeatures();
            if (tableFeatures != null) {
                for (TableFeatures tableFeaturesItem : tableFeatures) {
                    // TODO uncomment java.lang.NullPointerException
                    // at
                    // org.opendaylight.openflowjava.protocol.impl.serialization.match.AbstractOxmMatchEntrySerializer.serializeHeader(AbstractOxmMatchEntrySerializer.java:31
                    // allResults.add(JdkFutureAdapters.listenInPoolThread(
                    // tableForwarder.update(tableFeaturesII, null, tableFeaturesItem, nodeIdent)));
                }
            }
        }

        final ListenableFuture<RpcResult<Void>> singleVoidResult = Futures.transform(
                Futures.allAsList(allResults),
                ReconcileUtil.<UpdateTableOutput>createRpcResultCondenser("table update"));

        return Futures.transform(singleVoidResult,
                ReconcileUtil.chainBarrierFlush(PathUtil.digNodePath(nodeIdent), transactionService));
    }

    private ListenableFuture<RpcResult<Void>> flushAddGroupPortionAndBarrier(
            final InstanceIdentifier<FlowCapableNode> nodeIdent,
            final ItemSyncBox<Group> groupsPortion) {
        final List<ListenableFuture<RpcResult<AddGroupOutput>>> allResults = new ArrayList<>();
        final List<ListenableFuture<RpcResult<UpdateGroupOutput>>> allUpdateResults = new ArrayList<>();

        for (Group group : groupsPortion.getItemsToPush()) {
            final KeyedInstanceIdentifier<Group, GroupKey> groupIdent = nodeIdent.child(Group.class, group.getKey());
            allResults.add(JdkFutureAdapters.listenInPoolThread(groupForwarder.add(groupIdent, group, nodeIdent)));

        }

        for (ItemSyncBox.ItemUpdateTuple<Group> groupTuple : groupsPortion.getItemsToUpdate()) {
            final Group existingGroup = groupTuple.getOriginal();
            final Group group = groupTuple.getUpdated();

            final KeyedInstanceIdentifier<Group, GroupKey> groupIdent = nodeIdent.child(Group.class, group.getKey());
            allUpdateResults.add(JdkFutureAdapters.listenInPoolThread(
                    groupForwarder.update(groupIdent, existingGroup, group, nodeIdent)));
        }

        final ListenableFuture<RpcResult<Void>> singleVoidAddResult = Futures.transform(
                Futures.allAsList(allResults), ReconcileUtil.<AddGroupOutput>createRpcResultCondenser("group add"));

        final ListenableFuture<RpcResult<Void>> singleVoidUpdateResult = Futures.transform(
                Futures.allAsList(allUpdateResults),
                ReconcileUtil.<UpdateGroupOutput>createRpcResultCondenser("group update"));

        final ListenableFuture<RpcResult<Void>> summaryResult = Futures.transform(
                Futures.allAsList(singleVoidAddResult, singleVoidUpdateResult),
                ReconcileUtil.<Void>createRpcResultCondenser("group add/update"));


        return Futures.transform(summaryResult, ReconcileUtil.chainBarrierFlush(
                PathUtil.digNodePath(nodeIdent), transactionService));
    }

    ListenableFuture<RpcResult<Void>> addMissingMeters(final NodeId nodeId,
                                                       final InstanceIdentifier<FlowCapableNode> nodeIdent,
                                                       final ItemSyncBox<Meter> syncBox,
                                                       final SyncCrudCounters counters) {
        if (syncBox.isEmpty()) {
            LOG.trace("no meters configured for node: {} -> SKIPPING", nodeId.getValue());
            return RpcResultBuilder.<Void>success().buildFuture();
        }

        final CrudCounts meterCrudCounts = counters.getMeterCrudCounts();

        final List<ListenableFuture<RpcResult<AddMeterOutput>>> allResults = new ArrayList<>();
        final List<ListenableFuture<RpcResult<UpdateMeterOutput>>> allUpdateResults = new ArrayList<>();
        for (Meter meter : syncBox.getItemsToPush()) {
            final KeyedInstanceIdentifier<Meter, MeterKey> meterIdent = nodeIdent.child(Meter.class, meter.getKey());
            LOG.debug("adding meter {} - absent on device {}",
                    meter.getMeterId(), nodeId);
            allResults.add(JdkFutureAdapters.listenInPoolThread(
                    meterForwarder.add(meterIdent, meter, nodeIdent)));
            meterCrudCounts.incAdded();
        }

        for (ItemSyncBox.ItemUpdateTuple<Meter> meterTuple : syncBox.getItemsToUpdate()) {
            final Meter existingMeter = meterTuple.getOriginal();
            final Meter updated = meterTuple.getUpdated();
            final KeyedInstanceIdentifier<Meter, MeterKey> meterIdent = nodeIdent.child(Meter.class, updated.getKey());
            LOG.trace("meter {} - needs update on device {}", updated.getMeterId(), nodeId);
            allUpdateResults.add(JdkFutureAdapters.listenInPoolThread(
                    meterForwarder.update(meterIdent, existingMeter, updated, nodeIdent)));
            meterCrudCounts.incUpdated();
        }

        final ListenableFuture<RpcResult<Void>> singleVoidAddResult = Futures.transform(
                Futures.allAsList(allResults), ReconcileUtil.<AddMeterOutput>createRpcResultCondenser("meter add"));

        final ListenableFuture<RpcResult<Void>> singleVoidUpdateResult = Futures.transform(
                Futures.allAsList(allUpdateResults),
                ReconcileUtil.<UpdateMeterOutput>createRpcResultCondenser("meter update"));

        return Futures.transform(Futures.allAsList(singleVoidUpdateResult, singleVoidAddResult),
                ReconcileUtil.<Void>createRpcResultCondenser("meter add/update"));
    }

    ListenableFuture<RpcResult<Void>> addMissingGroups(final NodeId nodeId,
                                                       final InstanceIdentifier<FlowCapableNode> nodeIdent,
                                                       final List<ItemSyncBox<Group>> groupsAddPlan,
                                                       final SyncCrudCounters counters) {
        if (groupsAddPlan.isEmpty()) {
            LOG.trace("no groups configured for node: {} -> SKIPPING", nodeId.getValue());
            return RpcResultBuilder.<Void>success().buildFuture();
        }

        ListenableFuture<RpcResult<Void>> chainedResult;
        try {
            if (!groupsAddPlan.isEmpty()) {
                final CrudCounts groupCrudCounts = counters.getGroupCrudCounts();
                groupCrudCounts.setAdded(ReconcileUtil.countTotalPushed(groupsAddPlan));
                groupCrudCounts.setUpdated(ReconcileUtil.countTotalUpdated(groupsAddPlan));

                if (LOG.isDebugEnabled()) {
                    LOG.debug("adding groups: planSteps={}, toAddTotal={}, toUpdateTotal={}",
                            groupsAddPlan.size(),
                            groupCrudCounts.getAdded(),
                            groupCrudCounts.getUpdated());
                }

                chainedResult = flushAddGroupPortionAndBarrier(nodeIdent, groupsAddPlan.get(0));
                for (final ItemSyncBox<Group> groupsPortion : Iterables.skip(groupsAddPlan, 1)) {
                    chainedResult =
                            Futures.transform(chainedResult, new AsyncFunction<RpcResult<Void>, RpcResult<Void>>() {
                                @Override
                                public ListenableFuture<RpcResult<Void>> apply(final RpcResult<Void> input)
                                        throws Exception {
                                    final ListenableFuture<RpcResult<Void>> result;
                                    if (input.isSuccessful()) {
                                        result = flushAddGroupPortionAndBarrier(nodeIdent, groupsPortion);
                                    } else {
                                        // pass through original unsuccessful rpcResult
                                        result = Futures.immediateFuture(input);
                                    }

                                    return result;
                                }
                            });
                }
            } else {
                chainedResult = RpcResultBuilder.<Void>success().buildFuture();
            }
        } catch (IllegalStateException e) {
            chainedResult = RpcResultBuilder.<Void>failed()
                    .withError(RpcError.ErrorType.APPLICATION, "failed to add missing groups", e)
                    .buildFuture();
        }

        return chainedResult;
    }


    public SyncPlanPushStrategyIncrementalImpl setFlowForwarder(final FlowForwarder flowForwarder) {
        this.flowForwarder = flowForwarder;
        return this;
    }

    public SyncPlanPushStrategyIncrementalImpl setTableForwarder(final TableForwarder tableForwarder) {
        this.tableForwarder = tableForwarder;
        return this;
    }

    public SyncPlanPushStrategyIncrementalImpl setMeterForwarder(final MeterForwarder meterForwarder) {
        this.meterForwarder = meterForwarder;
        return this;
    }

    public SyncPlanPushStrategyIncrementalImpl setGroupForwarder(final GroupForwarder groupForwarder) {
        this.groupForwarder = groupForwarder;
        return this;
    }

    public SyncPlanPushStrategyIncrementalImpl setTransactionService(final FlowCapableTransactionService transactionService) {
        this.transactionService = transactionService;
        return this;
    }

}
