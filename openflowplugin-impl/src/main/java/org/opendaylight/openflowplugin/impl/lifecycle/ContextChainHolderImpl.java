/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import com.google.common.base.Verify;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.openflowplugin.api.openflow.OFPManager;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionStatus;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChain;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainHolder;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.openflow.provider.config.ContextChainConfig;

public class ContextChainHolderImpl implements ContextChainHolder {

    private DeviceManager deviceManager;
    private RpcManager rpcManager;
    private StatisticsManager statisticsManager;
    private ConcurrentHashMap<DeviceInfo, ContextChain> contextChainMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<DeviceInfo, ConnectionContext> latestConnections = new ConcurrentHashMap<>();
    private final ContextChainConfig config;

    public ContextChainHolderImpl(final ContextChainConfig config) {
        this.config = config;
    }

    @Override
    public <T extends OFPManager> void addManager(final T manager) {
        if (Objects.isNull(deviceManager) && manager instanceof DeviceManager) {
            deviceManager = (DeviceManager) manager;
        } else if (Objects.isNull(rpcManager) && manager instanceof RpcManager) {
            rpcManager = (RpcManager) manager;
        } else if (Objects.isNull(statisticsManager) && manager instanceof StatisticsManager) {
            statisticsManager = (StatisticsManager) manager;
        }
    }

    @Override
    public ContextChain createContextChain(final ConnectionContext connectionContext) {

        ContextChain contextChain = new ContextChainImpl();
        LifecycleService lifecycleService = new LifecycleServiceImpl();

        final DeviceContext deviceContext = deviceManager.createContext(connectionContext);
        final RpcContext rpcContext = rpcManager.createContext(connectionContext.getDeviceInfo(), deviceContext);
        final StatisticsContext statisticsContext = statisticsManager.createContext(connectionContext.getDeviceInfo(), lifecycleService);
        lifecycleService.setDeviceContext(deviceContext);
        lifecycleService.setRpcContext(rpcContext);
        lifecycleService.setStatContext(statisticsContext);

        contextChain.addContext(deviceContext);
        contextChain.addContext(rpcContext);
        contextChain.addContext(statisticsContext);

        return contextChain;

    }

    @Override
    public ListenableFuture<Void> connectionLost(final DeviceInfo deviceInfo) {
        if (!this.checkChainContext(deviceInfo)) {
            return Futures.immediateFuture(null);
        }
        return null;
    }

    @Override
    public void destroyContextChain(final DeviceInfo deviceInfo) {
        ContextChain chain = contextChainMap.get(deviceInfo);
        if (Objects.nonNull(chain)) {
            chain.close();
        }
    }

    @Override
    public void pairConnection(final ConnectionContext connectionContext) {
        DeviceInfo deviceInfo = connectionContext.getDeviceInfo();
        latestConnections.put(deviceInfo, connectionContext);
        if (checkChainContext(deviceInfo)) {
            contextChainMap.get(deviceInfo).changePrimaryConnection(connectionContext);
        }
    }

    @Override
    public ConnectionStatus deviceConnected(final ConnectionContext connectionContext) throws Exception {
        Verify.verify(this.checkAllManagers(),"Not all manager were set.");
        ContextChain chain = contextChainMap.get(connectionContext.getDeviceInfo());
        if (Objects.isNull(chain)) {
            contextChainMap.put(connectionContext.getDeviceInfo(), createContextChain(connectionContext));
        }
        return ConnectionStatus.MAY_CONTINUE;
    }

    private boolean checkAllManagers() {
        return Objects.nonNull(deviceManager) && Objects.nonNull(rpcManager) && Objects.nonNull(statisticsManager);
    }

    private boolean checkChainContext(final DeviceInfo deviceInfo) {
        return contextChainMap.containsKey(deviceInfo);
    }

}
