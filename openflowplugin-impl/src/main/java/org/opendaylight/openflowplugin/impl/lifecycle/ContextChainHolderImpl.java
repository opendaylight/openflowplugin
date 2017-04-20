/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Verify;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.internal.ConcurrentSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipChange;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListenerRegistration;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowplugin.api.openflow.OFPManager;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionStatus;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChain;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainHolder;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextChainHolderImpl implements ContextChainHolder {

    private static final Logger LOG = LoggerFactory.getLogger(ContextChainHolderImpl.class);

    private static final String CONTEXT_CREATED_FOR_CONNECTION = " context created for connection: {}";
    private static final long DEFAULT_CHECK_ROLE_MASTER = 10000L;
    private static final String SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.ServiceEntityType";
    private static final String ASYNC_SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.AsyncServiceCloseEntityType";

    private final ConcurrentHashMap<DeviceInfo, ContextChain> contextChainMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<DeviceInfo, ContextChain> withoutRoleChains = new ConcurrentHashMap<>();
    private final Set<DeviceInfo> markToBeRemoved = new ConcurrentSet<>();
    private final HashedWheelTimer timer;
    private final Long checkRoleMaster;
    private final Object removeLock = new Object();

    private DeviceManager deviceManager;
    private RpcManager rpcManager;
    private StatisticsManager statisticsManager;
    private EntityOwnershipListenerRegistration eosListenerRegistration;
    private ClusterSingletonServiceProvider singletonServicesProvider;
    private boolean timerIsRunningRole;

    public ContextChainHolderImpl(final HashedWheelTimer timer) {
        this.timerIsRunningRole = false;
        this.timer = timer;
        this.checkRoleMaster = DEFAULT_CHECK_ROLE_MASTER;
    }

    @Override
    public <T extends OFPManager> void addManager(final T manager) {
        if (Objects.isNull(deviceManager) && manager instanceof DeviceManager) {
            LOG.debug("Device manager was set.");
            deviceManager = (DeviceManager) manager;
        } else if (Objects.isNull(rpcManager) && manager instanceof RpcManager) {
            LOG.debug("RPC manager was set.");
            rpcManager = (RpcManager) manager;
        } else if (Objects.isNull(statisticsManager) && manager instanceof StatisticsManager) {
            LOG.debug("Statistics manager was set.");
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

        final ContextChain contextChain = new ContextChainImpl(connectionContext);
        final LifecycleService lifecycleService = new LifecycleServiceImpl(this);
        lifecycleService.registerDeviceRemovedHandler(deviceManager);
        lifecycleService.registerDeviceRemovedHandler(rpcManager);
        lifecycleService.registerDeviceRemovedHandler(statisticsManager);

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
        this.withoutRoleChains.put(deviceInfo, contextChain);
        if (!this.timerIsRunningRole) {
            this.startTimerRole();
        }
        deviceContext.onPublished();
        contextChain.registerServices(this.singletonServicesProvider);

        return contextChain;
    }

    @Override
    public ListenableFuture<Void> destroyContextChain(final DeviceInfo deviceInfo) {
        synchronized (removeLock) {
            ContextChain chain = contextChainMap.remove(deviceInfo);
            if (chain != null) {
                chain.close();
            }
            if (markToBeRemoved.contains(deviceInfo)) {
                markToBeRemoved.remove(deviceInfo);
                LOG.info("Removing device: {} from DS", deviceInfo.getLOGValue());
                return deviceManager.removeDeviceFromOperationalDS(deviceInfo);
            } else {
                return Futures.immediateFuture(null);
            }
        }
    }

    @Override
    public ConnectionStatus deviceConnected(final ConnectionContext connectionContext) throws Exception {

        DeviceInfo deviceInfo = connectionContext.getDeviceInfo();
        LOG.info("Device {} connected.", deviceInfo.getLOGValue());
        ContextChain contextChain = contextChainMap.get(deviceInfo);
        if (contextChain != null) {
            if (contextChain.addAuxiliaryConnection(connectionContext)) {
                LOG.info("An auxiliary connection was added to device: {}", deviceInfo.getLOGValue());
                return ConnectionStatus.MAY_CONTINUE;
            } else {
                LOG.warn("Device {} already connected. Closing all connection to the device.", deviceInfo.getLOGValue());
                destroyContextChain(deviceInfo);
                return ConnectionStatus.ALREADY_CONNECTED;
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No context chain found for device: {}, creating new.", deviceInfo.getLOGValue());
            }
            contextChainMap.put(deviceInfo, createContextChain(connectionContext));
        }

        return ConnectionStatus.MAY_CONTINUE;
    }

    @Override
    public void addSingletonServicesProvider(final ClusterSingletonServiceProvider singletonServicesProvider) {
        this.singletonServicesProvider = singletonServicesProvider;
    }

    @Override
    public void onNotAbleToStartMastership(final DeviceInfo deviceInfo, @Nonnull final String reason) {
        this.withoutRoleChains.remove(deviceInfo);
        LOG.warn("Not able to set MASTER role on device {}, reason: {}", deviceInfo.getLOGValue(), reason);
        if (contextChainMap.containsKey(deviceInfo)) {
            destroyContextChain(deviceInfo);
        }
    }

    @Override
    public void onMasterRoleAcquired(final DeviceInfo deviceInfo,
                                     @Nonnull final ContextChainMastershipState mastershipState) {
        this.withoutRoleChains.remove(deviceInfo);
        ContextChain contextChain = contextChainMap.get(deviceInfo);
        if (contextChain != null) {
            if (contextChain.isMastered(mastershipState)) {
                LOG.info("Role MASTER was granted to device {}", deviceInfo.getLOGValue());
                this.sendNotificationNodeAdded(deviceInfo);
            }
        }
    }

    @Override
    public void onSlaveRoleAcquired(final DeviceInfo deviceInfo) {
        this.withoutRoleChains.remove(deviceInfo);
        ContextChain contextChain = contextChainMap.get(deviceInfo);
        if (contextChain != null) {
            contextChain.makeContextChainStateSlave();
        }
    }

    @Override
    public void onSlaveRoleNotAcquired(final DeviceInfo deviceInfo) {
        this.withoutRoleChains.remove(deviceInfo);
        ContextChain contextChain = contextChainMap.get(deviceInfo);
        if (contextChain != null) {
            destroyContextChain(deviceInfo);
        }
    }

    @Override
    public void onDeviceDisconnected(final ConnectionContext connectionContext) {

        final DeviceInfo deviceInfo = connectionContext.getDeviceInfo();
        ContextChain chain;
        DeviceInfo info = null;

        if (deviceInfo != null) {
            chain = contextChainMap.get(deviceInfo);
            if (chain != null) {
                if (chain.auxiliaryConnectionDropped(connectionContext)) {
                    LOG.info("Auxiliary connection from device {} disconnected.", deviceInfo.getLOGValue());
                    return;
                } else {
                    LOG.info("Device {} disconnected.", deviceInfo.getLOGValue());
                    Futures.transform(chain.connectionDropped(), new Function<Void, Object>() {
                        @Nullable
                        @Override
                        public Object apply(@Nullable Void aVoid) {
                            destroyContextChain(deviceInfo);
                            return null;
                        }
                    });
                }
            }
        }

        /*
         * Following part of code:
         * In some cases if controller is reconnected without switches being restarted
         * plugin get disconnect event after controller rejoining with new connection adapter
         * and only thing can work as identifier were remote address.
         */

        chain = null;
        for (Map.Entry<DeviceInfo, ContextChain> chainEntry : withoutRoleChains.entrySet()) {
            if (chainEntry.getKey().getRemoteAddress().getHostName().equals(
                    connectionContext.getConnectionAdapter().getRemoteAddress().getHostName())) {
                chain = chainEntry.getValue();
                info = chainEntry.getKey();
                break;
            }
        }

        final DeviceInfo finalInfo = info;
        if (chain != null) {
            if (chain.auxiliaryConnectionDropped(connectionContext)) {
                LOG.info("Auxiliary connection from device {} disconnected.", info.getLOGValue());
            } else {
                LOG.info("Device {} disconnected.", info.getLOGValue());
                Futures.transform(chain.connectionDropped(), new Function<Void, Object>() {
                    @Nullable
                    @Override
                    public Object apply(@Nullable Void aVoid) {
                        destroyContextChain(finalInfo);
                        return null;
                    }
                });
            }
        }
    }

    @Override
    public void changeEntityOwnershipService(final EntityOwnershipService entityOwnershipService) {
        if (Objects.nonNull(this.eosListenerRegistration)) {
            LOG.warn("EOS Listener already registered.");
        } else {
            this.eosListenerRegistration = Verify.verifyNotNull(entityOwnershipService.registerListener
                    (SERVICE_ENTITY_TYPE, this));
        }
    }

    private void startTimerRole() {
        this.timerIsRunningRole = true;
        if (LOG.isDebugEnabled()) {
            LOG.debug("There is a context chain without role, starting timer.");
        }
        timer.newTimeout(new RoleTimerTask(), this.checkRoleMaster, TimeUnit.MILLISECONDS);
    }

    private void stopTimerRole() {
        this.timerIsRunningRole = false;
        if (LOG.isDebugEnabled()) {
            LOG.debug("There are no context chains, stopping timer.");
        }
    }

    private void timerTickRole() {
        if (!withoutRoleChains.isEmpty()) {
            this.withoutRoleChains.forEach((deviceInfo, contextChain) -> contextChain.makeDeviceSlave());
            timer.newTimeout(new RoleTimerTask(), this.checkRoleMaster, TimeUnit.MILLISECONDS);
        } else {
            final Set<DeviceInfo> setOfClosedChains = new ConcurrentSet<>();
            if (!this.contextChainMap.isEmpty()) {
                this.contextChainMap.forEach((deviceInfo, contextChain) -> {
                    if (!contextChain.hasState()) {
                        LOG.warn("Context chain {} is long time without state. Closing.", deviceInfo);
                        setOfClosedChains.add(deviceInfo);
                        contextChain.close();
                    }
                });
                setOfClosedChains.forEach(this.contextChainMap::remove);
            }
            if (this.contextChainMap.isEmpty()) {
                this.stopTimerRole();
            } else {
                timer.newTimeout(new RoleTimerTask(), this.checkRoleMaster, TimeUnit.MILLISECONDS);
            }
        }
    }

    @VisibleForTesting
    boolean checkAllManagers() {
        return Objects.nonNull(deviceManager) && Objects.nonNull(rpcManager) && Objects.nonNull(statisticsManager);
    }

    @Override
    public void close() throws Exception {
        this.contextChainMap.forEach((deviceInfo, contextChain) -> {
            if (contextChain.isMastered(ContextChainMastershipState.CHECK)) {
                contextChain.stopChain(true);
            }
            contextChain.close();
        });
        if (Objects.nonNull(eosListenerRegistration)) {
            eosListenerRegistration.close();
        }
    }

    @Override
    public void ownershipChanged(EntityOwnershipChange entityOwnershipChange) {
        if (!entityOwnershipChange.hasOwner() && !entityOwnershipChange.isOwner() && entityOwnershipChange.wasOwner()) {
            final YangInstanceIdentifier yii = entityOwnershipChange.getEntity().getId();
            final YangInstanceIdentifier.NodeIdentifierWithPredicates niiwp =
                    (YangInstanceIdentifier.NodeIdentifierWithPredicates) yii.getLastPathArgument();
            String entityName =  niiwp.getKeyValues().values().iterator().next().toString();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Last master for entity : {}", entityName);
            }

            if (entityName != null ){
                final NodeId nodeId = new NodeId(entityName);
                DeviceInfo inMap = null;
                synchronized (removeLock) {
                    for (Map.Entry<DeviceInfo, ContextChain> entry : contextChainMap.entrySet()) {
                        if (entry.getKey().getNodeId().equals(nodeId)) {
                            inMap = entry.getKey();
                            break;
                        }
                    }
                    if (Objects.nonNull(inMap)) {
                        markToBeRemoved.add(inMap);
                    } else {
                        try {
                            LOG.info("Removing device: {} from DS", nodeId);
                            deviceManager
                                    .removeDeviceFromOperationalDS(DeviceStateUtil.createNodeInstanceIdentifier(nodeId))
                                    .checkedGet(5L, TimeUnit.SECONDS);
                        } catch (TimeoutException | TransactionCommitFailedException e) {
                            LOG.warn("Not able to remove device {} from DS", nodeId);
                        }
                    }
                }
            }
        }
    }

    private void sendNotificationNodeAdded(final DeviceInfo deviceInfo) {
        this.deviceManager.sendNodeAddedNotification(deviceInfo);
    }

    private class RoleTimerTask implements TimerTask {

        @Override
        public void run(Timeout timeout) throws Exception {
            timerTickRole();
        }

    }
}

