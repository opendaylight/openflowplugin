/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal;

import com.google.common.util.concurrent.ListeningExecutorService;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Assert;
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
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.core.NotificationEnqueuer;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SwitchSessionKeyOF;
import org.opendaylight.openflowplugin.openflow.md.core.ThreadPoolLoggingExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Capabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak mbobak@cisco.com on 9/22/14.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConcurrentSalRegistrationManagerTest {


    /** registration related action must end within this amount of seconds */
    private static final int REGISTRATION_ACTION_TIMEOUT = 5;
    protected SalRegistrationManager registrationManager;
    protected static final Logger LOG = LoggerFactory.getLogger(ConcurrentSalRegistrationManagerTest.class);
    protected static final SwitchSessionKeyOF SWITCH_SESSION_KEY_OF = new SwitchSessionKeyOF();

    private static final long THREAD_SLEEP_MILLIS = 100;
    private static final String DELAYED_THREAD = "DELAYED_THREAD";
    private static final String NO_DELAY_THREAD = "NO_DELAY_THREAD";

    private ThreadPoolCollectingExecutor taskExecutor;

    @Mock
    protected SessionContext context;
    @Mock
    private ConnectionConductor connectionConductor;
    @Mock
    private ListeningExecutorService rpcPool;
    @Mock
    private NotificationProviderService notificationProviderService;
    @Mock
    private RpcProviderRegistry rpcProviderRegistry;
    @Mock
    private DataBroker dataBroker;
    @Mock
    private NotificationEnqueuer notificationEnqueuer;
    @Mock
    private ConnectionAdapter connectionAdapter;

    private GetFeaturesOutput features;

    /**
     * prepare surrounding objects
     */
    @Before
    public void setUp() {
        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        registrationManager = new SalRegistrationManager(convertorManager);
        SWITCH_SESSION_KEY_OF.setDatapathId(BigInteger.ONE);
        Mockito.when(context.getNotificationEnqueuer()).thenReturn(notificationEnqueuer);

        // features mockery
        features = new GetFeaturesOutputBuilder()
        .setVersion(OFConstants.OFP_VERSION_1_3)
        .setDatapathId(BigInteger.valueOf(42))
        .setCapabilities(new Capabilities(true, true, true, true, true, true, true))
        .build();
        Mockito.when(context.getFeatures()).thenReturn(features);

        Mockito.when(context.getPrimaryConductor()).thenReturn(connectionConductor);
        Mockito.when(context.getSessionKey()).thenReturn(SWITCH_SESSION_KEY_OF);
        Mockito.when(connectionConductor.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);

        // provider context - registration responder
        Mockito.when(rpcProviderRegistry.addRoutedRpcImplementation(Matchers.<Class<RpcService>>any(), Matchers.any(RpcService.class)))
        .then(new Answer<RoutedRpcRegistration<?>>() {
            @Override
            public RoutedRpcRegistration<?> answer(InvocationOnMock invocation) {
                if (Thread.currentThread().getName().equals(DELAYED_THREAD)) {
                    try {
                        LOG.info(String.format("Will wait for %d millis", THREAD_SLEEP_MILLIS/10));
                        Thread.sleep(THREAD_SLEEP_MILLIS);
                    } catch (InterruptedException e) {
                        LOG.error("delaying of worker thread [{}] failed.", Thread.currentThread().getName(), e);
                    }
                }

                Object[] args = invocation.getArguments();
                RoutedRpcRegistration<RpcService> registration = Mockito.mock(RoutedRpcRegistration.class);
                Mockito.when(registration.getInstance()).thenReturn((RpcService) args[1]);

                return registration;
            }
        });

        Mockito.when(connectionConductor.getConnectionAdapter()).thenReturn(connectionAdapter);
        Mockito.when(connectionAdapter.getRemoteAddress()).thenReturn(new InetSocketAddress("10.1.2.3", 4242));

        taskExecutor = new ThreadPoolCollectingExecutor(
                2, 2, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(2), "junit");

        registrationManager.setRpcProviderRegistry(rpcProviderRegistry);
        registrationManager.setDataService(dataBroker);
        registrationManager.setPublishService(notificationProviderService);
        registrationManager.init();
        OFSessionUtil.getSessionManager().setRpcPool(rpcPool);
    }

    /**
     * clean up
     * @throws InterruptedException
     */
    @After
    public void tearDown() throws InterruptedException {
        taskExecutor.shutdown();
        taskExecutor.awaitTermination(1, TimeUnit.SECONDS);
        if (!taskExecutor.isTerminated()) {
            taskExecutor.shutdownNow();
        }
        LOG.info("All tasks have finished.");

        LOG.info("amount of scheduled threads: {}, exited threads: {}, failed threads: {}",
                taskExecutor.getTaskCount(), taskExecutor.getThreadExitCounter(), taskExecutor.getFailLogbook().size());
        for (String exitStatus : taskExecutor.getFailLogbook()) {
            LOG.debug(exitStatus);
        }

        OFSessionUtil.releaseSessionManager();
        Assert.assertTrue("there should not be any failed threads in the pool", taskExecutor.getFailLogbook().isEmpty());
        Assert.assertTrue("there should not be any living thread in the pool", taskExecutor.getActiveCount() == 0);
    }

    /**
     * Test method which verifies that session could not be invalidated while in creation.
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     */
    @Test
    public void testConcurrentRemoveSessionContext() throws InterruptedException, ExecutionException, TimeoutException {
        // run registrations
        Callable<Void> delayedThread = new Callable<Void>() {
            @Override
            public Void call() {
                LOG.info("Delayed session adding thread started.");
                Thread.currentThread().setName(DELAYED_THREAD);
                OFSessionUtil.getSessionManager().addSessionContext(SWITCH_SESSION_KEY_OF, context);
                LOG.info("Delayed session adding thread finished.");
                return null;
            }
        };

        Callable<Void> noDelayThread = new Callable<Void>() {
            @Override
            public Void call() {
                LOG.info("Session removing thread started.");
                Thread.currentThread().setName(NO_DELAY_THREAD);
                OFSessionUtil.getSessionManager().invalidateSessionContext(SWITCH_SESSION_KEY_OF);
                LOG.info("Session removing thread finished.");
                return null;
            }
        };

        Future<Void> addSessionResult = taskExecutor.submit(delayedThread);
        Future<Void> removeSessionResult = taskExecutor.submit(noDelayThread);

        addSessionResult.get(REGISTRATION_ACTION_TIMEOUT, TimeUnit.SECONDS);
        removeSessionResult.get(REGISTRATION_ACTION_TIMEOUT, TimeUnit.SECONDS);
    }

    private static class ThreadPoolCollectingExecutor extends ThreadPoolLoggingExecutor {

        private List<String> failLogbook;
        private int threadExitCounter = 0;

        /**
         * @param corePoolSize
         * @param maximumPoolSize
         * @param keepAliveTime
         * @param unit
         * @param workQueue
         * @param poolName
         */
        public ThreadPoolCollectingExecutor(int corePoolSize,
                int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                BlockingQueue<Runnable> workQueue, String poolName) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, poolName);

            failLogbook = Collections.synchronizedList(new ArrayList<String>());
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            threadExitCounter ++;

            if (t != null) {
                failLogbook.add("job ["+r+"] exited with throwable:" + t.getMessage());
            }
        }

        /**
         * @return the chronicles
         */
        public List<String> getFailLogbook() {
            return failLogbook;
        }

        /**
         * @return the threadExitCounter
         */
        public int getThreadExitCounter() {
            return threadExitCounter;
        }
    }
}
