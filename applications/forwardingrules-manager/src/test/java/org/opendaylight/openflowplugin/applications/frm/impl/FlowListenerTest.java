/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Dscp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.StaleFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.StaleFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.StaleFlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

@RunWith(MockitoJUnitRunner.class)
public class FlowListenerTest extends AbstractFRMTest {
    private static final NodeId NODE_ID = new NodeId("testnode:1");
    private static final NodeKey NODE_KEY = new NodeKey(NODE_ID);
    TableKey tableKey = new TableKey(Uint8.TWO);

    @Before
    public void setUp() {
        setUpForwardingRulesManager();
        setDeviceMastership(NODE_ID);
    }

    @Test
    public void addTwoFlowsTest() {
        addFlowCapableNode(NODE_KEY);

        FlowKey flowKey = new FlowKey(new FlowId("test_Flow"));
        final var tableII = DataObjectIdentifier.builder(Nodes.class).child(Node.class, NODE_KEY)
            .augmentation(FlowCapableNode.class).child(Table.class, tableKey).build();
        var flowII = DataObjectIdentifier.builder(Nodes.class).child(Node.class, NODE_KEY)
            .augmentation(FlowCapableNode.class).child(Table.class, tableKey).child(Flow.class, flowKey).build();
        Table table = new TableBuilder().withKey(tableKey).build();
        Flow flow = new FlowBuilder().withKey(flowKey).setTableId(Uint8.TWO).build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, tableII, table);
        writeTx.put(LogicalDatastoreType.CONFIGURATION, flowII, flow);
        assertCommit(writeTx.commit());
        await().atMost(10, SECONDS).until(() -> addFlow.calls.size() == 1);
        var addFlowCalls = addFlow.calls;
        assertEquals(1, addFlowCalls.size());
        assertEquals("DOM-0", addFlowCalls.get(0).getTransactionUri().getValue());

        flowKey = new FlowKey(new FlowId("test_Flow2"));
        flowII = DataObjectIdentifier.builder(Nodes.class).child(Node.class, NODE_KEY)
            .augmentation(FlowCapableNode.class).child(Table.class, tableKey).child(Flow.class, flowKey).build();
        flow = new FlowBuilder().withKey(flowKey).setTableId(Uint8.TWO).build();
        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, flowII, flow);

        assertCommit(writeTx.commit());
        await().atMost(10, SECONDS).until(() -> addFlow.calls.size() == 2);
        addFlowCalls = addFlow.calls;
        assertEquals(2, addFlowCalls.size());
        assertEquals("DOM-1", addFlowCalls.get(1).getTransactionUri().getValue());
        assertEquals(2, addFlowCalls.get(1).getTableId().intValue());
        assertEquals(flowII, addFlowCalls.get(1).getFlowRef().getValue());
    }

    @Test
    public void updateFlowTest() {
        addFlowCapableNode(NODE_KEY);

        FlowKey flowKey = new FlowKey(new FlowId("test_Flow"));
        final var tableII = DataObjectIdentifier.builder(Nodes.class).child(Node.class, NODE_KEY)
            .augmentation(FlowCapableNode.class).child(Table.class, tableKey).build();
        var flowII = DataObjectIdentifier.builder(Nodes.class).child(Node.class, NODE_KEY)
            .augmentation(FlowCapableNode.class).child(Table.class, tableKey).child(Flow.class, flowKey).build();
        Table table = new TableBuilder().withKey(tableKey).build();
        Flow flow = new FlowBuilder().withKey(flowKey).setTableId(Uint8.TWO).build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, tableII, table);
        writeTx.put(LogicalDatastoreType.CONFIGURATION, flowII, flow);
        assertCommit(writeTx.commit());
        await().atMost(10, SECONDS).until(() -> addFlow.calls.size() == 1);

        final var addFlowCalls = addFlow.calls;
        assertEquals(1, addFlowCalls.size());
        assertEquals("DOM-0", addFlowCalls.get(0).getTransactionUri().getValue());

        flowKey = new FlowKey(new FlowId("test_Flow"));
        flowII = DataObjectIdentifier.builder(Nodes.class).child(Node.class, NODE_KEY)
            .augmentation(FlowCapableNode.class).child(Table.class, tableKey).child(Flow.class, flowKey).build();
        flow = new FlowBuilder().withKey(flowKey).setTableId(Uint8.TWO).setOutGroup(Uint32.valueOf(5)).build();
        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, flowII, flow);
        assertCommit(writeTx.commit());
        await().atMost(10, SECONDS).until(() -> updateFlow.calls.size() == 1);
        final var updateFlowCalls = updateFlow.calls;
        assertEquals(1, updateFlowCalls.size());
        assertEquals("DOM-1", updateFlowCalls.get(0).getTransactionUri().getValue());
        assertEquals(flowII, updateFlowCalls.get(0).getFlowRef().getValue());
        assertEquals(Boolean.TRUE, updateFlowCalls.get(0).getOriginalFlow().getStrict());
        assertEquals(Boolean.TRUE, updateFlowCalls.get(0).getUpdatedFlow().getStrict());
    }

    @Test
    public void updateFlowScopeTest() {
        addFlowCapableNode(NODE_KEY);

        FlowKey flowKey = new FlowKey(new FlowId("test_Flow"));
        final var tableII = DataObjectIdentifier.builder(Nodes.class).child(Node.class, NODE_KEY)
            .augmentation(FlowCapableNode.class).child(Table.class, tableKey).build();
        var flowII = DataObjectIdentifier.builder(Nodes.class).child(Node.class, NODE_KEY)
            .augmentation(FlowCapableNode.class).child(Table.class, tableKey).child(Flow.class, flowKey).build();
        Table table = new TableBuilder().withKey(tableKey).build();
        IpMatch ipMatch = new IpMatchBuilder().setIpDscp(new Dscp(Uint8.valueOf(4))).build();
        Match match = new MatchBuilder().setIpMatch(ipMatch).build();
        Flow flow = new FlowBuilder().setMatch(match).withKey(flowKey).setTableId(Uint8.TWO).build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, tableII, table);
        writeTx.put(LogicalDatastoreType.CONFIGURATION, flowII, flow);
        assertCommit(writeTx.commit());
        await().atMost(10, SECONDS).until(() -> addFlow.calls.size() == 1);
        final var addFlowCalls = addFlow.calls;
        assertEquals(1, addFlowCalls.size());
        assertEquals("DOM-0", addFlowCalls.get(0).getTransactionUri().getValue());

        flowKey = new FlowKey(new FlowId("test_Flow"));
        flowII = DataObjectIdentifier.builder(Nodes.class).child(Node.class, NODE_KEY)
            .augmentation(FlowCapableNode.class).child(Table.class, tableKey).child(Flow.class, flowKey).build();
        ipMatch = new IpMatchBuilder().setIpDscp(new Dscp(Uint8.valueOf(5))).build();
        match = new MatchBuilder().setIpMatch(ipMatch).build();
        flow = new FlowBuilder().setMatch(match).withKey(flowKey).setTableId(Uint8.TWO).build();
        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, flowII, flow);
        assertCommit(writeTx.commit());
        await().atMost(10, SECONDS).until(() -> updateFlow.calls.size() == 1);
        final var updateFlowCalls = updateFlow.calls;
        assertEquals(1, updateFlowCalls.size());
        assertEquals("DOM-1", updateFlowCalls.get(0).getTransactionUri().getValue());
        assertEquals(flowII, updateFlowCalls.get(0).getFlowRef().getValue());
        assertEquals(ipMatch, updateFlowCalls.get(0).getUpdatedFlow().getMatch().getIpMatch());
    }

    @Test
    public void deleteFlowTest() {
        addFlowCapableNode(NODE_KEY);

        FlowKey flowKey = new FlowKey(new FlowId("test_Flow"));
        var tableII = DataObjectIdentifier.builder(Nodes.class).child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class).child(Table.class, tableKey).build();
        var flowII = DataObjectIdentifier.builder(Nodes.class).child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class).child(Table.class, tableKey).child(Flow.class, flowKey).build();
        Table table = new TableBuilder().withKey(tableKey).build();
        Flow flow = new FlowBuilder().withKey(flowKey).setTableId(Uint8.TWO).build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, tableII, table);
        writeTx.put(LogicalDatastoreType.CONFIGURATION, flowII, flow);
        assertCommit(writeTx.commit());
        await().atMost(10, SECONDS).until(() -> addFlow.calls.size() == 1);
        final var addFlowCalls = addFlow.calls;
        assertEquals(1, addFlowCalls.size());
        assertEquals("DOM-0", addFlowCalls.get(0).getTransactionUri().getValue());

        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.delete(LogicalDatastoreType.CONFIGURATION, flowII);
        assertCommit(writeTx.commit());
        await().atMost(10, SECONDS).until(() -> removeFlow.calls.size() == 1);
        final var removeFlowCalls = removeFlow.calls;
        assertEquals(1, removeFlowCalls.size());
        assertEquals("DOM-1", removeFlowCalls.get(0).getTransactionUri().getValue());
        assertEquals(flowII, removeFlowCalls.get(0).getFlowRef().getValue());
        assertEquals(Boolean.TRUE, removeFlowCalls.get(0).getStrict());
    }

    @Test
    public void staleMarkedFlowCreationTest() throws Exception {
        addFlowCapableNode(NODE_KEY);

        StaleFlowKey flowKey = new StaleFlowKey(new FlowId("stale_Flow"));
        final var tableII = DataObjectIdentifier.builder(Nodes.class).child(Node.class, NODE_KEY)
            .augmentation(FlowCapableNode.class).child(Table.class, tableKey).build();
        final var flowII = DataObjectIdentifier.builder(Nodes.class).child(Node.class, NODE_KEY)
            .augmentation(FlowCapableNode.class).child(Table.class, tableKey).child(StaleFlow.class, flowKey).build();
        Table table = new TableBuilder().withKey(tableKey).build();
        StaleFlow flow = new StaleFlowBuilder().withKey(flowKey).setTableId(Uint8.TWO).build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, tableII, table);
        writeTx.put(LogicalDatastoreType.CONFIGURATION, flowII, flow);
        assertCommit(writeTx.commit());
    }

    @After
    public void tearDown() throws Exception {
        getForwardingRulesManager().close();
    }
}
