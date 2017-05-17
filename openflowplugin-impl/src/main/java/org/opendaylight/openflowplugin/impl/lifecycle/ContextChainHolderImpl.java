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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
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

    private final Map<DeviceInfo, ContextChain> contextChainMap =
            Collections.synchronizedMap(new HashMap<>());
    private final Map<DeviceInfo, ContextChain> withoutRoleChains =
            Collections.synchronizedMap(new HashMap<>());
    private final Map<DeviceInfo, ContextChain> withoutRoleChainsQueue =
            Collections.synchronizedMap(new HashMap<>());
    private final List<DeviceInfo> markToBeRemoved = new ArrayList<>();
    private final HashedWheelTimer timer;
    private final Long delayCheckRoleMaster;

    private DeviceManager deviceManager;
    private RpcManager rpcManager;
    private StatisticsManager statisticsManager;
    private EntityOwnershipListenerRegistration eosListenerRegistration;
    private ClusterSingletonServiceProvider singletonServicesProvider;
    private AtomicBoolean timerIsRunningRole;
    private Timeout runningSlaveTask = null;

    public ContextChainHolderImpl(final HashedWheelTimer timer) {
        this.timerIsRunningRole = new AtomicBoolean(false);
        this.timer = timer;
        this.delayCheckRoleMaster = DEFAULT_CHECK_ROLE_MASTER;
    }

    @Override
    public <T extends OFPManager> void addManager(final T manager) {
        if (Objects.isNull(deviceManager) && manager instanceof DeviceManager) {
            LOG.trace("Context chain holder: Device manager OK.");
            deviceManager = (DeviceManager) manager;
        } else if (Objects.isNull(rpcManager) && manager instanceof RpcManager) {
            LOG.trace("Context chain holder: RPC manager OK.");
            rpcManager = (RpcManager) manager;
        } else if (Objects.isNull(statisticsManager) && manager instanceof StatisticsManager) {
            LOG.trace("Context chain holder: Statistics manager OK.");
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
        if (!this.timerIsRunningRole.get()) {
            this.addWithoutRoleChain(deviceInfo, contextChain);
            this.startTimerRole();
        } else {
            this.withoutRoleChainsQueue.put(deviceInfo, contextChain);
        }
        deviceContext.onPublished();
        contextChain.registerServices(this.singletonServicesProvider);

        return contextChain;
    }

    @Override
    public synchronized ListenableFuture<Void> destroyContextChain(final DeviceInfo deviceInfo) {
        ContextChain chain = contextChainMap.remove(deviceInfo);
        if (chain != null) {
            chain.close();
        }
        if (markToBeRemoved.contains(deviceInfo)) {
            markToBeRemoved.remove(deviceInfo);
            LOG.info("CONTEXT: Removing device: {} from DS", deviceInfo.getLOGValue());
            return deviceManager.removeDeviceFromOperationalDS(deviceInfo);
        } else {
            return Futures.immediateFuture(null);
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
    public void onNotAbleToStartMastership(final DeviceInfo deviceInfo, @Nonnull final String reason, final boolean mandatory) {
        this.removeWithoutRoleChain(deviceInfo);
        LOG.warn("Not able to set MASTER role on device {}, reason: {}", deviceInfo.getLOGValue(), reason);
        if (mandatory && contextChainMap.containsKey(deviceInfo)) {
            LOG.warn("This mastering is mandatory, destroying context chain and closing connection.");
            Futures.transform(contextChainMap.get(deviceInfo).stopChain(), new Function<Void, Object>() {
                        @Nullable
                        @Override
                        public Object apply(@Nullable Void aVoid) {
                            destroyContextChain(deviceInfo);
                            return null;
                        }
                    });
        }
    }

    @Override
    public void onMasterRoleAcquired(final DeviceInfo deviceInfo,
                                     @Nonnull final ContextChainMastershipState mastershipState) {
        this.removeWithoutRoleChain(deviceInfo);
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
        this.removeWithoutRoleChain(deviceInfo);
        ContextChain contextChain = contextChainMap.get(deviceInfo);
        if (contextChain != null) {
            contextChain.makeContextChainStateSlave();
        }
    }

    @Override
    public void onSlaveRoleNotAcquired(final DeviceInfo deviceInfo) {
        this.removeWithoutRoleChain(deviceInfo);
        ContextChain contextChain = contextChainMap.get(deviceInfo);
        if (contextChain != null) {
            destroyContextChain(deviceInfo);
        }
    }

    @Override
    public void onDeviceDisconnected(final ConnectionContext connectionContext) {

        final DeviceInfo deviceInfo = connectionContext.getDeviceInfo();
        if (deviceInfo != null) {
            ContextChain chain = contextChainMap.get(deviceInfo);
            if (chain != null) {
                if (chain.auxiliaryConnectionDropped(connectionContext)) {
                    LOG.info("Auxiliary connection from device {} disconnected.", deviceInfo.getLOGValue());
                } else {
                    LOG.info("Device {} disconnected.", deviceInfo.getLOGValue());
                    removeWithoutRoleChain(deviceInfo);
                    sendNotificationNodeRemoved(deviceInfo);
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
    }

    @Override
    public void changeEntityOwnershipService(final EntityOwnershipService entityOwnershipService) {
        if (Objects.nonNull(this.eosListenerRegistration)) {
            LOG.warn("EOS Listener already registered.");
        } else {
            this.eosListenerRegistration = Verify.verifyNotNull(entityOwnershipService.registerListener
                    (ASYNC_SERVICE_ENTITY_TYPE, this));
        }
    }

    private void startTimerRole() {
        this.timerIsRunningRole.set(true);
        runningSlaveTask = timer.newTimeout(new RoleTimerTask(), this.delayCheckRoleMaster, TimeUnit.MILLISECONDS);
    }

    private void stopTimerRole() {
        synchronized (withoutRoleChainsQueue) {
            this.timerIsRunningRole.set(false);
            if (!this.withoutRoleChainsQueue.isEmpty()) {
                this.withoutRoleChainsQueue.forEach(this::addWithoutRoleChain);
                this.withoutRoleChainsQueue.clear();
                this.startTimerRole();
            }
        }
    }

    private void timerTickRole() {
        synchronized (withoutRoleChains) {
            if (!withoutRoleChains.isEmpty()) {
                this.withoutRoleChains.forEach((deviceInfo, contextChain) -> contextChain.makeDeviceSlave());
                runningSlaveTask = timer.newTimeout(new RoleTimerTask(), this.delayCheckRoleMaster, TimeUnit.MILLISECONDS);
            } else {
                if (this.runningSlaveTask != null) {
                    this.runningSlaveTask.cancel();
                }
                this.runningSlaveTask = null;
                this.stopTimerRole();
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
                this.sendNotificationNodeRemoved(deviceInfo);
                contextChain.stopChain();
            }
            contextChain.close();
        });
        if (Objects.nonNull(eosListenerRegistration)) {
            eosListenerRegistration.close();
        }
    }

    @Override
    public void ownershipChanged(EntityOwnershipChange entityOwnershipChange) {
        if (!entityOwnershipChange.hasOwner()) {
            final YangInstanceIdentifier yii = entityOwnershipChange.getEntity().getId();
            final YangInstanceIdentifier.NodeIdentifierWithPredicates niiwp =
                    (YangInstanceIdentifier.NodeIdentifierWithPredicates) yii.getLastPathArgument();
            String entityName =  niiwp.getKeyValues().values().iterator().next().toString();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entity {} has no owner", entityName);
            }

            if (entityName != null ){
                final NodeId nodeId = new NodeId(entityName);
                DeviceInfo inMap = null;
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
                        LOG.info("EOS: Removing device: {} from DS", nodeId);
                        deviceManager
                                .removeDeviceFromOperationalDS(DeviceStateUtil.createNodeInstanceIdentifier(nodeId))
                                .checkedGet(5L, TimeUnit.SECONDS);
                    } catch (TimeoutException | TransactionCommitFailedException e) {
                        LOG.info("Not able to remove device {} from DS. Probably removed by another cluster node.",
                                nodeId);
                    }
                }
            }
        }
    }

    private void sendNotificationNodeRemoved(final DeviceInfo deviceInfo) {
        this.deviceManager.sendNodeRemovedNotification(deviceInfo);
    }

    private void sendNotificationNodeAdded(final DeviceInfo deviceInfo) {
        this.deviceManager.sendNodeAddedNotification(deviceInfo);
    }

    private void removeWithoutRoleChain(final DeviceInfo deviceInfo) {
        this.withoutRoleChainsQueue.remove(deviceInfo);
        this.withoutRoleChains.remove(deviceInfo);
        if (withoutRoleChains.isEmpty()) {
            if (runningSlaveTask != null) {
                runningSlaveTask.cancel();
                runningSlaveTask = null;
            }
            this.stopTimerRole();
        }
    }

    private void addWithoutRoleChain(final DeviceInfo deviceInfo, ContextChain contextChain) {
        this.withoutRoleChains.put(deviceInfo, contextChain);
    }

    private class RoleTimerTask implements TimerTask {

        @Override
        public void run(Timeout timeout) throws Exception {
            timerTickRole();
        }

    }
}

