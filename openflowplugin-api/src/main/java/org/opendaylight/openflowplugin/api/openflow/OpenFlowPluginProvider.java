/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.api.types.rev150327.OfpRole;
import java.util.Collection;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 27.3.2015.
 */
public interface OpenFlowPluginProvider extends AutoCloseable {


    public void onSessionInitiated(BindingAwareBroker.ProviderContext session);

    /**
     * Method sets openflow java's connection providers.
     */
    public void setSwitchConnectionProviders(Collection<SwitchConnectionProvider> switchConnectionProvider);


    /**
     * Method sets role of this application in clustered environment.
     */
    public void setRole(OfpRole role);

    /**
     * Method initializes all DeviceManager, RpcManager and related contexts.
     */
    public void initialize();

}
