/*
 * Copyright (c) 2016, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.bulk.o.matic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.AddFlowsDsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.AddFlowsDsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.AddFlowsRpcInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.AddFlowsRpcInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.FlowRpcAddMultipleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.FlowRpcAddMultipleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.FlowRpcAddTestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.FlowRpcAddTestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.FlowRpcAddTestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.FlowTestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.FlowTestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.ReadFlowTestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.ReadFlowTestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.ReadFlowTestOutput;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Test for {@link SalBulkFlowServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SalBulkFlowServiceImplTest {

    @Mock
    private DataBroker mockDataBroker;
    @Mock
    private SalFlowService mockSalFlowService;
    @Mock
    private WriteTransaction writeTransaction;
    @Mock
    private ReadTransaction readOnlyTransaction;
    @Mock
    private Nodes mockNodes;
    @Mock
    private Node mockNode;
    @Captor
    private ArgumentCaptor<Flow> flowArgumentCaptor;

    private SalBulkFlowServiceImpl salBulkFlowService;

    @Before
    public void setUp() {
        when(mockDataBroker.newWriteOnlyTransaction()).thenReturn(writeTransaction);
        when(mockDataBroker.newReadOnlyTransaction()).thenReturn(readOnlyTransaction);

        lenient().doReturn(FluentFutures.immediateFluentFuture(Optional.of(mockNode))).when(readOnlyTransaction)
            .read(any(LogicalDatastoreType.class), any());
        salBulkFlowService = new SalBulkFlowServiceImpl(mockSalFlowService, mockDataBroker);
    }

    @Test
    public void testAddRemoveFlowsDs() {
        doReturn(CommitInfo.emptyFluentFuture()).when(writeTransaction).commit();

        final BulkFlowDsItemBuilder bulkFlowDsItemBuilder = new BulkFlowDsItemBuilder().setFlowId(new FlowId("1"))
                .setTableId(Uint8.TWO);

        final InstanceIdentifier<Node> nodeId = BulkOMaticUtils.getFlowCapableNodeId("1");
        bulkFlowDsItemBuilder.setNode(new NodeRef(nodeId));
        final BulkFlowDsItem bulkFlowDsItem = bulkFlowDsItemBuilder.build();

        final List<BulkFlowDsItem> bulkFlowDsItems = new ArrayList<>();
        bulkFlowDsItems.add(bulkFlowDsItem);

        final AddFlowsDsInputBuilder addFlowsDsInputBuilder = new AddFlowsDsInputBuilder();
        addFlowsDsInputBuilder.setBulkFlowDsItem(bulkFlowDsItems);

        final AddFlowsDsInput addFlowsDsInput = addFlowsDsInputBuilder.build();
        salBulkFlowService.addFlowsDs(addFlowsDsInput);

        verify(writeTransaction).commit();
        verify(writeTransaction).mergeParentStructurePut(ArgumentMatchers.any(), ArgumentMatchers.any(),
                flowArgumentCaptor.capture());

        Flow flow = flowArgumentCaptor.getValue();
        Assert.assertEquals("1", flow.getId().getValue());
        Assert.assertEquals((short) 2, flow.getTableId().shortValue());

        final RemoveFlowsDsInputBuilder removeFlowsDsInputBuilder = new RemoveFlowsDsInputBuilder();
        removeFlowsDsInputBuilder.setBulkFlowDsItem(bulkFlowDsItems);

        final RemoveFlowsDsInput removeFlowsDsInput = removeFlowsDsInputBuilder.build();

        salBulkFlowService.removeFlowsDs(removeFlowsDsInput);
        verify(writeTransaction).delete(ArgumentMatchers.any(),
                ArgumentMatchers.<InstanceIdentifier<Flow>>any());
        verify(writeTransaction, times(2)).commit();
    }

    @Test
    public void testAddRemoveFlowsRpc() {
        Mockito.when(mockSalFlowService.addFlow(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new AddFlowOutputBuilder().build()).buildFuture());

        Mockito.when(mockSalFlowService.removeFlow(ArgumentMatchers.any()))
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

        verify(mockSalFlowService).addFlow(ArgumentMatchers.any());

        final RemoveFlowsRpcInputBuilder removeFlowsRpcInputBuilder = new RemoveFlowsRpcInputBuilder();
        removeFlowsRpcInputBuilder.setBulkFlowItem(bulkFlowItems);

        final RemoveFlowsRpcInput removeFlowsRpcInput = removeFlowsRpcInputBuilder.build();
        salBulkFlowService.removeFlowsRpc(removeFlowsRpcInput);

        verify(mockSalFlowService).removeFlow(ArgumentMatchers.any());
    }

    @Test
    public void testReadFlowTest() throws Exception {
        final ReadFlowTestInputBuilder readFlowTestInputBuilder = new ReadFlowTestInputBuilder().setDpnCount(Uint32.ONE)
                .setStartTableId(Uint32.ONE).setEndTableId(Uint32.TWO).setIsConfigDs(false).setFlowsPerDpn(Uint32.ONE)
                .setVerbose(true);

        final ReadFlowTestInput readFlowTestInput = readFlowTestInputBuilder.build();
        final ListenableFuture<RpcResult<ReadFlowTestOutput>> resultFuture
                = salBulkFlowService.readFlowTest(readFlowTestInput);

        Assert.assertTrue(resultFuture.get().isSuccessful());
    }

    @Test
    public void testFlowRpcAddTest() throws Exception {
        doReturn(FluentFutures.immediateFluentFuture(Optional.of(mockNodes))).when(readOnlyTransaction)
            .read(any(LogicalDatastoreType.class), any());

        final FlowRpcAddTestInputBuilder flowRpcAddTestInputBuilder = new FlowRpcAddTestInputBuilder()
                .setFlowCount(Uint32.ONE).setDpnId("1").setRpcBatchSize(Uint32.ONE);

        final FlowRpcAddTestInput flowRpcAddTestInput = flowRpcAddTestInputBuilder.build();
        final ListenableFuture<RpcResult<FlowRpcAddTestOutput>> resultFuture
                = salBulkFlowService.flowRpcAddTest(flowRpcAddTestInput);

        Assert.assertTrue(resultFuture.get().isSuccessful());
    }

    @Test
    public void testFlowTest() throws Exception {
        final FlowTestInputBuilder flowTestInputBuilder = new FlowTestInputBuilder()
                .setBatchSize(Uint32.ONE).setDpnCount(Uint32.ONE).setEndTableId(Uint32.TWO).setFlowsPerDpn(Uint32.ONE)
                .setIsAdd(true).setSeq(true).setSleepAfter(Uint32.valueOf(20)).setSleepFor(Uint32.ONE)
                .setStartTableId(Uint32.ONE).setTxChain(true).setCreateParents(true);

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
        doReturn(FluentFutures.immediateFluentFuture(Optional.of(mockNodes))).when(readOnlyTransaction)
            .read(any(LogicalDatastoreType.class), any());

        final FlowRpcAddMultipleInputBuilder flowRpcAddMultipleInputBuilder = new FlowRpcAddMultipleInputBuilder()
                .setFlowCount(Uint32.ONE).setRpcBatchSize(Uint32.ONE);

        final FlowRpcAddMultipleInput flowRpcAddMultipleInput = flowRpcAddMultipleInputBuilder.build();

        Assert.assertTrue(salBulkFlowService.flowRpcAddMultiple(flowRpcAddMultipleInput).get().isSuccessful());
    }

    @Test
    public void testTableTest() throws Exception {
        final TableTestInputBuilder tableTestInputBuilder = new TableTestInputBuilder().setStartTableId(Uint32.ZERO)
                .setEndTableId(Uint32.valueOf(99)).setDpnCount(Uint32.ONE).setOperation(Operation.Add);

        TableTestInput tableTestInput = tableTestInputBuilder.build();

        Assert.assertTrue(salBulkFlowService.tableTest(tableTestInput).get().isSuccessful());

        tableTestInputBuilder.setOperation(Operation.Delete);
        tableTestInput = tableTestInputBuilder.build();

        Assert.assertTrue(salBulkFlowService.tableTest(tableTestInput).get().isSuccessful());
    }
}
