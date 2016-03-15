/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.output.list.grouping.BatchGroupsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.output.list.grouping.BatchGroupsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * provides group util methods
 */
public final class GroupUtil {

    private GroupUtil() {
        throw new IllegalStateException("This class should not be instantiated.");
    }

    /**
     * @param nodePath
     * @param groupId
     * @return instance identifier assembled for given node and group
     */
    public static GroupRef buildGroupPath(final InstanceIdentifier<Node> nodePath, final GroupId groupId) {
        final KeyedInstanceIdentifier<Group, GroupKey> groupPath = nodePath
                .augmentation(FlowCapableNode.class)
                .child(Group.class, new GroupKey(groupId));

        return new GroupRef(groupPath);
    }

    public static <O> Function<List<RpcResult<O>>, ArrayList<BatchGroupsOutput>> createCumulativeFunction(
            final Iterable<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group> inputBatchGroups) {
        return createCumulativeFunction(inputBatchGroups, Iterables.size(inputBatchGroups));
    }

    public static <O> Function<List<RpcResult<O>>, ArrayList<BatchGroupsOutput>> createCumulativeFunction(
            final Iterable<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group> inputBatchGroups,
            final int sizeOfInputBatch) {
        return new Function<List<RpcResult<O>>, ArrayList<BatchGroupsOutput>>() {
            @Nullable
            @Override
            public ArrayList<BatchGroupsOutput> apply(@Nullable final List<RpcResult<O>> innerInput) {
                final int sizeOfFutures = innerInput.size();
                Preconditions.checkArgument(sizeOfFutures == sizeOfInputBatch,
                        "wrong amount of returned futures: {} <> {}", sizeOfFutures, sizeOfInputBatch);

                final ArrayList<BatchGroupsOutput> batchFlows = new ArrayList<>();
                final Iterator<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group>
                        batchFlowIterator = inputBatchGroups.iterator();

                for (RpcResult<O> groupModOutput : innerInput) {
                    final GroupId groupId = batchFlowIterator.next().getGroupId();

                    //TODO: preserve/propagate errors
                    batchFlows.add(
                            new BatchGroupsOutputBuilder()
                                    .setGroupId(groupId)
                                    .setSuccess(groupModOutput.isSuccessful())
                                    .build());
                }

                return batchFlows;
            }
        };
    }
}
