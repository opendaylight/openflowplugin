/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.shell.command;

import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.util.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * Base for Karaf shell commands.
 *
 * @author Thomas Vachuska
 */
public abstract class AbstractShellCommand extends OsgiCommandSupport {

    /**
     * Retrieves the reference to the specified service class implementation.
     *
     * @param serviceClass service class
     * @param <T>          type of service
     * @return service implementation
     */
    static <T> T get(Class<T> serviceClass) {
        BundleContext bc = FrameworkUtil.getBundle(AbstractShellCommand.class).getBundleContext();
        return bc.getService(bc.getServiceReference(serviceClass));
    }

    protected void print(String fmt, Object... items) {
        System.out.println(StringUtils.format(fmt, items));
    }

}
