/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionConfiguration;
import org.opendaylight.openflowjava.protocol.api.connection.SwitchConnectionHandler;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * @author mirehak
 *
 */
public class MDController implements IMDController {

    private static final Logger LOG = LoggerFactory.getLogger(MDController.class);

    private SwitchConnectionProvider switchConnectionProvider;

    private ConcurrentMap<Class<? extends DataObject>, Collection<IMDMessageListener>> messageListeners;

    public Map<Class<? extends DataObject>, Collection<IMDMessageListener>> getMessageListeners() {
        return messageListeners;
    }


    public void init() {
        LOG.debug("Initializing!");
        this.messageListeners = new ConcurrentHashMap<Class<? extends DataObject>, Collection<IMDMessageListener>>();
    }

    /**
     * @param switchConnectionProvider
     *            the switchConnectionProvider to set
     */
    public void setSwitchConnectionProvider(SwitchConnectionProvider switchConnectionProvider) {
        this.switchConnectionProvider = switchConnectionProvider;
    }

    /**
     * @param switchConnectionProviderToUnset
     *            the switchConnectionProvider to unset
     */
    public void unsetSwitchConnectionProvider(SwitchConnectionProvider switchConnectionProviderToUnset) {
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
        LOG.debug("switchConnectionProvider: " + switchConnectionProvider);
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
    private static Collection<ConnectionConfiguration> getConnectionConfiguration() {
        // TODO:: get config from state manager
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

    @Override
    public void addMessageListener(Class<? extends DataObject> messageType, IMDMessageListener listener) {

        Collection<IMDMessageListener> existingValues = messageListeners.get(messageType);
        if (existingValues == null) {
               existingValues = new ArrayList<IMDMessageListener>();
               messageListeners.put(messageType, existingValues);
        }
        existingValues.add(listener);
        // Push the updated Listeners to Session Manager which will be then picked up by ConnectionConductor eventually
        OFSessionUtil.getSessionManager().setListenerMapping(messageListeners);
        LOG.debug("{} is now listened by {}", messageType, listener);
    }

    @Override
    public void removeMessageListener(Class<? extends DataObject> messageType, IMDMessageListener listener) {

        Collection<IMDMessageListener> values = messageListeners.get(messageType);
        if (values != null) {
                    values.remove(listener);
                    if (values.size() == 0) {
                        messageListeners.remove(messageType);
                    }
                    //Push the updated Listeners to Session Manager which will be then picked up by ConnectionConductor eventually
                    OFSessionUtil.getSessionManager().setListenerMapping(messageListeners);
                    LOG.debug("{} is now removed", listener);
         }
    }



}
