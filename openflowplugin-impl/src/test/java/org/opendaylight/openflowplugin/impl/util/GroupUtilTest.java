/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.output.list.grouping.BatchGroupsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link GroupUtil}.
 */
public class GroupUtilTest {

    public static final NodeId DUMMY_NODE_ID = new NodeId("dummyNodeId");
    private static final GroupId DUMMY_GROUP_ID = new GroupId(42L);
    private static final GroupId DUMMY_GROUP_ID_2 = new GroupId(43L);

    @Test
    public void testBuildGroupPath() throws Exception {
        final InstanceIdentifier<Node> nodePath = InstanceIdentifier
                .create(Nodes.class)
                .child(Node.class, new NodeKey(DUMMY_NODE_ID));

        final GroupRef groupRef = GroupUtil.buildGroupPath(nodePath, DUMMY_GROUP_ID);
        final InstanceIdentifier<?> groupRefValue = groupRef.getValue();
        Assert.assertEquals(DUMMY_NODE_ID, groupRefValue.firstKeyOf(Node.class).getId());
        Assert.assertEquals(DUMMY_GROUP_ID, groupRefValue.firstKeyOf(Group.class).getGroupId());
    }

    @Test
    public void testCreateCumulativeFunction() throws Exception {
        final Function<List<RpcResult<String>>, ArrayList<BatchGroupsOutput>> function =
                GroupUtil.createCumulativeFunction(Lists.newArrayList(
                        createBatchGroup(DUMMY_GROUP_ID),
                        createBatchGroup(DUMMY_GROUP_ID_2)));

        final ArrayList<BatchGroupsOutput> output = function.apply(Lists.newArrayList(
                RpcResultBuilder.success("a").build(),
                RpcResultBuilder.<String>failed().build()));

        Assert.assertEquals(2, output.size());
        Assert.assertEquals(DUMMY_GROUP_ID, output.get(0).getGroupId());
        Assert.assertEquals(DUMMY_GROUP_ID_2, output.get(1).getGroupId());
    }

    private org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group createBatchGroup(final GroupId groupId) {
        return new GroupBuilder()
                .setGroupId(groupId)
                .build();
    }
}