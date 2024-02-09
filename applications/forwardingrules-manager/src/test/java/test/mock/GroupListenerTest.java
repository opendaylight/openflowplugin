/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.mock;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.StaleGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.StaleGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.StaleGroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint32;
import test.mock.util.AbstractFRMTest;

@RunWith(MockitoJUnitRunner.class)
public class GroupListenerTest extends AbstractFRMTest {
    private static final NodeId NODE_ID = new NodeId("testnode:1");
    private static final NodeKey NODE_KEY = new NodeKey(NODE_ID);

    @Before
    public void setUp() {
        setUpForwardingRulesManager();
        setDeviceMastership(NODE_ID);
    }

    @Test
    public void addTwoGroupsTest() {
        addFlowCapableNode(NODE_KEY);

        GroupKey groupKey = new GroupKey(new GroupId(Uint32.valueOf(255)));
        InstanceIdentifier<Group> groupII = InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class).child(Group.class, groupKey);
        Group group = new GroupBuilder().withKey(groupKey).setGroupName("Group1").build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, groupII, group);
        assertCommit(writeTx.commit());

        final var addGroupCaptor = ArgumentCaptor.forClass(AddGroupInput.class);
        verify(addGroup).invoke(addGroupCaptor.capture());
        await().atMost(10, SECONDS).until(() ->  addGroupCaptor.getAllValues().size() == 1);
        var addGroupCalls = addGroupCaptor.getAllValues();
        assertEquals(1, addGroupCalls.size());
        assertEquals("DOM-0", addGroupCalls.get(0).getTransactionUri().getValue());

        groupKey = new GroupKey(new GroupId(Uint32.valueOf(256)));
        groupII = InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class).child(Group.class, groupKey);
        group = new GroupBuilder().withKey(groupKey).setGroupName("Group1").build();
        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, groupII, group);
        assertCommit(writeTx.commit());
        await().atMost(10, SECONDS).until(() -> addGroupCaptor.getAllValues().size() == 2);
        addGroupCalls = addGroupCaptor.getAllValues();
        assertEquals(2, addGroupCalls.size());
        assertEquals("DOM-1", addGroupCalls.get(1).getTransactionUri().getValue());
    }

    @Test
    public void updateGroupTest() {
        addFlowCapableNode(NODE_KEY);

        GroupKey groupKey = new GroupKey(new GroupId(Uint32.valueOf(255)));
        InstanceIdentifier<Group> groupII = InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class).child(Group.class, groupKey);
        Group group = new GroupBuilder().withKey(groupKey).setGroupName("Group1").build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, groupII, group);
        assertCommit(writeTx.commit());

        final var addGroupCaptor = ArgumentCaptor.forClass(AddGroupInput.class);
        verify(addGroup).invoke(addGroupCaptor.capture());
        await().atMost(10, SECONDS).until(() -> addGroupCaptor.getAllValues().size() == 1);
        final var addGroupCalls = addGroupCaptor.getAllValues();
        assertEquals(1, addGroupCalls.size());
        assertEquals("DOM-0", addGroupCalls.get(0).getTransactionUri().getValue());

        group = new GroupBuilder().withKey(groupKey).setGroupName("Group2").build();
        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, groupII, group);
        assertCommit(writeTx.commit());

        final var updateGroupCaptor = ArgumentCaptor.forClass(UpdateGroupInput.class);
        verify(updateGroup).invoke(updateGroupCaptor.capture());
        await().atMost(10, SECONDS).until(() -> updateGroupCaptor.getAllValues().size() == 1);
        final var updateGroupCalls = updateGroupCaptor.getAllValues();
        assertEquals(1, updateGroupCalls.size());
        assertEquals("DOM-1", updateGroupCalls.get(0).getTransactionUri().getValue());
    }

    @Test
    public void removeGroupTest() {
        addFlowCapableNode(NODE_KEY);

        GroupKey groupKey = new GroupKey(new GroupId(Uint32.valueOf(255)));
        InstanceIdentifier<Group> groupII = InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class).child(Group.class, groupKey);
        Group group = new GroupBuilder().withKey(groupKey).setGroupName("Group1").build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, groupII, group);
        assertCommit(writeTx.commit());

        final var addGroupCaptor = ArgumentCaptor.forClass(AddGroupInput.class);
        verify(addGroup).invoke(addGroupCaptor.capture());
        await().atMost(10, SECONDS).until(() -> addGroupCaptor.getAllValues().size() == 1);
        final var addGroupCalls = addGroupCaptor.getAllValues();
        assertEquals(1, addGroupCalls.size());
        assertEquals("DOM-0", addGroupCalls.get(0).getTransactionUri().getValue());

        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.delete(LogicalDatastoreType.CONFIGURATION, groupII);
        assertCommit(writeTx.commit());

        final var removeGroupCaptor = ArgumentCaptor.forClass(RemoveGroupInput.class);
        verify(removeGroup).invoke(removeGroupCaptor.capture());
        await().atMost(10, SECONDS).until(() -> removeGroupCaptor.getAllValues().size() == 1);
        final var removeGroupCalls = removeGroupCaptor.getAllValues();
        assertEquals(1, removeGroupCalls.size());
        assertEquals("DOM-1", removeGroupCalls.get(0).getTransactionUri().getValue());
    }

    @Test
    public void staleGroupCreationTest() {
        addFlowCapableNode(NODE_KEY);

        StaleGroupKey groupKey = new StaleGroupKey(new GroupId(Uint32.valueOf(255)));
        InstanceIdentifier<StaleGroup> groupII = InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class).child(StaleGroup.class, groupKey);
        StaleGroup group = new StaleGroupBuilder().withKey(groupKey).setGroupName("Stale_Group1").build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, groupII, group);
        assertCommit(writeTx.commit());
    }

    @After
    public void tearDown() throws Exception {
        getForwardingRulesManager().close();
    }
}
