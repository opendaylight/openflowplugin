/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl.reconciliate;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesCommiter;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.openflowplugin.applications.frm.impl.util.FlowCapableNodeLookups;
import org.opendaylight.openflowplugin.applications.frm.impl.util.ReconcileUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
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

/**
 * FlowNode Reconciliation Listener - strict config limited implementation
 * <br>
 * Reconciliation for a new FlowCapableNode. Stategy:
 * <ul>
 * <li>add all missing objects in order: table-features, groups*, meters, flows</li>
 * <li>send barrier</li>
 * <li>remove redundant objects in order: flows, meters, groups*, table-features</li>
 * <li>send barrier</li>
 * </ul>
 * *need to reorder and place barriers between dependency tree levels
 *
 */
public class FlowCapableNodeReconciliatorStrictConfigLimitedImpl extends AbstractFlowNodeReconciliator {

    private static final Logger LOG = LoggerFactory.getLogger(FlowCapableNodeReconciliatorStrictConfigLimitedImpl.class);

    public FlowCapableNodeReconciliatorStrictConfigLimitedImpl(final ForwardingRulesManager manager, final DataBroker db) {
        super(manager, db);
    }

    @Override
    void reconciliation(final InstanceIdentifier<FlowCapableNode> nodeIdent, final FlowCapableNode flowCapableNodeConfigured) {
        final NodeId nodeId = digNodeId(nodeIdent);
        final CheckedFuture<Optional<FlowCapableNode>, ReadFailedException> operationalFlowCapableNode = readOperationalFlowCapableNode(nodeIdent);

        Futures.transform(operationalFlowCapableNode, new Function<Optional<FlowCapableNode>, Void>() {
            @Override
            public Void apply(final Optional<FlowCapableNode> input) {
                if (input != null && input.isPresent()) {
                    final FlowCapableNode flowCapableNodeOperational = input.get();
                    // do reconciliation
                    /** reconciliation strategy - phase 1:
                     *  - add missing objects in order
                     *    - table features
                     *    - groups (reordered)
                     *    - meters
                     *    - flows
                     **/

                    ListenableFuture<RpcResult<Void>> resultVehicle;

                    /* Tables - have to be pushed before groups */
                    // TODO: add future result to table update
                    updateTableFeatures(nodeIdent, getTableFeaturesCommiter(), flowCapableNodeConfigured);
                    resultVehicle = addMissingGroups(nodeIdent, getGroupCommiter(), flowCapableNodeConfigured, flowCapableNodeOperational);
                    resultVehicle = Futures.transform(resultVehicle, new AsyncFunction<RpcResult<Void>, RpcResult<Void>>() {
                        @Override
                        public ListenableFuture<RpcResult<Void>> apply(final RpcResult<Void> input) throws Exception {
                            return addMissingMeters(nodeIdent, getMeterCommiter(), flowCapableNodeConfigured, flowCapableNodeOperational);
                        }
                    });
                    resultVehicle = Futures.transform(resultVehicle, new AsyncFunction<RpcResult<Void>, RpcResult<Void>>() {
                        @Override
                        public ListenableFuture<RpcResult<Void>> apply(final RpcResult<Void> input) throws Exception {
                            return addMissingFlows(nodeIdent, getFlowCommiter(), flowCapableNodeConfigured, flowCapableNodeOperational);
                        }
                    });

                    /** reconciliation strategy - phase 2:
                     *  - remove redundand objects in order
                     *    - flows
                     *    - meters
                     *    - groups (reordered)
                     **/
                    //        removeRedundandFlows(nodeIdent, flowCapableNodeConfigured, nodeId);
                    //        removeRedundandMeters(nodeIdent, flowCapableNodeConfigured);
                    //        removeRedundandGroups(nodeIdent, flowCapableNodeConfigured);

                    // log final result
                    Futures.addCallback(resultVehicle, new FutureCallback<RpcResult<Void>>() {
                        @Override
                        public void onSuccess(@Nullable final RpcResult<Void> result) {
                            if (result != null) {
                                if (result.isSuccessful())
                                    LOG.debug("reconciliation finished successfully: {}", nodeId.getValue());
                                else {
                                    LOG.debug("reconciliation failed: {} -> ", nodeId.getValue(), result.getErrors());
                                }
                            } else {
                                LOG.debug("reconciliation failed: {} -> ", nodeId.getValue(), result.getErrors());
                            }
                        }

                        @Override
                        public void onFailure(final Throwable t) {
                            LOG.debug("reconciliation failed seriously: {}", nodeId.getValue(), t);
                        }
                    });
                } else {
                    LOG.debug("operational flow-capable-node not available: {}", nodeId.getValue());
                }

                return null;
            }
        });

    }

    private ListenableFuture<RpcResult<Void>> addMissingFlows(final InstanceIdentifier<FlowCapableNode> nodeIdent,
                                                              final ForwardingRulesCommiter<Flow, AddFlowOutput> flowCommiter, final FlowCapableNode flowCapableNodeConfigured,
                                                              final FlowCapableNode flowCapableNodeOperational) {
        final NodeId nodeId = digNodeId(nodeIdent);
        final List<Table> tablesConfigured = flowCapableNodeConfigured.getTable();

        if (tablesConfigured == null || tablesConfigured.isEmpty()) {
            LOG.debug("no tables configured for node: {} -> SKIPPING", nodeId.getValue());
            return RpcResultBuilder.<Void>success().buildFuture();
        }

        final Map<Short, Table> tableOperationalMap = FlowCapableNodeLookups.wrapTablesToMap(flowCapableNodeOperational.getTable());
        final List<ListenableFuture<RpcResult<AddFlowOutput>>> allResults = new ArrayList<>();

        for (final Table tableConfigured : tablesConfigured) {
            final List<Flow> flowsConfigured = tableConfigured.getFlow();
            if (flowsConfigured == null || flowsConfigured.isEmpty()) {
                continue;
            }

            final KeyedInstanceIdentifier<Table, TableKey> tableIdent = nodeIdent.child(Table.class, tableConfigured.getKey());

            // lookup table (on device)
            final Table tableOperational = tableOperationalMap.get(tableConfigured.getId());
            // wrap existing (on device) flows in current table into map
            final Map<FlowId, Flow> flowOperationalMap = tableOperational != null
                    ? FlowCapableNodeLookups.wrapFlowsToMap(tableOperational.getFlow())
                    : Collections.<FlowId, Flow>emptyMap();

            // loop configured flows and check if already present on device
            for (final Flow flow : flowsConfigured) {

                if (!flowOperationalMap.containsKey(flow.getId())) {
                    LOG.trace("adding flow {} in table {} - absent on device {}",
                            flow.getId(), tableConfigured.getKey(), nodeId);
                    final KeyedInstanceIdentifier<Flow, FlowKey> flowIdent = tableIdent.child(Flow.class, flow.getKey());
                    allResults.add(JdkFutureAdapters.listenInPoolThread(
                            flowCommiter.add(flowIdent, flow, nodeIdent)));
                } else {
                    LOG.trace("skipping flow {} in table {} - already present on device {}",
                            flow.getId(), tableConfigured.getKey(), nodeId);
                }
            }
        }

        final ListenableFuture<RpcResult<Void>> singleVoidResult = Futures.transform(
                Futures.allAsList(allResults), ReconcileUtil.CONDENSE_FLOW_ADDS_TO_VOID);
        return Futures.transform(singleVoidResult, ReconcileUtil.chainBarrierFlush(nodeIdent, getFlowCapableTransactionService()));
    }

    private ListenableFuture<RpcResult<Void>> addMissingMeters(final InstanceIdentifier<FlowCapableNode> nodeIdent,
                                                               final ForwardingRulesCommiter<Meter, AddMeterOutput> meterCommiter,
                                                               final FlowCapableNode flowCapableNodeConfigured,
                                                               final FlowCapableNode flowCapableNodeOperational) {

        final NodeId nodeId = digNodeId(nodeIdent);
        final List<Meter> metersConfigured = flowCapableNodeConfigured.getMeter();
        if (metersConfigured == null || metersConfigured.isEmpty()) {
            LOG.debug("no meters configured for node: {} -> SKIPPING", nodeId.getValue());
            return RpcResultBuilder.<Void>success().buildFuture();
        }

        final Map<MeterId, Meter> meterOperationalMap = FlowCapableNodeLookups.wrapMetersToMap(flowCapableNodeOperational.getMeter());

        final List<ListenableFuture<RpcResult<AddMeterOutput>>> allResults = new ArrayList<>();
        for (Meter meter : metersConfigured) {
            if (!meterOperationalMap.containsKey(meter.getMeterId())) {
                LOG.trace("adding meter {} - absent on device {}",
                        meter.getMeterId(), nodeId);
                final KeyedInstanceIdentifier<Meter, MeterKey> meterIdent = nodeIdent.child(Meter.class, meter.getKey());
                allResults.add(JdkFutureAdapters.listenInPoolThread(
                        meterCommiter.add(meterIdent, meter, nodeIdent)));
            } else {
                LOG.trace("skipping meter {} - already present on device {}",
                        meter.getMeterId(), nodeId);
            }
        }

        final ListenableFuture<RpcResult<Void>> singleVoidResult = Futures.transform(
                Futures.allAsList(allResults), ReconcileUtil.CONDENSE_METER_ADDS_TO_VOID);
        return Futures.transform(singleVoidResult, ReconcileUtil.chainBarrierFlush(nodeIdent, getFlowCapableTransactionService()));
    }

    private static NodeId digNodeId(final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        return nodeIdent.firstKeyOf(Node.class).getId();
    }

    private ListenableFuture<RpcResult<Void>> addMissingGroups(final InstanceIdentifier<FlowCapableNode> nodeIdent,
                                                               final ForwardingRulesCommiter<Group, AddGroupOutput> groupCommiter,
                                                               final FlowCapableNode flowCapableNodeConfigured,
                                                               final FlowCapableNode flowCapableNodeOperational) {
        final NodeId nodeId = digNodeId(nodeIdent);
        final List<Group> groupsConfigured = flowCapableNodeConfigured.getGroup();
        if (groupsConfigured == null) {
            LOG.debug("no groups configured for node: {} -> SKIPPING", nodeId.getValue());
            return RpcResultBuilder.<Void>success().buildFuture();
        }

        final Map<Long, Group> groupOperationalMap = FlowCapableNodeLookups.wrapGroupsToMap(flowCapableNodeOperational.getGroup());
        final Set<Long> installedGroupIds = new HashSet<>();
        installedGroupIds.addAll(groupOperationalMap.keySet());

        final List<Group> pendingGroups = new ArrayList<>();
        pendingGroups.addAll(groupsConfigured);

        ListenableFuture<RpcResult<Void>> chainedResult;
        final List<Set<Group>> groupsAddPlan;
        try {
            groupsAddPlan = resolveAndDivideGroups(nodeId, installedGroupIds, pendingGroups);
            chainedResult = flushGroupPortionAndBarrier(groupCommiter, nodeIdent, groupsAddPlan.get(0));
            for (final Set<Group> groupsPortion : Iterables.skip(groupsAddPlan, 1)) {
                chainedResult = Futures.transform(chainedResult, new AsyncFunction<RpcResult<Void>, RpcResult<Void>>() {
                    @Override
                    public ListenableFuture<RpcResult<Void>> apply(final RpcResult<Void> input) throws Exception {
                        final ListenableFuture<RpcResult<Void>> result;
                        if (input.isSuccessful()) {
                            result = flushGroupPortionAndBarrier(groupCommiter, nodeIdent, groupsPortion);
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

    private ListenableFuture<RpcResult<Void>> flushGroupPortionAndBarrier(
            final ForwardingRulesCommiter<Group, AddGroupOutput> groupCommiter,
            final InstanceIdentifier<FlowCapableNode> nodeIdent,
            final Set<Group> groupsPortion) {
        List<ListenableFuture<RpcResult<AddGroupOutput>>> allResults = new ArrayList<>();
        for (Group group : groupsPortion) {
            final KeyedInstanceIdentifier<Group, GroupKey> groupIdent = nodeIdent.child(Group.class, group.getKey());
            allResults.add(JdkFutureAdapters.listenInPoolThread(groupCommiter.add(groupIdent, group, nodeIdent)));

        }

        final ListenableFuture<RpcResult<Void>> singleVoidResult = Futures.transform(
                Futures.allAsList(allResults), ReconcileUtil.CONDENSE_GROUP_ADDS_TO_VOID);
        return Futures.transform(singleVoidResult, ReconcileUtil.chainBarrierFlush(nodeIdent, getFlowCapableTransactionService()));
    }

    static List<Set<Group>> resolveAndDivideGroups(final NodeId nodeId, final Set<Long> installedGroupIds,
                                                   final Collection<Group> pendingGroups) {
        List<Set<Group>> plan = new ArrayList<>();
        while (!Iterables.isEmpty(pendingGroups)) {
            final Set<Group> stepPlan = new HashSet<>();
            final Iterator<Group> iterator = pendingGroups.iterator();
            final Set<Long> installIncrement = new HashSet<>();

            while (iterator.hasNext()) {
                final Group group = iterator.next();

                if (installedGroupIds.contains(group.getGroupId().getValue())) {
                    iterator.remove();
                } else if (checkGroupPrecondition(installedGroupIds, group)) {
                    iterator.remove();
                    installIncrement.add(group.getGroupId().getValue());
                    stepPlan.add(group);
                }
            }

            if (stepPlan.isEmpty()) {
                LOG.warn("Failed to resolve and divide groups into preconditions-match based ordered plan: {}, " +
                        "resolving stuck at level {}", nodeId.getValue(), plan.size());
                throw new IllegalStateException("Failed to resolve and divide groups when matching preconditions");
            }

            // atomic update of installed flows in order to keep plan portions clean of local group dependencies
            installedGroupIds.addAll(installIncrement);
            plan.add(stepPlan);
        }

        return plan;
    }

    static boolean checkGroupPrecondition(final Set<Long> installedGroupIds, final Group pendingGroup) {
        boolean okToInstall = true;
        for (Bucket bucket : pendingGroup.getBuckets().getBucket()) {
            for (Action action : bucket.getAction()) {
                if (GroupActionCase.class.equals(action.getAction().getImplementedInterface())) {
                    Long groupId = ((GroupActionCase) (action.getAction())).getGroupAction().getGroupId();
                    if (!installedGroupIds.contains(groupId)) {
                        okToInstall = false;
                        break;
                    }
                }
            }
            if (!okToInstall) {
                break;
            }
        }
        return okToInstall;
    }

    private void updateTableFeatures(final InstanceIdentifier<FlowCapableNode> nodeIdent,
                                     final ForwardingRulesCommiter<TableFeatures, UpdateTableOutput> tableFeaturesCommiter,
                                     final FlowCapableNode flowCapableNodeConfigured) {
        // CHECK if while pusing the update, updateTableInput can be null to emulate a table add
        List<Table> tableList = flowCapableNodeConfigured.getTable() != null
                ? flowCapableNodeConfigured.getTable() : Collections.<Table>emptyList();
        for (Table table : tableList) {
            TableKey tableKey = table.getKey();
            KeyedInstanceIdentifier<TableFeatures, TableFeaturesKey> tableFeaturesII
                    = nodeIdent.child(Table.class, tableKey).child(TableFeatures.class, new TableFeaturesKey(tableKey.getId()));
            List<TableFeatures> tableFeatures = table.getTableFeatures();
            if (tableFeatures != null) {
                for (TableFeatures tableFeaturesItem : tableFeatures) {
                    tableFeaturesCommiter.update(tableFeaturesII, tableFeaturesItem, null, nodeIdent);
                }
            }
        }
    }

}

