/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl;


import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import com.google.common.base.Preconditions;
import java.util.Collection;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginProvider;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.openflowplugin.impl.connection.ConnectionManagerImpl;
import org.opendaylight.openflowplugin.impl.device.DeviceManagerImpl;
import org.opendaylight.openflowplugin.impl.rpc.RpcManagerImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.api.types.rev150327.OfpRole;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 27.3.2015.
 */
public class OpenFlowPluginProviderImpl implements OpenFlowPluginProvider {

    protected static final Logger LOG = LoggerFactory.getLogger(OpenFlowPluginProviderImpl.class);

    private DeviceManager deviceManager;
    private RpcManager rpcManager;
    private StatisticsManager statisticsManager;
    private ConnectionManager connectionManager;
    private BindingAwareBroker bindingAwareBroker;
    private ProviderContext providerContext;
    private OfpRole role;
    private Collection<SwitchConnectionProvider> switchConnectionProviders;

    @Override
    public void onSessionInitiated(final ProviderContext providerContextArg) {
        this.providerContext = providerContextArg;
        connectionManager = new ConnectionManagerImpl();
        deviceManager = new DeviceManagerImpl(providerContext);
        connectionManager.setDeviceConnectedHandler(deviceManager);
        rpcManager = new RpcManagerImpl(providerContext);

        //TODO : initialize statistics manager
        //TODO : initialize translatorLibrary + inject into deviceMngr

        startSwitchConnections();
    }

    /**
     * @param connectionManager2
     */
    private void startSwitchConnections() {
        List<ListenableFuture<Boolean>> starterChain = new ArrayList<>(switchConnectionProviders.size());
        for (SwitchConnectionProvider switchConnectionPrv : switchConnectionProviders) {
            switchConnectionPrv.setSwitchConnectionHandler(connectionManager);
            ListenableFuture<Boolean> isOnlineFuture = switchConnectionPrv.startup();
            starterChain.add(isOnlineFuture);
        }

        ListenableFuture<List<Boolean>> srvStarted = Futures.allAsList(starterChain);
        Futures.addCallback(srvStarted, new FutureCallback<List<Boolean>>() {
            @Override
            public void onSuccess(List<Boolean> result) {
                LOG.info("All switchConnectionProviders are up and running ({}).",
                        result.size());
            }
            @Override
            public void onFailure(Throwable t) {
                LOG.warn("Some switchConnectionProviders failed to start.", t);
            }
        });
    }

    @Override
    public void setSwitchConnectionProviders(final Collection<SwitchConnectionProvider> switchConnectionProviders) {
        this.switchConnectionProviders = switchConnectionProviders;
    }

    @Override
    public void setRole(final OfpRole role) {
        this.role = role;
    }

    @Override
    public void setBindingAwareBroker(BindingAwareBroker bindingAwareBroker) {
        this.bindingAwareBroker = bindingAwareBroker;

    }

    @Override
    public void initialize() {
        Preconditions.checkNotNull(bindingAwareBroker, "missing bindingAwareBroker");
        bindingAwareBroker.registerProvider(this);
    }

    @Override
    public void close() throws Exception {
        //TODO: close all contexts, switchConnections (, managers)
    }
}
