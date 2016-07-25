/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.role;


import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.google.common.base.VerifyException;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import java.math.BigInteger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipCandidateRegistration;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipChange;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListener;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListenerRegistration;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceTerminationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleConductor;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.RoleChangeListener;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;

@RunWith(MockitoJUnitRunner.class)
public class RoleManagerImplTest {

    @Mock
    EntityOwnershipService entityOwnershipService;

    @Mock
    DataBroker dataBroker;

    @Mock
    DeviceContext deviceContext;

    @Mock
    DeviceManager deviceManager;

    @Mock
    EntityOwnershipListener entityOwnershipListener;

    @Mock
    EntityOwnershipListenerRegistration entityOwnershipListenerRegistration;

    @Mock
    EntityOwnershipCandidateRegistration entityOwnershipCandidateRegistration;

    @Mock
    ConnectionContext connectionContext;

    @Mock
    FeaturesReply featuresReply;

    @Mock
    DeviceInitializationPhaseHandler deviceInitializationPhaseHandler;

    @Mock
    DeviceTerminationPhaseHandler deviceTerminationPhaseHandler;

    @Mock
    WriteTransaction writeTransaction;

    @Mock
    LifecycleConductor conductor;

    @Mock
    DeviceState deviceState;

    @Mock
    DeviceInfo deviceInfo;

    @Mock
    DeviceInfo deviceInfo2;

    @Mock
    MessageSpy messageSpy;

    @Mock
    OutboundQueue outboundQueue;

    @Mock
    GetFeaturesOutput featuresOutput;

    @Mock
    LifecycleService lifecycleService;

    private RoleManagerImpl roleManager;
    private RoleManagerImpl roleManagerSpy;
    private RoleContext roleContextSpy;
    private final NodeId nodeId = NodeId.getDefaultInstance("openflow:1");
    private final NodeId nodeId2 = NodeId.getDefaultInstance("openflow:2");

    private InOrder inOrder;

    @Before
    public void setUp() throws Exception {
        CheckedFuture<Void, TransactionCommitFailedException> future = Futures.immediateCheckedFuture(null);
        Mockito.when(entityOwnershipService.registerListener(Mockito.anyString(), Mockito.any(EntityOwnershipListener.class))).thenReturn(entityOwnershipListenerRegistration);
        Mockito.when(entityOwnershipService.registerCandidate(Mockito.any(Entity.class))).thenReturn(entityOwnershipCandidateRegistration);
        Mockito.when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);
        Mockito.when(deviceContext.getDeviceState()).thenReturn(deviceState);
        Mockito.when(deviceContext.getDeviceInfo()).thenReturn(deviceInfo);
        Mockito.when(deviceContext.getMessageSpy()).thenReturn(messageSpy);
        Mockito.when(deviceContext.getPrimaryConnectionContext().getOutboundQueueProvider()).thenReturn(outboundQueue);
        Mockito.when(connectionContext.getFeatures()).thenReturn(featuresReply);
        Mockito.when(connectionContext.getNodeId()).thenReturn(nodeId);
        Mockito.when(connectionContext.getConnectionState()).thenReturn(ConnectionContext.CONNECTION_STATE.WORKING);
        Mockito.when(deviceInfo.getDatapathId()).thenReturn(new BigInteger("1"));
        Mockito.when(deviceInfo.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        Mockito.when(deviceInfo.getNodeId()).thenReturn(nodeId);
        Mockito.doNothing().when(deviceInitializationPhaseHandler).onDeviceContextLevelUp(Mockito.<DeviceInfo>any(), Mockito.<LifecycleService>any());
        Mockito.doNothing().when(deviceTerminationPhaseHandler).onDeviceContextLevelDown(Mockito.<DeviceInfo>any());
        Mockito.when(dataBroker.newWriteOnlyTransaction()).thenReturn(writeTransaction);
        Mockito.when(writeTransaction.submit()).thenReturn(future);
        Mockito.when(deviceInfo.getNodeId()).thenReturn(nodeId);
        Mockito.when(deviceInfo2.getNodeId()).thenReturn(nodeId2);
        Mockito.when(deviceInfo.getDatapathId()).thenReturn(BigInteger.TEN);
        roleManager = new RoleManagerImpl(dataBroker, conductor);
        roleManager.setDeviceInitializationPhaseHandler(deviceInitializationPhaseHandler);
        roleManager.setDeviceTerminationPhaseHandler(deviceTerminationPhaseHandler);
        Mockito.when(conductor.getDeviceContext(deviceInfo)).thenReturn(deviceContext);
        roleManagerSpy = Mockito.spy(roleManager);
        roleManagerSpy.onDeviceContextLevelUp(deviceInfo, lifecycleService);
        roleContextSpy = Mockito.spy(roleManager.getRoleContext(deviceInfo));
        Mockito.when(roleContextSpy.getDeviceInfo()).thenReturn(deviceInfo);
        Mockito.when(roleContextSpy.getDeviceInfo().getNodeId()).thenReturn(nodeId);
        inOrder = Mockito.inOrder(entityOwnershipListenerRegistration, roleManagerSpy, roleContextSpy);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test(expected = VerifyException.class)
    public void testOnDeviceContextLevelUp() throws Exception {
        roleManagerSpy.onDeviceContextLevelUp(deviceInfo, lifecycleService);
        inOrder.verify(roleManagerSpy).onDeviceContextLevelUp(deviceInfo, lifecycleService);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testCloseMaster() throws Exception {
        roleManagerSpy.close();
        inOrder.verify(roleManagerSpy).removeDeviceFromOperationalDS(Mockito.eq(deviceInfo), Mockito.anyInt());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testOnDeviceContextLevelDown() throws Exception {
        roleManagerSpy.onDeviceContextLevelDown(deviceInfo);
        inOrder.verify(roleManagerSpy).onDeviceContextLevelDown(deviceInfo);
        inOrder.verifyNoMoreInteractions();
    }
}