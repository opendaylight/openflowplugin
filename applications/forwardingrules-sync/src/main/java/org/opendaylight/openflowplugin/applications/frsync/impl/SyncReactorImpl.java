/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.util.FlowCapableNodeLookups;
import org.opendaylight.openflowplugin.applications.frsync.util.ItemSyncBox;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconcileUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Synchronization reactor implementation, applicable for both - syncup and reconciliation
 * 
 * @author Michal Rehak
 */
public class SyncReactorImpl implements SyncReactor {

    private static final Logger LOG = LoggerFactory.getLogger(SyncReactorImpl.class);

    private FlowForwarder flowForwarder;
    private TableForwarder tableForwarder;
    private MeterForwarder meterForwarder;
    private GroupForwarder groupForwarder;
    private FlowCapableTransactionService transactionService;

    @Override
    public ListenableFuture<Boolean> syncup(final InstanceIdentifier<FlowCapableNode> nodeIdent,
            final FlowCapableNode configTree, final FlowCapableNode operationalTree) {

        LOG.trace("syncup {} cfg:{} oper:{}", nodeIdent, configTree == null ? "is null" : "non null", operationalTree == null ? "is null" : "non null");

        /**
         * reconciliation strategy - phase 1: - add/update missing objects in following order -
         * table features - groups (reordered) - meters - flows
         **/
        ListenableFuture<RpcResult<Void>> resultVehicle = null;
        final NodeId nodeId = PathUtil.digNodeId(nodeIdent);

        /* Tables - have to be pushed before groups */
        resultVehicle = updateTableFeatures(nodeIdent, configTree);
        resultVehicle = Futures.transform(resultVehicle, new AsyncFunction<RpcResult<Void>, RpcResult<Void>>() {
            @Override
            public ListenableFuture<RpcResult<Void>> apply(final RpcResult<Void> input) throws Exception {
                if (!input.isSuccessful()) {
                    //TODO michal.rehak chain errors but not skip processing on first error return Futures.immediateFuture(input);
                    //final ListenableFuture<RpcResult<Void>> singleVoidUpdateResult = Futures.transform(
                    //        Futures.asList Arrays.asList(input, output),
                    //        ReconcileUtil.<UpdateFlowOutput>createRpcResultCondenser("TODO"));
                }
                return addMissingGroups(nodeIdent, configTree, operationalTree);
            }
        });
        Futures.addCallback(resultVehicle, logResultCallback(nodeId, "addMissingGroups"));
        resultVehicle = Futures.transform(resultVehicle, new AsyncFunction<RpcResult<Void>, RpcResult<Void>>() {
            @Override
            public ListenableFuture<RpcResult<Void>> apply(final RpcResult<Void> input) throws Exception {
                if (!input.isSuccessful()) {
                  //TODO michal.rehak chain errors but not skip processing on first error return Futures.immediateFuture(input);
                }
                return addMissingMeters(nodeIdent, configTree, operationalTree);
            }
        });
        Futures.addCallback(resultVehicle, logResultCallback(nodeId, "addMissingMeters"));
        resultVehicle = Futures.transform(resultVehicle, new AsyncFunction<RpcResult<Void>, RpcResult<Void>>() {
            @Override
            public ListenableFuture<RpcResult<Void>> apply(final RpcResult<Void> input) throws Exception {
                if (!input.isSuccessful()) {
                  //TODO michal.rehak chain errors but not skip processing on first error return Futures.immediateFuture(input);
                }
                return addMissingFlows(nodeIdent, configTree, operationalTree);
            }
        });
        Futures.addCallback(resultVehicle, logResultCallback(nodeId, "addMissingFlows"));

        /**
         * reconciliation strategy - phase 2: - remove redundand objects in following order - flows
         * - meters - groups (reordered)
         **/
        resultVehicle = Futures.transform(resultVehicle, new AsyncFunction<RpcResult<Void>, RpcResult<Void>>() {
            @Override
            public ListenableFuture<RpcResult<Void>> apply(final RpcResult<Void> input) throws Exception {
                if (!input.isSuccessful()) {
                  //TODO michal.rehak chain errors but not skip processing on first error return Futures.immediateFuture(input);
                }
                return removeRedundantFlows(nodeIdent, configTree, operationalTree);
            }
        });
        Futures.addCallback(resultVehicle, logResultCallback(nodeId, "removeRedundantFlows"));
        resultVehicle = Futures.transform(resultVehicle, new AsyncFunction<RpcResult<Void>, RpcResult<Void>>() {
            @Override
            public ListenableFuture<RpcResult<Void>> apply(final RpcResult<Void> input) throws Exception {
                if (!input.isSuccessful()) {
                  //TODO michal.rehak chain errors but not skip processing on first error return Futures.immediateFuture(input);
                }
                return removeRedundantMeters(nodeIdent, configTree, operationalTree);
            }
        });
        Futures.addCallback(resultVehicle, logResultCallback(nodeId, "removeRedundantMeters"));
        resultVehicle = Futures.transform(resultVehicle, new AsyncFunction<RpcResult<Void>, RpcResult<Void>>() {
            @Override
            public ListenableFuture<RpcResult<Void>> apply(final RpcResult<Void> input) throws Exception {
                if (!input.isSuccessful()) {
                  //TODO michal.rehak chain errors but not skip processing on first error return Futures.immediateFuture(input);
                }
                return removeRedundantGroups(nodeIdent, configTree, operationalTree);
            }
        });
        Futures.addCallback(resultVehicle, logResultCallback(nodeId, "removeRedundantGroups"));

        // log final result
        Futures.addCallback(resultVehicle, logResultCallback(nodeId, "final result"));

        return Futures.transform(resultVehicle, new Function<RpcResult<Void>, Boolean>() {
            @Override
            public Boolean apply(RpcResult<Void> input) {
                if (input == null) {
                    return false;
                }

                return input.isSuccessful();
            }
        });
    }

    private FutureCallback<RpcResult<Void>> logResultCallback(final NodeId nodeId, final String prefix) {
        return new FutureCallback<RpcResult<Void>>() {
            @Override
            public void onSuccess(@Nullable final RpcResult<Void> result) {
                if (result != null) {
                    if (result.isSuccessful()) {
                        LOG.debug(prefix + " finished successfully: {}", nodeId.getValue());
                    } else {
                        final Collection<RpcError> errors = MoreObjects.firstNonNull(result.getErrors(), ImmutableList.<RpcError>of());
                        LOG.debug(prefix + " failed: {} -> {}", nodeId.getValue(), Arrays.toString(errors.toArray()));
                    }
                } else {
                    LOG.debug(prefix + "reconciliation failed: {} -> null result", nodeId.getValue());
                }
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.debug(prefix + "reconciliation failed seriously: {}", nodeId.getValue(), t);
            }
        };
    }

    public SyncReactorImpl setFlowForwarder(final FlowForwarder flowForwarder) {
        this.flowForwarder = flowForwarder;
        return this;
    }

    public SyncReactorImpl setTableForwarder(final TableForwarder tableForwarder) {
        this.tableForwarder = tableForwarder;
        return this;
    }

    public SyncReactorImpl setMeterForwarder(final MeterForwarder meterForwarder) {
        this.meterForwarder = meterForwarder;
        return this;
    }

    public SyncReactorImpl setGroupForwarder(final GroupForwarder groupForwarder) {
        this.groupForwarder = groupForwarder;
        return this;
    }

    public SyncReactorImpl setTransactionService(final FlowCapableTransactionService transactionService) {
        this.transactionService = transactionService;
        return this;
    }

    ListenableFuture<RpcResult<Void>> updateTableFeatures(final InstanceIdentifier<FlowCapableNode> nodeIdent,
            final FlowCapableNode flowCapableNodeConfigured) {
        // CHECK if while pushing the update, updateTableInput can be null to emulate a table add
        final List<Table> tableList = safeTables(flowCapableNodeConfigured);

        final List<ListenableFuture<RpcResult<UpdateTableOutput>>> allResults = new ArrayList<>();
        for (Table table : tableList) {
            TableKey tableKey = table.getKey();
            KeyedInstanceIdentifier<TableFeatures, TableFeaturesKey> tableFeaturesII = nodeIdent
                    .child(Table.class, tableKey).child(TableFeatures.class, new TableFeaturesKey(tableKey.getId()));
            List<TableFeatures> tableFeatures = table.getTableFeatures();
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


    @VisibleForTesting
    ListenableFuture<RpcResult<Void>> addMissingGroups(final InstanceIdentifier<FlowCapableNode> nodeIdent,
            final FlowCapableNode flowCapableNodeConfigured,
            final FlowCapableNode flowCapableNodeOperational) {
        final NodeId nodeId = PathUtil.digNodeId(nodeIdent);
        final List<Group> groupsConfigured = safeGroups(flowCapableNodeConfigured);
        if (groupsConfigured.isEmpty()) {
            LOG.trace("no groups configured for node: {} -> SKIPPING", nodeId.getValue());
            return RpcResultBuilder.<Void>success().buildFuture();
        }

        final List<Group> groupsOperational = safeGroups(flowCapableNodeOperational);

        return addMissingGroups(nodeId, nodeIdent, groupsConfigured, groupsOperational);
    }

    protected ListenableFuture<RpcResult<Void>> addMissingGroups(NodeId nodeId,
            final InstanceIdentifier<FlowCapableNode> nodeIdent,
            final List<Group> groupsConfigured,
            final List<Group> groupsOperational) {

        final Map<Long, Group> groupOperationalMap = FlowCapableNodeLookups.wrapGroupsToMap(groupsOperational);

        final List<Group> pendingGroups = new ArrayList<>();
        pendingGroups.addAll(groupsConfigured);

        ListenableFuture<RpcResult<Void>> chainedResult;
        try {
            final List<ItemSyncBox<Group>> groupsAddPlan =
                    ReconcileUtil.resolveAndDivideGroups(nodeId, groupOperationalMap, pendingGroups);
            if (!groupsAddPlan.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("adding groups: inputGroups={}, planSteps={}, toAddTotal={}, toUpdateTotal={}",
                            pendingGroups.size(), groupsAddPlan.size(),
                            ReconcileUtil.countTotalAdds(groupsAddPlan),
                            ReconcileUtil.countTotalUpdated(groupsAddPlan));
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

    private ListenableFuture<RpcResult<Void>> flushAddGroupPortionAndBarrier(
            final InstanceIdentifier<FlowCapableNode> nodeIdent,
            final ItemSyncBox<Group> groupsPortion) {
        final List<ListenableFuture<RpcResult<AddGroupOutput>>> allResults = new ArrayList<>();
        final List<ListenableFuture<RpcResult<UpdateGroupOutput>>> allUpdateResults = new ArrayList<>();

        for (Group group : groupsPortion.getItemsToAdd()) {
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


        return Futures.transform(summaryResult,
                ReconcileUtil.chainBarrierFlush(
                        PathUtil.digNodePath(nodeIdent), transactionService));
    }

    ListenableFuture<RpcResult<Void>> addMissingMeters(final InstanceIdentifier<FlowCapableNode> nodeIdent,
            final FlowCapableNode flowCapableNodeConfigured,
            final FlowCapableNode flowCapableNodeOperational) {
        final NodeId nodeId = PathUtil.digNodeId(nodeIdent);
        final List<Meter> metersConfigured = safeMeters(flowCapableNodeConfigured);
        if (metersConfigured.isEmpty()) {
            LOG.trace("no meters configured for node: {} -> SKIPPING", nodeId.getValue());
            return RpcResultBuilder.<Void>success().buildFuture();
        }

        final List<Meter> metersOperational = safeMeters(flowCapableNodeOperational);

        return addMissingMeters(nodeId, nodeIdent, metersConfigured, metersOperational);
    }


    protected ListenableFuture<RpcResult<Void>> addMissingMeters(NodeId nodeId,
            final InstanceIdentifier<FlowCapableNode> nodeIdent,
            List<Meter> metersConfigured,
            List<Meter> metersOperational) {

        final Map<MeterId, Meter> meterOperationalMap = FlowCapableNodeLookups.wrapMetersToMap(metersOperational);

        final List<ListenableFuture<RpcResult<AddMeterOutput>>> allResults = new ArrayList<>();
        final List<ListenableFuture<RpcResult<UpdateMeterOutput>>> allUpdateResults = new ArrayList<>();
        for (Meter meter : metersConfigured) {
            final Meter existingMeter = meterOperationalMap.get(meter.getMeterId());
            final KeyedInstanceIdentifier<Meter, MeterKey> meterIdent = nodeIdent.child(Meter.class, meter.getKey());

            if (existingMeter == null) {
                LOG.debug("adding meter {} - absent on device {}",
                        meter.getMeterId(), nodeId);
                allResults.add(JdkFutureAdapters.listenInPoolThread(
                        meterForwarder.add(meterIdent, meter, nodeIdent)));
            } else {
                // compare content and eventually update
                LOG.trace("meter {} - already present on device {} .. comparing", meter.getMeterId(), nodeId);
                if (!meter.equals(existingMeter)) {
                    LOG.trace("meter {} - needs update on device {}", meter.getMeterId(), nodeId);
                    allUpdateResults.add(JdkFutureAdapters.listenInPoolThread(
                            meterForwarder.update(meterIdent, existingMeter, meter, nodeIdent)));
                } else {
                    LOG.trace("meter {} - on device {} is equal to the configured one", meter.getMeterId(), nodeId);
                }
            }
        }

        final ListenableFuture<RpcResult<Void>> singleVoidAddResult = Futures.transform(
                Futures.allAsList(allResults), ReconcileUtil.<AddMeterOutput>createRpcResultCondenser("meter add"));

        final ListenableFuture<RpcResult<Void>> singleVoidUpdateResult = Futures.transform(
                Futures.allAsList(allUpdateResults),
                ReconcileUtil.<UpdateMeterOutput>createRpcResultCondenser("meter update"));

        final ListenableFuture<RpcResult<Void>> summaryResults = Futures.transform(
                Futures.allAsList(singleVoidUpdateResult, singleVoidAddResult),
                ReconcileUtil.<Void>createRpcResultCondenser("meter add/update"));

        return Futures.transform(summaryResults,
                ReconcileUtil.chainBarrierFlush(PathUtil.digNodePath(nodeIdent), transactionService));
    }

    @VisibleForTesting
    ListenableFuture<RpcResult<Void>> addMissingFlows(final InstanceIdentifier<FlowCapableNode> nodeIdent,
            final FlowCapableNode flowCapableNodeConfigured,
            final FlowCapableNode flowCapableNodeOperational) {
        final NodeId nodeId = PathUtil.digNodeId(nodeIdent);
        final List<Table> tablesConfigured = safeTables(flowCapableNodeConfigured);
        if (tablesConfigured.isEmpty()) {
            LOG.trace("no tables in config for node: {} -> SKIPPING", nodeId.getValue());
            return RpcResultBuilder.<Void>success().buildFuture();
        }

        final List<Table> tablesOperational = safeTables(flowCapableNodeOperational);

        return addMissingFlows(nodeId, nodeIdent, tablesConfigured, tablesOperational);
    }

    protected ListenableFuture<RpcResult<Void>> addMissingFlows(NodeId nodeId,
            final InstanceIdentifier<FlowCapableNode> nodeIdent,
            List<Table> tablesConfigured, List<Table> tablesOperational) {

        final Map<Short, Table> tableOperationalMap = FlowCapableNodeLookups.wrapTablesToMap(tablesOperational);
        final List<ListenableFuture<RpcResult<AddFlowOutput>>> allResults = new ArrayList<>();
        final List<ListenableFuture<RpcResult<UpdateFlowOutput>>> allUpdateResults = new ArrayList<>();

        for (final Table tableConfigured : tablesConfigured) {
            final List<Flow> flowsConfigured = tableConfigured.getFlow();
            if (flowsConfigured == null || flowsConfigured.isEmpty()) {
                continue;
            }

            final KeyedInstanceIdentifier<Table, TableKey> tableIdent =
                    nodeIdent.child(Table.class, tableConfigured.getKey());

            // lookup table (on device)
            final Table tableOperational = tableOperationalMap.get(tableConfigured.getId());
            // wrap existing (on device) flows in current table into map
            final Map<FlowId, Flow> flowOperationalMap = FlowCapableNodeLookups.wrapFlowsToMap(
                    tableOperational != null
                            ? tableOperational.getFlow()
                            : null);


            // loop configured flows and check if already present on device
            for (final Flow flow : flowsConfigured) {
                final Flow existingFlow = flowOperationalMap.get(flow.getId());
                final KeyedInstanceIdentifier<Flow, FlowKey> flowIdent = tableIdent.child(Flow.class, flow.getKey());

                if (existingFlow == null) {
                    LOG.debug("adding flow {} in table {} - absent on device {}",
                            flow.getId(), tableConfigured.getKey(), nodeId);

                    allResults.add(JdkFutureAdapters.listenInPoolThread(
                            flowForwarder.add(flowIdent, flow, nodeIdent)));
                } else {
                    LOG.trace("flow {} in table {} - already present on device {} .. comparing",
                            flow.getId(), tableConfigured.getKey(), nodeId);
                    // check instructions and eventually update
                    if (!Objects.equals(flow.getInstructions(), existingFlow.getInstructions())) {
                        LOG.trace("flow {} in table {} - needs update on device {}",
                                flow.getId(), tableConfigured.getKey(), nodeId);
                        allUpdateResults.add(JdkFutureAdapters.listenInPoolThread(
                                flowForwarder.update(flowIdent, existingFlow, flow, nodeIdent)));
                    } else {
                        LOG.trace("flow {} in table {} - is equal to configured one on device {}",
                                flow.getId(), tableConfigured.getKey(), nodeId);
                    }
                }
            }
        }

        final ListenableFuture<RpcResult<Void>> singleVoidAddResult = Futures.transform(
                Futures.allAsList(allResults),
                ReconcileUtil.<AddFlowOutput>createRpcResultCondenser("flow adding"));

        final ListenableFuture<RpcResult<Void>> singleVoidUpdateResult = Futures.transform(
                Futures.allAsList(allUpdateResults),
                ReconcileUtil.<UpdateFlowOutput>createRpcResultCondenser("flow updating"));

        final ListenableFuture<RpcResult<Void>> summaryResult = Futures.transform(
                Futures.allAsList(singleVoidAddResult, singleVoidUpdateResult),
                ReconcileUtil.<Void>createRpcResultCondenser("flow add/update"));

        return Futures.transform(summaryResult,
                ReconcileUtil.chainBarrierFlush(PathUtil.digNodePath(nodeIdent), transactionService));
    }

    @VisibleForTesting
    ListenableFuture<RpcResult<Void>> removeRedundantFlows(
            final InstanceIdentifier<FlowCapableNode> nodeIdent,
            final FlowCapableNode flowCapableNodeConfigured,
            final FlowCapableNode flowCapableNodeOperational) {
        final NodeId nodeId = PathUtil.digNodeId(nodeIdent);
        final List<Table> tablesOperational = safeTables(flowCapableNodeOperational);

        if (tablesOperational.isEmpty()) {
            LOG.trace("no tables in operational for node: {} -> SKIPPING", nodeId.getValue());
            return RpcResultBuilder.<Void>success().buildFuture();
        }

        final List<Table> tablesConfigured = safeTables(flowCapableNodeConfigured);

        return removeRedundantFlows(nodeId, nodeIdent, tablesConfigured, tablesOperational);
    }

    protected ListenableFuture<RpcResult<Void>> removeRedundantFlows(
            NodeId nodeId, final InstanceIdentifier<FlowCapableNode> nodeIdent,
            final List<Table> tablesConfigured, final List<Table> tablesOperational) {
        final Map<Short, Table> tableConfigMap = FlowCapableNodeLookups.wrapTablesToMap(tablesConfigured);
        final List<ListenableFuture<RpcResult<RemoveFlowOutput>>> allResults = new ArrayList<>();

        for (final Table tableOperational : tablesOperational) {
            final List<Flow> flowsOperational = tableOperational.getFlow();
            if (flowsOperational == null || flowsOperational.isEmpty()) {
                continue;
            }

            final KeyedInstanceIdentifier<Table, TableKey> tableIdent =
                    nodeIdent.child(Table.class, tableOperational.getKey());

            // lookup configured table
            final Table tableConfig = tableConfigMap.get(tableOperational.getId());
            // wrap configured flows in current table into map
            final Map<FlowId, Flow> flowConfigMap = FlowCapableNodeLookups.wrapFlowsToMap(
                    tableConfig != null
                            ? tableConfig.getFlow()
                            : null);

            // loop flows on device and check if the are configured
            for (final Flow flow : flowsOperational) {
                if (!flowConfigMap.containsKey(flow.getId())) {
                    LOG.trace("removing flow {} in table {} - absent in config {}",
                            flow.getId(), tableOperational.getKey(), nodeId);

                    final KeyedInstanceIdentifier<Flow, FlowKey> flowIdent =
                            tableIdent.child(Flow.class, flow.getKey());
                    allResults.add(JdkFutureAdapters.listenInPoolThread(
                            flowForwarder.remove(flowIdent, flow, nodeIdent)));

                } else {
                    LOG.trace("skipping flow {} in table {} - present in config {}",
                            flow.getId(), tableOperational.getKey(), nodeId);
                }
            }
        }

        final ListenableFuture<RpcResult<Void>> singleVoidResult = Futures.transform(
                Futures.allAsList(allResults), ReconcileUtil.<RemoveFlowOutput>createRpcResultCondenser("flow remove"));
        return Futures.transform(singleVoidResult,
                ReconcileUtil.chainBarrierFlush(PathUtil.digNodePath(nodeIdent), transactionService));

    }

    @VisibleForTesting
    ListenableFuture<RpcResult<Void>> removeRedundantMeters(final InstanceIdentifier<FlowCapableNode> nodeIdent,
            final FlowCapableNode flowCapableNodeConfigured,
            final FlowCapableNode flowCapableNodeOperational) {

        final NodeId nodeId = PathUtil.digNodeId(nodeIdent);
        final List<Meter> metersOperational = safeMeters(flowCapableNodeOperational);
        if (metersOperational.isEmpty()) {
            LOG.trace("no meters on device for node: {} -> SKIPPING", nodeId.getValue());
            return RpcResultBuilder.<Void>success().buildFuture();
        }

        final List<Meter> metersConfigured = safeMeters(flowCapableNodeConfigured);

        return removeRedundantMeters(nodeId, nodeIdent, metersConfigured, metersOperational);
    }


    protected ListenableFuture<RpcResult<Void>> removeRedundantMeters(NodeId nodeId,
            final InstanceIdentifier<FlowCapableNode> nodeIdent,
            List<Meter> metersConfigured,
            List<Meter> metersOperational) {

        final Map<MeterId, Meter> meterConfigMap = FlowCapableNodeLookups.wrapMetersToMap(metersConfigured);

        final List<ListenableFuture<RpcResult<RemoveMeterOutput>>> allResults = new ArrayList<>();
        for (Meter meter : metersOperational) {
            if (!meterConfigMap.containsKey(meter.getMeterId())) {
                LOG.trace("removing meter {} - absent in config {}",
                        meter.getMeterId(), nodeId);
                final KeyedInstanceIdentifier<Meter, MeterKey> meterIdent =
                        nodeIdent.child(Meter.class, meter.getKey());
                allResults.add(JdkFutureAdapters.listenInPoolThread(
                        meterForwarder.remove(meterIdent, meter, nodeIdent)));
            } else {
                LOG.trace("skipping meter {} - present in config {}",
                        meter.getMeterId(), nodeId);
            }
        }

        final ListenableFuture<RpcResult<Void>> singleVoidResult = Futures.transform(
                Futures.allAsList(allResults),
                ReconcileUtil.<RemoveMeterOutput>createRpcResultCondenser("meter remove"));
        return Futures.transform(singleVoidResult,
                ReconcileUtil.chainBarrierFlush(PathUtil.digNodePath(nodeIdent), transactionService));
    }

    @VisibleForTesting
    ListenableFuture<RpcResult<Void>> removeRedundantGroups(final InstanceIdentifier<FlowCapableNode> nodeIdent,
            final FlowCapableNode flowCapableNodeConfigured,
            final FlowCapableNode flowCapableNodeOperational) {
        final NodeId nodeId = PathUtil.digNodeId(nodeIdent);
        final List<Group> groupsOperational = safeGroups(flowCapableNodeOperational);
        if (groupsOperational == null || groupsOperational.isEmpty()) {
            LOG.trace("no groups on device for node: {} -> SKIPPING", nodeId.getValue());
            return RpcResultBuilder.<Void>success().buildFuture();
        }

        final List<Group> groupsConfigured = safeGroups(flowCapableNodeConfigured);

        return removeRedundantGroups(nodeId, nodeIdent, groupsConfigured, groupsOperational);
    }

    ListenableFuture<RpcResult<Void>> removeRedundantGroups(NodeId nodeId,
            final InstanceIdentifier<FlowCapableNode> nodeIdent,
            List<Group> groupsConfigured, List<Group> groupsOperational) {

        final Map<Long, Group> groupConfigMap = FlowCapableNodeLookups.wrapGroupsToMap(groupsConfigured);

        final List<Group> pendingGroups = new ArrayList<>();
        pendingGroups.addAll(groupsOperational);

        ListenableFuture<RpcResult<Void>> chainedResult;
        try {
            final List<ItemSyncBox<Group>> groupsRemovePlan =
                    ReconcileUtil.resolveAndDivideGroups(nodeId, groupConfigMap, pendingGroups, false);
            if (!groupsRemovePlan.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("removing groups: inputGroups={}, planSteps={}, toRemoveTotal={}",
                            pendingGroups.size(), groupsRemovePlan.size(),
                            ReconcileUtil.countTotalAdds(groupsRemovePlan));
                }
                Collections.reverse(groupsRemovePlan);
                chainedResult = flushRemoveGroupPortionAndBarrier(nodeIdent, groupsRemovePlan.get(0));
                for (final ItemSyncBox<Group> groupsPortion : Iterables.skip(groupsRemovePlan, 1)) {
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

    static List<Group> safeGroups(FlowCapableNode node) {
        if (node == null) {
            return Collections.emptyList();
        }

        return MoreObjects.firstNonNull(node.getGroup(), ImmutableList.<Group>of());
    }

    static List<Table> safeTables(FlowCapableNode node) {
        if (node == null) {
            return Collections.emptyList();
        }

        return MoreObjects.firstNonNull(node.getTable(), ImmutableList.<Table>of());
    }

    static List<Meter> safeMeters(FlowCapableNode node) {
        if (node == null) {
            return Collections.emptyList();
        }

        return MoreObjects.firstNonNull(node.getMeter(), ImmutableList.<Meter>of());
    }

    private ListenableFuture<RpcResult<Void>> flushRemoveGroupPortionAndBarrier(
            final InstanceIdentifier<FlowCapableNode> nodeIdent,
            final ItemSyncBox<Group> groupsPortion) {
        List<ListenableFuture<RpcResult<RemoveGroupOutput>>> allResults = new ArrayList<>();
        for (Group group : groupsPortion.getItemsToAdd()) {
            final KeyedInstanceIdentifier<Group, GroupKey> groupIdent = nodeIdent.child(Group.class, group.getKey());
            allResults.add(JdkFutureAdapters.listenInPoolThread(groupForwarder.remove(groupIdent, group, nodeIdent)));
        }

        final ListenableFuture<RpcResult<Void>> singleVoidResult = Futures.transform(
                Futures.allAsList(allResults),
                ReconcileUtil.<RemoveGroupOutput>createRpcResultCondenser("group remove"));

        return Futures.transform(singleVoidResult,
                ReconcileUtil.chainBarrierFlush(PathUtil.digNodePath(nodeIdent), transactionService));
    }
}
