/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.opendaylight.openflowplugin.applications.frsync.SyncPlanPushStrategy;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.impl.strategy.SynchronizationDiffInput;
import org.opendaylight.openflowplugin.applications.frsync.util.CrudCounts;
import org.opendaylight.openflowplugin.applications.frsync.util.FlowCapableNodeLookups;
import org.opendaylight.openflowplugin.applications.frsync.util.FxChainUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.ItemSyncBox;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconcileUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.SyncCrudCounters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Synchronization reactor implementation, applicable for both - syncup and reconciliation.
 */
public class SyncReactorImpl implements SyncReactor {

    private static final Logger LOG = LoggerFactory.getLogger(SyncReactorImpl.class);
    private final SyncPlanPushStrategy syncPlanPushStrategy;

    public SyncReactorImpl(SyncPlanPushStrategy syncPlanPushStrategy) {
        this.syncPlanPushStrategy = Preconditions.checkNotNull(syncPlanPushStrategy, "execution strategy is mandatory");
    }

    @Override
    public ListenableFuture<Boolean> syncup(final InstanceIdentifier<FlowCapableNode> nodeIdent,
                                            final FlowCapableNode configTree, final FlowCapableNode operationalTree) {

        final NodeId nodeId = PathUtil.digNodeId(nodeIdent);
        LOG.trace("syncup impl {} cfg:{} oper:{}", nodeId.getValue(), configTree == null ? "is null" : "non null", operationalTree == null ? "is null" : "non null");
        final SyncCrudCounters counters = new SyncCrudCounters();

        /**
         * instructions:
         *  - extract diff changes and prepare change steps in safe order
         *    - optimization: decide if updates needed
         *  - execute chosen implementation (e.g. conventional API, bulk API, flat bulk API)
         *  - recommended order follows:
         * reconciliation strategy - phase 1: - add/update missing objects in following order:
         *  - table features - groups (reordered) - meters - flows
         *
         * reconciliation strategy - phase 2: - remove redundant objects in following order:
         *  - flows - meters - groups (reordered)
         **/

        final List<ItemSyncBox<Group>> groupsToAddOrUpdate = extractGroupsToAddOrUpdate(nodeId, configTree, operationalTree);
        final ItemSyncBox<Meter> metersToAddOrUpdate = extractMetersToAddOrUpdate(nodeId, configTree, operationalTree);
        final Map<TableKey, ItemSyncBox<Flow>> flowsToAddOrUpdate = extractFlowsToAddOrUpdate(nodeId, configTree, operationalTree);

        final Map<TableKey, ItemSyncBox<Flow>> flowsToRemove = extractFlowsToRemove(nodeId, configTree, operationalTree);
        final ItemSyncBox<Meter> metersToRemove = extractMetersToRemove(nodeId, configTree, operationalTree);
        final List<ItemSyncBox<Group>> groupsToRemove = extractGroupsToRemove(nodeId, configTree, operationalTree);

        final SynchronizationDiffInput input = new SynchronizationDiffInput(nodeIdent,
                groupsToAddOrUpdate, metersToAddOrUpdate, flowsToAddOrUpdate,
                flowsToRemove, metersToRemove, groupsToRemove);

        counters.setStartNano(System.nanoTime());
        final ListenableFuture<RpcResult<Void>> bootstrapResultFuture = RpcResultBuilder.<Void>success().buildFuture();
        final ListenableFuture<RpcResult<Void>> resultVehicle = syncPlanPushStrategy.executeSyncStrategy(
                bootstrapResultFuture, input, counters);

        // log final result
        Futures.addCallback(resultVehicle, FxChainUtil.logResultCallback(nodeId, "final result"));

        return Futures.transform(resultVehicle, new Function<RpcResult<Void>, Boolean>() {
            @Override
            public Boolean apply(RpcResult<Void> input) {
                if (input == null) {
                    return false;
                }

                if (LOG.isDebugEnabled()) {
                    final CrudCounts flowCrudCounts = counters.getFlowCrudCounts();
                    final CrudCounts meterCrudCounts = counters.getMeterCrudCounts();
                    final CrudCounts groupCrudCounts = counters.getGroupCrudCounts();
                    LOG.debug("sync-outcome[{}] (added/updated/removed): flow={}/{}/{}, meter={}/{}/{}, group={}/{}/{}, took={} ms",
                            nodeId.getValue(),
                            flowCrudCounts.getAdded(),
                            flowCrudCounts.getUpdated(),
                            flowCrudCounts.getRemoved(),
                            meterCrudCounts.getAdded(),
                            meterCrudCounts.getUpdated(),
                            meterCrudCounts.getRemoved(),
                            groupCrudCounts.getAdded(),
                            groupCrudCounts.getUpdated(),
                            groupCrudCounts.getRemoved(),
                            TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - counters.getStartNano())
                    );
                }

                return input.isSuccessful();
            }
        });
    }

    @VisibleForTesting
    static List<ItemSyncBox<Group>> extractGroupsToAddOrUpdate(final NodeId nodeId,
                                                               final FlowCapableNode flowCapableNodeConfigured,
                                                               final FlowCapableNode flowCapableNodeOperational) {
        final List<Group> groupsConfigured = ReconcileUtil.safeGroups(flowCapableNodeConfigured);
        final List<Group> groupsOperational = ReconcileUtil.safeGroups(flowCapableNodeOperational);
        final Map<Long, Group> groupOperationalMap = FlowCapableNodeLookups.wrapGroupsToMap(groupsOperational);

        final List<Group> pendingGroups = new ArrayList<>();
        pendingGroups.addAll(groupsConfigured);

        return ReconcileUtil.resolveAndDivideGroupDiffs(nodeId, groupOperationalMap, pendingGroups, true);
    }

    @VisibleForTesting
    static ItemSyncBox<Meter> extractMetersToAddOrUpdate(final NodeId nodeId,
                                                         final FlowCapableNode flowCapableNodeConfigured,
                                                         final FlowCapableNode flowCapableNodeOperational) {
        final List<Meter> metersConfigured = ReconcileUtil.safeMeters(flowCapableNodeConfigured);
        final List<Meter> metersOperational = ReconcileUtil.safeMeters(flowCapableNodeOperational);
        final Map<MeterId, Meter> meterOperationalMap = FlowCapableNodeLookups.wrapMetersToMap(metersOperational);

        return ReconcileUtil.resolveMeterDiffs(nodeId, meterOperationalMap, metersConfigured, true);
    }

    @VisibleForTesting
    static Map<TableKey, ItemSyncBox<Flow>> extractFlowsToAddOrUpdate(final NodeId nodeId,
                                                                      final FlowCapableNode flowCapableNodeConfigured,
                                                                      final FlowCapableNode flowCapableNodeOperational) {
        final List<Table> tablesConfigured = ReconcileUtil.safeTables(flowCapableNodeConfigured);
        if (tablesConfigured.isEmpty()) {
            return Collections.emptyMap();
        }

        final List<Table> tablesOperational = ReconcileUtil.safeTables(flowCapableNodeOperational);
        final Map<Short, Table> tableOperationalMap = FlowCapableNodeLookups.wrapTablesToMap(tablesOperational);

        return ReconcileUtil.resolveFlowDiffsInAllTables(nodeId, tableOperationalMap, tablesConfigured, true);
    }

    @VisibleForTesting
    static Map<TableKey, ItemSyncBox<Flow>> extractFlowsToRemove(final NodeId nodeId,
                                                                 final FlowCapableNode flowCapableNodeConfigured,
                                                                 final FlowCapableNode flowCapableNodeOperational) {
        final List<Table> tablesOperational = ReconcileUtil.safeTables(flowCapableNodeOperational);
        if (tablesOperational.isEmpty()) {
            return Collections.emptyMap();
        }

        final List<Table> tablesConfigured = ReconcileUtil.safeTables(flowCapableNodeConfigured);
        final Map<Short, Table> tableConfiguredMap = FlowCapableNodeLookups.wrapTablesToMap(tablesConfigured);

        return ReconcileUtil.resolveFlowDiffsInAllTables(nodeId, tableConfiguredMap, tablesOperational, false);
    }

    @VisibleForTesting
    static ItemSyncBox<Meter> extractMetersToRemove(final NodeId nodeId,
                                                    final FlowCapableNode flowCapableNodeConfigured,
                                                    final FlowCapableNode flowCapableNodeOperational) {
        final List<Meter> metersConfigured = ReconcileUtil.safeMeters(flowCapableNodeConfigured);
        final List<Meter> metersOperational = ReconcileUtil.safeMeters(flowCapableNodeOperational);
        final Map<MeterId, Meter> meterConfiguredMap = FlowCapableNodeLookups.wrapMetersToMap(metersConfigured);

        return ReconcileUtil.resolveMeterDiffs(nodeId, meterConfiguredMap, metersOperational, false);
    }

    @VisibleForTesting
    static List<ItemSyncBox<Group>> extractGroupsToRemove(final NodeId nodeId,
                                                          final FlowCapableNode flowCapableNodeConfigured,
                                                          final FlowCapableNode flowCapableNodeOperational) {
        final List<Group> groupsConfigured = ReconcileUtil.safeGroups(flowCapableNodeConfigured);
        final List<Group> groupsOperational = ReconcileUtil.safeGroups(flowCapableNodeOperational);
        final Map<Long, Group> groupConfiguredMap = FlowCapableNodeLookups.wrapGroupsToMap(groupsConfigured);

        final List<Group> pendingGroups = new ArrayList<>();
        pendingGroups.addAll(groupsOperational);

        return ReconcileUtil.resolveAndDivideGroupDiffs(nodeId, groupConfiguredMap, pendingGroups, false);
    }

}
