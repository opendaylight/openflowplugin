/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl;


import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginProvider;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.openflowplugin.impl.device.DeviceManagerImpl;
import org.opendaylight.openflowplugin.impl.rpc.RpcManagerImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.api.types.rev150327.OfpRole;
import java.util.Collection;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 27.3.2015.
 */
public class OpenFlowPluginProviderImpl implements OpenFlowPluginProvider {

    private DeviceManager deviceManager;
    private RpcManager rpcManager;
    private BindingAwareBroker.ProviderContext providerContext;
    private StatisticsManager statisticsManager;

    @Override
    public void onSessionInitiated(final BindingAwareBroker.ProviderContext session) {
        providerContext = session;
    }

    @Override
    public void setSwitchConnectionProviders(final Collection<SwitchConnectionProvider> switchConnectionProvider) {

    }

    @Override
    public void setRole(final OfpRole role) {

    }

    @Override
    public void initialize() {
        deviceManager = new DeviceManagerImpl();
        rpcManager = new RpcManagerImpl(providerContext);
        //TODO : initialize statistics manager
    }

    @Override
    public void close() throws Exception {

    }
}
