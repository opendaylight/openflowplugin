/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.HashedWheelTimer;
import io.netty.util.TimerTask;
import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ServiceChangeListener;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.impl.registry.flow.DeviceFlowRegistryImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

@RunWith(MockitoJUnitRunner.class)
public class LifecycleConductorImplTest {

    private LifecycleConductorImpl lifecycleConductor;

    @Mock
    private MessageIntelligenceAgency messageIntelligenceAgency;
    @Mock
    private ServiceChangeListener serviceChangeListener;
    @Mock
    private ConcurrentHashMap<DeviceInfo, ServiceChangeListener> serviceChangeListeners;
    @Mock
    private DeviceContext deviceContext;
    @Mock
    private DeviceManager deviceManager;
    @Mock
    private DeviceState deviceState;
    @Mock
    private ConnectionContext connectionContext;
    @Mock
    private FeaturesReply featuresReply;
    @Mock
    private TimerTask timerTask;
    @Mock
    private TimeUnit timeUnit;
    @Mock
    private HashedWheelTimer hashedWheelTimer;
    @Mock
    private ListenableFuture<Void> listenableFuture;
    @Mock
    private StatisticsManager statisticsManager;
    @Mock
    private RpcManager rpcManager;
    @Mock
    private RpcContext rpcContext;
    @Mock
    private DeviceInfo deviceInfo;

    private NodeId nodeId = new NodeId("openflow-junit:1");
    private OfpRole ofpRole = OfpRole.NOCHANGE;
    private KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId));

    @Before
    public void setUp() {
        nodeInstanceIdentifier = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId));

        lifecycleConductor = new LifecycleConductorImpl(messageIntelligenceAgency);
        lifecycleConductor.setSafelyManager(deviceManager);
        lifecycleConductor.setSafelyManager(statisticsManager);
        lifecycleConductor.setSafelyManager(rpcManager);

        when(deviceManager.gainContext(Mockito.any())).thenReturn(deviceContext);
        when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);
        when(deviceContext.getDeviceInfo()).thenReturn(deviceInfo);
        when(rpcManager.gainContext(Mockito.any())).thenReturn(rpcContext);
        when(deviceInfo.getNodeId()).thenReturn(nodeId);
        when(deviceInfo.getDatapathId()).thenReturn(BigInteger.TEN);
        when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(nodeInstanceIdentifier);
        when(deviceContext.getDeviceInfo()).thenReturn(deviceInfo);
        when(rpcManager.gainContext(Mockito.any())).thenReturn(rpcContext);
    }

    @Test
    public void addOneTimeListenerWhenServicesChangesDoneTest() {
        lifecycleConductor.addOneTimeListenerWhenServicesChangesDone(serviceChangeListener, deviceInfo);
        assertEquals(false,lifecycleConductor.isServiceChangeListenersEmpty());
    }


    /**
     * If serviceChangeListeners is empty NOTHING should happen
     */
    @Test
    public void notifyServiceChangeListenersTest1() {
        lifecycleConductor.notifyServiceChangeListeners(deviceInfo,true);
        when(serviceChangeListeners.size()).thenReturn(0);
        verify(serviceChangeListeners,times(0)).remove(deviceInfo);
    }

    /**
     * If serviceChangeListeners is NOT empty remove(nodeID) should be removed
     */
    @Test
    public void notifyServiceChangeListenersTest2() {
        lifecycleConductor.addOneTimeListenerWhenServicesChangesDone(serviceChangeListener, deviceInfo);
        assertEquals(false,lifecycleConductor.isServiceChangeListenersEmpty());
        lifecycleConductor.notifyServiceChangeListeners(deviceInfo,true);
        assertEquals(true,lifecycleConductor.isServiceChangeListenersEmpty());
    }


    /**
     * When success flag is set to FALSE nodeID connection should be closed
     */
    @Test
    public void roleInitializationDoneTest1() {
        lifecycleConductor.addOneTimeListenerWhenServicesChangesDone(serviceChangeListener, deviceInfo);
        lifecycleConductor.roleInitializationDone(deviceInfo,false);
        verify(deviceContext,times(1)).shutdownConnection();
    }

    /**
     * When success flag is set to TRUE LOG should be printed
     */
    @Test
    public void roleInitializationDoneTest2() {
        lifecycleConductor.addOneTimeListenerWhenServicesChangesDone(serviceChangeListener, deviceInfo);
        lifecycleConductor.roleInitializationDone(deviceInfo,true);
        verify(deviceContext,times(0)).shutdownConnection();
    }

    /**
     * When getDeviceContext returns null raise exception
     */
    @Test(expected = NullPointerException.class)
    public void roleChangeOnDeviceTest1() {
        when(deviceManager.gainContext(deviceInfo)).thenReturn(null);
        lifecycleConductor.roleChangeOnDevice(deviceInfo,ofpRole);
        verify(deviceContext,times(0)).shutdownConnection();
        lifecycleConductor.roleChangeOnDevice(deviceInfo,ofpRole);
        verify(deviceContext,times(0)).shutdownConnection();
    }

    /**
     * When OfpRole == BECOMEMASTER setRole(OfpRole.BECOMEMASTER) should be called
     */
    @Test
    public void roleChangeOnDeviceTest4() {
        final DataBroker dataBroker = mock(DataBroker.class);

        when(deviceContext.getDeviceState()).thenReturn(deviceState);
        when(deviceContext.getDeviceFlowRegistry()).thenReturn(new DeviceFlowRegistryImpl(dataBroker, nodeInstanceIdentifier));
        when(deviceManager.gainContext(deviceInfo)).thenReturn(deviceContext);
        when(deviceManager.onClusterRoleChange(deviceInfo, OfpRole.BECOMEMASTER)).thenReturn(listenableFuture);
        lifecycleConductor.roleChangeOnDevice(deviceInfo,OfpRole.BECOMEMASTER);
        verify(statisticsManager).startScheduling(Mockito.<DeviceInfo>any());
    }

    /**
     * When OfpRole != BECOMEMASTER setRole(OfpRole.ECOMESLAVE) should be called
     */
    @Test
    public void roleChangeOnDeviceTest5() {
        final DataBroker dataBroker = mock(DataBroker.class);

        when(deviceContext.getDeviceState()).thenReturn(deviceState);
        when(deviceContext.getDeviceFlowRegistry()).thenReturn(new DeviceFlowRegistryImpl(dataBroker, nodeInstanceIdentifier));
        when(deviceManager.gainContext(deviceInfo)).thenReturn(deviceContext);
        when(deviceManager.onClusterRoleChange(deviceInfo, OfpRole.BECOMESLAVE)).thenReturn(listenableFuture);

        lifecycleConductor.roleChangeOnDevice(deviceInfo,OfpRole.BECOMESLAVE);
        verify(statisticsManager).stopScheduling(Mockito.<DeviceInfo>any());
    }

    /**
     * If getDeviceContext return null then null should be returned
     */
    @Test
    public void gainConnectionStateSafelyTest1() {
        when(deviceManager.gainContext(deviceInfo)).thenReturn(null);
        assertNull(lifecycleConductor.gainConnectionStateSafely(deviceInfo));
    }

    /**
     * If getDeviceContext return deviceContext then getPrimaryConnectionContext should be called
     */
    @Test
    public void gainConnectionStateSafelyTest2() {
        lifecycleConductor.gainConnectionStateSafely(deviceInfo);
        verify(deviceContext,times(1)).getPrimaryConnectionContext();
    }

    /**
     * If getDeviceContext returns null then null should be returned
     */
    @Test
    public void reserveXidForDeviceMessageTest1() {
        when(deviceManager.gainContext(deviceInfo)).thenReturn(null);
        assertNull(lifecycleConductor.reserveXidForDeviceMessage(deviceInfo));
    }

    /**
     * If getDeviceContext returns deviceContext reserveXidForDeviceMessage() should be called
     */
    @Test
    public void reserveXidForDeviceMessageTest2() {
        lifecycleConductor.reserveXidForDeviceMessage(deviceInfo);
        verify(deviceContext,times(1)).reserveXidForDeviceMessage();
    }

    /**
     * When succes flag is set to FALSE connection should be closed
     */
    @Test
    public void deviceStartInitializationDoneTest() {
        lifecycleConductor.deviceStartInitializationDone(deviceInfo, false);
        verify(deviceContext,times(1)).shutdownConnection();
    }

    /**
     * When succes flag is set to FALSE connection should be closed
     */
    @Test
    public void deviceInitializationDoneTest() {
        lifecycleConductor.deviceInitializationDone(deviceInfo, false);
        verify(deviceContext,times(1)).shutdownConnection();
    }
}
