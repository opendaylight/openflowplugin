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
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.controller.test.sal.binding.it.TestHelper;
import org.opendaylight.openflowjava.protocol.impl.clients.ScenarioHandler;
import org.opendaylight.openflowjava.protocol.impl.clients.SimpleClient;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.OpendaylightInventoryListener;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exercise inventory listener ({@link OpendaylightInventoryListener#onNodeUpdated(NodeUpdated)})
 */
@RunWith(PaxExam.class)
public class SalIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(SalIntegrationTest.class);

    @Inject
    SwitchConnectionProvider switchConnectionProvider;

    @Inject
    BundleContext ctx;

    @Inject
    BindingAwareBroker broker;

    /**
     * @return timeout for case of failure
     */
    private static long getFailSafeTimeout() {
        return 20000;
    }
    
    /**
     * test setup
     * @throws InterruptedException
     */
    @Before
    public void setUp() throws InterruptedException {
        //FIXME: plugin should provide service exposing startup result via future 
        Thread.sleep(5000);
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
        LOG.debug("switchConnectionProvider: " + switchConnectionProvider);

        SimpleClient switchSim = new SimpleClient("localhost", 6653);
        switchSim.setSecuredClient(false);
        ScenarioHandler scenario = new ScenarioHandler(ScenarioFactory.createHandshakeScenarioVBM(
                ScenarioFactory.VERSION_BITMAP_13, (short) 0, ScenarioFactory.VERSION_BITMAP_10_13));
        switchSim.setScenarioHandler(scenario);
        switchSim.start();

        try {
            LOG.debug("tearing down simulator");
            switchSim.getScenarioDone().get(getFailSafeTimeout(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            String msg = "waiting for scenario to finish failed: "+e.getMessage();
            LOG.error(msg, e);
            Assert.fail(msg);
        }
        
        try {
            LOG.debug("checking if simulator succeeded to connect to controller");
            boolean simulatorWasOnline = switchSim.getIsOnlineFuture().get(100, TimeUnit.MILLISECONDS);
            Assert.assertTrue("simulator failed to connect to controller", simulatorWasOnline);
        } catch (Exception e) {
            String message = "simulator probably failed to connect to controller";
            LOG.error(message, e);
            Assert.fail(message);
        }
        
        Thread.sleep(4000);
        assertEquals(1, listener.nodeUpdated.size());
        assertNotNull(listener.nodeUpdated.get(0));
    }

    /**
     * @return bundle options
     */
    @Configuration
    public Option[] config() {
        return options(systemProperty("osgi.console").value("2401"),
                OFPaxOptionsAssistant.osgiConsoleBundles(),
                OFPaxOptionsAssistant.loggingBudles(),
                
                TestHelper.junitAndMockitoBundles(),
                TestHelper.mdSalCoreBundles(), 
                TestHelper.configMinumumBundles(),
                TestHelper.baseModelBundles(),
                TestHelper.flowCapableModelBundles(), 
                
                OFPaxOptionsAssistant.ofPluginBundles()
                );
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
