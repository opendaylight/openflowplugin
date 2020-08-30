/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.util.concurrent.Futures;
import java.util.Collections;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
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
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
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
    private ReadTransaction roTx;
    @Mock
    private DataTreeModification<FlowCapableNode> dataTreeModification;
    @Mock
    private DataObjectModification<FlowCapableNode> configModification;
    @Mock
    private FlowCapableNode dataBefore;
    @Mock
    private FlowCapableNode dataAfter;

    @Before
    public void setUp() {
        final DataBroker db = Mockito.mock(DataBroker.class);
        final FlowCapableNodeSnapshotDao configSnapshot = new FlowCapableNodeSnapshotDao();
        final FlowCapableNodeSnapshotDao operationalSnapshot = new FlowCapableNodeSnapshotDao();
        final FlowCapableNodeDao operationalDao = new FlowCapableNodeCachedDao(operationalSnapshot,
                new FlowCapableNodeOdlDao(db, LogicalDatastoreType.OPERATIONAL));

        nodeListenerConfig = new SimplifiedConfigListener(reactor, configSnapshot, operationalDao);
        fcNodePath = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(NODE_ID))
                .augmentation(FlowCapableNode.class);

        final DataTreeIdentifier<FlowCapableNode> dataTreeIdentifier =
                DataTreeIdentifier.create(LogicalDatastoreType.CONFIGURATION, fcNodePath);

        Mockito.when(db.newReadOnlyTransaction()).thenReturn(roTx);
        Mockito.when(dataTreeModification.getRootPath()).thenReturn(dataTreeIdentifier);
        Mockito.when(dataTreeModification.getRootNode()).thenReturn(configModification);
    }

    @Test
    public void testDSLogicalType() {
        Assert.assertEquals(LogicalDatastoreType.CONFIGURATION, nodeListenerConfig.dsType());
    }

    @Test
    public void testOnDataTreeChangedAdd() {
        Mockito.when(configModification.getDataBefore()).thenReturn(null);
        Mockito.when(configModification.getDataAfter()).thenReturn(dataAfter);
        final SyncupEntry syncupEntry =
                loadOperationalDSAndPrepareSyncupEntry(dataAfter, confgDS, dataBefore, operationalDS);

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
        Mockito.doReturn(FluentFutures.immediateFluentFuture(Optional.empty())).when(roTx)
            .read(LogicalDatastoreType.OPERATIONAL, fcNodePath);

        nodeListenerConfig.onDataTreeChanged(Collections.singleton(dataTreeModification));

        Mockito.verifyNoMoreInteractions(reactor);
        Mockito.verify(roTx).close();
    }

    private SyncupEntry loadOperationalDSAndPrepareSyncupEntry(final FlowCapableNode after,
            final LogicalDatastoreType dsTypeAfter, final FlowCapableNode before,
            final LogicalDatastoreType dsTypeBefore) {
        Mockito.doReturn(FluentFutures.immediateFluentFuture(Optional.of(dataBefore))).when(roTx)
            .read(LogicalDatastoreType.OPERATIONAL, fcNodePath);
        final SyncupEntry syncupEntry = new SyncupEntry(after, dsTypeAfter, before, dsTypeBefore);
        Mockito.when(reactor.syncup(ArgumentMatchers.any(),
                Mockito.eq(syncupEntry))).thenReturn(Futures.immediateFuture(Boolean.TRUE));
        return syncupEntry;
    }

}
