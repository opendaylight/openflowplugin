/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

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
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.applications.frm.impl.util.FRMTest;
import org.opendaylight.openflowplugin.applications.frm.impl.util.RpcProviderRegistryMock;
import org.opendaylight.openflowplugin.applications.frm.impl.util.SalTableServiceMock;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationManager;
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

@RunWith(MockitoJUnitRunner.class)
public class TableFeaturesListenerTest extends FRMTest {
    private ForwardingRulesManagerImpl forwardingRulesManager;
    private final static NodeId NODE_ID = new NodeId("testnode:1");
    private final static NodeKey s1Key = new NodeKey(NODE_ID);
    RpcProviderRegistry rpcProviderRegistryMock = new RpcProviderRegistryMock();
    @Mock
    MastershipChangeServiceManager mastershipChangeServiceManager;
    @Mock
    DeviceMastershipManagerImpl deviceMastershipManager;
    @Mock
    private ReconciliationManager reconciliationManager;


    @Before
    public void setUp() {
        forwardingRulesManager = new ForwardingRulesManagerImpl(
                getDataBroker(),
                rpcProviderRegistryMock,
                getConfig(),
                mastershipChangeServiceManager,
                getConfigurationService(),
                reconciliationManager);

        forwardingRulesManager.start();
        // TODO consider tests rewrite (added because of complicated access)
        forwardingRulesManager.setDeviceMastershipManager(deviceMastershipManager);
        Mockito.when(deviceMastershipManager.isDeviceMastered(NODE_ID)).thenReturn(true);
    }

    @Test
    public void updateFlowTest() {
        TableKey tableKey = new TableKey((short) 2);
        TableFeaturesKey tableFeaturesKey = new TableFeaturesKey(tableKey.getId());

        addTable(tableKey, s1Key);

        TableFeatures tableFeaturesData = new TableFeaturesBuilder().setKey(tableFeaturesKey).build();
        InstanceIdentifier<TableFeatures> tableFeaturesII = InstanceIdentifier.create(Nodes.class).child(Node.class, s1Key)
                .augmentation(FlowCapableNode.class).child(TableFeatures.class, tableFeaturesKey);
        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, tableFeaturesII, tableFeaturesData);
        assertCommit(writeTx.submit());

        tableFeaturesData = new TableFeaturesBuilder().setKey(tableFeaturesKey).setName("dummy name").build();
        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, tableFeaturesII, tableFeaturesData);
        assertCommit(writeTx.submit());

        SalTableServiceMock salTableServiceMock = (SalTableServiceMock) forwardingRulesManager.getSalTableService();
        List<UpdateTableInput> updateTableInputs = salTableServiceMock.getUpdateTableInput();
        assertEquals(1, updateTableInputs.size());
        assertEquals("DOM-0", updateTableInputs.get(0).getTransactionUri().getValue());
    }

    @After
    public void tearDown() throws Exception {
        forwardingRulesManager.close();
    }

}
