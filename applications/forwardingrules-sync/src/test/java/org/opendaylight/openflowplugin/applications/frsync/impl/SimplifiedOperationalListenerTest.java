/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import static org.opendaylight.openflowplugin.applications.frsync.impl.SimplifiedOperationalListener.flowCapableNodeAfter;

import java.util.Collections;

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
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeCachedDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeOdlDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeSnapshotDao;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;

/**
 * Test for {@link SimplifiedOperationalListener}.
 */
@SuppressWarnings("deprecation")
@RunWith(MockitoJUnitRunner.class)
public class SimplifiedOperationalListenerTest {

    public static final NodeId NODE_ID = new NodeId("testNode");
    @Mock
    private SyncReactor reactor;
    @Mock
    private ReadOnlyTransaction roTx;
    @Mock
    private DataTreeModification<Node> dataTreeModification;
    @Mock
    private FlowCapableNode configNode;
    @Mock
    private FlowCapableNode fcOperationalNode;

    private InstanceIdentifier<Node> nodePath;
    private InstanceIdentifier<FlowCapableNode> fcNodePath;
    private SimplifiedOperationalListener nodeListenerOperational;

    @SuppressWarnings("deprecation")
    @Before
    public void setUp() throws Exception {
        final DataBroker db = Mockito.mock(DataBroker.class);
        final DataObjectModification<Node> operationalModification = Mockito.mock(DataObjectModification.class);
        final Node operationalNode = Mockito.mock(Node.class);

        final FlowCapableNodeSnapshotDao configSnaphot = new FlowCapableNodeSnapshotDao();
        final FlowCapableNodeSnapshotDao operationalSnaphot = new FlowCapableNodeSnapshotDao();
        final FlowCapableNodeDao configDao = new FlowCapableNodeCachedDao(configSnaphot,
                new FlowCapableNodeOdlDao(db, LogicalDatastoreType.CONFIGURATION));
        nodeListenerOperational = new SimplifiedOperationalListener(reactor, operationalSnaphot, configDao);
        nodePath = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(NODE_ID));
        fcNodePath = nodePath.augmentation(FlowCapableNode.class);

        final DataTreeIdentifier<Node> dataTreeIdentifier =
                new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL, nodePath);

        Mockito.when(operationalNode.getId()).thenReturn(NODE_ID);
        Mockito.when(db.newReadOnlyTransaction()).thenReturn(roTx);
        Mockito.when(operationalNode.getAugmentation(FlowCapableNode.class)).thenReturn(fcOperationalNode);
        Mockito.when(dataTreeModification.getRootPath()).thenReturn(dataTreeIdentifier);
        Mockito.when(dataTreeModification.getRootNode()).thenReturn(operationalModification);
        Mockito.when(operationalModification.getDataAfter()).thenReturn(operationalNode);
    }

    @Test
    public void testDSLogicalType() throws Exception {
        Assert.assertEquals(LogicalDatastoreType.OPERATIONAL, nodeListenerOperational.dsType());
    }

    @Test
    public void testOnDataTreeChangedSyncup() throws Exception {
        Mockito.when(reactor.syncup(Matchers.<InstanceIdentifier<FlowCapableNode>>any(),Matchers.<FlowCapableNode>any(),
                Matchers.<FlowCapableNode>any())).thenReturn(Futures.immediateFuture(Boolean.TRUE));
        Mockito.when(roTx.read(LogicalDatastoreType.CONFIGURATION, fcNodePath))
                .thenReturn(Futures.immediateCheckedFuture(Optional.of(configNode)));

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verify(reactor, Mockito.times(1)).syncup(fcNodePath, configNode, fcOperationalNode);
        Mockito.verify(roTx).close();
    }

    @Test
    public void testOnDataTreeChangedSkip() throws Exception {
        // Related to bug 5920 -> https://bugs.opendaylight.org/show_bug.cgi?id=5920
        Mockito.when(roTx.read(LogicalDatastoreType.CONFIGURATION, fcNodePath))
                .thenReturn(Futures.immediateCheckedFuture(Optional.absent()));

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verifyZeroInteractions(reactor);
        Mockito.verify(roTx).close();
    }
}
