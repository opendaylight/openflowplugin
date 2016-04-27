/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

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

import com.google.common.base.Optional;
import com.google.common.util.concurrent.Futures;

/**
 * Test for {@link SimplifiedConfigListener}.
 */
@SuppressWarnings("deprecation")
@RunWith(MockitoJUnitRunner.class)
public class SimplifiedConfigListenerTest {

    @Mock
    private SyncReactor reactor;
    @Mock
    private DataBroker db;
    @Mock
    private DataTreeModification<FlowCapableNode> dataTreeModification;
    @Mock
    private ReadOnlyTransaction roTx;
    @Mock
    private DataObjectModification<FlowCapableNode> configModification;

    private InstanceIdentifier<FlowCapableNode> nodePath;
    private SimplifiedConfigListener nodeListenerConfig;

    @Before
    public void setUp() throws Exception {
        final FlowCapableNodeSnapshotDao configSnaphot = new FlowCapableNodeSnapshotDao();
        final FlowCapableNodeSnapshotDao operationalSnaphot = new FlowCapableNodeSnapshotDao();
        final FlowCapableNodeDao operationalDao = new FlowCapableNodeCachedDao(operationalSnaphot,
                new FlowCapableNodeOdlDao(db, LogicalDatastoreType.OPERATIONAL));

        
        nodeListenerConfig = new SimplifiedConfigListener(reactor, configSnaphot, operationalDao);
        nodePath = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId("testNode")))
                .augmentation(FlowCapableNode.class);
    }

    @Test
    public void testDSLogicalType() throws Exception {
        Assert.assertEquals(LogicalDatastoreType.CONFIGURATION, nodeListenerConfig.dsType());
    }

    @Test
    public void testOnDataTreeChanged() throws Exception {
        final FlowCapableNode configTree = Mockito.mock(FlowCapableNode.class);
        final FlowCapableNode operationalTree = Mockito.mock(FlowCapableNode.class);
        final DataTreeIdentifier<FlowCapableNode> dataTreeIdentifier =
                new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, nodePath);

        Mockito.when(dataTreeModification.getRootPath()).thenReturn(dataTreeIdentifier);
        Mockito.when(dataTreeModification.getRootNode()).thenReturn(configModification);
        Mockito.when(configModification.getDataAfter()).thenReturn(configTree);
        Mockito.when(db.newReadOnlyTransaction()).thenReturn(roTx);
        Mockito.doReturn(Futures.immediateCheckedFuture(Optional.of(operationalTree))).when(
                roTx).read(LogicalDatastoreType.OPERATIONAL, nodePath);
        Mockito.when(reactor.syncup(Matchers.<InstanceIdentifier<FlowCapableNode>>any(),Matchers.<FlowCapableNode>any(),Matchers.<FlowCapableNode>any()))
                .thenReturn(Futures.immediateFuture(Boolean.TRUE));

        nodeListenerConfig.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verify(reactor).syncup(nodePath, configTree, operationalTree);
        Mockito.verify(roTx).close();
    }
}