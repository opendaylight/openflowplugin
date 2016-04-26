/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * utility methods for group reconcil task (future chaining, transforms)
 */
public class ReconcileUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ReconcileUtil.class);

    /**
     * @param previousItemAction description for case when the triggering future contains failure
     * @param <D>                type of rpc output (gathered in list)
     * @return single rpc result of type Void honoring all partial rpc results
     */
    public static <D> Function<List<RpcResult<D>>, RpcResult<Void>> createRpcResultCondenser(final String previousItemAction) {
        return new Function<List<RpcResult<D>>, RpcResult<Void>>() {
            @Nullable
            @Override
            public RpcResult<Void> apply(@Nullable final List<RpcResult<D>> input) {
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
            }
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
        return new AsyncFunction<RpcResult<Void>, RpcResult<Void>>() {
            @Override
            public ListenableFuture<RpcResult<Void>> apply(final RpcResult<Void> input) throws Exception {
                final SendBarrierInput barrierInput = new SendBarrierInputBuilder()
                        .setNode(new NodeRef(nodeIdent))
                        .build();
                return JdkFutureAdapters.listenInPoolThread(flowCapableTransactionService.sendBarrier(barrierInput));
            }
        };
    }

    public static List<ItemSyncBox<Group>> resolveAndDivideGroups(final NodeId nodeId,
                                                                  final Map<Long, Group> installedGroupsArg,
                                                                  final Collection<Group> pendingGroups) {
        return resolveAndDivideGroups(nodeId, installedGroupsArg, pendingGroups, true);
    }

    public static List<ItemSyncBox<Group>> resolveAndDivideGroups(final NodeId nodeId,
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
                    stepPlan.getItemsToAdd().add(group);
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

    public static <E> int countTotalAdds(final List<ItemSyncBox<E>> groupsAddPlan) {
        int count = 0;
        for (ItemSyncBox<E> groupItemSyncBox : groupsAddPlan) {
            count += groupItemSyncBox.getItemsToAdd().size();
        }
        return count;
    }

    public static <E> int countTotalUpdated(final List<ItemSyncBox<E>> groupsAddPlan) {
        int count = 0;
        for (ItemSyncBox<E> groupItemSyncBox : groupsAddPlan) {
            count += groupItemSyncBox.getItemsToUpdate().size();
        }
        return count;
    }
}
