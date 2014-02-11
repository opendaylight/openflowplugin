/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.it;


import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.DefaultCompositeOption;

/**
 * @author mirehak
 *
 */
public abstract class OFPaxOptionsAssistant {
    
    /** system property required to enable osgi console and remote debugging, only presence matters */
    private static final String INSPECT_OSGI = "inspectOsgi";
    /** default remote debug port */
    public static final String DEBUG_PORT = "6000";
    /** base controller package */
    public static final String CONTROLLER = "org.opendaylight.controller";
    /** OFLibrary package */
    public static final String OFLIBRARY = "org.opendaylight.openflowjava";
    /** OFLibrary package */
    public static final String OFPLUGIN = "org.opendaylight.openflowplugin";
    /** controller.model package */
    public static final String CONTROLLER_MODEL = "org.opendaylight.controller.model";
    
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
            .add(CoreOptions.mavenBundle("equinoxSDK381", "org.eclipse.equinox.console").versionAsInProject())
            .add(CoreOptions.mavenBundle("equinoxSDK381", "org.apache.felix.gogo.shell").versionAsInProject())
            .add(CoreOptions.mavenBundle("equinoxSDK381", "org.apache.felix.gogo.runtime").versionAsInProject())
            .add(CoreOptions.mavenBundle("equinoxSDK381", "org.apache.felix.gogo.command").versionAsInProject());
        }
            
        return option;
    }

    /**
     * @return OFLibrary bundles
     */
    public static Option ofLibraryBundles() {
        return new DefaultCompositeOption(
                mavenBundle(OFLIBRARY, "openflow-protocol-impl").versionAsInProject(),
                mavenBundle(OFLIBRARY, "openflow-protocol-api").versionAsInProject(),
                mavenBundle(OFLIBRARY, "openflow-protocol-spi").versionAsInProject(),
                mavenBundle(OFLIBRARY, "simple-client").versionAsInProject().start());
    }

    /**
     * @return OFLibrary + OFPlugin bundles
     */
    public static Option ofPluginBundles() {
        return new DefaultCompositeOption(
                baseSalBundles(),
                ofLibraryBundles(),
                mavenBundle(CONTROLLER_MODEL, "model-flow-statistics").versionAsInProject(),
                mavenBundle(OFPLUGIN, "openflowplugin").versionAsInProject());
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
                mavenBundle("org.apache.felix", "org.apache.felix.dependencymanager").versionAsInProject(),
                mavenBundle(CONTROLLER, "sal").versionAsInProject());
    }

}
