/*
 * Copyright (c) 2015 Tata Consultancy services and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.statistics.manager.impl;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.statistics.manager.StatNodeRegistration;
import org.opendaylight.openflowplugin.applications.statistics.manager.StatisticsManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowHashIdMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowHashIdMappingBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.nodes.node.table.FlowHashIdMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.nodes.node.table.FlowHashIdMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.nodes.node.table.FlowHashIdMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.Futures;

/**
 * Unit tests for StatListenCommitFlow.
 * 
 * @author Monika Verma
 */
public class StatListenCommitFlowTest {

    @Mock
    private NotificationProviderService mockNotificationProviderService;

    @Mock
    private StatisticsManager mockStatisticsManager;

    @Mock
    private DataBroker mockDataBroker;

    @Mock
    private StatNodeRegistration statsNodeRegistration;

    private StatListenCommitFlow statCommitFlow;
    private TableKey tableKey = new TableKey((short) 12);

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        statCommitFlow = new StatListenCommitFlow(mockStatisticsManager, mockDataBroker,
                mockNotificationProviderService, statsNodeRegistration);
    }

    @Test
    public void testStatsFlowCommitAllWithAlienSystemFlowId() throws InvocationTargetException {
        Class[] argClasses = { List.class, InstanceIdentifier.class, ReadWriteTransaction.class };

        List<FlowAndStatisticsMapList> flowStats = new ArrayList<FlowAndStatisticsMapList>();
        flowStats.add(createFlowAndStatisticsMapList());

        FlowCapableNode flowCapableNode = createFlowCapableNode("#UF$TABLE*F1");

        FlowsStatisticsUpdate flowsStatisticsUpdate = createFlowsStatisticsUpdate();

        InstanceIdentifier<Node> nodeIdent = InstanceIdentifier.create(Nodes.class).child(Node.class,
                new NodeKey(flowsStatisticsUpdate.getId()));

        final InstanceIdentifier<FlowCapableNode> fNodeIdent = nodeIdent.augmentation(FlowCapableNode.class);

        InstanceIdentifier<Table> path = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(flowsStatisticsUpdate.getId())).augmentation(FlowCapableNode.class)
                .child(Table.class, tableKey);

        ReadWriteTransaction mockReadWriteTx = mock(ReadWriteTransaction.class);
        doReturn(mockReadWriteTx).when(mockDataBroker).newReadWriteTransaction();
        Optional<FlowCapableNode> expected = Optional.of(flowCapableNode);
        doReturn(Futures.immediateCheckedFuture(expected)).when(mockReadWriteTx).read(LogicalDatastoreType.OPERATIONAL,
                fNodeIdent);

        ReadOnlyTransaction mockReadTx = mock(ReadOnlyTransaction.class);
        doReturn(mockReadTx).when(mockDataBroker).newReadOnlyTransaction();
        Optional<Table> expected1 = Optional.of(flowCapableNode.getTable().get(0));
        doReturn(Futures.immediateCheckedFuture(expected1)).when(mockReadTx).read(LogicalDatastoreType.CONFIGURATION,
                path);

        Object[] argObjects = { flowStats, nodeIdent, mockReadWriteTx };

        Method method;
        try {
            method = StatListenCommitFlow.class.getDeclaredMethod("statsFlowCommitAll", argClasses);
            method.setAccessible(true);
            method.invoke(statCommitFlow, argObjects);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getCause().toString());
        }
    }

    private FlowsStatisticsUpdate createFlowsStatisticsUpdate() {
        return new FlowsStatisticsUpdateBuilder().setId(new NodeId("S1"))
                .setTransactionId(new TransactionId(new java.math.BigInteger("18446744073709551615")))
                .setMoreReplies(false).build();
    }

    private FlowAndStatisticsMapList createFlowAndStatisticsMapList() {
        return new FlowAndStatisticsMapListBuilder().setFlowName("testFlow").setFlowId(new FlowId("F1"))
                .setTableId(tableKey.getId()).setMatch(new MatchBuilder().build()).setPriority(2)
                .setCookie(new FlowCookie(new java.math.BigInteger("18446744073709551615"))).build();
    }

    private FlowCapableNode createFlowCapableNode(String flowId) {

        List<Table> tableList = new ArrayList<Table>();
        List<Flow> flowList = new ArrayList<Flow>();
        flowList.add(new FlowBuilder().setFlowName("testFlow")
                .setId(new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId("F1")).build());
        List<FlowHashIdMap> flowHashIdMapList = new ArrayList<FlowHashIdMap>();
        flowHashIdMapList.add(new FlowHashIdMapBuilder()
                .setFlowId(new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId(flowId))
                .setHash("TestFlow").setKey(new FlowHashIdMapKey("FM1")).build());
        tableList.add(new TableBuilder()
                .setFlow(flowList)
                .setId((short) 12)
                .setKey(tableKey)
                .addAugmentation(FlowHashIdMapping.class,
                        new FlowHashIdMappingBuilder().setFlowHashIdMap(flowHashIdMapList).build()).build());

        return new FlowCapableNodeBuilder().setDescription("test").setTable(tableList).build();
    }

    @Test
    public void testStatsFlowCommitAll() throws InvocationTargetException {
        Class[] argClasses = { List.class, InstanceIdentifier.class, ReadWriteTransaction.class };

        List<FlowAndStatisticsMapList> flowStats = new ArrayList<FlowAndStatisticsMapList>();
        flowStats.add(createFlowAndStatisticsMapList());

        FlowCapableNode flowCapableNode = createFlowCapableNode("F1");

        FlowsStatisticsUpdate flowsStatisticsUpdate = createFlowsStatisticsUpdate();

        InstanceIdentifier<Node> nodeIdent = InstanceIdentifier.create(Nodes.class).child(Node.class,
                new NodeKey(flowsStatisticsUpdate.getId()));

        final InstanceIdentifier<FlowCapableNode> fNodeIdent = nodeIdent.augmentation(FlowCapableNode.class);

        InstanceIdentifier<Table> path = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(flowsStatisticsUpdate.getId())).augmentation(FlowCapableNode.class)
                .child(Table.class, tableKey);

        ReadWriteTransaction mockReadWriteTx = mock(ReadWriteTransaction.class);
        doReturn(mockReadWriteTx).when(mockDataBroker).newReadWriteTransaction();
        Optional<FlowCapableNode> expected = Optional.of(flowCapableNode);
        doReturn(Futures.immediateCheckedFuture(expected)).when(mockReadWriteTx).read(LogicalDatastoreType.OPERATIONAL,
                fNodeIdent);

        ReadOnlyTransaction mockReadTx = mock(ReadOnlyTransaction.class);
        doReturn(mockReadTx).when(mockDataBroker).newReadOnlyTransaction();
        Optional<Table> expected1 = Optional.of(flowCapableNode.getTable().get(0));
        doReturn(Futures.immediateCheckedFuture(expected1)).when(mockReadTx).read(LogicalDatastoreType.CONFIGURATION,
                path);

        Object[] argObjects = { flowStats, nodeIdent, mockReadWriteTx };

        Method method;
        try {
            method = StatListenCommitFlow.class.getDeclaredMethod("statsFlowCommitAll", argClasses);
            method.setAccessible(true);
            method.invoke(statCommitFlow, argObjects);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getCause().toString());
        }
    }

    @Test
    public void testStatsFlowCommitAllWithDefaultAlienSystemFlowId() throws InvocationTargetException {
        Class[] argClasses = { List.class, InstanceIdentifier.class, ReadWriteTransaction.class };

        List<FlowAndStatisticsMapList> flowStats = new ArrayList<FlowAndStatisticsMapList>();
        flowStats.add(createFlowAndStatisticsMapList());

        FlowCapableNode flowCapableNode = createFlowCapableNode("#UF$TABLE*12-1");

        FlowsStatisticsUpdate flowsStatisticsUpdate = createFlowsStatisticsUpdate();

        InstanceIdentifier<Node> nodeIdent = InstanceIdentifier.create(Nodes.class).child(Node.class,
                new NodeKey(flowsStatisticsUpdate.getId()));

        final InstanceIdentifier<FlowCapableNode> fNodeIdent = nodeIdent.augmentation(FlowCapableNode.class);

        InstanceIdentifier<Table> path = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(flowsStatisticsUpdate.getId())).augmentation(FlowCapableNode.class)
                .child(Table.class, tableKey);

        ReadWriteTransaction mockReadWriteTx = mock(ReadWriteTransaction.class);
        doReturn(mockReadWriteTx).when(mockDataBroker).newReadWriteTransaction();
        Optional<FlowCapableNode> expected = Optional.of(flowCapableNode);
        doReturn(Futures.immediateCheckedFuture(expected)).when(mockReadWriteTx).read(LogicalDatastoreType.OPERATIONAL,
                fNodeIdent);

        ReadOnlyTransaction mockReadTx = mock(ReadOnlyTransaction.class);
        doReturn(mockReadTx).when(mockDataBroker).newReadOnlyTransaction();
        Optional<Table> expected1 = Optional.of(flowCapableNode.getTable().get(0));
        doReturn(Futures.immediateCheckedFuture(expected1)).when(mockReadTx).read(LogicalDatastoreType.CONFIGURATION,
                path);

        Object[] argObjects = { flowStats, nodeIdent, mockReadWriteTx };

        Method method;
        try {
            method = StatListenCommitFlow.class.getDeclaredMethod("statsFlowCommitAll", argClasses);
            method.setAccessible(true);
            method.invoke(statCommitFlow, argObjects);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getCause().toString());
        }
    }
}
