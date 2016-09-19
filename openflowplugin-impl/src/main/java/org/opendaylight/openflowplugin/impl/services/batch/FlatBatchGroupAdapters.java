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
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import javax.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.add.group._case.FlatBatchAddGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.remove.group._case.FlatBatchRemoveGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.update.group._case.FlatBatchUpdateGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailure;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailureBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.batch.failure.batch.item.id.choice.FlatBatchFailureGroupIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.BatchGroupOutputListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.add.groups.batch.input.BatchAddGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.add.groups.batch.input.BatchAddGroupsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.output.list.grouping.BatchFailedGroupsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.remove.groups.batch.input.BatchRemoveGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.remove.groups.batch.input.BatchRemoveGroupsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.update.groups.batch.input.BatchUpdateGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.update.groups.batch.input.BatchUpdateGroupsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Transform between FlatBatch API and group batch API.
 */
public class FlatBatchGroupAdapters {

    private FlatBatchGroupAdapters() {
        throw new IllegalStateException("This class should not be instantiated.");
    }

    /**
     * @param planStep batch step containing changes of the same type
     * @param node     pointer for RPC routing
     * @return input suitable for {@link org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.SalGroupsBatchService#addGroupsBatch(AddGroupsBatchInput)}
     */
    public static AddGroupsBatchInput adaptFlatBatchAddGroup(final BatchPlanStep planStep, final NodeRef node) {
        final List<BatchAddGroups> batchGroups = new ArrayList<>();
        for (FlatBatchAddGroup batchAddGroup : planStep.<FlatBatchAddGroup>getTaskBag()) {
            final BatchAddGroups addGroups = new BatchAddGroupsBuilder(batchAddGroup)
                    .setGroupId(batchAddGroup.getGroupId())
                    .build();
            batchGroups.add(addGroups);
        }

        return new AddGroupsBatchInputBuilder()
                .setBarrierAfter(planStep.isBarrierAfter())
                .setNode(node)
                .setBatchAddGroups(batchGroups)
                .build();
    }

    /**
     * @param planStep batch step containing changes of the same type
     * @param node     pointer for RPC routing
     * @return input suitable for {@link org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.SalGroupsBatchService#removeGroupsBatch(RemoveGroupsBatchInput)}
     */
    public static RemoveGroupsBatchInput adaptFlatBatchRemoveGroup(final BatchPlanStep planStep, final NodeRef node) {
        final List<BatchRemoveGroups> batchGroups = new ArrayList<>();
        for (FlatBatchRemoveGroup batchRemoveGroup : planStep.<FlatBatchRemoveGroup>getTaskBag()) {
            final BatchRemoveGroups removeGroups = new BatchRemoveGroupsBuilder(batchRemoveGroup)
                    .setGroupId(batchRemoveGroup.getGroupId())
                    .build();
            batchGroups.add(removeGroups);
        }

        return new RemoveGroupsBatchInputBuilder()
                .setBarrierAfter(planStep.isBarrierAfter())
                .setNode(node)
                .setBatchRemoveGroups(batchGroups)
                .build();
    }

    /**
     * @param planStep batch step containing changes of the same type
     * @param node     pointer for RPC routing
     * @return input suitable for {@link org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.SalGroupsBatchService#updateGroupsBatch(UpdateGroupsBatchInput)}
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
     * @param stepOffset offset of current batch plan step
     * @return converted {@link ProcessFlatBatchOutput} RPC result
     */
    @VisibleForTesting
    static <T extends BatchGroupOutputListGrouping> Function<RpcResult<T>, RpcResult<ProcessFlatBatchOutput>>
    convertBatchGroupResult(final int stepOffset) {
        return new Function<RpcResult<T>, RpcResult<ProcessFlatBatchOutput>>() {
            @Nullable
            @Override
            public RpcResult<ProcessFlatBatchOutput> apply(@Nullable final RpcResult<T> input) {
                List<BatchFailure> batchFailures = wrapBatchGroupFailuresForFlat(input, stepOffset);
                ProcessFlatBatchOutputBuilder outputBuilder = new ProcessFlatBatchOutputBuilder().setBatchFailure(batchFailures);
                return RpcResultBuilder.<ProcessFlatBatchOutput>status(input.isSuccessful())
                                       .withRpcErrors(input.getErrors())
                                       .withResult(outputBuilder.build())
                                       .build();
            }
        };
    }

    private static <T extends BatchGroupOutputListGrouping> List<BatchFailure> wrapBatchGroupFailuresForFlat(
            final RpcResult<T> input, final int stepOffset) {
        final List<BatchFailure> batchFailures = new ArrayList<>();
        if (input.getResult().getBatchFailedGroupsOutput() != null) {
            for (BatchFailedGroupsOutput stepOutput : input.getResult().getBatchFailedGroupsOutput()) {
                final BatchFailure batchFailure = new BatchFailureBuilder()
                        .setBatchOrder(stepOffset + stepOutput.getBatchOrder())
                        .setBatchItemIdChoice(new FlatBatchFailureGroupIdCaseBuilder()
                                .setGroupId(stepOutput.getGroupId())
                                .build())
                        .build();
                batchFailures.add(batchFailure);
            }
        }
        return batchFailures;
    }

    /**
     * shortcut for {@link #convertBatchGroupResult(int)} with conversion {@link ListenableFuture}
     *
     * @param <T>                     exact type of batch flow output
     * @param resultUpdateGroupFuture batch group rpc-result (add/remove/update)
     * @param currentOffset           offset of current batch plan step with respect to entire chain of steps
     * @return ListenableFuture with converted result {@link ProcessFlatBatchOutput}
     */
    public static <T extends BatchGroupOutputListGrouping> ListenableFuture<RpcResult<ProcessFlatBatchOutput>>
    convertGroupBatchFutureForChain(final Future<RpcResult<T>> resultUpdateGroupFuture,
                                    final int currentOffset) {
        return Futures.transform(JdkFutureAdapters.listenInPoolThread(resultUpdateGroupFuture),
                FlatBatchGroupAdapters.<T>convertBatchGroupResult(currentOffset));
    }
}
