/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.util;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.JdkFutureAdapters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util methods for group reconcil task (future chaining, transforms).
 */
public final class ReconcileUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ReconcileUtil.class);

    private ReconcileUtil() {
        throw new IllegalStateException("This class should not be instantiated.");
    }

    /**
     * @param previousItemAction description for case when the triggering future contains failure
     * @param <D>                type of rpc output (gathered in list)
     * @return single rpc result of type Void honoring all partial rpc results
     */
    public static <D> Function<List<RpcResult<D>>, RpcResult<Void>> createRpcResultCondenser(final String previousItemAction) {
        return input -> {
            final RpcResultBuilder<Void> resultSink;
            if (input != null) {
                List<RpcError> errors = new ArrayList<>();
                for (RpcResult<D> rpcResult : input) {
                    if (!rpcResult.isSuccessful()) {
                        errors.addAll(rpcResult.getErrors());
                    }
                }
                if (errors.isEmpty()) {
                    resultSink = RpcResultBuilder.success();
                } else {
                    resultSink = RpcResultBuilder.<Void>failed().withRpcErrors(errors);
                }
            } else {
                resultSink = RpcResultBuilder.<Void>failed()
                        .withError(RpcError.ErrorType.APPLICATION, "previous " + previousItemAction + " failed");
            }
            return resultSink.build();
        };
    }

    /**
     * @param actionDescription description for case when the triggering future contains failure
     * @param <D>               type of rpc output (gathered in list)
     * @return single rpc result of type Void honoring all partial rpc results
     */
    public static <D> Function<RpcResult<D>, RpcResult<Void>> createRpcResultToVoidFunction(final String actionDescription) {
        return input -> {
            final RpcResultBuilder<Void> resultSink;
            if (input != null) {
                List<RpcError> errors = new ArrayList<>();
                if (!input.isSuccessful()) {
                    errors.addAll(input.getErrors());
                    resultSink = RpcResultBuilder.<Void>failed().withRpcErrors(errors);
                } else {
                    resultSink = RpcResultBuilder.success();
                }
            } else {
                resultSink = RpcResultBuilder.<Void>failed()
                        .withError(RpcError.ErrorType.APPLICATION, "action of " + actionDescription + " failed");
            }
            return resultSink.build();
        };
    }

    /**
     * @param nodeIdent                     flow capable node path - target device for routed rpc
     * @param flowCapableTransactionService barrier rpc service
     * @return async barrier result
     */
    public static AsyncFunction<RpcResult<Void>, RpcResult<Void>> chainBarrierFlush(
            final InstanceIdentifier<Node> nodeIdent,
            final FlowCapableTransactionService flowCapableTransactionService) {
        return input -> {
            final SendBarrierInput barrierInput = new SendBarrierInputBuilder()
                    .setNode(new NodeRef(nodeIdent))
                    .build();
            return JdkFutureAdapters.listenInPoolThread(flowCapableTransactionService.sendBarrier(barrierInput));
        };
    }

    /**
     * @param nodeId             target node
     * @param installedGroupsArg groups resent on device
     * @param pendingGroups      groups configured for device
     * @return list of safe synchronization steps with updates
     */
    public static List<ItemSyncBox<Group>> resolveAndDivideGroupDiffs(final NodeId nodeId,
                                                                      final Map<Long, Group> installedGroupsArg,
                                                                      final Collection<Group> pendingGroups) {
        return resolveAndDivideGroupDiffs(nodeId, installedGroupsArg, pendingGroups, true);
    }

    /**
     * @param nodeId             target node
     * @param installedGroupsArg groups resent on device
     * @param pendingGroups      groups configured for device
     * @param gatherUpdates      check content of pending item if present on device (and create update task eventually)
     * @return list of safe synchronization steps
     */
    public static List<ItemSyncBox<Group>> resolveAndDivideGroupDiffs(final NodeId nodeId,
                                                                      final Map<Long, Group> installedGroupsArg,
                                                                      final Collection<Group> pendingGroups,
                                                                      final boolean gatherUpdates) {
        final Map<Long, Group> installedGroups = new HashMap<>(installedGroupsArg);
        final List<ItemSyncBox<Group>> plan = new ArrayList<>();

        while (!Iterables.isEmpty(pendingGroups)) {
            final ItemSyncBox<Group> stepPlan = new ItemSyncBox<>();
            final Iterator<Group> iterator = pendingGroups.iterator();
            final Map<Long, Group> installIncrement = new HashMap<>();

            while (iterator.hasNext()) {
                final Group group = iterator.next();

                final Group existingGroup = installedGroups.get(group.getGroupId().getValue());
                if (existingGroup != null) {
                    if (!gatherUpdates) {
                        iterator.remove();
                    } else {
                        // check buckets and eventually update
                        if (group.equals(existingGroup)) {
                            iterator.remove();
                        } else {
                            if (checkGroupPrecondition(installedGroups.keySet(), group)) {
                                iterator.remove();
                                LOG.trace("Group {} on device {} differs - planned for update", group.getGroupId(), nodeId);
                                stepPlan.getItemsToUpdate().add(new ItemSyncBox.ItemUpdateTuple<>(existingGroup, group));
                            }
                        }
                    }
                } else if (checkGroupPrecondition(installedGroups.keySet(), group)) {
                    iterator.remove();
                    installIncrement.put(group.getGroupId().getValue(), group);
                    stepPlan.getItemsToPush().add(group);
                }
            }

            if (!stepPlan.isEmpty()) {
                // atomic update of installed flows in order to keep plan portions clean of local group dependencies
                installedGroups.putAll(installIncrement);
                plan.add(stepPlan);
            } else if (!pendingGroups.isEmpty()) {
                LOG.warn("Failed to resolve and divide groups into preconditions-match based ordered plan: {}, " +
                        "resolving stuck at level {}", nodeId.getValue(), plan.size());
                throw new IllegalStateException("Failed to resolve and divide groups when matching preconditions");
            }
        }

        return plan;
    }

    public static boolean checkGroupPrecondition(final Set<Long> installedGroupIds, final Group pendingGroup) {
        boolean okToInstall = true;
        // check each bucket in the pending group
        for (Bucket bucket : pendingGroup.getBuckets().getBucket()) {
            for (Action action : bucket.getAction()) {
                // if the output action is a group
                if (GroupActionCase.class.equals(action.getAction().getImplementedInterface())) {
                    Long groupId = ((GroupActionCase) (action.getAction())).getGroupAction().getGroupId();
                    // see if that output group is installed
                    if (!installedGroupIds.contains(groupId)) {
                        // if not installed, we have missing dependencies and cannot install this pending group
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

    public static <E> int countTotalPushed(final Iterable<ItemSyncBox<E>> groupsAddPlan) {
        int count = 0;
        for (ItemSyncBox<E> groupItemSyncBox : groupsAddPlan) {
            count += groupItemSyncBox.getItemsToPush().size();
        }
        return count;
    }

    public static <E> int countTotalUpdated(final Iterable<ItemSyncBox<E>> groupsAddPlan) {
        int count = 0;
        for (ItemSyncBox<E> groupItemSyncBox : groupsAddPlan) {
            count += groupItemSyncBox.getItemsToUpdate().size();
        }
        return count;
    }

    /**
     * @param nodeId              target node
     * @param meterOperationalMap meters present on device
     * @param metersConfigured    meters configured for device
     * @param gatherUpdates       check content of pending item if present on device (and create update task eventually)
     * @return synchronization box
     */
    public static ItemSyncBox<Meter> resolveMeterDiffs(final NodeId nodeId,
                                                       final Map<MeterId, Meter> meterOperationalMap,
                                                       final List<Meter> metersConfigured,
                                                       final boolean gatherUpdates) {
        LOG.trace("resolving meters for {}", nodeId.getValue());
        final ItemSyncBox<Meter> syncBox = new ItemSyncBox<>();
        for (Meter meter : metersConfigured) {
            final Meter existingMeter = meterOperationalMap.get(meter.getMeterId());
            if (existingMeter == null) {
                syncBox.getItemsToPush().add(meter);
            } else {
                // compare content and eventually update
                if (gatherUpdates && !meter.equals(existingMeter)) {
                    syncBox.getItemsToUpdate().add(new ItemSyncBox.ItemUpdateTuple<>(existingMeter, meter));
                }
            }
        }
        return syncBox;
    }

    /**
     * @param flowsConfigured    flows resent on device
     * @param flowOperationalMap flows configured for device
     * @param gatherUpdates      check content of pending item if present on device (and create update task eventually)
     * @return list of safe synchronization steps
     */
    private static ItemSyncBox<Flow> resolveFlowDiffsInTable(final List<Flow> flowsConfigured,
                                                            final Map<FlowDescriptor, Flow> flowOperationalMap,
                                                            final boolean gatherUpdates) {
        final ItemSyncBox<Flow> flowsSyncBox = new ItemSyncBox<>();
        // loop configured flows and check if already present on device
        for (final Flow flow : flowsConfigured) {
            final Flow existingFlow = FlowCapableNodeLookups.flowMapLookupExisting(flow, flowOperationalMap);

            if (existingFlow == null) {
                flowsSyncBox.getItemsToPush().add(flow);
            } else {
                // check instructions and eventually update
                if (gatherUpdates && !Objects.equals(flow.getInstructions(), existingFlow.getInstructions())) {
                    flowsSyncBox.getItemsToUpdate().add(new ItemSyncBox.ItemUpdateTuple<>(existingFlow, flow));
                }
            }
        }
        return flowsSyncBox;
    }

    /**
     * @param nodeId              target node
     * @param tableOperationalMap flow-tables resent on device
     * @param tablesConfigured    flow-tables configured for device
     * @param gatherUpdates       check content of pending item if present on device (and create update task eventually)
     * @return map : key={@link TableKey}, value={@link ItemSyncBox} of safe synchronization steps
     */
    public static Map<TableKey, ItemSyncBox<Flow>> resolveFlowDiffsInAllTables(final NodeId nodeId,
                                                                               final Map<Short, Table> tableOperationalMap,
                                                                               final List<Table> tablesConfigured,
                                                                               final boolean gatherUpdates) {
        LOG.trace("resolving flows in tables for {}", nodeId.getValue());
        final Map<TableKey, ItemSyncBox<Flow>> tableFlowSyncBoxes = new HashMap<>();
        for (final Table tableConfigured : tablesConfigured) {
            final List<Flow> flowsConfigured = tableConfigured.getFlow();
            if (flowsConfigured == null || flowsConfigured.isEmpty()) {
                continue;
            }

            // lookup table (on device)
            final Table tableOperational = tableOperationalMap.get(tableConfigured.getId());
            // wrap existing (on device) flows in current table into map
            final Map<FlowDescriptor, Flow> flowOperationalMap = FlowCapableNodeLookups.wrapFlowsToMap(
                    tableOperational != null
                            ? tableOperational.getFlow()
                            : null);


            final ItemSyncBox<Flow> flowsSyncBox = resolveFlowDiffsInTable(
                    flowsConfigured, flowOperationalMap, gatherUpdates);
            if (!flowsSyncBox.isEmpty()) {
                tableFlowSyncBoxes.put(tableConfigured.getKey(), flowsSyncBox);
            }
        }
        return tableFlowSyncBoxes;
    }

    public static List<Group> safeGroups(FlowCapableNode node) {
        if (node == null) {
            return Collections.emptyList();
        }

        return MoreObjects.firstNonNull(node.getGroup(), ImmutableList.<Group>of());
    }

    public static List<Table> safeTables(FlowCapableNode node) {
        if (node == null) {
            return Collections.emptyList();
        }

        return MoreObjects.firstNonNull(node.getTable(), ImmutableList.<Table>of());
    }

    public static List<Meter> safeMeters(FlowCapableNode node) {
        if (node == null) {
            return Collections.emptyList();
        }

        return MoreObjects.firstNonNull(node.getMeter(), ImmutableList.<Meter>of());
    }
}
