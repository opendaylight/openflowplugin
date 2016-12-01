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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.ContextChainState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.openflow.provider.config.ContextChainConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextChainHolderImpl implements ContextChainHolder {

    private static final Logger LOG = LoggerFactory.getLogger(ContextChainHolderImpl.class);
    private static final String NOT_ALL_MANAGER_WERE_SET = "Not all manager were set.";
    private static final String MANAGER_WAS_SET = " manager was set";
    private static final String CONTEXT_CREATED_FOR_CONNECTION = " context created for connection: {}";
    private static final String SINGLETON_SERVICE_PROVIDER_WAS_NOT_SET_YET
            = "Singleton service provider was not set yet.";

    private DeviceManager deviceManager;
    private RpcManager rpcManager;
    private StatisticsManager statisticsManager;
    private ConcurrentHashMap<DeviceInfo, ContextChain> contextChainMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<DeviceInfo, ConnectionContext> latestConnections = new ConcurrentHashMap<>();
    private final ContextChainConfig config;
    private ClusterSingletonServiceProvider singletonServicesProvider;

    public ContextChainHolderImpl(final ContextChainConfig config) {
        this.config = config;
    }

    @Override
    public <T extends OFPManager> void addManager(final T manager) {
        if (Objects.isNull(deviceManager) && manager instanceof DeviceManager) {
            LOG.info("Device" + MANAGER_WAS_SET);
            deviceManager = (DeviceManager) manager;
        } else if (Objects.isNull(rpcManager) && manager instanceof RpcManager) {
            LOG.info("RPC" + MANAGER_WAS_SET);
            rpcManager = (RpcManager) manager;
        } else if (Objects.isNull(statisticsManager) && manager instanceof StatisticsManager) {
            LOG.info("Statistics" + MANAGER_WAS_SET);
            statisticsManager = (StatisticsManager) manager;
        }
    }

    @Override
    public ContextChain createContextChain(final ConnectionContext connectionContext) {

        final DeviceInfo deviceInfo = connectionContext.getDeviceInfo();
        final String deviceInfoLOGValue = deviceInfo.getLOGValue();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating a new chain" + CONTEXT_CREATED_FOR_CONNECTION, deviceInfoLOGValue);
        }

        ContextChain contextChain = new ContextChainImpl();
        LifecycleService lifecycleService = new LifecycleServiceImpl(this);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Lifecycle services" + CONTEXT_CREATED_FOR_CONNECTION, deviceInfoLOGValue);
        }

        final DeviceContext deviceContext = deviceManager.createContext(connectionContext);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Device" + CONTEXT_CREATED_FOR_CONNECTION, deviceInfoLOGValue);
        }

        final RpcContext rpcContext = rpcManager.createContext(connectionContext.getDeviceInfo(), deviceContext);

        if (LOG.isDebugEnabled()) {
            LOG.debug("RPC" + CONTEXT_CREATED_FOR_CONNECTION, deviceInfoLOGValue);
        }

        final StatisticsContext statisticsContext
                = statisticsManager.createContext(deviceContext);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Statistics" + CONTEXT_CREATED_FOR_CONNECTION, deviceInfoLOGValue);
        }

        deviceContext.setLifecycleInitializationPhaseHandler(statisticsContext);
        statisticsContext.setLifecycleInitializationPhaseHandler(rpcContext);
        statisticsContext.setInitialSubmitHandler(deviceContext);

        contextChain.addLifecycleService(lifecycleService);
        contextChain.addContext(deviceContext);
        contextChain.addContext(rpcContext);
        contextChain.addContext(statisticsContext);
        contextChain.makeDeviceSlave();

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

        Verify.verify(this.checkAllManagers(), NOT_ALL_MANAGER_WERE_SET);
        Verify.verifyNotNull(this.singletonServicesProvider, SINGLETON_SERVICE_PROVIDER_WAS_NOT_SET_YET);

        LOG.info("Device {} connected.", connectionContext.getDeviceInfo().getLOGValue());
        ContextChain chain = contextChainMap.get(connectionContext.getDeviceInfo());

        if (Objects.isNull(chain)) {
            contextChainMap.put(connectionContext.getDeviceInfo(), createContextChain(connectionContext));
        } else {
            this.pairConnection(connectionContext);
        }

        return ConnectionStatus.MAY_CONTINUE;
    }

    @Override
    public void addSingletonServicesProvider(final ClusterSingletonServiceProvider singletonServicesProvider) {
        this.singletonServicesProvider = singletonServicesProvider;
    }

    @Override
    public void onNotAbleToStartMastership(final DeviceInfo deviceInfo) {
        ContextChain contextChain = contextChainMap.get(deviceInfo);
        if (Objects.nonNull(contextChain)) {
            LOG.warn("Not able to set MASTER role on device {}", deviceInfo.getLOGValue());
            if (contextChain.getContextChainState().equals(ContextChainState.INITIALIZED)) {
                contextChain.getPrimaryConnectionContext().closeConnection(false);
            } else {
                contextChain.sleepTheChainAndDropConnection();
            }
        }
    }

    @Override
    public void onMasterRoleAcquired(final DeviceInfo deviceInfo) {
        ContextChain contextChain = contextChainMap.get(deviceInfo);
        if (Objects.nonNull(contextChain)) {
            if (contextChain.getContextChainState().equals(ContextChainState.WORKINGMASTER)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Device {} already working as MASTER no changes need to be done.",
                            deviceInfo.getLOGValue());
                }
            } else {
                if (contextChain.getContextChainState().equals(ContextChainState.INITIALIZED)) {
                    LOG.info("Device {} has not finish initial gathering yet.",
                            deviceInfo.getLOGValue());
                }
                contextChain.startChain();
            }
        }
    }

    @Override
    public void onSlaveRoleAcquired(final DeviceInfo deviceInfo) {
        ContextChain contextChain = contextChainMap.get(deviceInfo);
        if (Objects.nonNull(contextChain)) {
            if (contextChain.getContextChainState().equals(ContextChainState.INITIALIZED)) {
                contextChain.registerServices(this.singletonServicesProvider);
            } else {
                contextChain.stopChain();
            }
        }
    }

    @Override
    public void onSlaveRoleNotAcquired(final DeviceInfo deviceInfo) {
        ContextChain contextChain = contextChainMap.get(deviceInfo);
        if (Objects.nonNull(contextChain)) {
            contextChain.sleepTheChainAndDropConnection();
        }
    }

    private boolean checkAllManagers() {
        return Objects.nonNull(deviceManager) && Objects.nonNull(rpcManager) && Objects.nonNull(statisticsManager);
    }

    private boolean checkChainContext(final DeviceInfo deviceInfo) {
        return contextChainMap.containsKey(deviceInfo);
    }

}
