package org.opendaylight.openflowplugin.openflow.md.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opendaylight.controller.test.sal.binding.it.TestHelper.baseModelBundles;
import static org.opendaylight.controller.test.sal.binding.it.TestHelper.bindingAwareSalBundles;
import static org.opendaylight.controller.test.sal.binding.it.TestHelper.configMinumumBundles;
import static org.opendaylight.controller.test.sal.binding.it.TestHelper.flowCapableModelBundles;
import static org.opendaylight.controller.test.sal.binding.it.TestHelper.mdSalCoreBundles;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;
import org.opendaylight.controller.sal.binding.api.NotificationService;
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
 * @author mirehak
 */
@RunWith(PaxExam.class)
public class SalIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(SalIntegrationTest.class);

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

    @Inject
    BindingAwareBroker broker;

    /**
     * @return timeout for case of failure
     */
    private static long getFailSafeTimeout() {
        return 20000;
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

        switchSim.getScenarioDone().get(getFailSafeTimeout(), TimeUnit.MILLISECONDS);
        Thread.sleep(2000);
        assertEquals(1, listener.nodeUpdated.size());
        assertNotNull(listener.nodeUpdated.get(0));

    }

    /**
     * @return bundle options
     */
    @Configuration
    public Option[] config() {
        return options(systemProperty("osgi.console").value("2401"),
                mavenBundle("org.slf4j", "slf4j-api").versionAsInProject(), 
                mavenBundle("org.slf4j", "log4j-over-slf4j").versionAsInProject(),
                mavenBundle("ch.qos.logback", "logback-core").versionAsInProject(),
                mavenBundle("ch.qos.logback", "logback-classic").versionAsInProject(),
                
                mavenBundle("org.apache.felix", "org.apache.felix.dependencymanager").versionAsInProject(),

                mavenBundle(OFLIBRARY, "openflow-protocol-impl").versionAsInProject(),
                mavenBundle(OFLIBRARY, "openflow-protocol-api").versionAsInProject(),
                mavenBundle(OFLIBRARY, "openflow-protocol-spi").versionAsInProject(),

                mavenBundle(ODL, "sal").versionAsInProject(), 
                mavenBundle(ODL, "sal.connection").versionAsInProject(),
                mdSalCoreBundles(), baseModelBundles(), flowCapableModelBundles(),
                configMinumumBundles(),

                bindingAwareSalBundles(), 

                mavenBundle(NETTY, "netty-handler").versionAsInProject(), 
                mavenBundle(NETTY, "netty-buffer").versionAsInProject(), 
                mavenBundle(NETTY, "netty-common").versionAsInProject(),
                mavenBundle(NETTY, "netty-transport").versionAsInProject(), 
                mavenBundle(NETTY, "netty-codec").versionAsInProject(),

                mavenBundle(OFLIBRARY, "simple-client").versionAsInProject().start(),
                mavenBundle(OFPLUGIN, "openflowplugin").versionAsInProject(), junitBundles(),
                mavenBundle("org.opendaylight.controller.thirdparty", "org.openflow.openflowj").versionAsInProject()
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
