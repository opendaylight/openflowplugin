/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.mock;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowplugin.applications.frm.impl.DeviceMastershipManager;
import org.opendaylight.openflowplugin.applications.frm.impl.ForwardingRulesManagerImpl;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationManager;
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
import test.mock.util.FRMTest;
import test.mock.util.RpcProviderRegistryMock;
import test.mock.util.SalGroupServiceMock;

@RunWith(MockitoJUnitRunner.class)
public class GroupListenerTest extends FRMTest {
    private ForwardingRulesManagerImpl forwardingRulesManager;
    private final static NodeId NODE_ID = new NodeId("testnode:1");
    private final static NodeKey s1Key = new NodeKey(NODE_ID);
    RpcProviderRegistry rpcProviderRegistryMock = new RpcProviderRegistryMock();
    @Mock
    ClusterSingletonServiceProvider clusterSingletonService;
    @Mock
    DeviceMastershipManager deviceMastershipManager;
    @Mock
    private NotificationProviderService notificationService;
    @Mock
    private ReconciliationManager reconciliationManager;


    @Before
    public void setUp() {
        forwardingRulesManager = new ForwardingRulesManagerImpl(
                getDataBroker(),
                rpcProviderRegistryMock,
                getConfig(),
                clusterSingletonService,
                notificationService,
                getConfigurationService(),
                reconciliationManager);

        forwardingRulesManager.start();
        // TODO consider tests rewrite (added because of complicated access)
        forwardingRulesManager.setDeviceMastershipManager(deviceMastershipManager);
        Mockito.when(deviceMastershipManager.isDeviceMastered(NODE_ID)).thenReturn(true);
    }

    @Test
    public void addTwoGroupsTest() throws Exception {
        addFlowCapableNode(s1Key);

        GroupKey groupKey = new GroupKey(new GroupId((long) 255));
        InstanceIdentifier<Group> groupII = InstanceIdentifier.create(Nodes.class).child(Node.class, s1Key)
                .augmentation(FlowCapableNode.class).child(Group.class, groupKey);
        Group group = new GroupBuilder().setKey(groupKey).setGroupName("Group1").build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, groupII, group);
        assertCommit(writeTx.submit());
        SalGroupServiceMock salGroupService = (SalGroupServiceMock) forwardingRulesManager.getSalGroupService();
        List<AddGroupInput> addGroupCalls = salGroupService.getAddGroupCalls();
        assertEquals(1, addGroupCalls.size());
        assertEquals("DOM-0", addGroupCalls.get(0).getTransactionUri().getValue());

        groupKey = new GroupKey(new GroupId((long) 256));
        groupII = InstanceIdentifier.create(Nodes.class).child(Node.class, s1Key)
                .augmentation(FlowCapableNode.class).child(Group.class, groupKey);
        group = new GroupBuilder().setKey(groupKey).setGroupName("Group1").build();
        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, groupII, group);
        assertCommit(writeTx.submit());
        salGroupService = (SalGroupServiceMock) forwardingRulesManager.getSalGroupService();
        addGroupCalls = salGroupService.getAddGroupCalls();
        assertEquals(2, addGroupCalls.size());
        assertEquals("DOM-1", addGroupCalls.get(1).getTransactionUri().getValue());
    }

    @Test
    public void updateGroupTest() throws Exception {
        addFlowCapableNode(s1Key);

        GroupKey groupKey = new GroupKey(new GroupId((long) 255));
        InstanceIdentifier<Group> groupII = InstanceIdentifier.create(Nodes.class).child(Node.class, s1Key)
                .augmentation(FlowCapableNode.class).child(Group.class, groupKey);
        Group group = new GroupBuilder().setKey(groupKey).setGroupName("Group1").build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, groupII, group);
        assertCommit(writeTx.submit());
        SalGroupServiceMock salGroupService = (SalGroupServiceMock) forwardingRulesManager.getSalGroupService();
        List<AddGroupInput> addGroupCalls = salGroupService.getAddGroupCalls();
        assertEquals(1, addGroupCalls.size());
        assertEquals("DOM-0", addGroupCalls.get(0).getTransactionUri().getValue());

        group = new GroupBuilder().setKey(groupKey).setGroupName("Group2").build();
        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, groupII, group);
        assertCommit(writeTx.submit());
        salGroupService = (SalGroupServiceMock) forwardingRulesManager.getSalGroupService();
        List<UpdateGroupInput> updateGroupCalls = salGroupService.getUpdateGroupCalls();
        assertEquals(1, updateGroupCalls.size());
        assertEquals("DOM-1", updateGroupCalls.get(0).getTransactionUri().getValue());
    }

    @Test
    public void removeGroupTest() throws Exception {
        addFlowCapableNode(s1Key);

        GroupKey groupKey = new GroupKey(new GroupId((long) 255));
        InstanceIdentifier<Group> groupII = InstanceIdentifier.create(Nodes.class).child(Node.class, s1Key)
                .augmentation(FlowCapableNode.class).child(Group.class, groupKey);
        Group group = new GroupBuilder().setKey(groupKey).setGroupName("Group1").build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, groupII, group);
        assertCommit(writeTx.submit());
        SalGroupServiceMock salGroupService = (SalGroupServiceMock) forwardingRulesManager.getSalGroupService();
        List<AddGroupInput> addGroupCalls = salGroupService.getAddGroupCalls();
        assertEquals(1, addGroupCalls.size());
        assertEquals("DOM-0", addGroupCalls.get(0).getTransactionUri().getValue());

        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.delete(LogicalDatastoreType.CONFIGURATION, groupII);
        assertCommit(writeTx.submit());
        salGroupService = (SalGroupServiceMock) forwardingRulesManager.getSalGroupService();
        List<RemoveGroupInput> removeGroupCalls = salGroupService.getRemoveGroupCalls();
        assertEquals(1, removeGroupCalls.size());
        assertEquals("DOM-1", removeGroupCalls.get(0).getTransactionUri().getValue());
    }

    @Test
    public void staleGroupCreationTest() throws Exception {
        addFlowCapableNode(s1Key);

        StaleGroupKey groupKey = new StaleGroupKey(new GroupId((long) 255));
        InstanceIdentifier<StaleGroup> groupII = InstanceIdentifier.create(Nodes.class).child(Node.class, s1Key)
                .augmentation(FlowCapableNode.class).child(StaleGroup.class, groupKey);
        StaleGroup group = new StaleGroupBuilder().setKey(groupKey).setGroupName("Stale_Group1").build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, groupII, group);
        assertCommit(writeTx.submit());
    }

    @After
    public void tearDown() throws Exception {
        forwardingRulesManager.close();
    }

}
