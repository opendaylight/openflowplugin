/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.bulk.o.matic;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.Futures;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.AddFlowsDsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.AddFlowsDsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.AddFlowsRpcInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.AddFlowsRpcInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.FlowRpcAddMultipleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.FlowRpcAddMultipleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.FlowRpcAddTestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.FlowRpcAddTestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.FlowTestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.FlowTestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.ReadFlowTestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.ReadFlowTestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.RemoveFlowsDsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.RemoveFlowsDsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.RemoveFlowsRpcInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.RemoveFlowsRpcInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.TableTestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.TableTestInput.Operation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.TableTestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.bulk.flow.ds.list.grouping.BulkFlowDsItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.bulk.flow.ds.list.grouping.BulkFlowDsItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.bulk.flow.list.grouping.BulkFlowItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.bulk.flow.list.grouping.BulkFlowItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link SalBulkFlowServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SalBulkFlowServiceImplTest {

    private static final Logger LOG = LoggerFactory.getLogger(SalBulkFlowServiceImplTest.class);

    @Mock
    private DataBroker mockDataBroker;
    @Mock
    private SalFlowService mockSalFlowService;
    @Mock
    private WriteTransaction wTx;
    @Mock
    private ReadOnlyTransaction rTx;
    @Mock
    private Nodes mockNodes;
    @Mock
    private Node mockNode;
    @Captor
    private ArgumentCaptor<Flow> flowArgumentCaptor;

    private SalBulkFlowServiceImpl salBulkFlowService;

    @Before
    public void setUp() throws Exception {
        when(mockDataBroker.newWriteOnlyTransaction()).thenReturn(wTx);
        when(mockDataBroker.newReadOnlyTransaction()).thenReturn(rTx);
        when(rTx.read(Mockito.any(LogicalDatastoreType.class), Mockito.<InstanceIdentifier<Node>>any()))
                .thenReturn(Futures.immediateCheckedFuture(Optional.of(mockNode)));
        salBulkFlowService = new SalBulkFlowServiceImpl(mockSalFlowService, mockDataBroker);
    }

    @Test
    public void testAddRemoveFlowsDs() throws Exception {
        Mockito.when(wTx.submit()).thenReturn(Futures.immediateCheckedFuture(null));

        final BulkFlowDsItemBuilder bulkFlowDsItemBuilder = new BulkFlowDsItemBuilder()
            .setFlowId(new FlowId("1"))
            .setTableId((short)2);

        final InstanceIdentifier<Node> nodeId = BulkOMaticUtils.getFlowCapableNodeId("1");
        bulkFlowDsItemBuilder.setNode(new NodeRef(nodeId));
        final BulkFlowDsItem bulkFlowDsItem = bulkFlowDsItemBuilder.build();

        final List<BulkFlowDsItem> bulkFlowDsItems = new ArrayList<>();
        bulkFlowDsItems.add(bulkFlowDsItem);

        final AddFlowsDsInputBuilder addFlowsDsInputBuilder = new AddFlowsDsInputBuilder();
        addFlowsDsInputBuilder.setBulkFlowDsItem(bulkFlowDsItems);

        final AddFlowsDsInput addFlowsDsInput = addFlowsDsInputBuilder.build();
        salBulkFlowService.addFlowsDs(addFlowsDsInput);

        verify(wTx).submit();
        verify(wTx).put(Matchers.<LogicalDatastoreType>any(), Matchers.<InstanceIdentifier<Flow>>any(), flowArgumentCaptor.capture(), Mockito.anyBoolean());

        Flow flow = flowArgumentCaptor.getValue();
        Assert.assertEquals("1", flow.getId().getValue());
        Assert.assertEquals((short) 2, flow.getTableId().shortValue());

        final RemoveFlowsDsInputBuilder removeFlowsDsInputBuilder = new RemoveFlowsDsInputBuilder();
        removeFlowsDsInputBuilder.setBulkFlowDsItem(bulkFlowDsItems);

        final RemoveFlowsDsInput removeFlowsDsInput = removeFlowsDsInputBuilder.build();

        salBulkFlowService.removeFlowsDs(removeFlowsDsInput);
        verify(wTx).delete(Matchers.<LogicalDatastoreType>any(), Matchers.<InstanceIdentifier<Flow>>any());
        verify(wTx,times(2)).submit();
    }

    @Test
    public void testAddRemoveFlowsRpc() throws Exception {
        Mockito.when(mockSalFlowService.addFlow(Matchers.<AddFlowInput>any()))
                .thenReturn(RpcResultBuilder.success(new AddFlowOutputBuilder().build()).buildFuture());

        Mockito.when(mockSalFlowService.removeFlow(Matchers.<RemoveFlowInput>any()))
                .thenReturn(RpcResultBuilder.success(new RemoveFlowOutputBuilder().build()).buildFuture());

        final BulkFlowItemBuilder bulkFlowItemBuilder = new BulkFlowItemBuilder();
        final InstanceIdentifier<Node> nodeId = BulkOMaticUtils.getFlowCapableNodeId("1");
        bulkFlowItemBuilder.setNode(new NodeRef(nodeId));
        final BulkFlowItem bulkFlowItem = bulkFlowItemBuilder.build();

        final List<BulkFlowItem> bulkFlowItems = new ArrayList<>();
        bulkFlowItems.add(bulkFlowItem);

        final AddFlowsRpcInputBuilder addFlowsRpcInputBuilder = new AddFlowsRpcInputBuilder();
        addFlowsRpcInputBuilder.setBulkFlowItem(bulkFlowItems);

        final AddFlowsRpcInput addFlowsRpcInput = addFlowsRpcInputBuilder.build();
        salBulkFlowService.addFlowsRpc(addFlowsRpcInput);

        verify(mockSalFlowService).addFlow(Matchers.<AddFlowInput>any());

        final RemoveFlowsRpcInputBuilder removeFlowsRpcInputBuilder = new RemoveFlowsRpcInputBuilder();
        removeFlowsRpcInputBuilder.setBulkFlowItem(bulkFlowItems);

        final RemoveFlowsRpcInput removeFlowsRpcInput = removeFlowsRpcInputBuilder.build();
        salBulkFlowService.removeFlowsRpc(removeFlowsRpcInput);

        verify(mockSalFlowService).removeFlow(Matchers.<RemoveFlowInput>any());
    }

    @Test
    public void testReadFlowTest() throws Exception {
        final ReadFlowTestInputBuilder readFlowTestInputBuilder = new ReadFlowTestInputBuilder()
            .setDpnCount(1L)
            .setStartTableId(1L)
            .setEndTableId(2L)
            .setIsConfigDs(false)
            .setFlowsPerDpn(1L)
            .setVerbose(true);

        final ReadFlowTestInput readFlowTestInput = readFlowTestInputBuilder.build();
        final Future<RpcResult<Void>> resultFuture = salBulkFlowService.readFlowTest(readFlowTestInput);

        Assert.assertTrue(resultFuture.get().isSuccessful());
    }

    @Test
    public void testFlowRpcAddTest() throws Exception {
        when(rTx.read(Mockito.any(LogicalDatastoreType.class), Mockito.<InstanceIdentifier<Nodes>>any()))
                .thenReturn(Futures.immediateCheckedFuture(Optional.of(mockNodes)));

        final FlowRpcAddTestInputBuilder flowRpcAddTestInputBuilder = new FlowRpcAddTestInputBuilder()
                .setFlowCount(1L)
                .setDpnId("1")
                .setRpcBatchSize(1L);

        final FlowRpcAddTestInput flowRpcAddTestInput = flowRpcAddTestInputBuilder.build();
        final Future<RpcResult<Void>> resultFuture = salBulkFlowService.flowRpcAddTest(flowRpcAddTestInput);

        Assert.assertTrue(resultFuture.get().isSuccessful());
    }

    @Test
    public void testFlowTest() throws Exception {
        final FlowTestInputBuilder flowTestInputBuilder = new FlowTestInputBuilder()
                .setBatchSize(1L)
                .setDpnCount(1L)
                .setEndTableId(2L)
                .setFlowsPerDpn(1L)
                .setIsAdd(true)
                .setSeq(true)
                .setSleepAfter(20L)
                .setSleepFor(1L)
                .setStartTableId(1L)
                .setTxChain(true)
                .setCreateParents(true);

        FlowTestInput flowTestInput = flowTestInputBuilder.build();

        Assert.assertTrue(salBulkFlowService.flowTest(flowTestInput).get().isSuccessful());

        flowTestInputBuilder.setIsAdd(false);
        flowTestInput = flowTestInputBuilder.build();

        Assert.assertTrue(salBulkFlowService.flowTest(flowTestInput).get().isSuccessful());

        flowTestInputBuilder.setTxChain(false);
        flowTestInput = flowTestInputBuilder.build();

        Assert.assertTrue(salBulkFlowService.flowTest(flowTestInput).get().isSuccessful());

        flowTestInputBuilder.setIsAdd(true);
        flowTestInput = flowTestInputBuilder.build();

        Assert.assertTrue(salBulkFlowService.flowTest(flowTestInput).get().isSuccessful());

        flowTestInputBuilder.setSeq(false);
        flowTestInput = flowTestInputBuilder.build();

        Assert.assertTrue(salBulkFlowService.flowTest(flowTestInput).get().isSuccessful());

        flowTestInputBuilder.setIsAdd(false);
        flowTestInput = flowTestInputBuilder.build();

        Assert.assertTrue(salBulkFlowService.flowTest(flowTestInput).get().isSuccessful());
    }

    @Test
    public void testFlowRpcAddMultiple() throws Exception {
        when(rTx.read(Mockito.any(LogicalDatastoreType.class), Mockito.<InstanceIdentifier<Nodes>>any()))
                .thenReturn(Futures.immediateCheckedFuture(Optional.of(mockNodes)));

        final FlowRpcAddMultipleInputBuilder flowRpcAddMultipleInputBuilder = new FlowRpcAddMultipleInputBuilder()
                .setFlowCount(1L)
                .setRpcBatchSize(1L);

        final FlowRpcAddMultipleInput flowRpcAddMultipleInput = flowRpcAddMultipleInputBuilder.build();

        Assert.assertTrue(salBulkFlowService.flowRpcAddMultiple(flowRpcAddMultipleInput).get().isSuccessful());
    }

    @Test
    public void testTableTest() throws Exception {
        final TableTestInputBuilder tableTestInputBuilder = new TableTestInputBuilder()
                .setStartTableId(0L)
                .setEndTableId(99L)
                .setDpnCount(1L)
                .setOperation(Operation.Add);

        TableTestInput tableTestInput = tableTestInputBuilder.build();

        Assert.assertTrue(salBulkFlowService.tableTest(tableTestInput).get().isSuccessful());

        tableTestInputBuilder.setOperation(Operation.Delete);
        tableTestInput = tableTestInputBuilder.build();

        Assert.assertTrue(salBulkFlowService.tableTest(tableTestInput).get().isSuccessful());
    }
}