/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.mock;

import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesKey;

import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInput;
import test.mock.util.EntityOwnershipServiceMock;
import test.mock.util.SalTableServiceMock;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.applications.frm.impl.ForwardingRulesManagerImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import test.mock.util.FRMTest;
import test.mock.util.RpcProviderRegistryMock;
import java.util.List;
import static org.junit.Assert.assertEquals;

public class TableFeaturesListenerTest extends FRMTest {
    RpcProviderRegistry rpcProviderRegistryMock = new RpcProviderRegistryMock();
    EntityOwnershipService eos = new EntityOwnershipServiceMock();


    @Test
    public void updateFlowTest() throws Exception {
        NodeKey s1Key = new NodeKey(new NodeId("S1"));
        TableKey tableKey = new TableKey((short) 2);
        TableFeaturesKey tableFeaturesKey = new TableFeaturesKey(tableKey.getId());
        ForwardingRulesManagerImpl forwardingRulesManager = new ForwardingRulesManagerImpl(
                getDataBroker(),
                rpcProviderRegistryMock,
                getConfig(),
                eos);
        forwardingRulesManager.start();

        addTable(tableKey, s1Key);

        TableFeatures tableFeaturesData = new TableFeaturesBuilder().setKey(tableFeaturesKey).build();
        InstanceIdentifier<TableFeatures> tableFeaturesII = InstanceIdentifier.create(Nodes.class).child(Node.class, s1Key)
              .augmentation(FlowCapableNode.class).child(Table.class, tableKey).child(TableFeatures.class, tableFeaturesKey);
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

        forwardingRulesManager.close();
    }
}
