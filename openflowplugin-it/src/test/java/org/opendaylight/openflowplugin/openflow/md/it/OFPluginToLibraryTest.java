/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.it;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import java.util.Stack;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import junit.framework.Assert;

import org.junit.After;
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
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PaxExam.class)
public class OFPluginToLibraryTest {

    private static final Logger LOG = LoggerFactory
            .getLogger(OFPluginToLibraryTest.class);

    /** base controller package */
    public static final String ODL = "org.opendaylight.controller";
    /** controller.model package */
    public static final String ODL_MODEL = "org.opendaylight.controller.model";
    /** yangtools package */
    public static final String YANG = "org.opendaylight.yangtools";
    /** yangtools.model package */
    public static final String YANG_MODEL = "org.opendaylight.yangtools.model";
    /** OFPlugin package */
    public static final String OFPLUGIN = "org.opendaylight.openflowplugin";
    /** OFLibrary package */
    public static final String OFLIBRARY = "org.opendaylight.openflowjava";
    /** netty.io package */
    public static final String NETTY = "io.netty";

    @Inject
    SwitchConnectionProvider switchConnectionProvider;

    @Inject
    BundleContext ctx;

    private SimpleClient switchSim;

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
    }

    /**
     * test basic integration with OFLib running the handshake
     * @throws Exception
     */
    @Test
    public void handshakeOk1() throws Exception {
        LOG.debug("handshake integration test");
        LOG.debug("switchConnectionProvider: "+switchConnectionProvider);

        switchSim = new SimpleClient("localhost", 6653);
        switchSim.setSecuredClient(false);
        Stack<ClientEvent> handshakeScenario = ScenarioFactory.createHandshakeScenarioVBM(
                ScenarioFactory.VERSION_BITMAP_13, (short) 0, ScenarioFactory.VERSION_BITMAP_10_13);

        ScenarioHandler scenario = new ScenarioHandler(handshakeScenario);
        switchSim.setScenarioHandler(scenario);
        switchSim.start();
        try {
            switchSim.getScenarioDone().get(getFailSafeTimeout(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            String msg = "waiting for scenario to finish failed: "+e.getMessage();
            LOG.error(msg, e);
            Assert.fail(msg);
        }
        //TODO: dump errors of plugin
    }

    /**
     * test basic integration with OFLib running the handshake (with version bitmap)
     * @throws Exception
     */
    @Test
    public void handshakeOk2() throws Exception {
        LOG.debug("handshake integration test");
        LOG.debug("switchConnectionProvider: "+switchConnectionProvider);

        switchSim = new SimpleClient("localhost", 6653);
        switchSim.setSecuredClient(false);
        Stack<ClientEvent> handshakeScenario = ScenarioFactory.createHandshakeScenario(
                (short) 0, ScenarioFactory.VERSION_BITMAP_10_13);

        ScenarioHandler scenario = new ScenarioHandler(handshakeScenario);
        switchSim.setScenarioHandler(scenario);
        switchSim.start();
        try {
            switchSim.getScenarioDone().get(getFailSafeTimeout(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            String msg = "waiting for scenario to finish failed: "+e.getMessage();
            LOG.error(msg, e);
            Assert.fail(msg);
        }
        //TODO: dump errors of plugin
    }

    /**
     * test basic integration with OFLib running the handshake
     * @throws Exception
     */
    @Test
    public void handshakeFail1() throws Exception {
        LOG.debug("handshake integration test");
        LOG.debug("switchConnectionProvider: "+switchConnectionProvider);

        switchSim = new SimpleClient("localhost", 6653);
        switchSim.setSecuredClient(false);
        Stack<ClientEvent> handshakeScenario = ScenarioFactory.createHandshakeScenario((short) 1,
                ScenarioFactory.VERSION_BITMAP_10_13);

        ScenarioHandler scenario = new ScenarioHandler(handshakeScenario);
        switchSim.setScenarioHandler(scenario);
        switchSim.start();
        try {
            switchSim.getScenarioDone().get(getFailSafeTimeout(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            String msg = "waiting for scenario to finish failed: "+e.getMessage();
            LOG.error(msg, e);
            Assert.fail(msg);
        }
        //TODO: dump errors of plugin
    }

    /**
     * test basic integration with OFLib running the handshake
     * @throws Exception
     */
    @Test
    public void handshakeFail2() throws Exception {
        LOG.debug("handshake integration test");
        LOG.debug("switchConnectionProvider: "+switchConnectionProvider);

        switchSim = new SimpleClient("localhost", 6653);
        switchSim.setSecuredClient(false);
        Stack<ClientEvent> handshakeScenario = ScenarioFactory.createHandshakeScenario((short) 1,
                ScenarioFactory.VERSION_BITMAP_10_13);
        handshakeScenario.setElementAt(new SleepEvent(5000), 0);

        ScenarioHandler scenario = new ScenarioHandler(handshakeScenario);
        switchSim.setScenarioHandler(scenario);
        switchSim.start();
        tearDown();
        //TODO: dump errors of plugin
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
        return options(
                systemProperty("osgi.console").value("2401"),
                mavenBundle("org.slf4j", "slf4j-api").versionAsInProject(),
                mavenBundle("org.slf4j", "log4j-over-slf4j").versionAsInProject(),
                mavenBundle("ch.qos.logback", "logback-core").versionAsInProject(),
                mavenBundle("ch.qos.logback", "logback-classic").versionAsInProject(),
                mavenBundle("org.apache.felix", "org.apache.felix.dependencymanager").versionAsInProject(),
                TestHelper.mdSalCoreBundles(), TestHelper.bindingAwareSalBundles(),
                TestHelper.flowCapableModelBundles(), TestHelper.baseModelBundles(),


                mavenBundle(ODL, "sal").versionAsInProject(),
                mavenBundle(ODL, "sal.connection").versionAsInProject(),
                mavenBundle(ODL, "sal-common").versionAsInProject(),

                mavenBundle(ODL_MODEL, "model-flow-statistics").versionAsInProject(),

                mavenBundle(OFLIBRARY, "openflow-protocol-impl").versionAsInProject(),
                mavenBundle(OFLIBRARY, "openflow-protocol-api").versionAsInProject(),
                mavenBundle(OFLIBRARY, "openflow-protocol-spi").versionAsInProject(),

                mavenBundle(NETTY, "netty-handler").versionAsInProject(),
                mavenBundle(NETTY, "netty-buffer").versionAsInProject(),
                mavenBundle(NETTY, "netty-common").versionAsInProject(),
                mavenBundle(NETTY, "netty-transport").versionAsInProject(),
                mavenBundle(NETTY, "netty-codec").versionAsInProject(),

                mavenBundle(OFLIBRARY, "simple-client").versionAsInProject().start(),
                mavenBundle(OFPLUGIN, "openflowplugin").versionAsInProject(),
                junitBundles());
    }

}
