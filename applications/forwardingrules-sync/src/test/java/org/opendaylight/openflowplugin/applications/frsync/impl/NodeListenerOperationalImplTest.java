/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.opendaylight.openflowplugin.applications.frsync.util.SemaphoreKeeperImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Test for {@link NodeListenerOperationalImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeListenerOperationalImplTest {

    @Mock
    private SyncReactor reactor;
    @Mock
    private DataBroker db;
    @Mock
    private DataTreeModification<FlowCapableNode> dataTreeModification;
    @Mock
    private ReadOnlyTransaction roTx;
    @Mock
    private DataObjectModification<FlowCapableNode> operationalModification;

    private InstanceIdentifier<FlowCapableNode> nodePath;
    private NodeListenerOperationalImpl nodeListenerOperational;

    @Before
    public void setUp() throws Exception {
        nodeListenerOperational = new NodeListenerOperationalImpl(reactor, db, new SemaphoreKeeperImpl<NodeId>(1, true));
        nodePath = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId("testNode")))
                .augmentation(FlowCapableNode.class);
    }

    @Test
    public void testGetCounterpartDSLogicalType() throws Exception {
        Assert.assertEquals(LogicalDatastoreType.CONFIGURATION, nodeListenerOperational.getCounterpartDSLogicalType());
    }

    @Test
    public void testCreateNextStepFunction() throws Exception {
        final FlowCapableNode flowModOperational = Mockito.mock(FlowCapableNode.class);
        final FlowCapableNode flowModConfig = Mockito.mock(FlowCapableNode.class);
        final AsyncFunction<Optional<FlowCapableNode>, RpcResult<Void>> nextStepFunction =
                nodeListenerOperational.createNextStepFunction(nodePath, flowModOperational);

        nextStepFunction.apply(Optional.of(flowModConfig));
        Mockito.verify(reactor).syncup(nodePath, flowModConfig, flowModOperational);
    }

    @Test
    public void testOnDataTreeChanged() throws Exception {
        final FlowCapableNode configTree = Mockito.mock(FlowCapableNode.class);
        final FlowCapableNode operationalTree = Mockito.mock(FlowCapableNode.class);
        final DataTreeIdentifier<FlowCapableNode> dataTreeIdentifier =
                new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL, nodePath);

        Mockito.when(dataTreeModification.getRootPath()).thenReturn(dataTreeIdentifier);
        Mockito.when(dataTreeModification.getRootNode()).thenReturn(operationalModification);
        Mockito.when(operationalModification.getDataAfter()).thenReturn(operationalTree);
        Mockito.when(db.newReadOnlyTransaction()).thenReturn(roTx);
        Mockito.doReturn(Futures.immediateCheckedFuture(Optional.of(configTree))).when(
                roTx).read(LogicalDatastoreType.CONFIGURATION, nodePath);

        nodeListenerOperational.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verify(reactor).syncup(nodePath, configTree, operationalTree);
        Mockito.verify(roTx).close();
    }
}