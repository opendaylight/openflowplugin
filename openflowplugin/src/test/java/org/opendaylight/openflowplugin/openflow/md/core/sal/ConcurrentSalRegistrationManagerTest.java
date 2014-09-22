/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal;

import static java.lang.Thread.sleep;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.common.api.routing.RouteChangeListener;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareService;
import org.opendaylight.controller.sal.binding.api.NotificationListener;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.rpc.RpcContextIdentifier;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.session.SwitchSessionKeyOF;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.Path;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak mbobak@cisco.com on 9/22/14.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConcurrentSalRegistrationManagerTest {


    private static final ExecutorService taskExecutor = Executors.newFixedThreadPool(3);
    private static final SalRegistrationManager registrationManager = new SalRegistrationManager();

    private static final SwitchSessionKeyOF SWITCH_SESSION_KEY_OF = new SwitchSessionKeyOF();
    private static final MockProviderContext MOCK_PROVIDER_CONTEXT = new MockProviderContext();
    private final MockNotificationProviderService MOCK_NOTIFICATION_PROVIDER_SERVICE = new MockNotificationProviderService();

    private static final Logger LOG = LoggerFactory.getLogger(ConcurrentSalRegistrationManagerTest.class);
    private static final long THREAD_SLEEP_MILLIS = 1000;
    private static final String DELAYED_THREAD = "DELAYED_THREAD";
    private static final String NO_DELAY_THREAD = "NO_DELAY_THREAD";

    @Mock
    private SessionContext context;

    @Mock
    private GetFeaturesOutput features;

    @Mock
    private ConnectionConductor connectionConductor;

    @Before
    public void setupRegistrationManager() {
        registrationManager.onSessionInitiated(MOCK_PROVIDER_CONTEXT);
        SWITCH_SESSION_KEY_OF.setDatapathId(BigInteger.ONE);

        when(context.getFeatures()).thenReturn(features);
        when(features.getDatapathId()).thenReturn(BigInteger.valueOf(42));
        when(context.getPrimaryConductor()).thenReturn(connectionConductor);
        when(connectionConductor.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
    }

    @Test
    /**
     * Test method which verifies that session could not be invalidated while in creation.
     */
    public void testConcurrentRemoveSessionContext() throws InterruptedException, ExecutionException {


        Thread delayedThread = new Thread(new Runnable() {
            @Override
            public void run() {
                LOG.info("Delayed session adding thread started.");
                Thread.currentThread().setName(DELAYED_THREAD);
                registrationManager.setPublishService(MOCK_NOTIFICATION_PROVIDER_SERVICE);
                registrationManager.onSessionAdded(SWITCH_SESSION_KEY_OF, context);
                LOG.info("Delayed session adding thread finished.");
            }
        }
        );
        taskExecutor.execute(delayedThread);

        Thread noDelayThread = new Thread(new Runnable() {
            @Override
            public void run() {
                LOG.info("Session removing thread started.");
                Thread.currentThread().setName(NO_DELAY_THREAD);
                registrationManager.setPublishService(MOCK_NOTIFICATION_PROVIDER_SERVICE);
                registrationManager.onSessionRemoved(context);
                LOG.info("Session removing thread finished.");
            }
        }
        );
        taskExecutor.execute(noDelayThread);
        taskExecutor.shutdown();
        while (!taskExecutor.isTerminated()) {
        }
        LOG.info("All tasks have finished.");
    }

    private final class MockNotificationProviderService implements NotificationProviderService {

        @Override
        public void publish(Notification notification) {
            if (Thread.currentThread().getName().equals(DELAYED_THREAD)) {
                try {
                    LOG.info(String.format("Will wait for %d millis", THREAD_SLEEP_MILLIS));
                    sleep(THREAD_SLEEP_MILLIS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
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

    private static final class MockProviderContext implements BindingAwareBroker.ProviderContext {


        @Override
        public void registerFunctionality(BindingAwareProvider.ProviderFunctionality functionality) {

        }

        @Override
        public void unregisterFunctionality(BindingAwareProvider.ProviderFunctionality functionality) {

        }

        @Override
        public <T extends BindingAwareService> T getSALService(Class<T> service) {
            return null;
        }

        @Override
        public <T extends RpcService> BindingAwareBroker.RpcRegistration<T> addRpcImplementation(Class<T> serviceInterface, T implementation) throws IllegalStateException {
            return null;
        }

        @Override
        public <T extends RpcService> BindingAwareBroker.RoutedRpcRegistration<T> addRoutedRpcImplementation(Class<T> serviceInterface, T implementation) throws IllegalStateException {
            return new MockRpcRegistration(implementation);
        }

        @Override
        public <L extends RouteChangeListener<RpcContextIdentifier, InstanceIdentifier<?>>> ListenerRegistration<L> registerRouteChangeListener(L listener) {
            return null;
        }

        @Override
        public <T extends RpcService> T getRpcService(Class<T> serviceInterface) {
            return null;
        }
    }

    private static final class MockRpcRegistration implements BindingAwareBroker.RoutedRpcRegistration {

        private Object implementation;

        public MockRpcRegistration(Object instance) {
            this.implementation = instance;

        }

        @Override
        public void registerInstance(Class context, InstanceIdentifier instance) {

        }

        @Override
        public void unregisterInstance(Class context, InstanceIdentifier instance) {

        }

        @Override
        public Object getInstance() {
            return this.implementation;
        }

        @Override
        public void registerPath(Object context, Path path) {

        }

        @Override
        public void unregisterPath(Object context, Path path) {

        }

        @Override
        public Class getServiceType() {
            return null;
        }

        @Override
        public void close() {

        }
    }

}
