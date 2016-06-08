/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.role;


import java.math.BigInteger;

import com.google.common.base.VerifyException;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
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
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceTerminationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleConductor;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.RoleChangeListener;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
    GetFeaturesOutput featuresOutput;

    private RoleManagerImpl roleManager;
    private RoleManagerImpl roleManagerSpy;
    private RoleContext roleContextSpy;
    private final NodeId nodeId = NodeId.getDefaultInstance("openflow:1");
    private final NodeId nodeId2 = NodeId.getDefaultInstance("openflow:2");


    private final EntityOwnershipChange masterEntity = new EntityOwnershipChange(RoleManagerImpl.makeEntity(nodeId), false, true, true);
    private final EntityOwnershipChange masterTxEntity = new EntityOwnershipChange(RoleManagerImpl.makeTxEntity(nodeId), false, true, true);
    private final EntityOwnershipChange slaveEntity = new EntityOwnershipChange(RoleManagerImpl.makeEntity(nodeId), true, false, true);
    private final EntityOwnershipChange slaveTxEntityLast = new EntityOwnershipChange(RoleManagerImpl.makeTxEntity(nodeId), true, false, false);
    private final EntityOwnershipChange masterEntityNotOwner = new EntityOwnershipChange(RoleManagerImpl.makeEntity(nodeId), true, false, true);

    private InOrder inOrder;

    @Before
    public void setUp() throws Exception {
        CheckedFuture<Void, TransactionCommitFailedException> future = Futures.immediateCheckedFuture(null);
        Mockito.when(entityOwnershipService.registerListener(Mockito.anyString(), Mockito.any(EntityOwnershipListener.class))).thenReturn(entityOwnershipListenerRegistration);
        Mockito.when(entityOwnershipService.registerCandidate(Mockito.any(Entity.class))).thenReturn(entityOwnershipCandidateRegistration);
        Mockito.when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);
        Mockito.when(deviceContext.getDeviceState()).thenReturn(deviceState);
        Mockito.when(deviceContext.getDeviceInfo()).thenReturn(deviceInfo);
        Mockito.when(connectionContext.getFeatures()).thenReturn(featuresReply);
        Mockito.when(connectionContext.getNodeId()).thenReturn(nodeId);
        Mockito.when(connectionContext.getConnectionState()).thenReturn(ConnectionContext.CONNECTION_STATE.WORKING);
        Mockito.when(featuresReply.getDatapathId()).thenReturn(new BigInteger("1"));
        Mockito.when(featuresReply.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        Mockito.doNothing().when(deviceInitializationPhaseHandler).onDeviceContextLevelUp(Mockito.<DeviceInfo>any());
        Mockito.doNothing().when(deviceTerminationPhaseHandler).onDeviceContextLevelDown(Mockito.<DeviceInfo>any());
        Mockito.when(dataBroker.newWriteOnlyTransaction()).thenReturn(writeTransaction);
        Mockito.when(writeTransaction.submit()).thenReturn(future);
        Mockito.when(deviceManager.getDeviceContextFromNodeId(deviceInfo)).thenReturn(deviceContext);
        Mockito.when(deviceInfo.getNodeId()).thenReturn(nodeId);
        Mockito.when(deviceInfo2.getNodeId()).thenReturn(nodeId2);
        Mockito.when(deviceInfo.getDatapathId()).thenReturn(BigInteger.TEN);
        roleManager = new RoleManagerImpl(entityOwnershipService, dataBroker, conductor);
        roleManager.setDeviceInitializationPhaseHandler(deviceInitializationPhaseHandler);
        roleManager.setDeviceTerminationPhaseHandler(deviceTerminationPhaseHandler);
        Mockito.when(conductor.getDeviceContext(deviceInfo)).thenReturn(deviceContext);
        roleManagerSpy = Mockito.spy(roleManager);
        roleManagerSpy.onDeviceContextLevelUp(deviceInfo);
        roleContextSpy = Mockito.spy(roleManager.getRoleContext(nodeId));
        Mockito.when(roleContextSpy.getDeviceInfo().getNodeId()).thenReturn(nodeId);
        inOrder = Mockito.inOrder(entityOwnershipListenerRegistration, roleManagerSpy, roleContextSpy);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test(expected = VerifyException.class)
    public void testOnDeviceContextLevelUp() throws Exception {
        roleManagerSpy.onDeviceContextLevelUp(deviceInfo);
        inOrder.verify(roleManagerSpy).onDeviceContextLevelUp(deviceInfo);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testCloseMaster() throws Exception {
        roleManagerSpy.ownershipChanged(masterEntity);
        roleManagerSpy.ownershipChanged(masterTxEntity);
        roleManagerSpy.close();
        inOrder.verify(entityOwnershipListenerRegistration, Mockito.calls(2)).close();
        inOrder.verify(roleManagerSpy).removeDeviceFromOperationalDS(nodeId);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testCloseSlave() throws Exception {
        roleManagerSpy.ownershipChanged(slaveEntity);
        roleManagerSpy.close();
        inOrder.verify(entityOwnershipListenerRegistration, Mockito.calls(2)).close();
        inOrder.verify(roleManagerSpy, Mockito.never()).removeDeviceFromOperationalDS(nodeId);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testOnDeviceContextLevelDown() throws Exception {
        roleManagerSpy.onDeviceContextLevelDown(deviceInfo);
        inOrder.verify(roleManagerSpy).onDeviceContextLevelDown(deviceInfo);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testOwnershipChanged1() throws Exception {
        roleManagerSpy.ownershipChanged(masterEntity);
        inOrder.verify(roleManagerSpy, Mockito.calls(1)).changeOwnershipForMainEntity(Mockito.<EntityOwnershipChange>any(),Mockito.<RoleContext>any());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testOwnershipChanged2() throws Exception {
        Mockito.doNothing().when(roleManagerSpy).makeDeviceRoleChange(Mockito.<OfpRole>any(), Mockito.<RoleContext>any(), Mockito.anyBoolean());
        roleManagerSpy.ownershipChanged(masterEntity);
        roleManagerSpy.ownershipChanged(masterTxEntity);
        inOrder.verify(roleManagerSpy, Mockito.calls(1)).changeOwnershipForTxEntity(Mockito.<EntityOwnershipChange>any(),Mockito.<RoleContext>any());
        inOrder.verify(roleManagerSpy, Mockito.calls(1)).makeDeviceRoleChange(Mockito.<OfpRole>any(), Mockito.<RoleContext>any(), Mockito.anyBoolean());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testChangeOwnershipForMainEntity() throws Exception {
        roleManagerSpy.changeOwnershipForMainEntity(masterEntity, roleContextSpy);
        inOrder.verify(roleContextSpy, Mockito.atLeastOnce()).isMainCandidateRegistered();
        inOrder.verify(roleContextSpy, Mockito.atLeastOnce()).registerCandidate(Mockito.<Entity>any());
    }

    @Test
    public void testChangeOwnershipForMainEntity2() throws Exception {
        Mockito.when(roleContextSpy.isMainCandidateRegistered()).thenReturn(false);
        roleManagerSpy.changeOwnershipForMainEntity(masterEntity, roleContextSpy);
        inOrder.verify(roleContextSpy, Mockito.atLeastOnce()).isMainCandidateRegistered();
    }

    @Test
    public void testChangeOwnershipForTxEntity() throws Exception {
        Mockito.when(roleContextSpy.isTxCandidateRegistered()).thenReturn(true);
        roleManagerSpy.changeOwnershipForTxEntity(slaveTxEntityLast, roleContextSpy);
        inOrder.verify(roleContextSpy, Mockito.atLeastOnce()).isTxCandidateRegistered();
        inOrder.verify(roleContextSpy, Mockito.calls(1)).unregisterCandidate(Mockito.<Entity>any());
        inOrder.verify(roleContextSpy, Mockito.never()).close();
        inOrder.verify(roleManagerSpy, Mockito.calls(1)).removeDeviceFromOperationalDS(Mockito.<NodeId>any());
    }

    @Test
    public void testChangeOwnershipForTxEntity2() throws Exception {
        roleManagerSpy.changeOwnershipForMainEntity(masterEntity, roleContextSpy);
        roleManagerSpy.changeOwnershipForTxEntity(masterTxEntity, roleContextSpy);
        inOrder.verify(roleContextSpy, Mockito.atLeastOnce()).isMainCandidateRegistered();
        inOrder.verify(roleContextSpy, Mockito.calls(1)).registerCandidate(Mockito.<Entity>any());
        inOrder.verify(roleContextSpy, Mockito.atLeastOnce()).isTxCandidateRegistered();
        inOrder.verify(roleManagerSpy, Mockito.calls(1)).makeDeviceRoleChange(Mockito.<OfpRole>any(), Mockito.<RoleContext>any(), Mockito.anyBoolean());
    }

    @Test
    public void testChangeOwnershipForTxEntity3() throws Exception {
        Mockito.when(roleContextSpy.isTxCandidateRegistered()).thenReturn(false);
        roleManagerSpy.changeOwnershipForTxEntity(slaveTxEntityLast, roleContextSpy);
        verify(roleContextSpy).close();
        verify(conductor).closeConnection(deviceInfo);
    }

    @Test
    public void testChangeOwnershipForTxEntity4() throws Exception {
        Mockito.when(roleContextSpy.isTxCandidateRegistered()).thenReturn(true);
        roleManagerSpy.changeOwnershipForTxEntity(masterEntityNotOwner, roleContextSpy);
        verify(roleContextSpy).close();
        verify(conductor).closeConnection(deviceInfo);
    }

    @Test
    public void testAddListener() throws Exception {
        roleManager.addRoleChangeListener((new RoleChangeListener() {
            @Override
            public void roleInitializationDone(final DeviceInfo deviceInfo_, final boolean success) {
                Assert.assertTrue(deviceInfo.equals(deviceInfo_));
                Assert.assertTrue(success);
            }

            @Override
            public void roleChangeOnDevice(final DeviceInfo deviceInfo_, final boolean success, final OfpRole newRole, final boolean initializationPhase) {
                Assert.assertTrue(deviceInfo.equals(deviceInfo_));
                Assert.assertTrue(success);
                Assert.assertFalse(initializationPhase);
                Assert.assertTrue(newRole.equals(OfpRole.BECOMEMASTER));
            }
        }));
        roleManager.notifyListenersRoleInitializationDone(deviceInfo, true);
        roleManager.notifyListenersRoleChangeOnDevice(deviceInfo, true, OfpRole.BECOMEMASTER, false);
    }

    @Test
    public void testMakeDeviceRoleChange() throws Exception{
        roleManagerSpy.makeDeviceRoleChange(OfpRole.BECOMEMASTER, roleContextSpy, true);
        verify(roleManagerSpy, atLeastOnce()).sendRoleChangeToDevice(Mockito.<OfpRole>any(), Mockito.<RoleContext>any());
        verify(roleManagerSpy, atLeastOnce()).notifyListenersRoleChangeOnDevice(Mockito.<DeviceInfo>any(), eq(true), Mockito.<OfpRole>any(), eq(true));
    }

    @Test
    public void testServicesChangeDone() throws Exception {
        roleManagerSpy.setRoleContext(nodeId2, roleContextSpy);
        roleManagerSpy.servicesChangeDone(deviceInfo2, true);
        verify(roleContextSpy).unregisterCandidate(Mockito.<Entity>any());
    }

    @Test
    public void testServicesChangeDoneContextIsNull() throws Exception {
        roleManagerSpy.setRoleContext(nodeId, roleContextSpy);
        roleManagerSpy.servicesChangeDone(deviceInfo2, true);
        verify(roleContextSpy, never()).unregisterCandidate(Mockito.<Entity>any());
    }
}