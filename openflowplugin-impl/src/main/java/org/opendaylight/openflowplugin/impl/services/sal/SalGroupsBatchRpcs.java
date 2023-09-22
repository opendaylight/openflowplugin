/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.opendaylight.openflowplugin.impl.util.BarrierUtil;
import org.opendaylight.openflowplugin.impl.util.GroupUtil;
import org.opendaylight.openflowplugin.impl.util.PathUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.OriginalGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.UpdatedGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.BatchGroupInputUpdateGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.add.groups.batch.input.BatchAddGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.output.list.grouping.BatchFailedGroupsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.remove.groups.batch.input.BatchRemoveGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.update.groups.batch.input.BatchUpdateGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation delegates work to {@link SalGroupRpcs}.
 */
public class SalGroupsBatchRpcs {
    private static final Logger LOG = LoggerFactory.getLogger(SalGroupsBatchRpcs.class);

    private final SalGroupRpcs salGroupRpcs;
    private final FlowCapableTransactionRpc transactionRpc;

    public SalGroupsBatchRpcs(final SalGroupRpcs salGroupRpcs, final FlowCapableTransactionRpc transactionRpc) {
        this.salGroupRpcs = requireNonNull(salGroupRpcs);
        this.transactionRpc = requireNonNull(transactionRpc);
    }

    private ListenableFuture<RpcResult<UpdateGroupsBatchOutput>> updateGroupsBatch(final UpdateGroupsBatchInput input) {
        final List<BatchUpdateGroups> batchUpdateGroups = input.getBatchUpdateGroups();
        LOG.trace("Updating groups @ {} : {}", PathUtil.extractNodeId(input.getNode()), batchUpdateGroups.size());

        final ArrayList<ListenableFuture<RpcResult<UpdateGroupOutput>>> resultsLot = new ArrayList<>();
        for (BatchUpdateGroups batchGroup : batchUpdateGroups) {
            final UpdateGroupInput updateGroupInput = new UpdateGroupInputBuilder(input)
                    .setOriginalGroup(new OriginalGroupBuilder(batchGroup.getOriginalBatchedGroup()).build())
                    .setUpdatedGroup(new UpdatedGroupBuilder(batchGroup.getUpdatedBatchedGroup()).build())
                    .setGroupRef(createGroupRef(input.getNode(), batchGroup)).setNode(input.getNode()).build();
            resultsLot.add(salGroupRpcs.getRpcClassToInstanceMap().getInstance(UpdateGroup.class)
                .invoke(updateGroupInput));
        }

        final Iterable<Group> groups = batchUpdateGroups.stream()
                .map(BatchGroupInputUpdateGrouping::getUpdatedBatchedGroup).collect(Collectors.toList());

        final ListenableFuture<RpcResult<List<BatchFailedGroupsOutput>>> commonResult = Futures
                .transform(Futures.allAsList(resultsLot),
                           GroupUtil.createCumulatingFunction(groups, batchUpdateGroups.size()),
                           MoreExecutors.directExecutor());

        ListenableFuture<RpcResult<UpdateGroupsBatchOutput>> updateGroupsBulkFuture = Futures
                .transform(commonResult, GroupUtil.GROUP_UPDATE_TRANSFORM, MoreExecutors.directExecutor());

        if (input.getBarrierAfter()) {
            updateGroupsBulkFuture = BarrierUtil
                    .chainBarrier(updateGroupsBulkFuture, input.getNode(), transactionRpc,
                                  GroupUtil.GROUP_UPDATE_COMPOSING_TRANSFORM);
        }

        return updateGroupsBulkFuture;
    }

    private ListenableFuture<RpcResult<AddGroupsBatchOutput>> addGroupsBatch(final AddGroupsBatchInput input) {
        LOG.trace("Adding groups @ {} : {}", PathUtil.extractNodeId(input.getNode()), input.getBatchAddGroups().size());
        final ArrayList<ListenableFuture<RpcResult<AddGroupOutput>>> resultsLot = new ArrayList<>();
        for (BatchAddGroups addGroup : input.nonnullBatchAddGroups().values()) {
            final AddGroupInput addGroupInput = new AddGroupInputBuilder(addGroup)
                    .setGroupRef(createGroupRef(input.getNode(), addGroup)).setNode(input.getNode()).build();
            resultsLot.add(salGroupRpcs.getRpcClassToInstanceMap().getInstance(AddGroup.class).invoke(addGroupInput));
        }

        final ListenableFuture<RpcResult<List<BatchFailedGroupsOutput>>> commonResult = Futures
                .transform(Futures.allAsList(resultsLot),
                           GroupUtil.createCumulatingFunction(input.nonnullBatchAddGroups().values()),
                           MoreExecutors.directExecutor());

        ListenableFuture<RpcResult<AddGroupsBatchOutput>> addGroupsBulkFuture = Futures
                .transform(commonResult, GroupUtil.GROUP_ADD_TRANSFORM, MoreExecutors.directExecutor());

        if (input.getBarrierAfter()) {
            addGroupsBulkFuture = BarrierUtil.chainBarrier(addGroupsBulkFuture, input.getNode(), transactionRpc,
                                                           GroupUtil.GROUP_ADD_COMPOSING_TRANSFORM);
        }

        return addGroupsBulkFuture;
    }

    private ListenableFuture<RpcResult<RemoveGroupsBatchOutput>> removeGroupsBatch(final RemoveGroupsBatchInput input) {
        LOG.trace("Removing groups @ {} : {}", PathUtil.extractNodeId(input.getNode()),
                  input.getBatchRemoveGroups().size());
        final ArrayList<ListenableFuture<RpcResult<RemoveGroupOutput>>> resultsLot = new ArrayList<>();
        for (BatchRemoveGroups addGroup : input.nonnullBatchRemoveGroups().values()) {
            final RemoveGroupInput removeGroupInput = new RemoveGroupInputBuilder(addGroup)
                    .setGroupRef(createGroupRef(input.getNode(), addGroup)).setNode(input.getNode()).build();
            resultsLot.add(salGroupRpcs.getRpcClassToInstanceMap().getInstance(RemoveGroup.class)
                .invoke(removeGroupInput));
        }

        final ListenableFuture<RpcResult<List<BatchFailedGroupsOutput>>> commonResult = Futures
                .transform(Futures.allAsList(resultsLot),
                           GroupUtil.createCumulatingFunction(input.nonnullBatchRemoveGroups().values()),
                           MoreExecutors.directExecutor());

        ListenableFuture<RpcResult<RemoveGroupsBatchOutput>> removeGroupsBulkFuture = Futures
                .transform(commonResult, GroupUtil.GROUP_REMOVE_TRANSFORM, MoreExecutors.directExecutor());

        if (input.getBarrierAfter()) {
            removeGroupsBulkFuture = BarrierUtil
                    .chainBarrier(removeGroupsBulkFuture, input.getNode(), transactionRpc,
                                  GroupUtil.GROUP_REMOVE_COMPOSING_TRANSFORM);
        }

        return removeGroupsBulkFuture;
    }

    public ClassToInstanceMap<Rpc<?,?>> getRpcClassToInstanceMap() {
        return ImmutableClassToInstanceMap.<Rpc<?, ?>>builder()
            .put(RemoveGroupsBatch.class, this::removeGroupsBatch)
            .put(AddGroupsBatch.class, this::addGroupsBatch)
            .put(UpdateGroupsBatch.class, this::updateGroupsBatch)
            .build();
    }

    private static GroupRef createGroupRef(final NodeRef nodeRef, final Group batchGroup) {
        return GroupUtil.buildGroupPath((InstanceIdentifier<Node>) nodeRef.getValue(), batchGroup.getGroupId());
    }

    private static GroupRef createGroupRef(final NodeRef nodeRef, final BatchUpdateGroups batchGroup) {
        return GroupUtil.buildGroupPath((InstanceIdentifier<Node>) nodeRef.getValue(),
                                        batchGroup.getUpdatedBatchedGroup().getGroupId());
    }
}
