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
import org.opendaylight.openflowjava.protocol.impl.clients.ClientEvent;
import org.opendaylight.openflowjava.protocol.impl.clients.ScenarioFactory;
import org.opendaylight.openflowjava.protocol.impl.clients.ScenarioHandler;
import org.opendaylight.openflowjava.protocol.impl.clients.SendEvent;
import org.opendaylight.openflowjava.protocol.impl.clients.SimpleClient;
import org.opendaylight.openflowjava.protocol.impl.clients.SleepEvent;
import org.opendaylight.openflowjava.protocol.impl.clients.WaitForMessageEvent;
import org.opendaylight.openflowjava.protocol.impl.util.ByteBufUtils;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mirehak
 */
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
        Stack<ClientEvent> handshakeScenario = ScenarioFactory.createHandshakeScenario();
        
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
        Stack<ClientEvent> handshakeScenario = new Stack<>();
        // handshake with versionbitmap
        handshakeScenario.add(0, new SendEvent(ByteBufUtils.hexStringToBytes("04 00 00 10 00 00 00 01 00 01 00 08 00 00 00 10")));
        handshakeScenario.add(0, new WaitForMessageEvent(ByteBufUtils.hexStringToBytes("04 00 00 10 00 00 00 15 00 01 00 08 00 00 00 12")));
        handshakeScenario.add(0, new WaitForMessageEvent(ByteBufUtils.hexStringToBytes("04 05 00 08 00 00 00 03")));
        handshakeScenario.add(0, new SendEvent(ByteBufUtils.hexStringToBytes("04 06 00 20 00 00 00 03 "
                + "00 01 02 03 04 05 06 07 00 01 02 03 01 00 00 00 00 01 02 03 00 01 02 03")));
        
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
        Stack<ClientEvent> handshakeScenario = ScenarioFactory.createHandshakeScenario();
        SendEvent featuresReply = new SendEvent(new byte[] {4, 6, 0, 32, 0, 0, 0, 3, 0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 1, 1, 0, 0, 0, 1, 2, 3, 0, 1, 2, 3});
        handshakeScenario.setElementAt(featuresReply, 0);
        
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
        Stack<ClientEvent> handshakeScenario = ScenarioFactory.createHandshakeScenario();
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
                mavenBundle("org.opendaylight.yangtools.thirdparty", "xtend-lib-osgi").versionAsInProject(),
                mavenBundle("com.google.guava", "guava").versionAsInProject(),
                mavenBundle("org.javassist", "javassist").versionAsInProject(),
                mavenBundle("org.apache.felix", "org.apache.felix.dependencymanager").versionAsInProject(),
                mavenBundle("org.apache.commons", "commons-lang3").versionAsInProject(),

                mavenBundle(ODL, "sal").versionAsInProject(),
                mavenBundle(ODL, "sal.connection").versionAsInProject(),
                mavenBundle(ODL, "sal-binding-api").versionAsInProject(),
                mavenBundle(ODL, "sal-common").versionAsInProject(),
                mavenBundle(ODL, "sal-common-api").versionAsInProject(),
                mavenBundle(ODL, "sal-common-util").versionAsInProject(),

                mavenBundle("org.opendaylight.controller.thirdparty", "org.openflow.openflowj").versionAsInProject(),
                mavenBundle(ODL_MODEL, "model-flow-base").versionAsInProject(),
                mavenBundle(ODL_MODEL, "model-inventory").versionAsInProject(),
                mavenBundle(ODL_MODEL, "model-flow-service").versionAsInProject(),
                mavenBundle(ODL_MODEL, "model-flow-statistics").versionAsInProject(),

                mavenBundle(OFLIBRARY, "openflow-protocol-impl").versionAsInProject(),
                mavenBundle(OFLIBRARY, "openflow-protocol-api").versionAsInProject(),
                mavenBundle(OFLIBRARY, "openflow-protocol-spi").versionAsInProject(),

                mavenBundle(NETTY, "netty-handler").versionAsInProject(),
                mavenBundle(NETTY, "netty-buffer").versionAsInProject(),
                mavenBundle(NETTY, "netty-common").versionAsInProject(),
                mavenBundle(NETTY, "netty-transport").versionAsInProject(),
                mavenBundle(NETTY, "netty-codec").versionAsInProject(),

                mavenBundle(YANG_MODEL, "ietf-inet-types").versionAsInProject(),
                mavenBundle(YANG_MODEL, "ietf-yang-types").versionAsInProject(),
                mavenBundle(YANG_MODEL, "yang-ext").versionAsInProject(),
                mavenBundle(YANG_MODEL, "opendaylight-l2-types").versionAsInProject(),

                mavenBundle(YANG, "concepts").versionAsInProject(),
                mavenBundle(YANG, "yang-binding").versionAsInProject(),
                mavenBundle(YANG, "yang-common").versionAsInProject(),

                mavenBundle(OFLIBRARY, "simple-client").versionAsInProject().start(),
                mavenBundle(OFPLUGIN, "openflowplugin").versionAsInProject(),
                junitBundles());
    }

}
