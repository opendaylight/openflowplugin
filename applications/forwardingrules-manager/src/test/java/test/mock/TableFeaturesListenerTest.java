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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import test.mock.util.FRMTest;
import test.mock.util.RpcProviderRegistryMock;
import test.mock.util.SalTableServiceMock;

@RunWith(MockitoJUnitRunner.class)
public class TableFeaturesListenerTest extends FRMTest {
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
        forwardingRulesManager = new ForwardingRulesManagerImpl(getDataBroker(), rpcProviderRegistryMock,
                rpcProviderRegistryMock, getConfig(), mastershipChangeServiceManager, clusterSingletonService,
                getConfigurationService(), reconciliationManager, openflowServiceRecoveryHandler,
                serviceRecoveryRegistry, flowGroupCacheManager, getRegistrationHelper());


        forwardingRulesManager.start();
        // TODO consider tests rewrite (added because of complicated access)
        forwardingRulesManager.setDeviceMastershipManager(deviceMastershipManager);
        Mockito.when(deviceMastershipManager.isDeviceMastered(NODE_ID)).thenReturn(true);
    }

    @Test
    public void updateFlowTest() {
        TableKey tableKey = new TableKey((short) 2);
        TableFeaturesKey tableFeaturesKey = new TableFeaturesKey(tableKey.getId());

        addTable(tableKey, NODE_KEY);

        TableFeatures tableFeaturesData = new TableFeaturesBuilder().withKey(tableFeaturesKey).build();
        InstanceIdentifier<TableFeatures> tableFeaturesII = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, NODE_KEY).augmentation(FlowCapableNode.class)
                .child(TableFeatures.class, tableFeaturesKey);
        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, tableFeaturesII, tableFeaturesData);
        assertCommit(writeTx.commit());

        tableFeaturesData = new TableFeaturesBuilder().withKey(tableFeaturesKey).setName("dummy name").build();
        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, tableFeaturesII, tableFeaturesData);
        assertCommit(writeTx.commit());

        SalTableServiceMock salTableServiceMock = (SalTableServiceMock) forwardingRulesManager.getSalTableService();
        await().atMost(10, SECONDS).until(() -> salTableServiceMock.getUpdateTableInput().size() == 1);
        List<UpdateTableInput> updateTableInputs = salTableServiceMock.getUpdateTableInput();
        assertEquals(1, updateTableInputs.size());
        assertEquals("DOM-0", updateTableInputs.get(0).getTransactionUri().getValue());
    }

    @After
    public void tearDown() throws Exception {
        forwardingRulesManager.close();
    }
}
