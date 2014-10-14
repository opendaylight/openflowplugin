/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.session;

import java.math.BigInteger;
import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.NotificationEnqueuer;
import org.opendaylight.openflowplugin.openflow.md.core.NotificationQueueWrapper;
import org.opendaylight.openflowplugin.openflow.md.core.sal.SalRegistrationManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.RpcService;

import com.google.common.util.concurrent.ListeningExecutorService;

/**
 * test of {@link SessionManagerOFImpl}
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionManagerOFImplTest {
    
    private SessionManager sm;
    private SwitchSessionKeyOF sessionKey;
    @Mock
    private SessionContext context;
    @Mock
    private ConnectionConductor primaryConductor;
    @Mock
    private ListeningExecutorService rpcPool; 
    @Mock
    private NotificationProviderService notificationProviderService; 
    @Mock
    private ProviderContext providerContext;
    @Mock
    private NotificationEnqueuer notificationEnqueuer;
    @Mock
    private ConnectionAdapter connectionAdapter;

    /**
     * prepare session manager
     */
    @Before
    public void setUp() {
        // context
        Mockito.when(context.getPrimaryConductor()).thenReturn(primaryConductor);
        Mockito.when(context.getNotificationEnqueuer()).thenReturn(notificationEnqueuer);
        
        // provider context - registration responder
        Mockito.when(providerContext.addRoutedRpcImplementation(Matchers.any(Class.class), Matchers.any(RpcService.class)))
        .then(new Answer<RoutedRpcRegistration<?>>() {
            @Override
            public RoutedRpcRegistration<?> answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                RoutedRpcRegistration<RpcService> registration = Mockito.mock(RoutedRpcRegistration.class);
                Mockito.when(registration.getInstance()).thenReturn((RpcService) args[1]);
                return registration;
            }
        });
        
        // session listener - prepare registration and notification mockery
        SalRegistrationManager sessionListener = new SalRegistrationManager();
        sessionListener.setPublishService(notificationProviderService);
        sessionListener.setProviderContext(providerContext);
        
        // session manager (mimic SalRegistrationManager.onSessionInitiated())
        sm = SessionManagerOFImpl.getInstance();
        sm.setRpcPool(rpcPool);
        sm.registerSessionListener(sessionListener);
        sm.setNotificationProviderService(notificationProviderService);
        
        // session key - switch id
        sessionKey = new SwitchSessionKeyOF();
        sessionKey.setDatapathId(BigInteger.valueOf(42));
    }
    
    /**
     * free session manager
     */
    @After
    public void tearDown() {
        SessionManagerOFImpl.releaseInstance();
        sessionKey = null;
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.core.session.SessionManagerOFImpl#addSessionContext(org.opendaylight.openflowplugin.openflow.md.core.session.SwitchSessionKeyOF, org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext)}.
     */
    @Test
    public void testAddSessionContext() {
        // prepare mocks and values
        GetFeaturesOutputBuilder featuresBld = new GetFeaturesOutputBuilder().setDatapathId(BigInteger.valueOf(42));
        featuresBld.setVersion((short) 123);
        Mockito.when(context.getFeatures()).thenReturn(featuresBld.build());
        Mockito.when(primaryConductor.getConnectionAdapter()).thenReturn(connectionAdapter);
        Mockito.when(connectionAdapter.getRemoteAddress()).thenReturn(new InetSocketAddress("10.1.2.3", 4242));
        
        //test target
        sm.addSessionContext(sessionKey, context);
        
        //capture
        ArgumentCaptor<NotificationQueueWrapper> notifCaptor = ArgumentCaptor.forClass(NotificationQueueWrapper.class);
        Mockito.verify(notificationEnqueuer).enqueueNotification(notifCaptor.capture());
        //check
        Notification notification = notifCaptor.getValue().getNotification();
        Assert.assertEquals(NodeUpdated.class, notification.getImplementedInterface());
        FlowCapableNodeUpdated fcNodeUpdate = ((NodeUpdated) notification).getAugmentation(FlowCapableNodeUpdated.class);

        Assert.assertNotNull(fcNodeUpdate);
        Assert.assertEquals("10.1.2.3", fcNodeUpdate.getIpAddress().getIpv4Address().getValue());
    }

}
