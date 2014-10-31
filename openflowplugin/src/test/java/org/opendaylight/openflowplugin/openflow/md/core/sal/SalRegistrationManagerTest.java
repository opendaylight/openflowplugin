/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.util.concurrent.Futures;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.NotificationListener;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.common.util.Rpcs;
import org.opendaylight.openflowplugin.openflow.md.ModelDrivenSwitch;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.NotificationEnqueuer;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.session.IMessageDispatchService;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.session.SwitchSessionKeyOF;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yangtools.concepts.CompositeObjectRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Created by Martin Bobak mbobak@cisco.com on 8/26/14.
 */
@RunWith(MockitoJUnitRunner.class)
public class SalRegistrationManagerTest {


    private static final BigInteger dataPathId = BigInteger.ONE;

    private SalRegistrationManager salRegistrationManager;
    @Mock
    private SessionContext context;
    @Mock
    private ConnectionConductor conductor;
    @Mock
    private IMessageDispatchService messageDispatchService;
    @Mock
    private GetFeaturesOutput features;
    @Mock
    private BindingAwareBroker.ProviderContext providerContext;
    @Mock
    private NotificationEnqueuer notificationEnqueuer;

    private ModelDrivenSwitch mdSwitchOF13;

    CompositeObjectRegistration<ModelDrivenSwitch> registration;

    @Before
    public void setUp() {

        Mockito.when(context.getPrimaryConductor()).thenReturn(conductor);
        Mockito.when(context.getMessageDispatchService()).thenReturn(messageDispatchService);
        Mockito.when(conductor.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_0)
                .thenReturn(OFConstants.OFP_VERSION_1_3);
        Mockito.when(context.getFeatures()).thenReturn(features);

        mdSwitchOF13 = new ModelDrivenSwitchImpl(null, null, context);
        registration = new CompositeObjectRegistration<>(mdSwitchOF13, Collections.EMPTY_LIST);

        Mockito.when(context.getProviderRegistration()).thenReturn(registration);
        Mockito.when(context.getNotificationEnqueuer()).thenReturn(notificationEnqueuer);
        Mockito.when(features.getDatapathId()).thenReturn(BigInteger.valueOf(1));
        Mockito.when(features.getVersion()).thenReturn((short) 1);

        Set<RpcError> errorSet = Collections.emptySet();
        UpdateFlowOutputBuilder updateFlowOutput = new UpdateFlowOutputBuilder();
        RpcResult<UpdateFlowOutput> result = Rpcs.getRpcResult(true, updateFlowOutput.build(), errorSet);

        Mockito.when(
                messageDispatchService.flowMod(Matchers.any(FlowModInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        salRegistrationManager = new SalRegistrationManager();
        salRegistrationManager.onSessionInitiated(providerContext);
        salRegistrationManager.setPublishService(new MockNotificationProviderService());
    }

    /**
     * Test for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.SalRegistrationManager#identifierFromDatapathId(java.math.BigInteger)}
     */
    @Test
    public void testIdentifierFromDatapathId() {
        InstanceIdentifier<Node> node = salRegistrationManager.identifierFromDatapathId(dataPathId);
        assertNotNull(node);
        assertEquals("NodeKey [_id=Uri [_value=openflow:1]]", ((KeyedInstanceIdentifier) node).getKey().toString());
    }

    /**
     * Test for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.SalRegistrationManager#nodeKeyFromDatapathId(java.math.BigInteger)}
     */
    @Test
    public void testNodeKeyFromDatapathId() {
        NodeKey nodeKey = salRegistrationManager.nodeKeyFromDatapathId(dataPathId);
        assertNotNull(nodeKey);
        assertEquals("openflow:1", nodeKey.getId().getValue());
    }

    /**
     * Test for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.SalRegistrationManager#nodeIdFromDatapathId(java.math.BigInteger)}
     */
    @Test
    public void testNodeIdFromDatapathId() {
        NodeId nodeId = salRegistrationManager.nodeIdFromDatapathId(dataPathId);
        assertNotNull(nodeId);
        assertEquals("openflow:1", nodeId.getValue());
    }

    /**
     * Test for {@link SalRegistrationManager#getSessionManager()}
     */
    @Test
    public void testGetSessionManager() {
        assertNotNull(salRegistrationManager.getPublishService());
    }


    /**
     * Test for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.SalRegistrationManager#onSessionRemoved(org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext)}
     */
    @Test
    public void testOnSessionRemoved() {
        salRegistrationManager.onSessionRemoved(context);
    }

    /**
     * Test for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.SalRegistrationManager#onSessionAdded(org.opendaylight.openflowplugin.openflow.md.core.session.SwitchSessionKeyOF, org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext)}
     */
    public void testOnAdded() {
        SwitchSessionKeyOF switchSessionKeyOF = new SwitchSessionKeyOF();
        salRegistrationManager.onSessionAdded(switchSessionKeyOF, context);
    }


    private class MockNotificationProviderService implements NotificationProviderService {

        @Override
        public void publish(Notification notification) {

        }

        @Override
        public void publish(Notification notification, ExecutorService executorService) {

        }

        @Override
        public ListenerRegistration<NotificationInterestListener> registerInterestListener(NotificationInterestListener notificationInterestListener) {
            return null;
        }

        @Override
        public <T extends Notification> ListenerRegistration<NotificationListener<T>> registerNotificationListener(Class<T> tClass, NotificationListener<T> tNotificationListener) {
            return null;
        }

        @Override
        public ListenerRegistration<org.opendaylight.yangtools.yang.binding.NotificationListener> registerNotificationListener(org.opendaylight.yangtools.yang.binding.NotificationListener notificationListener) {
            return null;
        }
    }

}

