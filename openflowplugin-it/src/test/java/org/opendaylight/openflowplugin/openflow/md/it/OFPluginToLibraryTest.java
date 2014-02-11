/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.it;

import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import java.util.Stack;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.controller.test.sal.binding.it.TestHelper;
import org.opendaylight.openflowjava.protocol.impl.clients.ClientEvent;
import org.opendaylight.openflowjava.protocol.impl.clients.ScenarioHandler;
import org.opendaylight.openflowjava.protocol.impl.clients.SimpleClient;
import org.opendaylight.openflowjava.protocol.impl.clients.SleepEvent;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
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
 * covers basic handshake scenarios
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OFPluginToLibraryTest {

    private static final Logger LOG = LoggerFactory
            .getLogger(OFPluginToLibraryTest.class);

    @Inject @Filter(timeout=5000)
    SwitchConnectionProvider switchConnectionProvider;
    
    @Inject
    BundleContext ctx;

    private SimpleClient switchSim;

    /**
     * test setup
     * @throws InterruptedException 
     */
    @Before
    public void setUp() throws InterruptedException {
        LOG.debug("switchConnectionProvider: "+switchConnectionProvider);
        //FIXME: plugin should provide service exposing startup result via future 
        Thread.sleep(5000);
    }

    /**
     * test tear down
     */
    @After
    public void tearDown() {
        try {
            LOG.debug("tearing down simulator");
            switchSim.getScenarioDone().get(getFailSafeTimeout(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            String msg = "waiting for scenario to finish failed: "+e.getMessage();
            LOG.error(msg, e);
            Assert.fail(msg);
        }

        //TODO: dump errors of plugin (by exploiting ErrorHandler)
        
        try {
            LOG.debug("checking if simulator succeeded to connect to controller");
            boolean simulatorWasOnline = switchSim.getIsOnlineFuture().get(100, TimeUnit.MILLISECONDS);
            Assert.assertTrue("simulator failed to connect to controller", simulatorWasOnline);
        } catch (Exception e) {
            String message = "simulator probably failed to connect to controller";
            LOG.error(message, e);
            Assert.fail(message);
        } 
    }

    /**
     * test basic integration with OFLib running the handshake
     * @throws Exception
     */
    @Test
    public void handshakeOk1() throws Exception {
        LOG.debug("handshakeOk1 integration test");

        switchSim = createSimpleClient();
        switchSim.setSecuredClient(false);
        Stack<ClientEvent> handshakeScenario = ScenarioFactory.createHandshakeScenarioVBM(
                ScenarioFactory.VERSION_BITMAP_13, (short) 0, ScenarioFactory.VERSION_BITMAP_10_13);

        ScenarioHandler scenario = new ScenarioHandler(handshakeScenario);
        switchSim.setScenarioHandler(scenario);
        switchSim.start();
    }

    /**
     * test basic integration with OFLib running the handshake (with version bitmap)
     * @throws Exception
     */
    @Test
    public void handshakeOk2() throws Exception {
        LOG.debug("handshakeOk2 integration test");

        switchSim = createSimpleClient();
        switchSim.setSecuredClient(false);
        Stack<ClientEvent> handshakeScenario = ScenarioFactory.createHandshakeScenario(
                (short) 0, ScenarioFactory.VERSION_BITMAP_10_13);

        ScenarioHandler scenario = new ScenarioHandler(handshakeScenario);
        switchSim.setScenarioHandler(scenario);
        switchSim.start();
    }

    /**
     * test basic integration with OFLib running the handshake:
     * creating auxiliary connection without primary connection -- FAIL
     * @throws Exception
     */
    @Test
    public void handshakeFail1() throws Exception {
        LOG.debug("handshakeFail1 integration test");

        switchSim = createSimpleClient();
        switchSim.setSecuredClient(false);
        Stack<ClientEvent> handshakeScenario = ScenarioFactory.createHandshakeScenario((short) 1,
                ScenarioFactory.VERSION_BITMAP_10_13);

        ScenarioHandler scenario = new ScenarioHandler(handshakeScenario);
        switchSim.setScenarioHandler(scenario);
        switchSim.start();
    }

    /**
     * test basic integration with OFLib running the handshake
     * adding 5s wait as first event of switch -- FAIL
     * @throws Exception
     */
    @Test
    public void handshakeFail2() throws Exception {
        LOG.debug("handshakeFail2 integration test");
        LOG.debug("switchConnectionProvider: "+switchConnectionProvider);

        switchSim = createSimpleClient();
        switchSim.setSecuredClient(false);
        Stack<ClientEvent> handshakeScenario = ScenarioFactory.createHandshakeScenario((short) 0,
                ScenarioFactory.VERSION_BITMAP_10_13);
        handshakeScenario.setElementAt(new SleepEvent(5000), 0);

        ScenarioHandler scenario = new ScenarioHandler(handshakeScenario);
        switchSim.setScenarioHandler(scenario);
        switchSim.start();
    }

    /**
     * @return
     */
    private static SimpleClient createSimpleClient() {
        return new SimpleClient("localhost", 6653);
    }

    /**
     * @return timeout for case of failure
     */
    private static long getFailSafeTimeout() {
        return 20000;
    }


    /**
     * @return bundle options
     */
    @Configuration
    public Option[] config() {
        LOG.info("configuring...");
        return options(
                systemProperty("osgi.console").value("2401"),
                systemProperty("osgi.bundles.defaultStartLevel").value("4"),
                systemProperty("pax.exam.osgi.unresolved.fail").value("true"),

                OFPaxOptionsAssistant.osgiConsoleBundles(),
                OFPaxOptionsAssistant.loggingBudles(),
                
                TestHelper.junitAndMockitoBundles(),
                TestHelper.mdSalCoreBundles(), 
                TestHelper.configMinumumBundles(),
                TestHelper.baseModelBundles(),
                TestHelper.flowCapableModelBundles(), 
                
                OFPaxOptionsAssistant.ofPluginBundles());
    }

}
