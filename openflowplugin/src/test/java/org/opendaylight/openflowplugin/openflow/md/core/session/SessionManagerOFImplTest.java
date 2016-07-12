/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.session;

import static org.mockito.Matchers.any;

import com.google.common.util.concurrent.ListeningExecutorService;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.core.NotificationEnqueuer;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionManager;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SwitchSessionKeyOF;
import org.opendaylight.openflowplugin.openflow.md.core.role.OfEntityManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl;
import org.opendaylight.openflowplugin.openflow.md.core.sal.SalRegistrationManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yangtools.yang.binding.RpcService;

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
    private RpcProviderRegistry rpcProviderRegistry;
    @Mock
    private NotificationEnqueuer notificationEnqueuer;
    @Mock
    private ConnectionAdapter connectionAdapter;
    @Mock
    private DataBroker dataService;

    @Mock
    private OfEntityManager entManager;

    @Mock
    private ModelDrivenSwitchImpl ofSwitch;


    /**
     * prepare session manager
     */
    @Before
    public void setUp() {
        // context
        Mockito.when(context.getPrimaryConductor()).thenReturn(primaryConductor);
        Mockito.when(context.getNotificationEnqueuer()).thenReturn(notificationEnqueuer);

        // provider context - registration responder
        Mockito.when(rpcProviderRegistry.addRoutedRpcImplementation(Matchers.<Class<RpcService>> any(), any(RpcService.class)))
        .then(new Answer<RoutedRpcRegistration<?>>() {
            @Override
            public RoutedRpcRegistration<?> answer(final InvocationOnMock invocation) {
                final Object[] args = invocation.getArguments();
                final RoutedRpcRegistration<RpcService> registration = Mockito.mock(RoutedRpcRegistration.class);
                Mockito.when(registration.getInstance()).thenReturn((RpcService) args[1]);
                return registration;
            }
        });

        // session listener - prepare registration and notification mockery
        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        final SalRegistrationManager sessionListener = new SalRegistrationManager(convertorManager);
        sessionListener.setPublishService(notificationProviderService);
        sessionListener.setRpcProviderRegistry(rpcProviderRegistry);
        sessionListener.setDataService(dataService);
        sessionListener.setOfEntityManager(entManager);

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
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.core.session.SessionManagerOFImpl#addSessionContext(SwitchSessionKeyOF, SessionContext)}.
     */
    @Test
    public void testAddSessionContext() {
        // prepare mocks and values
        final GetFeaturesOutputBuilder featuresBld = new GetFeaturesOutputBuilder().setDatapathId(BigInteger.valueOf(42));
        featuresBld.setVersion((short) 123);
        Mockito.when(context.getFeatures()).thenReturn(featuresBld.build());
        Mockito.when(primaryConductor.getConnectionAdapter()).thenReturn(connectionAdapter);
        Mockito.when(connectionAdapter.getRemoteAddress()).thenReturn(new InetSocketAddress("10.1.2.3", 4242));

        //test target
        sm.addSessionContext(sessionKey, context);

        //capture
        //ArgumentCaptor<NotificationQueueWrapper> notifCaptor = ArgumentCaptor.forClass(NotificationQueueWrapper.class);
        //Mockito.verify(notificationEnqueuer).enqueueNotification(notifCaptor.capture());
        //check
        //Notification notification = notifCaptor.getValue().getNotification();
        //Assert.assertEquals(NodeUpdated.class, notification.getImplementedInterface());
        //FlowCapableNodeUpdated fcNodeUpdate = ((NodeUpdated) notification).getAugmentation(FlowCapableNodeUpdated.class);

        //Assert.assertNotNull(fcNodeUpdate);
        //Assert.assertEquals("10.1.2.3", fcNodeUpdate.getIpAddress().getIpv4Address().getValue());
    }

}
