/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionConfiguration;
import org.opendaylight.openflowjava.protocol.api.connection.SwitchConnectionHandler;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * @author mirehak
 *
 */
public class MDController {

    private static final Logger LOG = LoggerFactory
            .getLogger(MDController.class);

    private SwitchConnectionProvider switchConnectionProvider;

    /**
     * @param switchConnectionProvider the switchConnectionProvider to set
     */
    public void setSwitchConnectionProvider(
            SwitchConnectionProvider switchConnectionProvider) {
        this.switchConnectionProvider = switchConnectionProvider;
    }

    /**
     * @param switchConnectionProviderToUnset the switchConnectionProvider to unset
     */
    public void unsetSwitchConnectionProvider(
            SwitchConnectionProvider switchConnectionProviderToUnset) {
        if (this.switchConnectionProvider == switchConnectionProviderToUnset) {
            this.switchConnectionProvider = null;
        }
    }

    /**
     * Function called by dependency manager after "init ()" is called and after
     * the services provided by the class are registered in the service registry
     *
     */
    public void start() {
        LOG.debug("starting ..");
        LOG.debug("switchConnectionProvider: "+switchConnectionProvider);
        // setup handler
        SwitchConnectionHandler switchConnectionHandler = new SwitchConnectionHandlerImpl();
        switchConnectionProvider.setSwitchConnectionHandler(switchConnectionHandler);
        // configure and startup library servers
        switchConnectionProvider.configure(getConnectionConfiguration());
        Future<List<Boolean>> srvStarted = switchConnectionProvider.startup();
    }

    /**
     * @return wished connections configurations
     */
    private Collection<ConnectionConfiguration> getConnectionConfiguration() {
        //TODO:: get config from state manager
        ConnectionConfiguration configuration = ConnectionConfigurationFactory.getDefault();
        return Lists.newArrayList(configuration);
    }

    /**
     * Function called by the dependency manager before the services exported by
     * the component are unregistered, this will be followed by a "destroy ()"
     * calls
     *
     */
    public void stop() {
        LOG.debug("stopping");
    }

    /**
     * Function called by the dependency manager when at least one dependency
     * become unsatisfied or when the component is shutting down because for
     * example bundle is being stopped.
     *
     */
    public void destroy() {
        // do nothing
    }

}
