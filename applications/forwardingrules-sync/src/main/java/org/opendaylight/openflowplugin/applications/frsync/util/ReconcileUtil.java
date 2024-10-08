/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frsync.util;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.Collection;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util methods for group reconcil task (future chaining, transforms).
 */
public final class ReconcileUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ReconcileUtil.class);

    private ReconcileUtil() {
        // Hidden on purpose
    }

    /**
     * Creates a single rpc result of type Void honoring all partial rpc results.
     *
     * @param previousItemAction description for case when the triggering future contains failure
     * @param <D>                type of rpc output (gathered in list)
     * @return single rpc result of type Void honoring all partial rpc results
     */
    public static <D> Function<List<RpcResult<D>>, RpcResult<Void>> createRpcResultCondenser(
            final String previousItemAction) {
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
                        .withError(ErrorType.APPLICATION, "previous " + previousItemAction + " failed");
            }
            return resultSink.build();
        };
    }

    /**
     * Creates a single rpc result of type Void honoring all partial rpc results.
     *
     * @param actionDescription description for case when the triggering future contains failure
     * @param <D>               type of rpc output (gathered in list)
     * @return single rpc result of type Void honoring all partial rpc results
     */
    public static <D> Function<RpcResult<D>, RpcResult<Void>> createRpcResultToVoidFunction(
            final String actionDescription) {
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
                        .withError(ErrorType.APPLICATION, "action of " + actionDescription + " failed");
            }
            return resultSink.build();
        };
    }

    /**
     * Flushes a chain barrier.
     *
     * @param nodeIdent flow capable node path - target device for routed rpc
     * @param sendBarrier barrier rpc service
     * @return async barrier result
     */
    public static AsyncFunction<RpcResult<Void>, RpcResult<Void>> chainBarrierFlush(
            final InstanceIdentifier<Node> nodeIdent, final SendBarrier sendBarrier) {
        return input -> Futures.transformAsync(sendBarrier.invoke(new SendBarrierInputBuilder()
            .setNode(new NodeRef(nodeIdent.toIdentifier()))
            .build()),
            result -> result.isSuccessful() ? Futures.immediateFuture(RpcResultBuilder.<Void>success().build())
                : Futures.immediateFailedFuture(null),
            MoreExecutors.directExecutor());
    }

    /**
     * Returns a list of safe synchronization steps with updates.
     *
     * @param nodeId target node
     * @param installedGroupsArg groups resent on device
     * @param pendingGroups      groups configured for device
     * @return list of safe synchronization steps with updates
     */
    public static List<ItemSyncBox<Group>> resolveAndDivideGroupDiffs(final NodeId nodeId,
                                                                      final Map<Uint32, Group> installedGroupsArg,
                                                                      final Collection<Group> pendingGroups) {
        return resolveAndDivideGroupDiffs(nodeId, installedGroupsArg, pendingGroups, true);
    }

    /**
     * Returns a list of safe synchronization steps.
     *
     * @param nodeId             target node
     * @param installedGroupsArg groups resent on device
     * @param pendingGroups      groups configured for device
     * @param gatherUpdates      check content of pending item if present on device (and create update task eventually)
     * @return list of safe synchronization steps
     */
    public static List<ItemSyncBox<Group>> resolveAndDivideGroupDiffs(final NodeId nodeId,
                                                                      final Map<Uint32, Group> installedGroupsArg,
                                                                      final Collection<Group> pendingGroups,
                                                                      final boolean gatherUpdates) {
        final Map<Uint32, Group> installedGroups = new HashMap<>(installedGroupsArg);
        final List<ItemSyncBox<Group>> plan = new ArrayList<>();

        while (!Iterables.isEmpty(pendingGroups)) {
            final ItemSyncBox<Group> stepPlan = new ItemSyncBox<>();
            final Iterator<Group> iterator = pendingGroups.iterator();
            final Map<Uint32, Group> installIncrement = new HashMap<>();

            while (iterator.hasNext()) {
                final Group group = iterator.next();

                final Group existingGroup = installedGroups.get(group.getGroupId().getValue());
                if (existingGroup != null) {
                    if (!gatherUpdates || group.equals(existingGroup)) {
                        iterator.remove();
                    } else if (checkGroupPrecondition(installedGroups.keySet(), group)) {
                        // check buckets and eventually update
                        iterator.remove();
                        LOG.trace("Group {} on device {} differs - planned for update", group.getGroupId(), nodeId);
                        stepPlan.getItemsToUpdate().add(new ItemSyncBox.ItemUpdateTuple<>(existingGroup, group));
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
                LOG.warn("Failed to resolve and divide groups into preconditions-match based ordered plan: {}, "
                        + "resolving stuck at level {}", nodeId.getValue(), plan.size());
                throw new IllegalStateException("Failed to resolve and divide groups when matching preconditions");
            }
        }

        return plan;
    }

    public static boolean checkGroupPrecondition(final Set<Uint32> installedGroupIds, final Group pendingGroup) {
        boolean okToInstall = true;
        // check each bucket in the pending group
        for (Bucket bucket : pendingGroup.getBuckets().nonnullBucket().values()) {
            for (Action action : bucket.nonnullAction().values()) {
                // if the output action is a group
                if (GroupActionCase.class.equals(action.getAction().implementedInterface())) {
                    Uint32 groupId = ((GroupActionCase) action.getAction()).getGroupAction().getGroupId();
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
     * Resolves meter differences.
     *
     * @param nodeId              target node
     * @param meterOperationalMap meters present on device
     * @param metersConfigured    meters configured for device
     * @param gatherUpdates       check content of pending item if present on device (and create update task eventually)
     * @return synchronization box
     */
    public static ItemSyncBox<Meter> resolveMeterDiffs(final NodeId nodeId,
                                                       final Map<MeterId, Meter> meterOperationalMap,
                                                       final Collection<Meter> metersConfigured,
                                                       final boolean gatherUpdates) {
        LOG.trace("resolving meters for {}", nodeId.getValue());
        final ItemSyncBox<Meter> syncBox = new ItemSyncBox<>();
        for (Meter meter : metersConfigured) {
            final Meter existingMeter = meterOperationalMap.get(meter.getMeterId());
            if (existingMeter == null) {
                syncBox.getItemsToPush().add(meter);
            } else if (gatherUpdates && !meter.equals(existingMeter)) {
                // compare content and eventually update
                syncBox.getItemsToUpdate().add(new ItemSyncBox.ItemUpdateTuple<>(existingMeter, meter));
            }
        }
        return syncBox;
    }

    /**
     * Resolves flow differences in a table.
     *
     * @param flowsConfigured    flows resent on device
     * @param flowOperationalMap flows configured for device
     * @param gatherUpdates      check content of pending item if present on device (and create update task eventually)
     * @return list of safe synchronization steps
     */
    private static ItemSyncBox<Flow> resolveFlowDiffsInTable(final Collection<Flow> flowsConfigured,
                                                            final Map<FlowDescriptor, Flow> flowOperationalMap,
                                                            final boolean gatherUpdates) {
        final ItemSyncBox<Flow> flowsSyncBox = new ItemSyncBox<>();
        // loop configured flows and check if already present on device
        for (final Flow flow : flowsConfigured) {
            final Flow existingFlow = FlowCapableNodeLookups.flowMapLookupExisting(flow, flowOperationalMap);

            if (existingFlow == null) {
                flowsSyncBox.getItemsToPush().add(flow);
            } else if (gatherUpdates && !Objects.equals(flow.getInstructions(), existingFlow.getInstructions())) {
                // check instructions and eventually update
                flowsSyncBox.getItemsToUpdate().add(new ItemSyncBox.ItemUpdateTuple<>(existingFlow, flow));
            }
        }
        return flowsSyncBox;
    }

    /**
     * Resolves flow differences in all tables.
     *
     * @param nodeId              target node
     * @param tableOperationalMap flow-tables resent on device
     * @param tablesConfigured    flow-tables configured for device
     * @param gatherUpdates       check content of pending item if present on device (and create update task eventually)
     * @return map : key={@link TableKey}, value={@link ItemSyncBox} of safe synchronization steps
     */
    public static Map<TableKey, ItemSyncBox<Flow>> resolveFlowDiffsInAllTables(final NodeId nodeId,
            final Map<Uint8, Table> tableOperationalMap, final Collection<Table> tablesConfigured,
            final boolean gatherUpdates) {
        LOG.trace("resolving flows in tables for {}", nodeId.getValue());
        final Map<TableKey, ItemSyncBox<Flow>> tableFlowSyncBoxes = new HashMap<>();
        for (final Table tableConfigured : tablesConfigured) {
            final Collection<Flow> flowsConfigured = tableConfigured.nonnullFlow().values();
            if (flowsConfigured.isEmpty()) {
                continue;
            }

            // lookup table (on device)
            final Table tableOperational = tableOperationalMap.get(tableConfigured.getId());
            // wrap existing (on device) flows in current table into map
            final Map<FlowDescriptor, Flow> flowOperationalMap = FlowCapableNodeLookups.wrapFlowsToMap(
                    tableOperational != null
                            ? tableOperational.nonnullFlow().values()
                            : null);


            final ItemSyncBox<Flow> flowsSyncBox = resolveFlowDiffsInTable(
                    flowsConfigured, flowOperationalMap, gatherUpdates);
            if (!flowsSyncBox.isEmpty()) {
                tableFlowSyncBoxes.put(tableConfigured.key(), flowsSyncBox);
            }
        }
        return tableFlowSyncBoxes;
    }

    public static Collection<Group> safeGroups(final FlowCapableNode node) {
        return node == null ? List.of() : node.nonnullGroup().values();
    }

    public static Collection<Table> safeTables(final FlowCapableNode node) {
        return node == null ? List.of() : node.nonnullTable().values();
    }

    public static Collection<Meter> safeMeters(final FlowCapableNode node) {
        return node == null ? List.of() : node.nonnullMeter().values();
    }
}
