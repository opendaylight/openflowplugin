/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.Futures;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeCachedDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeOdlDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeSnapshotDao;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconciliationRegistry;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableStatisticsGatheringStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.snapshot.gathering.status.grouping.SnapshotGatheringStatusEnd;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Test for {@link SimplifiedOperationalListener}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SimplifiedOperationalListenerTest {

    private static final NodeId NODE_ID = new NodeId("testNode");
    private InstanceIdentifier<FlowCapableNode> fcNodePath;
    private SimplifiedOperationalListener nodeListenerOperational;
    private final LogicalDatastoreType dsType = LogicalDatastoreType.OPERATIONAL;
    private final String timestampBefore = "0000-12-12T01:01:01.000-07:00";
    private final String timestampAfter = "9999-12-12T01:01:01.000-07:00";
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ReconciliationRegistry.DATE_AND_TIME_FORMAT);

    @Mock
    private SyncReactor reactor;
    @Mock
    private ReadOnlyTransaction roTx;
    @Mock
    private DataTreeModification<Node> dataTreeModification;
    @Mock
    private DataObjectModification<Node> operationalModification;
    @Mock
    private FlowCapableNode configNode;
    @Mock
    private Node operationalNode;
    @Mock
    private FlowCapableNode fcOperationalNode;
    @Mock
    private ReconciliationRegistry reconciliationRegistry;
    @Mock
    private FlowCapableStatisticsGatheringStatus statisticsGatheringStatus;
    @Mock
    private SnapshotGatheringStatusEnd snapshotGatheringStatusEnd;

    @Before
    public void setUp() throws Exception {
        final DataBroker db = Mockito.mock(DataBroker.class);
        final FlowCapableNodeSnapshotDao configSnapshot = new FlowCapableNodeSnapshotDao();
        final FlowCapableNodeSnapshotDao operationalSnapshot = new FlowCapableNodeSnapshotDao();
        final FlowCapableNodeDao configDao = new FlowCapableNodeCachedDao(configSnapshot,
                new FlowCapableNodeOdlDao(db, LogicalDatastoreType.CONFIGURATION));

        nodeListenerOperational = new SimplifiedOperationalListener(reactor, operationalSnapshot, configDao, reconciliationRegistry);
        InstanceIdentifier<Node> nodePath = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(NODE_ID));
        fcNodePath = nodePath.augmentation(FlowCapableNode.class);

        final DataTreeIdentifier<Node> dataTreeIdentifier =
                new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL, nodePath);

        Mockito.when(db.newReadOnlyTransaction()).thenReturn(roTx);
        Mockito.when(operationalNode.getId()).thenReturn(NODE_ID);
        Mockito.when(dataTreeModification.getRootPath()).thenReturn(dataTreeIdentifier);
        Mockito.when(dataTreeModification.getRootNode()).thenReturn(operationalModification);
        Mockito.when(operationalNode.getAugmentation(FlowCapableNode.class)).thenReturn(fcOperationalNode);
    }

    @Test
    public void testDSLogicalType() throws Exception {
        Assert.assertEquals(LogicalDatastoreType.OPERATIONAL, nodeListenerOperational.dsType());
    }

    @Test
    public void testOnDataTreeChangedSyncupAdd() throws InterruptedException {
        Mockito.when(roTx.read(LogicalDatastoreType.CONFIGURATION, fcNodePath))
                .thenReturn(Futures.immediateCheckedFuture(Optional.of(configNode)));
        Mockito.when(reactor.syncup(Matchers.<InstanceIdentifier<FlowCapableNode>>any(), Matchers.<FlowCapableNode>any(),
                Matchers.<FlowCapableNode>any(), Matchers.<LogicalDatastoreType>any())).thenReturn(Futures.immediateFuture(Boolean.TRUE));
        Mockito.when(operationalModification.getDataAfter()).thenReturn(operationalNode);

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verify(reactor).syncup(fcNodePath, configNode, fcOperationalNode, dsType);
        Mockito.verifyNoMoreInteractions(reactor);
        Mockito.verify(roTx).close();
    }

    @Test
    public void testOnDataTreeChangedAddSkip() {
        // Related to bug 5920 -> https://bugs.opendaylight.org/show_bug.cgi?id=5920
        Mockito.when(roTx.read(LogicalDatastoreType.CONFIGURATION, fcNodePath))
                .thenReturn(Futures.immediateCheckedFuture(Optional.absent()));
        Mockito.when(operationalModification.getDataAfter()).thenReturn(operationalNode);

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verifyZeroInteractions(reactor);
        Mockito.verify(roTx).close();
    }

    @Test
    public void testOnDataTreeChangedSyncupDeletePhysical() {
        Mockito.when(operationalModification.getDataBefore()).thenReturn(operationalNode);
        Mockito.when(dataTreeModification.getRootNode().getModificationType()).thenReturn(ModificationType.DELETE);

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verifyZeroInteractions(reactor);
    }

    @Test
    public void testOnDataTreeChangedSyncupDeleteLogical() {
        Mockito.when(operationalModification.getDataBefore()).thenReturn(operationalNode);
        List<NodeConnector> nodeConnectorList = Mockito.mock(List.class);
        Mockito.when(operationalNode.getNodeConnector()).thenReturn(nodeConnectorList);

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verifyZeroInteractions(reactor);
    }

    @Test
    public void testOnDataTreeChangedReconcileNotRegistered() {
        Mockito.when(operationalModification.getDataBefore()).thenReturn(operationalNode);
        Mockito.when(operationalModification.getDataAfter()).thenReturn(operationalNode);
        Mockito.when(reconciliationRegistry.isRegistered(NODE_ID)).thenReturn(false);

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verifyZeroInteractions(reactor);
    }

    @Test
    public void testOnDataTreeChangedReconcileButStaticsGatheringNotStarted() {
        Mockito.when(operationalModification.getDataBefore()).thenReturn(operationalNode);
        Mockito.when(operationalModification.getDataAfter()).thenReturn(operationalNode);
        Mockito.when(reconciliationRegistry.isRegistered(NODE_ID)).thenReturn(true);
        Mockito.when(operationalNode.getAugmentation(FlowCapableStatisticsGatheringStatus.class)).thenReturn(null);

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verifyZeroInteractions(reactor);
    }

    @Test
    public void testOnDataTreeChangedReconcileButStaticsGatheringNotFinished() {
        Mockito.when(operationalModification.getDataBefore()).thenReturn(operationalNode);
        Mockito.when(operationalModification.getDataAfter()).thenReturn(operationalNode);
        Mockito.when(reconciliationRegistry.isRegistered(NODE_ID)).thenReturn(true);
        Mockito.when(operationalNode.getAugmentation(FlowCapableStatisticsGatheringStatus.class)).thenReturn(statisticsGatheringStatus);
        Mockito.when(statisticsGatheringStatus.getSnapshotGatheringStatusEnd()).thenReturn(null);

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verifyZeroInteractions(reactor);
    }

    @Test
    public void testOnDataTreeChangedReconcileButStaticsGatheringNotSuccessful() {
        Mockito.when(operationalModification.getDataBefore()).thenReturn(operationalNode);
        Mockito.when(operationalModification.getDataAfter()).thenReturn(operationalNode);
        Mockito.when(reconciliationRegistry.isRegistered(NODE_ID)).thenReturn(true);
        Mockito.when(operationalNode.getAugmentation(FlowCapableStatisticsGatheringStatus.class)).thenReturn(statisticsGatheringStatus);
        Mockito.when(statisticsGatheringStatus.getSnapshotGatheringStatusEnd()).thenReturn(snapshotGatheringStatusEnd);
        Mockito.when(snapshotGatheringStatusEnd.isSucceeded()).thenReturn(false);

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verifyZeroInteractions(reactor);
    }

    @Test
    public void testOnDataTreeChangedReconcileAndFreshOperationalNotPresent() throws ParseException {
        final DateAndTime timestamp = Mockito.mock(DateAndTime.class);
        Mockito.when(operationalModification.getDataBefore()).thenReturn(operationalNode);
        Mockito.when(operationalModification.getDataAfter()).thenReturn(operationalNode);
        Mockito.when(reconciliationRegistry.isRegistered(NODE_ID)).thenReturn(true);
        Mockito.when(operationalNode.getAugmentation(FlowCapableStatisticsGatheringStatus.class)).thenReturn(statisticsGatheringStatus);
        Mockito.when(statisticsGatheringStatus.getSnapshotGatheringStatusEnd()).thenReturn(snapshotGatheringStatusEnd);
        Mockito.when(snapshotGatheringStatusEnd.isSucceeded()).thenReturn(true);
        Mockito.when(snapshotGatheringStatusEnd.getEnd()).thenReturn(timestamp);
        Mockito.when(snapshotGatheringStatusEnd.getEnd().getValue()).thenReturn(timestampBefore);
        Mockito.when(reconciliationRegistry.getRegistration(NODE_ID)).thenReturn(simpleDateFormat.parse(timestampAfter));

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verifyZeroInteractions(reactor);
    }

    @Test
    public void testOnDataTreeChangedReconcileAndFreshOperationalPresent() throws Exception {
        final DateAndTime timestamp = Mockito.mock(DateAndTime.class);
        Mockito.when(roTx.read(LogicalDatastoreType.CONFIGURATION, fcNodePath))
                .thenReturn(Futures.immediateCheckedFuture(Optional.of(configNode)));
        Mockito.when(reactor.syncup(Matchers.<InstanceIdentifier<FlowCapableNode>>any(), Matchers.<FlowCapableNode>any(),
                Matchers.<FlowCapableNode>any(), Matchers.<LogicalDatastoreType>any())).thenReturn(Futures.immediateFuture(Boolean.TRUE));
        Mockito.when(operationalModification.getDataBefore()).thenReturn(operationalNode);
        Mockito.when(operationalModification.getDataAfter()).thenReturn(operationalNode);
        Mockito.when(reconciliationRegistry.isRegistered(NODE_ID)).thenReturn(true);
        Mockito.when(operationalNode.getAugmentation(FlowCapableStatisticsGatheringStatus.class)).thenReturn(statisticsGatheringStatus);
        Mockito.when(statisticsGatheringStatus.getSnapshotGatheringStatusEnd()).thenReturn(snapshotGatheringStatusEnd);
        Mockito.when(snapshotGatheringStatusEnd.isSucceeded()).thenReturn(true);
        Mockito.when(snapshotGatheringStatusEnd.getEnd()).thenReturn(timestamp);
        Mockito.when(snapshotGatheringStatusEnd.getEnd().getValue()).thenReturn(timestampAfter);
        Mockito.when(reconciliationRegistry.getRegistration(NODE_ID)).thenReturn(simpleDateFormat.parse(timestampBefore));

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verify(reactor).syncup(fcNodePath, configNode, fcOperationalNode, dsType);
        Mockito.verifyNoMoreInteractions(reactor);
        Mockito.verify(roTx).close();
    }

    @Test
    public void testOnDataTreeChangedReconcileAndNodeDeleted() {
        Mockito.when(operationalModification.getDataBefore()).thenReturn(operationalNode);
        Mockito.when(dataTreeModification.getRootNode().getModificationType()).thenReturn(DataObjectModification.ModificationType.DELETE);

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verify(reconciliationRegistry).unregisterIfRegistered(NODE_ID);
        Mockito.verifyZeroInteractions(reactor);
    }
}
