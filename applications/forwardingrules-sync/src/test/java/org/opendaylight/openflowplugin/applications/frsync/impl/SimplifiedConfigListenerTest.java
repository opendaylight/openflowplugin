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
import org.opendaylight.openflowplugin.applications.frsync.util.SyncupEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Test for {@link SimplifiedConfigListener}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SimplifiedConfigListenerTest {

    private static final NodeId NODE_ID = new NodeId("testNode");
    private InstanceIdentifier<FlowCapableNode> fcNodePath;
    private SimplifiedConfigListener nodeListenerConfig;
    private final LogicalDatastoreType confgDS = LogicalDatastoreType.CONFIGURATION;
    private final LogicalDatastoreType operationalDS = LogicalDatastoreType.OPERATIONAL;

    @Mock
    private SyncReactor reactor;
    @Mock
    private ReadOnlyTransaction roTx;
    @Mock
    private DataTreeModification<FlowCapableNode> dataTreeModification;
    @Mock
    private DataObjectModification<FlowCapableNode> configModification;
    @Mock
    private FlowCapableNode dataBefore;
    @Mock
    private FlowCapableNode dataAfter;

    @Before
    public void setUp() throws Exception {
        final DataBroker db = Mockito.mock(DataBroker.class);
        final FlowCapableNodeSnapshotDao configSnapshot = new FlowCapableNodeSnapshotDao();
        final FlowCapableNodeSnapshotDao operationalSnapshot = new FlowCapableNodeSnapshotDao();
        final FlowCapableNodeDao operationalDao = new FlowCapableNodeCachedDao(operationalSnapshot,
                new FlowCapableNodeOdlDao(db, LogicalDatastoreType.OPERATIONAL));

        nodeListenerConfig = new SimplifiedConfigListener(reactor, configSnapshot, operationalDao);
        fcNodePath = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(NODE_ID))
                .augmentation(FlowCapableNode.class);

        final DataTreeIdentifier<FlowCapableNode> dataTreeIdentifier =
                new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, fcNodePath);

        Mockito.when(db.newReadOnlyTransaction()).thenReturn(roTx);
        Mockito.when(dataTreeModification.getRootPath()).thenReturn(dataTreeIdentifier);
        Mockito.when(dataTreeModification.getRootNode()).thenReturn(configModification);
    }

    @Test
    public void testDSLogicalType() throws Exception {
        Assert.assertEquals(LogicalDatastoreType.CONFIGURATION, nodeListenerConfig.dsType());
    }

    @Test
    public void testOnDataTreeChangedAdd() {
        Mockito.when(configModification.getDataBefore()).thenReturn(null);
        Mockito.when(configModification.getDataAfter()).thenReturn(dataAfter);
        final SyncupEntry syncupEntry = loadOperationalDSAndPrepareSyncupEntry(dataAfter, confgDS, dataBefore, operationalDS);

        nodeListenerConfig.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verify(reactor).syncup(fcNodePath, syncupEntry);
        Mockito.verifyNoMoreInteractions(reactor);
        Mockito.verify(roTx).close();
    }

    @Test
    public void testOnDataTreeChangedUpdate() {
        Mockito.when(configModification.getDataBefore()).thenReturn(dataBefore);
        Mockito.when(configModification.getDataAfter()).thenReturn(dataAfter);
        final SyncupEntry syncupEntry = loadOperationalDSAndPrepareSyncupEntry(dataAfter, confgDS, dataBefore, confgDS);

        nodeListenerConfig.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verify(reactor).syncup(fcNodePath, syncupEntry);
        Mockito.verifyNoMoreInteractions(reactor);
        Mockito.verify(roTx).close();
    }

    @Test
    public void testOnDataTreeChangedDelete() {
        Mockito.when(configModification.getDataBefore()).thenReturn(dataBefore);
        Mockito.when(configModification.getDataAfter()).thenReturn(null);
        final SyncupEntry syncupEntry = loadOperationalDSAndPrepareSyncupEntry(null, confgDS, dataBefore, confgDS);

        nodeListenerConfig.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verify(reactor).syncup(fcNodePath, syncupEntry);
        Mockito.verifyNoMoreInteractions(reactor);
        Mockito.verify(roTx).close();
    }

    @Test
    public void testOnDataTreeChangedSkip() {
        Mockito.when(roTx.read(LogicalDatastoreType.OPERATIONAL, fcNodePath)).
                thenReturn(Futures.immediateCheckedFuture(Optional.absent()));

        nodeListenerConfig.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verifyZeroInteractions(reactor);
        Mockito.verify(roTx).close();
    }

    private SyncupEntry loadOperationalDSAndPrepareSyncupEntry(final FlowCapableNode after, final LogicalDatastoreType dsTypeAfter,
                                                               final FlowCapableNode before, final LogicalDatastoreType dsTypeBefore) {
        Mockito.when(roTx.read(LogicalDatastoreType.OPERATIONAL, fcNodePath))
                .thenReturn(Futures.immediateCheckedFuture(Optional.of(dataBefore)));
        final SyncupEntry syncupEntry = new SyncupEntry(after, dsTypeAfter, before, dsTypeBefore);
        Mockito.when(reactor.syncup(Matchers.<InstanceIdentifier<FlowCapableNode>>any(), Mockito.eq(syncupEntry)))
                .thenReturn(Futures.immediateFuture(Boolean.TRUE));
        return syncupEntry;
    }

}