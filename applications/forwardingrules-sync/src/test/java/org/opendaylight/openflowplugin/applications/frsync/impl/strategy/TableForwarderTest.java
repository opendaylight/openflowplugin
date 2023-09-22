/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frsync.impl.strategy;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Test for {@link TableForwarder}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TableForwarderTest {

    private final NodeKey s1Key = new NodeKey(new NodeId("S1"));
    private final Uint8 tableId = Uint8.valueOf(42);
    private final TableKey tableKey = new TableKey(tableId);
    private final TableFeaturesKey tableFeaturesKey = new TableFeaturesKey(tableId);
    private final TableFeatures tableFeatures = new TableFeaturesBuilder()
            .setName("test-table")
            .setTableId(tableId)
            .build();

    private final KeyedInstanceIdentifier<Node, NodeKey> nodePath = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, s1Key);
    private final InstanceIdentifier<FlowCapableNode> flowCapableNodePath = nodePath
            .augmentation(FlowCapableNode.class);
    private final KeyedInstanceIdentifier<Table, TableKey> tablePath = flowCapableNodePath
            .child(Table.class, tableKey);
    private final InstanceIdentifier<TableFeatures> tableFeaturesPath = flowCapableNodePath
            .child(TableFeatures.class, tableFeaturesKey);

    @Captor
    private ArgumentCaptor<UpdateTableInput> updateTableInputCpt;
    @Mock
    private RpcConsumerRegistry rpcRegistry;

    private TransactionId txId;

    private TableForwarder tableForwarder;


    @Before
    public void setUp() {
        tableForwarder = new TableForwarder(rpcRegistry);
        txId = new TransactionId(Uint64.ONE);
    }

    @Test
    public void testUpdate() throws Exception {
        Mockito.when(rpcRegistry.getRpc(UpdateTable.class).invoke(updateTableInputCpt.capture())).thenReturn(
                RpcResultBuilder.success(
                        new UpdateTableOutputBuilder()
                                .setTransactionId(txId)
                                .build()
                ).buildFuture()
        );

        final TableFeatures tableFeaturesUpdate = new TableFeaturesBuilder(tableFeatures)
                .setName("another-table")
                .build();
        final Future<RpcResult<UpdateTableOutput>> updateResult = tableForwarder.update(
                tableFeaturesPath, tableFeatures, tableFeaturesUpdate, flowCapableNodePath);

        Mockito.verify(rpcRegistry).getRpc(UpdateTable.class).invoke(ArgumentMatchers.any());

        Assert.assertTrue(updateResult.isDone());
        final RpcResult<UpdateTableOutput> updateTableResult = updateResult.get(2, TimeUnit.SECONDS);
        Assert.assertTrue(updateTableResult.isSuccessful());

        Assert.assertEquals(1, updateTableResult.getResult().getTransactionId().getValue().intValue());

        final UpdateTableInput updateTableInput = updateTableInputCpt.getValue();
        Assert.assertEquals(tablePath, updateTableInput.getTableRef().getValue());
        Assert.assertEquals(nodePath, updateTableInput.getNode().getValue());

        Assert.assertEquals(1, updateTableInput.getOriginalTable().nonnullTableFeatures().size());
        Assert.assertEquals("test-table", updateTableInput.getOriginalTable().nonnullTableFeatures().values()
                .iterator().next().getName());
        Assert.assertEquals(1, updateTableInput.getUpdatedTable().nonnullTableFeatures().size());
        Assert.assertEquals("another-table", updateTableInput.getUpdatedTable().nonnullTableFeatures().values()
                .iterator().next().getName());
    }
}