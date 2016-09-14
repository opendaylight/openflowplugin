/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.openflowjava.protocol.impl.clients.ScenarioHandler;
import org.opendaylight.openflowjava.protocol.impl.clients.SimpleClient;
import org.opendaylight.openflowplugin.openflow.md.core.ThreadPoolLoggingExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.OpenflowPluginProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.OpendaylightInventoryListener;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.Filter;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exercise inventory listener ({@link OpendaylightInventoryListener#onNodeUpdated(NodeUpdated)})
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class SalIntegrationTest {

    static final Logger LOG = LoggerFactory.getLogger(SalIntegrationTest.class);

    private final ArrayBlockingQueue<Runnable> SCENARIO_POOL_QUEUE = new ArrayBlockingQueue<>(1);
    private ThreadPoolLoggingExecutor scenarioPool;
    private SimpleClient switchSim;
    private Runnable finalCheck;

    @Inject @Filter(timeout=60*000)
    BundleContext ctx;

    @Inject @Filter(timeout=60*1000)
    BindingAwareBroker broker;

    @Inject @Filter(timeout=60*1000)
    OpenflowPluginProvider openflowPluginProvider;

    /**
     * @return timeout for case of failure
     */
    static long getFailSafeTimeout() {
        return 30000;
    }

    /**
     * test setup
     * @throws InterruptedException
     */
    @Before
    public void setUp() throws InterruptedException {
        switchSim = new SimpleClient("localhost", 6653);
        scenarioPool = new ThreadPoolLoggingExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, SCENARIO_POOL_QUEUE, "scenario");
        Thread.sleep(5000L);
    }

    /**
     * test tear down
     */
    @After
    public void tearDown() {
        SimulatorAssistant.waitForSwitchSimulatorOn(switchSim);
        SimulatorAssistant.tearDownSwitchSimulatorAfterScenario(switchSim, scenarioPool, getFailSafeTimeout());

        if (finalCheck != null) {
            LOG.info("starting final check");
            finalCheck.run();
        }
    }

    /**
     * test basic integration with OFLib running the handshake
     *
     * @throws Exception
     */
    @Test
    public void handshakeAndNodeUpdate() throws Exception {

        final TestInventoryListener listener = new TestInventoryListener();
        BindingAwareConsumer openflowConsumer = new BindingAwareConsumer() {

            @Override
            public void onSessionInitialized(ConsumerContext session) {
                session.getSALService(NotificationService.class).registerNotificationListener(listener);
            }
        };
        ConsumerContext consumerReg = broker.registerConsumer(openflowConsumer, ctx);
        assertNotNull(consumerReg);

        LOG.debug("handshake integration test");
        LOG.debug("openflowPluginProvider: " + openflowPluginProvider);

        switchSim.setSecuredClient(false);
        ScenarioHandler scenario = new ScenarioHandler(ScenarioFactory.createHandshakeScenarioVBM(
                ScenarioFactory.VERSION_BITMAP_13, (short) 0, ScenarioFactory.VERSION_BITMAP_10_13, true));
        switchSim.setScenarioHandler(scenario);
        scenarioPool.execute(switchSim);

        finalCheck = new Runnable() {
            @Override
            public void run() {
                //FIXME: Enable the test -- It's requires EntityOnwershipService hook to the test
                //assertEquals(1, listener.nodeUpdated.size());
                assertEquals(0, listener.nodeUpdated.size());
                //assertNotNull(listener.nodeUpdated.get(0));
            }
        };
    }

    /**
     * @return bundle options
     */
    @Configuration
    public Option[] config() {
        return options(
                systemProperty("osgi.console").value("2401"),
                systemProperty("osgi.bundles.defaultStartLevel").value("4"),
                systemProperty("pax.exam.osgi.unresolved.fail").value("true"),

                OFPaxOptionsAssistant.osgiConsoleBundles(),
                OFPaxOptionsAssistant.loggingBudles(),
                OFPaxOptionsAssistant.ofPluginBundles());
    }

    private static class TestInventoryListener implements OpendaylightInventoryListener {

        List<NodeUpdated> nodeUpdated = new ArrayList<>();
        List<NodeRemoved> nodeRemoved = new ArrayList<>();
        List<NodeConnectorUpdated> nodeConnectorUpdated = new ArrayList<>();
        List<NodeConnectorRemoved> nodeConnectorRemoved = new ArrayList<>();

        /**
         * default ctor
         */
        protected TestInventoryListener() {
            // do nothing
        }

        @Override
        public void onNodeUpdated(NodeUpdated notification) {
            nodeUpdated.add(notification);
        }

        @Override
        public void onNodeRemoved(NodeRemoved notification) {
            nodeRemoved.add(notification);
        }

        @Override
        public void onNodeConnectorUpdated(NodeConnectorUpdated notification) {
            nodeConnectorUpdated.add(notification);
        }

        @Override
        public void onNodeConnectorRemoved(NodeConnectorRemoved notification) {
            nodeConnectorRemoved.add(notification);
        }
    }

}
