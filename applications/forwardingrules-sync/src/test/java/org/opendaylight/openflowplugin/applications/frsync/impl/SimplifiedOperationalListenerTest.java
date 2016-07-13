/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SimplifiedOperationalListener.DATE_AND_TIME_FORMAT);

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
    private FlowCapableStatisticsGatheringStatus statisticsGatheringStatus;
    @Mock
    private SnapshotGatheringStatusEnd snapshotGatheringStatusEnd;
    @Mock
    private ReconciliationRegistry reconciliationRegistry;
    @Mock
    private ClusterServiceManager clusterServiceManager;

    @Before
    public void setUp() throws Exception {
        final DataBroker db = Mockito.mock(DataBroker.class);
        final FlowCapableNodeSnapshotDao configSnapshot = new FlowCapableNodeSnapshotDao();
        final FlowCapableNodeSnapshotDao operationalSnapshot = new FlowCapableNodeSnapshotDao();
        final FlowCapableNodeDao configDao = new FlowCapableNodeCachedDao(configSnapshot,
                new FlowCapableNodeOdlDao(db, LogicalDatastoreType.CONFIGURATION));

        nodeListenerOperational = new SimplifiedOperationalListener(reactor, operationalSnapshot, configDao, reconciliationRegistry, clusterServiceManager);
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
    public void testOnDataTreeChangeAddSyncup() throws Exception {
        setMasteredAndRegisterReconcile();
        operationalAdd();
        prepareFreshOperational(true);

        Mockito.when(roTx.read(LogicalDatastoreType.CONFIGURATION, fcNodePath))
                .thenReturn(Futures.immediateCheckedFuture(Optional.of(configNode)));
        Mockito.when(reactor.syncup(Matchers.<InstanceIdentifier<FlowCapableNode>>any(), Matchers.<FlowCapableNode>any(),
                Matchers.<FlowCapableNode>any(), Matchers.<LogicalDatastoreType>any())).thenReturn(Futures.immediateFuture(Boolean.TRUE));

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verify(clusterServiceManager).onDeviceConnected(NODE_ID);
        Mockito.verify(reactor).syncup(fcNodePath, configNode, fcOperationalNode, dsType);
        Mockito.verify(roTx).close();
    }

    @Test
    public void testOnDataTreeChangedAddSkip() throws Exception {
        // Related to bug 5920 -> https://bugs.opendaylight.org/show_bug.cgi?id=5920
        setMasteredAndRegisterReconcile();
        operationalAdd();
        prepareFreshOperational(true);

        Mockito.when(roTx.read(LogicalDatastoreType.CONFIGURATION, fcNodePath))
                .thenReturn(Futures.immediateCheckedFuture(Optional.absent()));

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verify(clusterServiceManager).onDeviceConnected(NODE_ID);
        Mockito.verifyZeroInteractions(reactor);
        Mockito.verify(roTx).close();
    }

    @Test
    public void testOnDataTreeChangedSyncupDeletePhysical() throws Exception {
        Mockito.when(operationalModification.getDataBefore()).thenReturn(operationalNode);
        Mockito.when(operationalModification.getDataAfter()).thenReturn(null);
        Mockito.when(dataTreeModification.getRootNode().getModificationType()).thenReturn(ModificationType.DELETE);
        Mockito.when(clusterServiceManager.isDeviceMastered(NODE_ID)).thenReturn(true);
        Mockito.when(reconciliationRegistry.isRegistered(NODE_ID)).thenReturn(false);

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verify(clusterServiceManager).onDeviceDisconnected(NODE_ID);
        Mockito.verifyZeroInteractions(reactor);
    }

    @Test
    public void testOnDataTreeChangedSyncupDeleteLogical() {
        Mockito.when(operationalModification.getDataBefore()).thenReturn(operationalNode);
        List<NodeConnector> nodeConnectorList = Mockito.mock(List.class);
        Mockito.when(operationalNode.getNodeConnector()).thenReturn(nodeConnectorList);
        Mockito.when(clusterServiceManager.isDeviceMastered(NODE_ID)).thenReturn(true);
        Mockito.when(reconciliationRegistry.isRegistered(NODE_ID)).thenReturn(false);

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verify(clusterServiceManager).onDeviceDisconnected(NODE_ID);
        Mockito.verifyZeroInteractions(reactor);
    }

    @Test
    public void testOnDataTreeChangedReconcileNotRegistered() {
        operationalUpdate();
        Mockito.when(clusterServiceManager.isDeviceMastered(NODE_ID)).thenReturn(true);
        Mockito.when(reconciliationRegistry.isRegistered(NODE_ID)).thenReturn(false);

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verifyZeroInteractions(reactor);
    }

    @Test
    public void testOnDataTreeChangedReconcileButStaticsGatheringNotStarted() {
        setMasteredAndRegisterReconcile();
        operationalUpdate();
        Mockito.when(operationalNode.getAugmentation(FlowCapableStatisticsGatheringStatus.class)).thenReturn(null);

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verifyZeroInteractions(reactor);
    }

    @Test
    public void testOnDataTreeChangedReconcileButStaticsGatheringNotFinished() {
        setMasteredAndRegisterReconcile();
        operationalUpdate();
        Mockito.when(operationalNode.getAugmentation(FlowCapableStatisticsGatheringStatus.class)).thenReturn(statisticsGatheringStatus);
        Mockito.when(statisticsGatheringStatus.getSnapshotGatheringStatusEnd()).thenReturn(null);

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verifyZeroInteractions(reactor);
    }

    @Test
    public void testOnDataTreeChangedReconcileButStaticsGatheringNotSuccessful() {
        setMasteredAndRegisterReconcile();
        operationalUpdate();
        Mockito.when(operationalNode.getAugmentation(FlowCapableStatisticsGatheringStatus.class)).thenReturn(statisticsGatheringStatus);
        Mockito.when(statisticsGatheringStatus.getSnapshotGatheringStatusEnd()).thenReturn(snapshotGatheringStatusEnd);
        Mockito.when(snapshotGatheringStatusEnd.isSucceeded()).thenReturn(false);

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verifyZeroInteractions(reactor);
    }

    @Test
    public void testOnDataTreeChangedReconcileAndFreshOperationalNotPresent() throws ParseException {
        setMasteredAndRegisterReconcile();
        operationalUpdate();
        prepareFreshOperational(false);

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verifyZeroInteractions(reactor);
    }

    @Test
    public void testOnDataTreeChangedReconcileAndFreshOperationalPresent() throws Exception {
        setMasteredAndRegisterReconcile();
        operationalUpdate();
        prepareFreshOperational(true);

        Mockito.when(roTx.read(LogicalDatastoreType.CONFIGURATION, fcNodePath))
                .thenReturn(Futures.immediateCheckedFuture(Optional.of(configNode)));
        Mockito.when(reactor.syncup(Matchers.<InstanceIdentifier<FlowCapableNode>>any(), Matchers.<FlowCapableNode>any(),
                Matchers.<FlowCapableNode>any(), Matchers.<LogicalDatastoreType>any())).thenReturn(Futures.immediateFuture(Boolean.TRUE));

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verify(reactor).syncup(fcNodePath, configNode, fcOperationalNode, dsType);
        Mockito.verify(roTx).close();
    }

    @Test
    public void testOnDataTreeChangedSlave() {
        Mockito.when(clusterServiceManager.isDeviceMastered(NODE_ID)).thenReturn(false);
        Mockito.when(operationalModification.getDataAfter()).thenReturn(operationalNode);
        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));
        Mockito.verifyZeroInteractions(reactor);
    }

    private void prepareFreshOperational(final boolean afterRegistration) throws ParseException {
        Mockito.when(operationalNode.getAugmentation(FlowCapableStatisticsGatheringStatus.class)).thenReturn(statisticsGatheringStatus);
        Mockito.when(statisticsGatheringStatus.getSnapshotGatheringStatusEnd()).thenReturn(snapshotGatheringStatusEnd);
        Mockito.when(snapshotGatheringStatusEnd.isSucceeded()).thenReturn(true);
        Mockito.when(snapshotGatheringStatusEnd.getEnd()).thenReturn(Mockito.mock(DateAndTime.class));
        final String timestampBefore = "0000-12-12T01:01:01.000-07:00";
        final String timestampAfter = "9999-12-12T01:01:01.000-07:00";
        if (afterRegistration) {
            Mockito.when(snapshotGatheringStatusEnd.getEnd().getValue()).thenReturn(timestampAfter);
            Mockito.when(reconciliationRegistry.getRegistrationTimestamp(NODE_ID)).thenReturn(simpleDateFormat.parse(timestampBefore));
        } else {
            Mockito.when(snapshotGatheringStatusEnd.getEnd().getValue()).thenReturn(timestampBefore);
            Mockito.when(reconciliationRegistry.getRegistrationTimestamp(NODE_ID)).thenReturn(simpleDateFormat.parse(timestampAfter));
        }
    }

    private void setMasteredAndRegisterReconcile() {
        Mockito.when(clusterServiceManager.isDeviceMastered(NODE_ID)).thenReturn(true);
        Mockito.when(reconciliationRegistry.isRegistered(NODE_ID)).thenReturn(true);
    }

    private void operationalAdd() {
        Mockito.when(operationalModification.getDataBefore()).thenReturn(null);
        Mockito.when(operationalModification.getDataAfter()).thenReturn(operationalNode);
    }

    private void operationalUpdate() {
        Mockito.when(operationalModification.getDataBefore()).thenReturn(operationalNode);
        Mockito.when(operationalModification.getDataAfter()).thenReturn(operationalNode);
    }
}
