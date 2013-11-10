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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionConfiguration;
import org.opendaylight.openflowjava.protocol.api.connection.SwitchConnectionHandler;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.translator.ErrorTranslator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

/**
 * @author mirehak
 *
 */
public class MDController implements IMDController {

    private static final Logger LOG = LoggerFactory.getLogger(MDController.class);

    private SwitchConnectionProvider switchConnectionProvider;

    private ConcurrentMap<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, DataObject>>> messageTranslators;

    /**
     * @return translator mapping
     */
    public Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, DataObject>>> getMessageTranslators() {
        return messageTranslators;
    }

    /**
     * provisioning of translator mapping
     */
    public void init() {
        LOG.debug("Initializing!");
        messageTranslators = new ConcurrentHashMap<>();
        addMessageTranslator(ErrorMessage.class, 4, new ErrorTranslator());
        addMessageTranslator(ErrorMessage.class, 1, new ErrorTranslator());
        
        // Push the updated Listeners to Session Manager which will be then picked up by ConnectionConductor eventually
        OFSessionUtil.getSessionManager().setTranslatorMapping(messageTranslators);
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
        Future<List<Boolean>> srvStopped = switchConnectionProvider.shutdown();
        try {
            srvStopped.get(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.error(e.getMessage(), e);
        }
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
    public void addMessageTranslator(Class<? extends DataObject> messageType, int version, IMDMessageTranslator<OfHeader, DataObject> translator) {
        TranslatorKey tKey = new TranslatorKey(version, messageType.getName());
        
        Collection<IMDMessageTranslator<OfHeader, DataObject>> existingValues = messageTranslators.get(tKey);
        if (existingValues == null) {
            existingValues = new ArrayList<>();
            messageTranslators.put(tKey, existingValues);
        }
        existingValues.add(translator);
        LOG.debug("{} is now listened by {}", messageType, translator);
    }

    @Override
    public void removeMessageTranslator(Class<? extends DataObject> messageType, int version, IMDMessageTranslator<OfHeader, DataObject> translator) {
        TranslatorKey tKey = new TranslatorKey(version, messageType.getName());
        Collection<IMDMessageTranslator<OfHeader, DataObject>> values = messageTranslators.get(tKey);
        if (values != null) {
            values.remove(translator);
            if (values.isEmpty()) {
                messageTranslators.remove(tKey);
            }
            LOG.debug("{} is now removed", translator);
         }
    }


}
