/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import com.google.common.util.concurrent.Futures;
import java.util.concurrent.ExecutorService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.mdsal.eos.binding.api.Entity;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipChange;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListenerRegistration;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipService;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipChangeState;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionStatus;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipState;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.api.openflow.mastership.ReconciliationFrameworkEvent;
import org.opendaylight.openflowplugin.api.openflow.mastership.ReconciliationFrameworkRegistration;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.openflowplugin.impl.mastership.MastershipChangeServiceManagerImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;

@RunWith(MockitoJUnitRunner.class)
public class ContextChainHolderImplTest {

    private static final String ENTITY_TEST = "EntityTest";
    private static final String OPENFLOW_TEST = "openflow:test";
    private static final Short AUXILIARY_ID = 0;
    @Mock
    private StatisticsManager statisticsManager;
    @Mock
    private RpcManager rpcManager;
    @Mock
    private DeviceManager deviceManager;
    @Mock
    private RoleManager roleManager;
    @Mock
    private StatisticsContext statisticsContext;
    @Mock
    private RpcContext rpcContext;
    @Mock
    private DeviceContext deviceContext;
    @Mock
    private RoleContext roleContext;
    @Mock
    private ConnectionContext connectionContext;
    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private ClusterSingletonServiceProvider singletonServicesProvider;
    @Mock
    private ExecutorService executorService;
    @Mock
    private ClusterSingletonServiceRegistration clusterSingletonServiceRegistration;
    @Mock
    private EntityOwnershipService entityOwnershipService;
    @Mock
    private EntityOwnershipListenerRegistration entityOwnershipListenerRegistration;
    @Mock
    private ReconciliationFrameworkEvent reconciliationFrameworkEvent;
    @Mock
    private FeaturesReply featuresReply;

    private ContextChainHolderImpl contextChainHolder;
    private ReconciliationFrameworkRegistration registration;
    private MastershipChangeServiceManager manager = new MastershipChangeServiceManagerImpl();

    @Before
    public void setUp() throws Exception {
        Mockito.doAnswer(invocation -> {
            invocation.getArgumentAt(0, Runnable.class).run();
            return null;
        }).when(executorService).submit(Mockito.<Runnable>any());


        Mockito.when(connectionContext.getDeviceInfo()).thenReturn(deviceInfo);
        Mockito.when(deviceManager.createContext(connectionContext)).thenReturn(deviceContext);
        Mockito.when(rpcManager.createContext(deviceContext)).thenReturn(rpcContext);
        Mockito.when(roleManager.createContext(deviceContext)).thenReturn(roleContext);
        Mockito.when(statisticsManager.createContext(Mockito.eq(deviceContext), Mockito.anyBoolean()))
            .thenReturn(statisticsContext);
        Mockito.when(deviceContext.getDeviceInfo()).thenReturn(deviceInfo);

        Mockito.when(singletonServicesProvider.registerClusterSingletonService(Mockito.any()))
                .thenReturn(clusterSingletonServiceRegistration);
        Mockito.when(entityOwnershipService.registerListener(Mockito.any(), Mockito.any()))
                .thenReturn(entityOwnershipListenerRegistration);
        Mockito.when(connectionContext.getFeatures()).thenReturn(featuresReply);
        Mockito.when(featuresReply.getAuxiliaryId()).thenReturn(AUXILIARY_ID);

        registration = manager.reconciliationFrameworkRegistration(reconciliationFrameworkEvent);

        contextChainHolder = new ContextChainHolderImpl(
                executorService,
                singletonServicesProvider,
                entityOwnershipService,
                manager);
        contextChainHolder.addManager(statisticsManager);
        contextChainHolder.addManager(rpcManager);
        contextChainHolder.addManager(deviceManager);
        contextChainHolder.addManager(roleManager);
    }

    @Test
    public void addManager() throws Exception {
        Assert.assertTrue(contextChainHolder.checkAllManagers());
    }

    @Test
    public void createContextChain() throws Exception {
        contextChainHolder.createContextChain(connectionContext);
        Mockito.verify(deviceManager).createContext(Mockito.any(ConnectionContext.class));
        Mockito.verify(rpcManager).createContext(Mockito.any(DeviceContext.class));
        Mockito.verify(roleManager).createContext(Mockito.any(DeviceContext.class));
        Mockito.verify(statisticsManager).createContext(Mockito.any(DeviceContext.class), Mockito.anyBoolean());
    }


    @Test
    public void reconciliationFrameworkFailure() throws Exception {
        Mockito.when(reconciliationFrameworkEvent.onDevicePrepared(deviceInfo))
            .thenReturn(Futures.immediateFailedFuture(new Throwable("test")));
        contextChainHolder.createContextChain(connectionContext);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.INITIAL_FLOW_REGISTRY_FILL);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.INITIAL_GATHERING);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.RPC_REGISTRATION);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.MASTER_ON_DEVICE);
        Mockito.verify(connectionContext).closeConnection(false);
    }

    @Test
    public void reconciliationFrameworkDisconnect() throws Exception {
        Mockito.when(reconciliationFrameworkEvent.onDevicePrepared(deviceInfo))
            .thenReturn(Futures.immediateFuture(ResultState.DISCONNECT));
        contextChainHolder.createContextChain(connectionContext);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.INITIAL_FLOW_REGISTRY_FILL);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.INITIAL_GATHERING);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.RPC_REGISTRATION);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.MASTER_ON_DEVICE);
        Mockito.verify(connectionContext).closeConnection(false);
    }

    @Test
    public void reconciliationFrameworkSuccess() throws Exception {
        contextChainHolder.createContextChain(connectionContext);
        Mockito.when(reconciliationFrameworkEvent.onDevicePrepared(deviceInfo))
            .thenReturn(Futures.immediateFuture(ResultState.DONOTHING));
        contextChainHolder.createContextChain(connectionContext);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.INITIAL_FLOW_REGISTRY_FILL);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.INITIAL_GATHERING);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.RPC_REGISTRATION);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.MASTER_ON_DEVICE);
        Mockito.verify(reconciliationFrameworkEvent).onDevicePrepared(deviceInfo);
    }

    @Test
    public void reconciliationFrameworkSuccessButNotSubmit() throws Exception {
        contextChainHolder.createContextChain(connectionContext);
        Mockito.when(reconciliationFrameworkEvent.onDevicePrepared(deviceInfo))
            .thenReturn(Futures.immediateFuture(ResultState.DONOTHING));
        contextChainHolder.createContextChain(connectionContext);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.INITIAL_FLOW_REGISTRY_FILL);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.RPC_REGISTRATION);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.MASTER_ON_DEVICE);
        contextChainHolder.onNotAbleToStartMastershipMandatory(deviceInfo, "Test reason");
        Mockito.verify(reconciliationFrameworkEvent).onDeviceDisconnected(deviceInfo);
        Mockito.verify(connectionContext).closeConnection(false);
    }

    @Test
    public void deviceMastered() throws Exception {
        registration.close();
        contextChainHolder.createContextChain(connectionContext);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.INITIAL_FLOW_REGISTRY_FILL);
        Assert.assertFalse(contextChainHolder.isAnyDeviceMastered());
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.INITIAL_GATHERING);
        Assert.assertFalse(contextChainHolder.isAnyDeviceMastered());
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.RPC_REGISTRATION);
        Assert.assertFalse(contextChainHolder.isAnyDeviceMastered());
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.MASTER_ON_DEVICE);
        Assert.assertFalse(contextChainHolder.isAnyDeviceMastered());
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.INITIAL_SUBMIT);
        Assert.assertTrue(contextChainHolder.isAnyDeviceMastered());
        Assert.assertTrue(contextChainHolder.listOfMasteredDevices().size() == 1);
    }

    @Test
    public void deviceConnected() throws Exception {
        registration.close();
        Assert.assertTrue(contextChainHolder.deviceConnected(connectionContext)
                == ConnectionStatus.MAY_CONTINUE);
        Short auxiliaryId1 = 1;
        Mockito.when(featuresReply.getAuxiliaryId()).thenReturn(auxiliaryId1);
        Assert.assertTrue(contextChainHolder.deviceConnected(connectionContext)
                == ConnectionStatus.MAY_CONTINUE);
        Mockito.when(featuresReply.getAuxiliaryId()).thenReturn(AUXILIARY_ID);
        Assert.assertTrue(contextChainHolder.deviceConnected(connectionContext)
                == ConnectionStatus.MAY_CONTINUE);
    }

    @Test
    public void notToAbleMastership() throws Exception {
        registration.close();
        contextChainHolder.deviceConnected(connectionContext);
        contextChainHolder.onNotAbleToStartMastership(deviceInfo, "Test reason", true);
        Mockito.verify(deviceContext).close();
        Mockito.verify(statisticsContext).close();
        Mockito.verify(rpcContext).close();
    }

    @Test
    public void notAbleToSetSlave() throws Exception {
        registration.close();
        contextChainHolder.deviceConnected(connectionContext);
        contextChainHolder.onSlaveRoleNotAcquired(deviceInfo, "Test reason");
        Mockito.verify(deviceContext).close();
        Mockito.verify(statisticsContext).close();
        Mockito.verify(rpcContext).close();
    }

    @Test
    public void deviceDisconnected() throws Exception {
        registration.close();
        contextChainHolder.createContextChain(connectionContext);
        contextChainHolder.onDeviceDisconnected(connectionContext);
        Mockito.verify(deviceContext).close();
        Mockito.verify(statisticsContext).close();
        Mockito.verify(rpcContext).close();
    }

    @Test
    public void onClose() throws Exception {
        registration.close();
        contextChainHolder.createContextChain(connectionContext);
        contextChainHolder.close();
        Mockito.verify(deviceContext).close();
        Mockito.verify(statisticsContext).close();
        Mockito.verify(rpcContext).close();
    }

    @Test
    public void ownershipChanged() throws Exception {
        registration.close();
        contextChainHolder.createContextChain(connectionContext);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.INITIAL_FLOW_REGISTRY_FILL);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.INITIAL_GATHERING);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.RPC_REGISTRATION);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.MASTER_ON_DEVICE);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.INITIAL_SUBMIT);
        EntityOwnershipChange ownershipChange = new EntityOwnershipChange(
                new Entity(ENTITY_TEST, OPENFLOW_TEST),
                EntityOwnershipChangeState.LOCAL_OWNERSHIP_LOST_NO_OWNER
        );
        contextChainHolder.ownershipChanged(ownershipChange);
        Mockito.verify(deviceManager).removeDeviceFromOperationalDS(Mockito.any());
    }

    @Test
    public void ownershipChangedButHasOwner() throws Exception {
        registration.close();
        contextChainHolder.createContextChain(connectionContext);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.INITIAL_FLOW_REGISTRY_FILL);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.INITIAL_GATHERING);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.RPC_REGISTRATION);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.MASTER_ON_DEVICE);
        contextChainHolder.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.INITIAL_SUBMIT);
        EntityOwnershipChange ownershipChange = new EntityOwnershipChange(
                new Entity(ENTITY_TEST, OPENFLOW_TEST),
                EntityOwnershipChangeState.LOCAL_OWNERSHIP_LOST_NEW_OWNER
        );
        contextChainHolder.ownershipChanged(ownershipChange);
        Mockito.verify(deviceManager,Mockito.never()).removeDeviceFromOperationalDS(Mockito.any());
    }
}
