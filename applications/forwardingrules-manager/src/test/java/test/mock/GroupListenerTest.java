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

import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupCacheManager;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.applications.frm.impl.DeviceMastershipManager;
import org.opendaylight.openflowplugin.applications.frm.impl.ForwardingRulesManagerImpl;
import org.opendaylight.openflowplugin.applications.frm.recovery.OpenflowServiceRecoveryHandler;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationManager;
import org.opendaylight.serviceutils.srm.ServiceRecoveryRegistry;
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
    private static final NodeId NODE_ID = new NodeId("testnode:1");
    private static final NodeKey NODE_KEY = new NodeKey(NODE_ID);
    RpcProviderRegistryMock rpcProviderRegistryMock = new RpcProviderRegistryMock();
    @Mock
    ClusterSingletonServiceProvider clusterSingletonService;
    @Mock
    DeviceMastershipManager deviceMastershipManager;
    @Mock
    private ReconciliationManager reconciliationManager;
    @Mock
    private OpenflowServiceRecoveryHandler openflowServiceRecoveryHandler;
    @Mock
    private ServiceRecoveryRegistry serviceRecoveryRegistry;
    @Mock
    private MastershipChangeServiceManager mastershipChangeServiceManager;
    @Mock
    private FlowGroupCacheManager flowGroupCacheManager;


    @Before
    public void setUp() {
        forwardingRulesManager = new ForwardingRulesManagerImpl(
                getDataBroker(),
                rpcProviderRegistryMock,
                rpcProviderRegistryMock,
                getConfig(),
                mastershipChangeServiceManager,
                clusterSingletonService,
                getConfigurationService(),
                reconciliationManager,
                openflowServiceRecoveryHandler,
                serviceRecoveryRegistry,
                flowGroupCacheManager,
                getRegistrationHelper()
                );

        forwardingRulesManager.start();
        // TODO consider tests rewrite (added because of complicated access)
        forwardingRulesManager.setDeviceMastershipManager(deviceMastershipManager);
        Mockito.when(deviceMastershipManager.isDeviceMastered(NODE_ID)).thenReturn(true);
    }

    @Test
    public void addTwoGroupsTest() {
        addFlowCapableNode(NODE_KEY);

        GroupKey groupKey = new GroupKey(new GroupId((long) 255));
        InstanceIdentifier<Group> groupII = InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class).child(Group.class, groupKey);
        Group group = new GroupBuilder().withKey(groupKey).setGroupName("Group1").build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, groupII, group);
        assertCommit(writeTx.commit());
        final SalGroupServiceMock salGroupService = (SalGroupServiceMock) forwardingRulesManager.getSalGroupService();
        await().atMost(10, SECONDS).until(() -> salGroupService.getAddGroupCalls().size() == 1);
        List<AddGroupInput> addGroupCalls = salGroupService.getAddGroupCalls();
        assertEquals(1, addGroupCalls.size());
        assertEquals("DOM-0", addGroupCalls.get(0).getTransactionUri().getValue());

        groupKey = new GroupKey(new GroupId((long) 256));
        groupII = InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class).child(Group.class, groupKey);
        group = new GroupBuilder().withKey(groupKey).setGroupName("Group1").build();
        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, groupII, group);
        assertCommit(writeTx.commit());
        await().atMost(10, SECONDS).until(() -> salGroupService.getAddGroupCalls().size() == 2);
        addGroupCalls = salGroupService.getAddGroupCalls();
        assertEquals(2, addGroupCalls.size());
        assertEquals("DOM-1", addGroupCalls.get(1).getTransactionUri().getValue());
    }

    @Test
    public void updateGroupTest() {
        addFlowCapableNode(NODE_KEY);

        GroupKey groupKey = new GroupKey(new GroupId((long) 255));
        InstanceIdentifier<Group> groupII = InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class).child(Group.class, groupKey);
        Group group = new GroupBuilder().withKey(groupKey).setGroupName("Group1").build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, groupII, group);
        assertCommit(writeTx.commit());
        final SalGroupServiceMock salGroupService = (SalGroupServiceMock) forwardingRulesManager.getSalGroupService();
        await().atMost(10, SECONDS).until(() -> salGroupService.getAddGroupCalls().size() == 1);
        List<AddGroupInput> addGroupCalls = salGroupService.getAddGroupCalls();
        assertEquals(1, addGroupCalls.size());
        assertEquals("DOM-0", addGroupCalls.get(0).getTransactionUri().getValue());

        group = new GroupBuilder().withKey(groupKey).setGroupName("Group2").build();
        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, groupII, group);
        assertCommit(writeTx.commit());
        await().atMost(10, SECONDS).until(() -> salGroupService.getUpdateGroupCalls().size() == 1);
        List<UpdateGroupInput> updateGroupCalls = salGroupService.getUpdateGroupCalls();
        assertEquals(1, updateGroupCalls.size());
        assertEquals("DOM-1", updateGroupCalls.get(0).getTransactionUri().getValue());
    }

    @Test
    public void removeGroupTest() {
        addFlowCapableNode(NODE_KEY);

        GroupKey groupKey = new GroupKey(new GroupId((long) 255));
        InstanceIdentifier<Group> groupII = InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class).child(Group.class, groupKey);
        Group group = new GroupBuilder().withKey(groupKey).setGroupName("Group1").build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, groupII, group);
        assertCommit(writeTx.commit());
        SalGroupServiceMock salGroupService = (SalGroupServiceMock) forwardingRulesManager.getSalGroupService();
        await().atMost(10, SECONDS).until(() -> salGroupService.getAddGroupCalls().size() == 1);
        List<AddGroupInput> addGroupCalls = salGroupService.getAddGroupCalls();
        assertEquals(1, addGroupCalls.size());
        assertEquals("DOM-0", addGroupCalls.get(0).getTransactionUri().getValue());

        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.delete(LogicalDatastoreType.CONFIGURATION, groupII);
        assertCommit(writeTx.commit());
        await().atMost(10, SECONDS).until(() -> salGroupService.getRemoveGroupCalls().size() == 1);
        List<RemoveGroupInput> removeGroupCalls = salGroupService.getRemoveGroupCalls();
        assertEquals(1, removeGroupCalls.size());
        assertEquals("DOM-1", removeGroupCalls.get(0).getTransactionUri().getValue());
    }

    @Test
    public void staleGroupCreationTest() {
        addFlowCapableNode(NODE_KEY);

        StaleGroupKey groupKey = new StaleGroupKey(new GroupId((long) 255));
        InstanceIdentifier<StaleGroup> groupII = InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class).child(StaleGroup.class, groupKey);
        StaleGroup group = new StaleGroupBuilder().withKey(groupKey).setGroupName("Stale_Group1").build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, groupII, group);
        assertCommit(writeTx.commit());
    }

    @After
    public void tearDown() throws Exception {
        forwardingRulesManager.close();
    }
}
