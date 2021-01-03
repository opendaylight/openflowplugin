/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frsync.SyncPlanPushStrategy;
import org.opendaylight.openflowplugin.applications.frsync.impl.strategy.SynchronizationDiffInput;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconcileUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.SyncupEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Test for {@link SyncReactorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SyncReactorImplTest {
    private static final NodeId NODE_ID = new NodeId("unit-nodeId");
    private static final InstanceIdentifier<FlowCapableNode> NODE_IDENT = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(NODE_ID)).augmentation(FlowCapableNode.class);
    private SyncReactorImpl reactor;

    @Mock
    private DataBroker db;
    @Mock
    private SyncPlanPushStrategy syncPlanPushStrategy;
    @Captor
    private ArgumentCaptor<Group> groupCaptor;
    @Captor
    private ArgumentCaptor<Group> groupUpdateCaptor;
    @Captor
    private ArgumentCaptor<Flow> flowCaptor;
    @Captor
    private ArgumentCaptor<Flow> flowUpdateCaptor;
    @Captor
    private ArgumentCaptor<Meter> meterCaptor;
    @Captor
    private ArgumentCaptor<Meter> meterUpdateCaptor;
    @Captor
    private ArgumentCaptor<TableFeatures> tableFeaturesCaptor;
    @Captor
    private ArgumentCaptor<SynchronizationDiffInput> syncDiffInputCaptor;

    @Before
    public void setUp() {
        reactor = new SyncReactorImpl(syncPlanPushStrategy);
    }

    @Test
    public void testSyncup() throws Exception {
        final FlowCapableNode configFcn = new FlowCapableNodeBuilder()
                .setGroup(BindingMap.of(DSInputFactory.createGroup(Uint32.ONE)))
                .setTable(BindingMap.of(new TableBuilder()
                        .setId(Uint8.valueOf(42))
                        .setFlow(BindingMap.of(DSInputFactory.createFlow("f1", 1)))
                        .build()))
                .setMeter(BindingMap.of(DSInputFactory.createMeter(Uint32.ONE)))
                .build();

        final FlowCapableNode operationalFcn = new FlowCapableNodeBuilder()
                .setGroup(BindingMap.of(DSInputFactory.createGroup(Uint32.TWO)))
                .setTable(BindingMap.of(new TableBuilder()
                        .setId(Uint8.valueOf(42))
                        .setFlow(BindingMap.of(DSInputFactory.createFlow("f2", 2)))
                        .build()))
                .setMeter(BindingMap.of(DSInputFactory.createMeter(Uint32.TWO)))
                .build();

        final SyncupEntry syncupEntry = new SyncupEntry(configFcn, LogicalDatastoreType.CONFIGURATION,
                                                        operationalFcn, LogicalDatastoreType.OPERATIONAL);

        Mockito.when(syncPlanPushStrategy.executeSyncStrategy(
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.<Void>success().buildFuture());

        final ListenableFuture<Boolean> syncupResult = reactor.syncup(NODE_IDENT, syncupEntry);
        Assert.assertTrue(syncupResult.isDone());
        final Boolean voidRpcResult = syncupResult.get(2, TimeUnit.SECONDS);
        Assert.assertTrue(voidRpcResult);

        Mockito.verify(syncPlanPushStrategy).executeSyncStrategy(
                ArgumentMatchers.any(),
                syncDiffInputCaptor.capture(),
                ArgumentMatchers.any());

        final SynchronizationDiffInput diffInput = syncDiffInputCaptor.getValue();
        Assert.assertEquals(1, ReconcileUtil.countTotalPushed(diffInput.getFlowsToAddOrUpdate().values()));
        Assert.assertEquals(0, ReconcileUtil.countTotalUpdated(diffInput.getFlowsToAddOrUpdate().values()));
        Assert.assertEquals(1, ReconcileUtil.countTotalPushed(diffInput.getFlowsToRemove().values()));

        Assert.assertEquals(1, ReconcileUtil.countTotalPushed(diffInput.getGroupsToAddOrUpdate()));
        Assert.assertEquals(0, ReconcileUtil.countTotalUpdated(diffInput.getGroupsToAddOrUpdate()));
        Assert.assertEquals(1, ReconcileUtil.countTotalPushed(diffInput.getGroupsToRemove()));

        Assert.assertEquals(1, diffInput.getMetersToAddOrUpdate().getItemsToPush().size());
        Assert.assertEquals(0, diffInput.getMetersToAddOrUpdate().getItemsToUpdate().size());
        Assert.assertEquals(1, diffInput.getMetersToRemove().getItemsToPush().size());
    }
}
