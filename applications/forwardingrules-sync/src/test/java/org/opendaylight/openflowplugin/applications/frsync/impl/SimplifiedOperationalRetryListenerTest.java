/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeCachedDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeOdlDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeSnapshotDao;
import org.opendaylight.openflowplugin.applications.frsync.util.RetryRegistry;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableStatisticsGatheringStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.snapshot.gathering.status.grouping.SnapshotGatheringStatusEnd;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Test for {@link SimplifiedOperationalRetryListener}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SimplifiedOperationalRetryListenerTest {

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(RetryRegistry.DATE_AND_TIME_FORMAT);
    private static final NodeId NODE_ID = new NodeId("testNode");
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
    private RetryRegistry retryRegistry;
    @Mock
    private FlowCapableStatisticsGatheringStatus statisticsGatheringStatus;
    @Mock
    private SnapshotGatheringStatusEnd snapshotGatheringStatusEnd;

    private InstanceIdentifier<FlowCapableNode> fcNodePath;
    private SimplifiedOperationalRetryListener nodeListenerOperational;

    @Before
    public void setUp() throws Exception {
        final DataBroker db = Mockito.mock(DataBroker.class);
        final FlowCapableNodeSnapshotDao configSnapshot = new FlowCapableNodeSnapshotDao();
        final FlowCapableNodeSnapshotDao operationalSnapshot = new FlowCapableNodeSnapshotDao();
        final FlowCapableNodeDao configDao = new FlowCapableNodeCachedDao(configSnapshot,
                new FlowCapableNodeOdlDao(db, LogicalDatastoreType.CONFIGURATION));

        nodeListenerOperational = new SimplifiedOperationalRetryListener(reactor, operationalSnapshot, configDao, retryRegistry);
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
    public void testOnDataTreeChangedRetryNotRegistered() {
        Mockito.when(operationalModification.getDataBefore()).thenReturn(operationalNode);
        Mockito.when(operationalModification.getDataAfter()).thenReturn(operationalNode);

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verifyZeroInteractions(reactor);
    }

    @Test
    public void testOnDataTreeChangedRetryButStaticsGatheringNotStarted() {
        Mockito.when(operationalModification.getDataBefore()).thenReturn(operationalNode);
        Mockito.when(operationalModification.getDataAfter()).thenReturn(operationalNode);
        Mockito.when(retryRegistry.isRegistered(NODE_ID)).thenReturn(true);
        Mockito.when(operationalNode.getAugmentation(FlowCapableStatisticsGatheringStatus.class)).thenReturn(null);

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verifyZeroInteractions(reactor);
    }

    @Test
    public void testOnDataTreeChangedRetryButStaticsGatheringNotFinished() {
        Mockito.when(operationalModification.getDataBefore()).thenReturn(operationalNode);
        Mockito.when(operationalModification.getDataAfter()).thenReturn(operationalNode);
        Mockito.when(retryRegistry.isRegistered(NODE_ID)).thenReturn(true);
        Mockito.when(operationalNode.getAugmentation(FlowCapableStatisticsGatheringStatus.class)).thenReturn(statisticsGatheringStatus);
        Mockito.when(statisticsGatheringStatus.getSnapshotGatheringStatusEnd()).thenReturn(null);

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verifyZeroInteractions(reactor);
    }

    @Test
    public void testOnDataTreeChangedRetryButStaticsGatheringNotSuccessful() {
        Mockito.when(operationalModification.getDataBefore()).thenReturn(operationalNode);
        Mockito.when(operationalModification.getDataAfter()).thenReturn(operationalNode);
        Mockito.when(retryRegistry.isRegistered(NODE_ID)).thenReturn(true);
        Mockito.when(operationalNode.getAugmentation(FlowCapableStatisticsGatheringStatus.class)).thenReturn(statisticsGatheringStatus);
        Mockito.when(statisticsGatheringStatus.getSnapshotGatheringStatusEnd()).thenReturn(snapshotGatheringStatusEnd);
        Mockito.when(snapshotGatheringStatusEnd.isSucceeded()).thenReturn(false);

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verifyZeroInteractions(reactor);
    }

    @Test
    public void testOnDataTreeChangedRetryAndFreshOperationalNotPresent() throws ParseException {
        final DateAndTime timestamp = Mockito.mock(DateAndTime.class);
        Mockito.when(operationalModification.getDataBefore()).thenReturn(operationalNode);
        Mockito.when(operationalModification.getDataAfter()).thenReturn(operationalNode);
        Mockito.when(retryRegistry.isRegistered(NODE_ID)).thenReturn(true);
        Mockito.when(operationalNode.getAugmentation(FlowCapableStatisticsGatheringStatus.class)).thenReturn(statisticsGatheringStatus);
        Mockito.when(statisticsGatheringStatus.getSnapshotGatheringStatusEnd()).thenReturn(snapshotGatheringStatusEnd);
        Mockito.when(snapshotGatheringStatusEnd.isSucceeded()).thenReturn(true);
        Mockito.when(snapshotGatheringStatusEnd.getEnd()).thenReturn(timestamp);
        Mockito.when(snapshotGatheringStatusEnd.getEnd().getValue()).thenReturn("0000-12-12T01:01:01.000-07:00");
        Mockito.when(retryRegistry.getRegistration(NODE_ID)).thenReturn(simpleDateFormat.parse("9999-12-12T01:01:01.000-07:00"));

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verifyZeroInteractions(reactor);
    }

    @Test
    public void testOnDataTreeChangedRetryAndFreshOperationalPresent() throws Exception {
        final DateAndTime timestamp = Mockito.mock(DateAndTime.class);
        Mockito.when(roTx.read(LogicalDatastoreType.CONFIGURATION, fcNodePath))
                .thenReturn(Futures.immediateCheckedFuture(Optional.of(configNode)));
        Mockito.when(reactor.syncup(Matchers.<InstanceIdentifier<FlowCapableNode>>any(),Matchers.<FlowCapableNode>any(),Matchers.<FlowCapableNode>any()))
                .thenReturn(Futures.immediateFuture(Boolean.TRUE));
        Mockito.when(operationalModification.getDataBefore()).thenReturn(operationalNode);
        Mockito.when(operationalModification.getDataAfter()).thenReturn(operationalNode);
        Mockito.when(retryRegistry.isRegistered(NODE_ID)).thenReturn(true);
        Mockito.when(operationalNode.getAugmentation(FlowCapableStatisticsGatheringStatus.class)).thenReturn(statisticsGatheringStatus);
        Mockito.when(statisticsGatheringStatus.getSnapshotGatheringStatusEnd()).thenReturn(snapshotGatheringStatusEnd);
        Mockito.when(snapshotGatheringStatusEnd.isSucceeded()).thenReturn(true);
        Mockito.when(snapshotGatheringStatusEnd.getEnd()).thenReturn(timestamp);
        Mockito.when(snapshotGatheringStatusEnd.getEnd().getValue()).thenReturn("9999-12-12T01:01:01.000-07:00");
        Mockito.when(retryRegistry.getRegistration(NODE_ID)).thenReturn(simpleDateFormat.parse("0000-12-12T01:01:01.000-07:00"));

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verify(reactor).syncup(fcNodePath, configNode, fcOperationalNode);
        Mockito.verify(roTx).close();
    }

    @Test
    public void testOnDataTreeChangedRetryAndNodeDelete() {
        Mockito.when(operationalModification.getDataBefore()).thenReturn(operationalNode);
        Mockito.when(dataTreeModification.getRootNode().getModificationType()).thenReturn(DataObjectModification.ModificationType.DELETE);

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verify(retryRegistry).unregisterIfRegistered(NODE_ID);
        Mockito.verifyZeroInteractions(reactor);
    }

    @Test
    public void testOnDataTreeChangedRetryAndNodeDeleteThenAdd() throws InterruptedException {
        // remove
        Mockito.when(operationalModification.getDataBefore()).thenReturn(operationalNode);
        Mockito.when(dataTreeModification.getRootNode().getModificationType()).thenReturn(DataObjectModification.ModificationType.DELETE);

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verify(retryRegistry).unregisterIfRegistered(NODE_ID);
        Mockito.verifyZeroInteractions(reactor);

        // add
        Mockito.when(roTx.read(LogicalDatastoreType.CONFIGURATION, fcNodePath))
                .thenReturn(Futures.immediateCheckedFuture(Optional.of(configNode)));
        Mockito.when(reactor.syncup(Matchers.<InstanceIdentifier<FlowCapableNode>>any(),Matchers.<FlowCapableNode>any(),Matchers.<FlowCapableNode>any()))
                .thenReturn(Futures.immediateFuture(Boolean.TRUE));
        Mockito.when(operationalModification.getDataBefore()).thenReturn(null);
        Mockito.when(operationalModification.getDataAfter()).thenReturn(operationalNode);
        Mockito.when(dataTreeModification.getRootNode().getModificationType()).thenReturn(DataObjectModification.ModificationType.SUBTREE_MODIFIED);

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verify(reactor).syncup(fcNodePath, configNode, fcOperationalNode);
        Mockito.verify(roTx).close();

    }

}