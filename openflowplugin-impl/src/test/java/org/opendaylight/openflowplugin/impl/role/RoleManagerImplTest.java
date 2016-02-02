/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.role;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipChange;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListener;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListenerRegistration;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipState;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SalRoleService;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

/**
 * Created by Jozef Bacigal
 * Date: 22.1.2016.
 * Time: 23:50
 */
public class RoleManagerImplTest {


    @Mock
    private DeviceInitializationPhaseHandler deviceInitializationPhaseHandler;

    @Mock
    private EntityOwnershipService entityOwnershipService;

    @Mock
    private DataBroker dataBroker;

    @Mock
    private RpcProviderRegistry rpcProviderRegistry;

    @Mock
    private DeviceContext deviceContext;

    @Mock
    private ConnectionContext connectionContext;

    @Mock
    private DeviceState deviceState;

    @Mock
    private SalRoleService salRoleService;

    @Mock
    private GetFeaturesOutput getFeaturesOutput;

    @Mock
    private FeaturesReply featuresReply;

    @Mock
    RoleContextImpl roleContext;

    @Mock
    Optional<EntityOwnershipState> ownershipState;

    @Mock
    EntityOwnershipState entityOwnershipState;

    @Mock
    WriteTransaction wTx;

    @Mock
    CheckedFuture<Void, TransactionCommitFailedException> future;// = Futures.immediateFailedCheckedFuture(new TransactionCommitFailedException);

    RoleManagerImpl roleManager;

    @Mock
    EntityOwnershipListenerRegistration entityOwnershipListenerRegistration;

    private final NodeId nodeId = NodeId.getDefaultInstance("openflow:1");
    private final KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier = DeviceStateUtil.createNodeInstanceIdentifier(nodeId);
    private final Entity entity = new Entity(RoleManager.ENTITY_TYPE, nodeId.getValue());

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(dataBroker.newWriteOnlyTransaction()).thenReturn(wTx);
        when(wTx.submit()).thenReturn(future);
        when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);
        when(deviceState.getNodeInstanceIdentifier()).thenReturn(instanceIdentifier);
        when(deviceState.getFeatures()).thenReturn(getFeaturesOutput);
        when(deviceContext.getDeviceState()).thenReturn(deviceState);
        when(connectionContext.getNodeId()).thenReturn(nodeId);
        when(rpcProviderRegistry.getRpcService(SalRoleService.class)).thenReturn(salRoleService);
        when(getFeaturesOutput.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(deviceContext.getPrimaryConnectionContext().getFeatures()).thenReturn(featuresReply);
        when(deviceContext.getPrimaryConnectionContext().getConnectionState()).thenReturn(ConnectionContext.CONNECTION_STATE.WORKING);
        when(deviceContext.getDeviceState().getNodeId()).thenReturn(nodeId);
        when(entityOwnershipService.registerListener(anyString(), any(EntityOwnershipListener.class))).thenReturn(entityOwnershipListenerRegistration);
        when(roleContext.getEntity()).thenReturn(entity);
        when(ownershipState.isPresent()).thenReturn(true);
        when(ownershipState.get()).thenReturn(entityOwnershipState);
        when(entityOwnershipService.getOwnershipState(entity)).thenReturn(ownershipState);
        roleManager = new RoleManagerImpl(entityOwnershipService, dataBroker, false);
        roleManager.setDeviceInitializationPhaseHandler(deviceInitializationPhaseHandler);
    }


    @Test
    public void testOnDeviceContextLevelUpVersion13() throws Exception {
        roleManager.onDeviceContextLevelUp(deviceContext);
        verify(deviceContext).addDeviceContextClosedHandler(roleManager);
        verify(entityOwnershipService).registerCandidate(entity);
    }

    @Test
    public void testOnDeviceContextLevelUpVersion10() throws Exception {
        when(getFeaturesOutput.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_0);
        roleManager.onDeviceContextLevelUp(deviceContext);
        verify(deviceContext, never()).addDeviceContextClosedHandler(roleManager);
        verify(entityOwnershipService, never()).registerCandidate(entity);
        verify(deviceInitializationPhaseHandler).onDeviceContextLevelUp(deviceContext);
    }

    @Test
    public void testOnDeviceContextLevelUp() throws Exception {
        roleManager.onDeviceContextLevelUp(deviceContext);
        verify(deviceContext).addDeviceContextClosedHandler(roleManager);
        verify(entityOwnershipService).registerCandidate(entity);
        Assert.assertTrue(roleManager.getContexts().size() == 1);
    }

    @Test
    public void testGetRoleContextLevelUp() throws Exception {
        roleManager.getRoleContextLevelUp(deviceContext);
        verify(deviceInitializationPhaseHandler).onDeviceContextLevelUp(deviceContext);
    }

    @Test
    public void testCloseHasOwner() throws Exception {
        when(entityOwnershipState.hasOwner()).thenReturn(true);
        roleManager.onDeviceContextLevelUp(deviceContext);
        roleManager.close();
        verify(entityOwnershipListenerRegistration, atLeastOnce()).close();
        verify(dataBroker, never()).newWriteOnlyTransaction();
        Assert.assertTrue(roleManager.getContexts().size() == 0);
    }

    @Test
    public void testCloseHasOwnerFalse() throws Exception {
        when(entityOwnershipState.hasOwner()).thenReturn(false);
        roleManager.onDeviceContextLevelUp(deviceContext);
        roleManager.close();
        verify(entityOwnershipListenerRegistration, atLeastOnce()).close();
        verify(dataBroker).newWriteOnlyTransaction();
        Assert.assertTrue(roleManager.getContexts().size() == 0);
    }

    @Test
    public void testOnDeviceContextClosedRoleNull() throws Exception {
        roleManager.onDeviceContextLevelUp(deviceContext);
        roleManager.onDeviceContextClosed(deviceContext);
        Assert.assertTrue(roleManager.getContexts().size() == 0);
    }

    @Test
    public void testOnDeviceContextClosedRoleMaster() throws Exception {
        when(deviceState.getRole()).thenReturn(OfpRole.BECOMEMASTER);
        roleManager.onDeviceContextLevelUp(deviceContext);
        Assert.assertTrue(roleManager.getContexts().size() == 1);
        roleManager.onDeviceContextClosed(deviceContext);
        Assert.assertTrue(roleManager.getContexts().size() == 0);
    }

    @Test
    public void testOnDeviceContextClosedRoleSlave() throws Exception {
        when(deviceState.getRole()).thenReturn(OfpRole.BECOMESLAVE);
        roleManager.onDeviceContextLevelUp(deviceContext);
        roleManager.onDeviceContextClosed(deviceContext);
        Assert.assertTrue(roleManager.getContexts().size() == 0);
    }

    @Test
    public void testOwnershipChanged() throws Exception {
        roleManager.onDeviceContextLevelUp(deviceContext);
        EntityOwnershipChange entityOwnershipChange  = new EntityOwnershipChange(entity, true, false, false);
        roleManager.ownershipChanged(entityOwnershipChange);
        verify(dataBroker).newWriteOnlyTransaction();
    }
}