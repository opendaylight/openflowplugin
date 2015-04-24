/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow;

import java.util.Collection;
import org.opendaylight.controller.md.sal.binding.api.BindingService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.api.types.rev150327.OfpRole;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 27.3.2015.
 */
public interface OpenFlowPluginProvider extends AutoCloseable, BindingService {

    /**
     * Method sets openflow java's connection providers.
     */
    void setSwitchConnectionProviders(Collection<SwitchConnectionProvider> switchConnectionProvider);

    /**
     * setter
     *
     * @param dataBroker
     */
    void setDataBroker(DataBroker dataBroker);

    void setRpcProviderRegistry(RpcProviderRegistry rpcProviderRegistry);

    void setNotificationProviderService(NotificationProviderService notificationProviderService);

    /**
     * Method sets role of this application in clustered environment.
     */
    void setRole(OfpRole role);

    /**
     * Method initializes all DeviceManager, RpcManager and related contexts.
     */
    void initialize();

}
