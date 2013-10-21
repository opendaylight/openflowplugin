package org.opendaylight.openflowplugin.openflow.md.it;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.openflowjava.protocol.impl.clients.ScenarioFactory;
import org.opendaylight.openflowjava.protocol.impl.clients.ScenarioHandler;
import org.opendaylight.openflowjava.protocol.impl.clients.SimpleClient;
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

    /**
     * test basic integration with OFLib running the handshake
     * @throws Exception
     */
    @Test
    public void handshake() throws Exception {
        LOG.debug("handshake integration test");
        LOG.debug("switchConnectionProvider: "+switchConnectionProvider);

        SimpleClient switchSim = new SimpleClient("localhost", 6653);
        switchSim.setSecuredClient(false);
        ScenarioHandler scenario = new ScenarioHandler(ScenarioFactory.createHandshakeScenario());
        switchSim.setScenarioHandler(scenario);
        switchSim.start();
        switchSim.getScenarioDone().get();
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
