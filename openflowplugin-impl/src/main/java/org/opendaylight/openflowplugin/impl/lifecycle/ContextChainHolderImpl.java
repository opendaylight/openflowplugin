/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Verify;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.HashedWheelTimer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
import org.opendaylight.openflowplugin.impl.util.ItemScheduler;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextChainHolderImpl implements ContextChainHolder {
    private static final Logger LOG = LoggerFactory.getLogger(ContextChainHolderImpl.class);

    private static final String CONTEXT_CREATED_FOR_CONNECTION = " context created for connection: {}";
    private static final long CHECK_ROLE_MASTER_TIMEOUT = 20000L;
    private static final long CHECK_ROLE_MASTER_TOLERANCE = CHECK_ROLE_MASTER_TIMEOUT / 2;
    private static final long REMOVE_DEVICE_FROM_DS_TIMEOUT = 5000L;
    private static final String ASYNC_SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.AsyncServiceCloseEntityType";

    private final Map<DeviceInfo, ContextChain> contextChainMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<KeyedInstanceIdentifier<Node, NodeKey>, ContextChain> toBeUnregistered = Collections.synchronizedMap(new HashMap<>());
    private DeviceManager deviceManager;
    private RpcManager rpcManager;
    private StatisticsManager statisticsManager;
    private EntityOwnershipListenerRegistration eosListenerRegistration;
    private ClusterSingletonServiceProvider singletonServicesProvider;
    private final ItemScheduler<DeviceInfo, ContextChain> scheduler;

    public ContextChainHolderImpl(final HashedWheelTimer timer) {
        this.scheduler = new ItemScheduler<>(
                timer,
                CHECK_ROLE_MASTER_TIMEOUT,
                CHECK_ROLE_MASTER_TOLERANCE,
                ContextChain::makeDeviceSlave);
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
        final ContextChain contextChain = new ContextChainImpl(connectionContext);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Context chain" + CONTEXT_CREATED_FOR_CONNECTION, deviceInfoLOGValue);
        }

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

        final StatisticsContext statisticsContext = statisticsManager.createContext(deviceContext);

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

        LOG.info("Starting timer for setting SLAVE role on node {} if no role will be set in {}s.",
                deviceInfo.getLOGValue(), CHECK_ROLE_MASTER_TIMEOUT / 1000L);
        scheduler.add(deviceInfo, contextChain);
        scheduler.startIfNotRunning();

        deviceContext.onPublished();
        toBeUnregistered.remove(deviceInfo.getNodeInstanceIdentifier());
        contextChain.registerServices(this.singletonServicesProvider);
        return contextChain;
    }

    @Override
    public synchronized void destroyContextChain(final DeviceInfo deviceInfo) {
        Optional.ofNullable(contextChainMap.remove(deviceInfo)).ifPresent(contextChain -> {
            deviceManager.sendNodeRemovedNotification(deviceInfo.getNodeInstanceIdentifier());
            contextChain.close();
            toBeUnregistered.put(deviceInfo.getNodeInstanceIdentifier(), contextChain);
        });
    }

    @Override
    public ConnectionStatus deviceConnected(final ConnectionContext connectionContext) throws Exception {
        final DeviceInfo deviceInfo = connectionContext.getDeviceInfo();
        final ContextChain contextChain = contextChainMap.get(deviceInfo);
        LOG.info("Device {} connected.", deviceInfo.getLOGValue());

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
        LOG.warn("Not able to set MASTER role on device {}, reason: {}", deviceInfo.getLOGValue(), reason);

        if (!mandatory) {
            return;
        }

        Optional.ofNullable(contextChainMap.get(deviceInfo)).ifPresent(contextChain -> {
            LOG.warn("This mastering is mandatory, destroying context chain and closing connection for device {}.", deviceInfo.getLOGValue());
            addDestroyChainCallback(contextChain.stopChain(), deviceInfo);
        });
    }

    @Override
    public void onMasterRoleAcquired(final DeviceInfo deviceInfo, @Nonnull final ContextChainMastershipState mastershipState) {
        scheduler.remove(deviceInfo);

        Optional.ofNullable(contextChainMap.get(deviceInfo)).ifPresent(contextChain -> {
            if (contextChain.isMastered(mastershipState)) {
                LOG.info("Role MASTER was granted to device {}", deviceInfo.getLOGValue());
                deviceManager.sendNodeAddedNotification(deviceInfo.getNodeInstanceIdentifier());
            }
        });
    }

    @Override
    public void onSlaveRoleAcquired(final DeviceInfo deviceInfo) {
        Optional.ofNullable(contextChainMap.get(deviceInfo)).ifPresent(ContextChain::makeContextChainStateSlave);
    }

    @Override
    public void onSlaveRoleNotAcquired(final DeviceInfo deviceInfo) {
        Optional.ofNullable(contextChainMap.get(deviceInfo)).ifPresent(contextChain -> destroyContextChain(deviceInfo));
    }

    @Override
    public void onDeviceDisconnected(final ConnectionContext connectionContext) {
        final DeviceInfo deviceInfo = connectionContext.getDeviceInfo();

        if (Objects.isNull(deviceInfo)) {
            return;
        }

        Optional.ofNullable(contextChainMap.get(deviceInfo)).ifPresent(contextChain -> {
            if (contextChain.auxiliaryConnectionDropped(connectionContext)) {
                LOG.info("Auxiliary connection from device {} disconnected.", deviceInfo.getLOGValue());
            } else {
                LOG.info("Device {} disconnected.", deviceInfo.getLOGValue());
                addDestroyChainCallback(contextChain.connectionDropped(), deviceInfo);
            }
        });
    }

    @Override
    public void changeEntityOwnershipService(@Nonnull final EntityOwnershipService entityOwnershipService) {
        if (Objects.nonNull(this.eosListenerRegistration)) {
            LOG.warn("Entity ownership service listener is already registered.");
        } else {
            this.eosListenerRegistration = Verify.verifyNotNull(entityOwnershipService.registerListener
                    (ASYNC_SERVICE_ENTITY_TYPE, this));
        }
    }

    @VisibleForTesting
    boolean checkAllManagers() {
        return Objects.nonNull(deviceManager) && Objects.nonNull(rpcManager) && Objects.nonNull(statisticsManager);
    }

    @Override
    public void close() throws Exception {
        scheduler.close();

        contextChainMap.forEach((deviceInfo, contextChain) -> {
            if (contextChain.isMastered(ContextChainMastershipState.CHECK)) {
                addDestroyChainCallback(contextChain.stopChain(), deviceInfo);
            } else {
                destroyContextChain(deviceInfo);
            }
        });

        contextChainMap.clear();


        if (Objects.nonNull(eosListenerRegistration)) {
            eosListenerRegistration.close();
            eosListenerRegistration = null;
        }

        toBeUnregistered.values().forEach(ContextChain::unregisterServices);
        toBeUnregistered.clear();
    }

    @Override
    public void ownershipChanged(EntityOwnershipChange entityOwnershipChange) {
        final String entityName = getEntityNameFromOwnershipChange(entityOwnershipChange);

        if (Objects.nonNull(entityName)) {
            final NodeId nodeId = new NodeId(entityName);

            final KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier =
                    DeviceStateUtil.createNodeInstanceIdentifier(nodeId);

            Optional.ofNullable(toBeUnregistered.remove(nodeInstanceIdentifier)).ifPresent(ContextChain::unregisterServices);

            if (!entityOwnershipChange.hasOwner()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Entity {} has no owner", entityName);
                }

                try {
                    deviceManager.sendNodeRemovedNotification(nodeInstanceIdentifier);

                    LOG.info("Removing device {} from operational DS", nodeId);
                    deviceManager
                            .removeDeviceFromOperationalDS(nodeInstanceIdentifier)
                            .checkedGet(REMOVE_DEVICE_FROM_DS_TIMEOUT, TimeUnit.MILLISECONDS);
                } catch (TimeoutException | TransactionCommitFailedException e) {
                    LOG.info("Not able to remove device {} from operational DS. Probably removed by another cluster node.",
                            nodeId);
                }
            }
        }
    }

    private String getEntityNameFromOwnershipChange(final EntityOwnershipChange entityOwnershipChange) {
        final YangInstanceIdentifier.NodeIdentifierWithPredicates lastIdArgument =
                (YangInstanceIdentifier.NodeIdentifierWithPredicates) entityOwnershipChange
                        .getEntity()
                        .getId()
                        .getLastPathArgument();

        return lastIdArgument
                .getKeyValues()
                .values()
                .iterator()
                .next()
                .toString();
    }

    private void addDestroyChainCallback(final ListenableFuture<Void> future, final DeviceInfo deviceInfo) {
        scheduler.remove(deviceInfo);

        Futures.addCallback(future, new FutureCallback<Void>() {
            @Override
            public void onSuccess(@Nullable final Void aVoid) {
                destroyContextChain(deviceInfo);
            }

            @Override
            public void onFailure(@Nonnull final Throwable throwable) {
                destroyContextChain(deviceInfo);
            }
        });
    }
}