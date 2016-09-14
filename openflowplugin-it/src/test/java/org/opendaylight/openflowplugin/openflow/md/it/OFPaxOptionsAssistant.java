/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.it;


import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

import org.opendaylight.controller.test.sal.binding.it.TestHelper;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.DefaultCompositeOption;

/**
 * The main wiring is assembled in {@link #ofPluginBundles()}
 * @author mirehak
 */
public abstract class OFPaxOptionsAssistant {

    /** system property required to enable osgi console and remote debugging, only presence matters */
    private static final String INSPECT_OSGI = "inspectOsgi";
    /** default remote debug port */
    public static final String DEBUG_PORT = "6000";
    /** base controller package */
    public static final String CONTROLLER = "org.opendaylight.controller";
    /** base controller md-sal package */
    public static final String CONTROLLER_MD = "org.opendaylight.controller.md";
    /** OFLibrary package */
    public static final String OFLIBRARY = "org.opendaylight.openflowjava";
    /** OFPlugin package */
    public static final String OFPLUGIN = "org.opendaylight.openflowplugin";
    /** OFPlugin applications package */
    public static final String OFPLUGIN_APPS = "org.opendaylight.openflowplugin.applications";
    /** OFPlugin model package */
    public static final String OFPLUGIN_MODEL = "org.opendaylight.openflowplugin.model";
    /** controller.model package */
    public static final String CONTROLLER_MODEL = "org.opendaylight.controller.model";

    public static final String YANGTOOLS = "org.opendaylight.yangtools";


    /**
     * Works only if property -DinspectOsgi is used
     * @return equinox console setup (in order to inspect running IT through osgi console (telnet))
     * and remote debugging on port {@link #DEBUG_PORT}
     */
    public static Option osgiConsoleBundles() {
        DefaultCompositeOption option = new DefaultCompositeOption();
        if (System.getProperty(INSPECT_OSGI) != null) {
            option
            .add(CoreOptions.vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address="+DEBUG_PORT))
            .add(CoreOptions.mavenBundle("equinoxSDK381", "org.eclipse.equinox.console").versionAsInProject());
        }

        return option;
    }

    /**
     * @return OFLibrary bundles
     */
    public static Option ofLibraryBundles() {
        return new DefaultCompositeOption(
                mavenBundle(OFLIBRARY, "openflowjava-util").versionAsInProject(),
                mavenBundle(OFLIBRARY, "openflow-protocol-impl").versionAsInProject(),
                mavenBundle(OFLIBRARY, "openflow-protocol-api").versionAsInProject(),
                mavenBundle(OFLIBRARY, "openflow-protocol-spi").versionAsInProject(),
                mavenBundle(OFLIBRARY, "simple-client").versionAsInProject().start());
    }

    /**
     * Here we construct whole wiring
     * @return OFLibrary + OFPlugin bundles
     */
    public static Option ofPluginBundles() {
        return new DefaultCompositeOption(
                baseSalBundles(),
                mdSalApiBundles(),
                mdSalImplBundles(),
                mdSalBaseModelBundles(),
                ofLibraryBundles(),
                mavenBundle(CONTROLLER_MODEL, "model-inventory").versionAsInProject(),
                mavenBundle(OFPLUGIN_MODEL, "model-flow-statistics").versionAsInProject(),
                mavenBundle(OFPLUGIN_MODEL, "model-flow-base").versionAsInProject(),
                mavenBundle(OFPLUGIN_MODEL, "model-flow-service").versionAsInProject(),
                mavenBundle(OFPLUGIN, "openflowplugin-common").versionAsInProject(),
                mavenBundle(OFPLUGIN, "openflowplugin-api").versionAsInProject(),
                mavenBundle(OFPLUGIN, "openflowplugin-extension-api").versionAsInProject(),
                mavenBundle(OFPLUGIN, "openflowplugin").versionAsInProject(),
                mavenBundle(OFPLUGIN_APPS, "forwardingrules-manager").versionAsInProject(),
                mavenBundle(OFPLUGIN_APPS, "inventory-manager").versionAsInProject(),
                mavenBundle("openexi", "nagasena").versionAsInProject()
                );
    }

    /**
     * @return logging bundles
     */
    public static Option loggingBudles() {
        return new DefaultCompositeOption(
                mavenBundle("org.slf4j", "slf4j-api").versionAsInProject(),
                mavenBundle("org.slf4j", "log4j-over-slf4j").versionAsInProject(),
                mavenBundle("ch.qos.logback", "logback-core").versionAsInProject(),
                mavenBundle("ch.qos.logback", "logback-classic").versionAsInProject());
    }

    /**
     * @return sal + dependencymanager
     */
    public static Option baseSalBundles() {
        return new DefaultCompositeOption(
//                mavenBundle("org.apache.felix", "org.apache.felix.dependencymanager").versionAsInProject(),
//                mavenBundle(CONTROLLER, "sal").versionAsInProject(),
                mavenBundle("io.netty", "netty-common").versionAsInProject(), //
                mavenBundle("io.netty", "netty-buffer").versionAsInProject(), //
                mavenBundle("io.netty", "netty-handler").versionAsInProject(), //
                mavenBundle("io.netty", "netty-codec").versionAsInProject(), //
                mavenBundle("io.netty", "netty-transport").versionAsInProject(), //

                mavenBundle(CONTROLLER, "liblldp").versionAsInProject(),
                mavenBundle(OFPLUGIN_APPS, "topology-lldp-discovery").versionAsInProject(),
                mavenBundle("org.antlr", "antlr4-runtime").versionAsInProject());
    }

    /**
     * @return sal + dependencymanager
     */
    public static Option mdSalApiBundles() {
        return new DefaultCompositeOption(
                TestHelper.junitAndMockitoBundles(),
                TestHelper.mdSalCoreBundles(),
                TestHelper.configMinumumBundles(),
                mavenBundle("org.antlr", "antlr4-runtime").versionAsInProject());
    }

    private static Option mdSalImplBundles() {
        return new DefaultCompositeOption(
                TestHelper.bindingAwareSalBundles()
        );
    }

    private static Option mdSalBaseModelBundles() {
        return new DefaultCompositeOption(
                TestHelper.baseModelBundles(),
                mavenBundle(CONTROLLER_MODEL, "model-inventory").versionAsInProject()
        );
    }

}
