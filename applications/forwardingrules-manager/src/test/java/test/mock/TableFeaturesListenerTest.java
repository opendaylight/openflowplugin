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
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
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
import org.opendaylight.yangtools.yang.common.Uint8;
import test.mock.util.FRMTest;
import test.mock.util.SalTableServiceMock;

@RunWith(MockitoJUnitRunner.class)
public class TableFeaturesListenerTest extends FRMTest {
    private static final NodeId NODE_ID = new NodeId("testnode:1");
    private static final NodeKey NODE_KEY = new NodeKey(NODE_ID);

    @Before
    public void setUp() {
        setUpForwardingRulesManager();
        setDeviceMastership(NODE_ID);
    }

    @Test
    public void updateFlowTest() {
        TableKey tableKey = new TableKey(Uint8.TWO);
        TableFeaturesKey tableFeaturesKey = new TableFeaturesKey(tableKey.getId());

        addTable(tableKey);

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

        SalTableServiceMock salTableServiceMock =
                (SalTableServiceMock) getForwardingRulesManager().getSalTableService();
        await().atMost(10, SECONDS).until(() -> salTableServiceMock.getUpdateTableInput().size() == 1);
        List<UpdateTableInput> updateTableInputs = salTableServiceMock.getUpdateTableInput();
        assertEquals(1, updateTableInputs.size());
        assertEquals("DOM-0", updateTableInputs.get(0).getTransactionUri().getValue());
    }

    @After
    public void tearDown() throws Exception {
        getForwardingRulesManager().close();
    }

    private void addTable(final TableKey tableKey) {
        addFlowCapableNode(NODE_KEY);
        final Table table = new TableBuilder().withKey(tableKey).build();
        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        InstanceIdentifier<Table> tableII = InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class).child(Table.class, tableKey);
        writeTx.put(LogicalDatastoreType.CONFIGURATION, tableII, table);
        assertCommit(writeTx.commit());
    }
}
