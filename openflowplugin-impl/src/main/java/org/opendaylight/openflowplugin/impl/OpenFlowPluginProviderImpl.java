/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl;


import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginProvider;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.openflowplugin.impl.connection.ConnectionManagerImpl;
import org.opendaylight.openflowplugin.impl.device.DeviceManagerImpl;
import org.opendaylight.openflowplugin.impl.rpc.RpcManagerImpl;
import org.opendaylight.openflowplugin.impl.statistics.StatisticsManagerImpl;
import org.opendaylight.openflowplugin.impl.translator.PacketReceivedTranslator;
import org.opendaylight.openflowplugin.impl.translator.PortUpdateTranslator;
import org.opendaylight.openflowplugin.impl.translator.TranslatorKeyFactory;
import org.opendaylight.openflowplugin.impl.translator.TranslatorLibraryBuilder;
import org.opendaylight.openflowplugin.impl.util.TranslatorLibraryUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.api.types.rev150327.OfpRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        providerContext = providerContextArg;
        connectionManager = new ConnectionManagerImpl();
        rpcManager = new RpcManagerImpl(providerContext);
        deviceManager = new DeviceManagerImpl(rpcManager, providerContext.getSALService(DataBroker.class));
        connectionManager.setDeviceConnectedHandler(deviceManager);
        statisticsManager = new StatisticsManagerImpl();
        TranslatorLibraryUtil.setBasicTranslatorLibrary(deviceManager);
        deviceManager.setDeviceInitializationPhaseHandler(statisticsManager);
        statisticsManager.setDeviceInitializationPhaseHandler(rpcManager);

        final TranslatorKeyFactory of13TranslatorKeyFactory = new TranslatorKeyFactory(OFConstants.OFP_VERSION_1_3);
        final TranslatorLibrary translatorLibrary = new TranslatorLibraryBuilder().
                addTranslator(of13TranslatorKeyFactory.createTranslatorKey(PacketReceived.class), new PacketReceivedTranslator()).
                addTranslator(of13TranslatorKeyFactory.createTranslatorKey(PacketReceived.class), new PortUpdateTranslator()).
                build();
        //TODO : initialize translatorLibrary + inject into deviceMngr
        startSwitchConnections();
    }

    private void startSwitchConnections() {
        final List<ListenableFuture<Boolean>> starterChain = new ArrayList<>(switchConnectionProviders.size());
        for (final SwitchConnectionProvider switchConnectionPrv : switchConnectionProviders) {
            switchConnectionPrv.setSwitchConnectionHandler(connectionManager);
            final ListenableFuture<Boolean> isOnlineFuture = switchConnectionPrv.startup();
            starterChain.add(isOnlineFuture);
        }

        final ListenableFuture<List<Boolean>> srvStarted = Futures.allAsList(starterChain);
        Futures.addCallback(srvStarted, new FutureCallback<List<Boolean>>() {
            @Override
            public void onSuccess(final List<Boolean> result) {
                LOG.info("All switchConnectionProviders are up and running ({}).",
                        result.size());
            }

            @Override
            public void onFailure(final Throwable t) {
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
    public void setBindingAwareBroker(final BindingAwareBroker bindingAwareBroker) {
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
