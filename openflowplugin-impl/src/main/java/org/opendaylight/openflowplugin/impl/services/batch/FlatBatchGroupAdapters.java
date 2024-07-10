/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.batch;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.failure.ids.aug.FlatBatchFailureGroupIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.group.crud._case.aug.flat.batch.add.group._case.FlatBatchAddGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.group.crud._case.aug.flat.batch.remove.group._case.FlatBatchRemoveGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.group.crud._case.aug.flat.batch.update.group._case.FlatBatchUpdateGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailure;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailureBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailureKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.BatchGroupOutputListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.add.groups.batch.input.BatchAddGroupsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.remove.groups.batch.input.BatchRemoveGroupsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.update.groups.batch.input.BatchUpdateGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.update.groups.batch.input.BatchUpdateGroupsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;

/**
 * Transform between FlatBatch API and group batch API.
 */
public final class FlatBatchGroupAdapters {

    private FlatBatchGroupAdapters() {
    }

    /**
     * Adapt flat batch add group.
     * @param planStep batch step containing changes of the same type
     * @param node     pointer for RPC routing
     * @return input suitable for {@link org.opendaylight.yang.gen.v1.urn
     * .opendaylight.groups.service.rev160315.SalGroupsBatchService#addGroupsBatch(AddGroupsBatchInput)}
     */
    public static AddGroupsBatchInput adaptFlatBatchAddGroup(final BatchPlanStep planStep, final NodeRef node) {
        final var batchGroups = planStep.<FlatBatchAddGroup>getTaskBag().stream()
            .map(batchAddGroup -> new BatchAddGroupsBuilder(batchAddGroup)
                    .setGroupId(batchAddGroup.getGroupId())
                    .build())
            .collect(BindingMap.toOrderedMap());

        return new AddGroupsBatchInputBuilder()
                .setBarrierAfter(planStep.isBarrierAfter())
                .setNode(node)
                .setBatchAddGroups(batchGroups)
                .build();
    }

    /**
     * Adapt flat batch remove group.
     * @param planStep batch step containing changes of the same type
     * @param node     pointer for RPC routing
     * @return input suitable for {@link org.opendaylight.yang.gen.v1.urn
     * .opendaylight.groups.service.rev160315.SalGroupsBatchService#removeGroupsBatch(RemoveGroupsBatchInput)}
     */
    public static RemoveGroupsBatchInput adaptFlatBatchRemoveGroup(final BatchPlanStep planStep, final NodeRef node) {
        final var batchGroups = planStep.<FlatBatchRemoveGroup>getTaskBag().stream()
            .map(batchRemoveGroup -> new BatchRemoveGroupsBuilder(batchRemoveGroup)
                .setGroupId(batchRemoveGroup.getGroupId())
                .build())
            .collect(BindingMap.toOrderedMap());

        return new RemoveGroupsBatchInputBuilder()
                .setBarrierAfter(planStep.isBarrierAfter())
                .setNode(node)
                .setBatchRemoveGroups(batchGroups)
                .build();
    }

    /**
     * Adapt flat batch update group.
     * @param planStep batch step containing changes of the same type
     * @param node     pointer for RPC routing
     * @return input suitable for {@link org.opendaylight.yang.gen.v1.urn
     * .opendaylight.groups.service.rev160315.SalGroupsBatchService#updateGroupsBatch(UpdateGroupsBatchInput)}
     */
    public static UpdateGroupsBatchInput adaptFlatBatchUpdateGroup(final BatchPlanStep planStep, final NodeRef node) {
        final List<BatchUpdateGroups> batchGroups = new ArrayList<>();
        for (FlatBatchUpdateGroup batchUpdateGroup : planStep.<FlatBatchUpdateGroup>getTaskBag()) {
            final BatchUpdateGroups updateGroups = new BatchUpdateGroupsBuilder(batchUpdateGroup)
                    .build();
            batchGroups.add(updateGroups);
        }

        return new UpdateGroupsBatchInputBuilder()
                .setBarrierAfter(planStep.isBarrierAfter())
                .setNode(node)
                .setBatchUpdateGroups(batchGroups)
                .build();
    }

    /**
     * Convert batch group result.
     * @param stepOffset offset of current batch plan step
     * @return converted {@link ProcessFlatBatchOutput} RPC result
     */
    @VisibleForTesting
    static <T extends BatchGroupOutputListGrouping> Function<RpcResult<T>, RpcResult<ProcessFlatBatchOutput>>
        convertBatchGroupResult(final int stepOffset) {
        return input -> RpcResultBuilder.<ProcessFlatBatchOutput>status(input.isSuccessful())
            .withRpcErrors(input.getErrors())
            .withResult(new ProcessFlatBatchOutputBuilder()
                .setBatchFailure(wrapBatchGroupFailuresForFlat(input, stepOffset))
                .build())
            .build();
    }

    private static <T extends BatchGroupOutputListGrouping>
            Map<BatchFailureKey, BatchFailure> wrapBatchGroupFailuresForFlat(final RpcResult<T> input,
                final int stepOffset) {
        return input.getResult().nonnullBatchFailedGroupsOutput().values().stream()
            .map(stepOutput -> new BatchFailureBuilder()
                .setBatchOrder(Uint16.valueOf(stepOffset + stepOutput.getBatchOrder().toJava()))
                .setBatchItemIdChoice(new FlatBatchFailureGroupIdCaseBuilder()
                    .setGroupId(stepOutput.getGroupId())
                    .build())
                .build())
            .collect(BindingMap.toOrderedMap());
    }

    /**
     * Shortcut for {@link #convertBatchGroupResult(int)} with conversion {@link ListenableFuture}.
     *
     * @param <T>                     exact type of batch flow output
     * @param resultUpdateGroupFuture batch group rpc-result (add/remove/update)
     * @param currentOffset           offset of current batch plan step with respect to entire chain of steps
     * @return ListenableFuture with converted result {@link ProcessFlatBatchOutput}
     */
    public static <T extends BatchGroupOutputListGrouping> ListenableFuture<RpcResult<ProcessFlatBatchOutput>>
        convertGroupBatchFutureForChain(final ListenableFuture<RpcResult<T>> resultUpdateGroupFuture,
                                    final int currentOffset) {
        return Futures.transform(resultUpdateGroupFuture,
                FlatBatchGroupAdapters.convertBatchGroupResult(currentOffset), MoreExecutors.directExecutor());
    }
}
