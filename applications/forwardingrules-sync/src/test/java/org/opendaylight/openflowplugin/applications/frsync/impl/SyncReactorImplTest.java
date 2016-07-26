/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frsync.SyncPlanPushStrategy;
import org.opendaylight.openflowplugin.applications.frsync.impl.strategy.SynchronizationDiffInput;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconcileUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.SyncCrudCounters;
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
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link SyncReactorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SyncReactorImplTest {

    private static final Logger LOG = LoggerFactory.getLogger(SyncReactorImplTest.class);

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
    public void setUp() throws Exception {
        reactor = new SyncReactorImpl(syncPlanPushStrategy);
    }

    @Test
    public void testSyncup() throws Exception {
        final FlowCapableNode configFcn = new FlowCapableNodeBuilder()
                .setGroup(Collections.singletonList(DSInputFactory.createGroup(1L)))
                .setTable(Collections.singletonList(new TableBuilder()
                        .setFlow(Collections.singletonList(DSInputFactory.createFlow("f1", 1)))
                        .build()))
                .setMeter(Collections.singletonList(DSInputFactory.createMeter(1L)))
                .build();

        final FlowCapableNode operationalFcn = new FlowCapableNodeBuilder()
                .setGroup(Collections.singletonList(DSInputFactory.createGroup(2L)))
                .setTable(Collections.singletonList(new TableBuilder()
                        .setFlow(Collections.singletonList(DSInputFactory.createFlow("f2", 2)))
                        .build()))
                .setMeter(Collections.singletonList(DSInputFactory.createMeter(2L)))
                .build();

        final SyncupEntry syncupEntry = new SyncupEntry(configFcn, LogicalDatastoreType.CONFIGURATION,
                                                        operationalFcn, LogicalDatastoreType.OPERATIONAL);

        Mockito.when(syncPlanPushStrategy.executeSyncStrategy(
                Matchers.<ListenableFuture<RpcResult<Void>>>any(),
                Matchers.<SynchronizationDiffInput>any(),
                Matchers.<SyncCrudCounters>any()))
                .thenReturn(RpcResultBuilder.<Void>success().buildFuture());

        final ListenableFuture<Boolean> syncupResult = reactor.syncup(NODE_IDENT, syncupEntry);
        try {
            Assert.assertTrue(syncupResult.isDone());
            final Boolean voidRpcResult = syncupResult.get(2, TimeUnit.SECONDS);
            Assert.assertTrue(voidRpcResult);

            Mockito.verify(syncPlanPushStrategy).executeSyncStrategy(
                    Matchers.<ListenableFuture<RpcResult<Void>>>any(),
                    syncDiffInputCaptor.capture(),
                    Matchers.<SyncCrudCounters>any()
            );

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
        } catch (Exception e) {
            LOG.warn("syncup failed", e);
            Assert.fail("syncup failed: " + e.getMessage());
        }
    }
}