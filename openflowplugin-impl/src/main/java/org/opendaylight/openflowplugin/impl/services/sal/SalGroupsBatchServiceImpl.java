/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.sal;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.impl.util.BarrierUtil;
import org.opendaylight.openflowplugin.impl.util.GroupUtil;
import org.opendaylight.openflowplugin.impl.util.PathUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.OriginalGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.UpdatedGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.SalGroupsBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.add.groups.batch.input.BatchAddGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.output.list.grouping.BatchFailedGroupsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.remove.groups.batch.input.BatchRemoveGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.update.groups.batch.input.BatchUpdateGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link SalGroupsBatchService} - delegates work to {@link SalGroupService}.
 */
public class SalGroupsBatchServiceImpl implements SalGroupsBatchService {

    private static final Logger LOG = LoggerFactory.getLogger(SalGroupsBatchServiceImpl.class);

    private final SalGroupService salGroupService;
    private final FlowCapableTransactionService transactionService;

    public SalGroupsBatchServiceImpl(final SalGroupService salGroupService, final FlowCapableTransactionService transactionService) {
        this.salGroupService = Preconditions.checkNotNull(salGroupService);
        this.transactionService = Preconditions.checkNotNull(transactionService);
    }

    @Override
    public Future<RpcResult<UpdateGroupsBatchOutput>> updateGroupsBatch(final UpdateGroupsBatchInput input) {
        final List<BatchUpdateGroups> batchUpdateGroups = input.getBatchUpdateGroups();
        LOG.trace("Updating groups @ {} : {}", PathUtil.extractNodeId(input.getNode()), batchUpdateGroups.size());

        final ArrayList<ListenableFuture<RpcResult<UpdateGroupOutput>>> resultsLot = new ArrayList<>();
        for (BatchUpdateGroups batchGroup : batchUpdateGroups) {
            final UpdateGroupInput updateGroupInput = new UpdateGroupInputBuilder(input)
                    .setOriginalGroup(new OriginalGroupBuilder(batchGroup.getOriginalBatchedGroup()).build())
                    .setUpdatedGroup(new UpdatedGroupBuilder(batchGroup.getUpdatedBatchedGroup()).build())
                    .setGroupRef(createGroupRef(input.getNode(), batchGroup))
                    .setNode(input.getNode())
                    .build();
            resultsLot.add(JdkFutureAdapters.listenInPoolThread(salGroupService.updateGroup(updateGroupInput)));
        }

        final Iterable<Group> groups = Iterables.transform(batchUpdateGroups, new Function<BatchUpdateGroups, Group>() {
                    @Nullable
                    @Override
                    public Group apply(@Nullable final BatchUpdateGroups input) {
                        return input.getUpdatedBatchedGroup();
                    }
                }
        );

        final ListenableFuture<RpcResult<List<BatchFailedGroupsOutput>>> commonResult =
                Futures.transform(Futures.allAsList(resultsLot), GroupUtil.<UpdateGroupOutput>createCumulatingFunction(
                        groups, batchUpdateGroups.size()));

        ListenableFuture<RpcResult<UpdateGroupsBatchOutput>> updateGroupsBulkFuture = Futures.transform(
                commonResult, GroupUtil.GROUP_UPDATE_TRANSFORM);

        if (input.isBarrierAfter()) {
            updateGroupsBulkFuture = BarrierUtil.chainBarrier(updateGroupsBulkFuture, input.getNode(),
                    transactionService, GroupUtil.GROUP_UPDATE_COMPOSING_TRANSFORM);
        }

        return updateGroupsBulkFuture;
    }

    @Override
    public Future<RpcResult<AddGroupsBatchOutput>> addGroupsBatch(final AddGroupsBatchInput input) {
        LOG.trace("Adding groups @ {} : {}", PathUtil.extractNodeId(input.getNode()), input.getBatchAddGroups().size());
        final ArrayList<ListenableFuture<RpcResult<AddGroupOutput>>> resultsLot = new ArrayList<>();
        for (BatchAddGroups addGroup : input.getBatchAddGroups()) {
            final AddGroupInput addGroupInput = new AddGroupInputBuilder(addGroup)
                    .setGroupRef(createGroupRef(input.getNode(), addGroup))
                    .setNode(input.getNode())
                    .build();
            resultsLot.add(JdkFutureAdapters.listenInPoolThread(salGroupService.addGroup(addGroupInput)));
        }

        final ListenableFuture<RpcResult<List<BatchFailedGroupsOutput>>> commonResult =
                Futures.transform(Futures.allAsList(resultsLot),
                        GroupUtil.<AddGroupOutput>createCumulatingFunction(input.getBatchAddGroups()));

        ListenableFuture<RpcResult<AddGroupsBatchOutput>> addGroupsBulkFuture =
                Futures.transform(commonResult, GroupUtil.GROUP_ADD_TRANSFORM);

        if (input.isBarrierAfter()) {
            addGroupsBulkFuture = BarrierUtil.chainBarrier(addGroupsBulkFuture, input.getNode(),
                    transactionService, GroupUtil.GROUP_ADD_COMPOSING_TRANSFORM);
        }

        return addGroupsBulkFuture;
    }

    @Override
    public Future<RpcResult<RemoveGroupsBatchOutput>> removeGroupsBatch(final RemoveGroupsBatchInput input) {
        LOG.trace("Removing groups @ {} : {}", PathUtil.extractNodeId(input.getNode()), input.getBatchRemoveGroups().size());
        final ArrayList<ListenableFuture<RpcResult<RemoveGroupOutput>>> resultsLot = new ArrayList<>();
        for (BatchRemoveGroups addGroup : input.getBatchRemoveGroups()) {
            final RemoveGroupInput removeGroupInput = new RemoveGroupInputBuilder(addGroup)
                    .setGroupRef(createGroupRef(input.getNode(), addGroup))
                    .setNode(input.getNode())
                    .build();
            resultsLot.add(JdkFutureAdapters.listenInPoolThread(salGroupService.removeGroup(removeGroupInput)));
        }

        final ListenableFuture<RpcResult<List<BatchFailedGroupsOutput>>> commonResult =
                Futures.transform(Futures.allAsList(resultsLot),
                        GroupUtil.<RemoveGroupOutput>createCumulatingFunction(input.getBatchRemoveGroups()));

        ListenableFuture<RpcResult<RemoveGroupsBatchOutput>> removeGroupsBulkFuture =
                Futures.transform(commonResult, GroupUtil.GROUP_REMOVE_TRANSFORM);

        if (input.isBarrierAfter()) {
            removeGroupsBulkFuture = BarrierUtil.chainBarrier(removeGroupsBulkFuture, input.getNode(),
                    transactionService, GroupUtil.GROUP_REMOVE_COMPOSING_TRANSFORM);
        }

        return removeGroupsBulkFuture;
    }

    private static GroupRef createGroupRef(final NodeRef nodeRef, final Group batchGroup) {
        return GroupUtil.buildGroupPath((InstanceIdentifier<Node>) nodeRef.getValue(), batchGroup.getGroupId());
    }

    private static GroupRef createGroupRef(final NodeRef nodeRef, final BatchUpdateGroups batchGroup) {
        return GroupUtil.buildGroupPath((InstanceIdentifier<Node>) nodeRef.getValue(),
                batchGroup.getUpdatedBatchedGroup().getGroupId());
    }
}
