/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.BatchGroupOutputListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.output.list.grouping.BatchFailedGroupsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.output.list.grouping.BatchFailedGroupsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Provides group util methods.
 */
public final class GroupUtil {
    private static final RpcResultBuilder<List<BatchFailedGroupsOutput>> SUCCESSFUL_GROUP_OUTPUT_RPC_RESULT =
            RpcResultBuilder.success(Collections.emptyList());

    public static final Function<RpcResult<List<BatchFailedGroupsOutput>>, RpcResult<AddGroupsBatchOutput>>
        GROUP_ADD_TRANSFORM = batchGroupsCumulatedResult -> {
            final AddGroupsBatchOutput batchOutput = new AddGroupsBatchOutputBuilder()
                    .setBatchFailedGroupsOutput(batchGroupsCumulatedResult.getResult()).build();

            final RpcResultBuilder<AddGroupsBatchOutput> resultBld =
                    createCumulativeRpcResult(batchGroupsCumulatedResult, batchOutput);
            return resultBld.build();
        };
    public static final Function<Pair<RpcResult<AddGroupsBatchOutput>,
                                 RpcResult<SendBarrierOutput>>,
                                 RpcResult<AddGroupsBatchOutput>>
        GROUP_ADD_COMPOSING_TRANSFORM = createComposingFunction();

    public static final Function<RpcResult<List<BatchFailedGroupsOutput>>, RpcResult<RemoveGroupsBatchOutput>>
        GROUP_REMOVE_TRANSFORM =
            batchGroupsCumulatedResult -> {
                final RemoveGroupsBatchOutput batchOutput = new RemoveGroupsBatchOutputBuilder()
                        .setBatchFailedGroupsOutput(batchGroupsCumulatedResult.getResult()).build();

                final RpcResultBuilder<RemoveGroupsBatchOutput> resultBld =
                        createCumulativeRpcResult(batchGroupsCumulatedResult, batchOutput);
                return resultBld.build();
            };
    public static final Function<Pair<RpcResult<RemoveGroupsBatchOutput>,
                                      RpcResult<SendBarrierOutput>>,
                                      RpcResult<RemoveGroupsBatchOutput>>
        GROUP_REMOVE_COMPOSING_TRANSFORM = createComposingFunction();

    public static final Function<RpcResult<List<BatchFailedGroupsOutput>>, RpcResult<UpdateGroupsBatchOutput>>
        GROUP_UPDATE_TRANSFORM =
            batchGroupsCumulatedResult -> {
                final UpdateGroupsBatchOutput batchOutput = new UpdateGroupsBatchOutputBuilder()
                        .setBatchFailedGroupsOutput(batchGroupsCumulatedResult.getResult()).build();

                final RpcResultBuilder<UpdateGroupsBatchOutput> resultBld =
                        createCumulativeRpcResult(batchGroupsCumulatedResult, batchOutput);
                return resultBld.build();
            };
    public static final Function<Pair<RpcResult<UpdateGroupsBatchOutput>,
                                      RpcResult<SendBarrierOutput>>,
                                      RpcResult<UpdateGroupsBatchOutput>>
            GROUP_UPDATE_COMPOSING_TRANSFORM = createComposingFunction();

    private GroupUtil() {
        // Hidden on purpose
    }

    /**
     * Method build the group path.
     * @param nodePath - node path
     * @param groupId - group Id
     * @return instance identifier assembled for given node and group
     */
    public static GroupRef buildGroupPath(final InstanceIdentifier<Node> nodePath, final GroupId groupId) {
        final KeyedInstanceIdentifier<Group, GroupKey> groupPath = nodePath
                .augmentation(FlowCapableNode.class)
                .child(Group.class, new GroupKey(groupId));

        return new GroupRef(groupPath);
    }

    public static <O> Function<List<RpcResult<O>>, RpcResult<List<BatchFailedGroupsOutput>>> createCumulatingFunction(
            final Iterable<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group>
                    inputBatchGroups) {
        return createCumulatingFunction(inputBatchGroups, Iterables.size(inputBatchGroups));
    }

    public static <O> Function<List<RpcResult<O>>, RpcResult<List<BatchFailedGroupsOutput>>> createCumulatingFunction(
            final Iterable<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group>
                    inputBatchGroups, final int sizeOfInputBatch) {
        return new CumulatingFunction<O>(inputBatchGroups, sizeOfInputBatch).invoke();
    }

    /*
     * Method returns the bitmap of actions supported by each group.
     *
     * @param actionsSupported - list of supported actions
     * @return
     */
    public static List<Uint32> extractGroupActionsSupportBitmap(final List<ActionType> actionsSupported) {
        List<Uint32> supportActionByGroups = new ArrayList<>();
        for (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionType supportedActions
                : actionsSupported) {
            long supportActionBitmap = 0;
            supportActionBitmap |= supportedActions.getOFPATOUTPUT() ? 1 : 0;
            supportActionBitmap |= supportedActions.getOFPATCOPYTTLOUT() ? 1 << 11 : 0;
            supportActionBitmap |= supportedActions.getOFPATCOPYTTLIN() ? 1 << 12 : 0;
            supportActionBitmap |= supportedActions.getOFPATSETMPLSTTL() ? 1 << 15 : 0;
            supportActionBitmap |= supportedActions.getOFPATDECMPLSTTL() ? 1 << 16 : 0;
            supportActionBitmap |= supportedActions.getOFPATPUSHVLAN() ? 1 << 17 : 0;
            supportActionBitmap |= supportedActions.getOFPATPOPVLAN() ? 1 << 18 : 0;
            supportActionBitmap |= supportedActions.getOFPATPUSHMPLS() ? 1 << 19 : 0;
            supportActionBitmap |= supportedActions.getOFPATPOPMPLS() ? 1 << 20 : 0;
            supportActionBitmap |= supportedActions.getOFPATSETQUEUE() ? 1 << 21 : 0;
            supportActionBitmap |= supportedActions.getOFPATGROUP() ? 1 << 22 : 0;
            supportActionBitmap |= supportedActions.getOFPATSETNWTTL() ? 1 << 23 : 0;
            supportActionBitmap |= supportedActions.getOFPATDECNWTTL() ? 1 << 24 : 0;
            supportActionBitmap |= supportedActions.getOFPATSETFIELD() ? 1 << 25 : 0;
            supportActionBitmap |= supportedActions.getOFPATPUSHPBB() ? 1 << 26 : 0;
            supportActionBitmap |= supportedActions.getOFPATPOPPBB() ? 1 << 27 : 0;
            supportActionByGroups.add(Uint32.valueOf(supportActionBitmap));
        }
        return supportActionByGroups;
    }

    /**
     * Factory method: create {@link Function} which attaches barrier response to given {@link RpcResult}&lt;T&gt;
     * and changes success flag if needed.
     * <br>
     * Original rpcResult is the {@link Pair#getLeft()} and barrier result is the {@link Pair#getRight()}.
     *
     * @param <T> type of rpcResult value
     * @return reusable static function
     */
    @VisibleForTesting
    static <T extends BatchGroupOutputListGrouping> Function<Pair<RpcResult<T>, RpcResult<SendBarrierOutput>>,
            RpcResult<T>>
        createComposingFunction() {
        return input -> {
            final RpcResultBuilder<T> resultBld;
            if (input.getLeft().isSuccessful() && input.getRight().isSuccessful()) {
                resultBld = RpcResultBuilder.success();
            } else {
                resultBld = RpcResultBuilder.failed();
            }

            final ArrayList<RpcError> rpcErrors = new ArrayList<>(input.getLeft().getErrors());
            rpcErrors.addAll(input.getRight().getErrors());
            resultBld.withRpcErrors(rpcErrors);

            resultBld.withResult(input.getLeft().getResult());

            return resultBld.build();
        };
    }

    /**
     * Wrap given list of problematic group-ids into {@link RpcResult} of given type.
     *
     * @param batchGroupsCumulativeResult list of ids failed groups
     * @param batchOutput group operation type
     * @return batch group operation output of given type containing list of group-ids and corresponding success flag
     */
    private static <T extends BatchGroupOutputListGrouping> RpcResultBuilder<T> createCumulativeRpcResult(
        final RpcResult<List<BatchFailedGroupsOutput>> batchGroupsCumulativeResult, final T batchOutput) {
        final RpcResultBuilder<T> resultBld;
        if (batchGroupsCumulativeResult.isSuccessful()) {
            resultBld = RpcResultBuilder.success(batchOutput);
        } else {
            resultBld = RpcResultBuilder.failed();
            resultBld.withResult(batchOutput)
                    .withRpcErrors(batchGroupsCumulativeResult.getErrors());
        }
        return resultBld;
    }

    private static class CumulatingFunction<O> {
        private final Iterable<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group>
                inputBatchGroups;
        private final int sizeOfInputBatch;

        CumulatingFunction(
                final Iterable<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group>
                        inputBatchGroups, final int sizeOfInputBatch) {
            this.inputBatchGroups = inputBatchGroups;
            this.sizeOfInputBatch = sizeOfInputBatch;
        }

        public Function<List<RpcResult<O>>, RpcResult<List<BatchFailedGroupsOutput>>> invoke() {
            return innerInput -> {
                final int sizeOfFutures = innerInput.size();
                Preconditions.checkArgument(sizeOfFutures == sizeOfInputBatch,
                        "wrong amount of returned futures: {} <> {}", sizeOfFutures, sizeOfInputBatch);

                final List<BatchFailedGroupsOutput> batchGroups = new ArrayList<>();
                final Iterator<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group>
                        batchGroupIterator = inputBatchGroups.iterator();

                Collection<RpcError> groupErrors = new ArrayList<>(sizeOfFutures);

                int batchOrder = 0;
                for (RpcResult<O> groupModOutput : innerInput) {
                    final GroupId groupId = batchGroupIterator.next().getGroupId();

                    if (!groupModOutput.isSuccessful()) {
                        batchGroups.add(new BatchFailedGroupsOutputBuilder()
                                .setGroupId(groupId)
                                .setBatchOrder(batchOrder)
                                .build());
                        groupErrors.addAll(groupModOutput.getErrors());
                    }
                    batchOrder++;
                }

                final RpcResultBuilder<List<BatchFailedGroupsOutput>> resultBuilder;
                if (!groupErrors.isEmpty()) {
                    resultBuilder = RpcResultBuilder.<List<BatchFailedGroupsOutput>>failed()
                            .withRpcErrors(groupErrors).withResult(batchGroups);
                } else {
                    resultBuilder = SUCCESSFUL_GROUP_OUTPUT_RPC_RESULT;
                }
                return resultBuilder.build();
            };
        }
    }
}
