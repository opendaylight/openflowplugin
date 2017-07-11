/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.FutureCallback;
import io.netty.util.HashedWheelTimer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
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
import org.opendaylight.openflowplugin.api.openflow.lifecycle.MasterChecker;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.OwnershipChangeListener;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.openflowplugin.impl.util.ItemScheduler;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextChainHolderImpl implements ContextChainHolder, MasterChecker {
    private static final Logger LOG = LoggerFactory.getLogger(ContextChainHolderImpl.class);

    private static final String CONTEXT_CREATED_FOR_CONNECTION = " context created for connection: {}";
    private static final long CHECK_ROLE_MASTER_TIMEOUT = 20000L;
    private static final long CHECK_ROLE_MASTER_TOLERANCE = CHECK_ROLE_MASTER_TIMEOUT / 2;
    private static final long REMOVE_DEVICE_FROM_DS_TIMEOUT = 5000L;
    private static final String ASYNC_SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.AsyncServiceCloseEntityType";

    private final Map<DeviceInfo, ContextChain> contextChainMap = Collections.synchronizedMap(new HashMap<>());
    private final EntityOwnershipListenerRegistration eosListenerRegistration;
    private final HashedWheelTimer timer;
    private final ClusterSingletonServiceProvider singletonServiceProvider;
    private final ItemScheduler<DeviceInfo, ContextChain> scheduler;
    private final ExecutorService executorService;
    private final OwnershipChangeListener ownershipChangeListener;
    private final OpenflowProviderConfig config;
    private DeviceManager deviceManager;
    private RpcManager rpcManager;
    private StatisticsManager statisticsManager;

    public ContextChainHolderImpl(final HashedWheelTimer timer,
                                  final ExecutorService executorService,
                                  final ClusterSingletonServiceProvider singletonServiceProvider,
                                  final EntityOwnershipService entityOwnershipService,
                                  final OwnershipChangeListener ownershipChangeListener,
                                  final OpenflowProviderConfig config) {
        this.timer = timer;
        this.singletonServiceProvider = singletonServiceProvider;
        this.executorService = executorService;
        this.ownershipChangeListener = ownershipChangeListener;
        this.ownershipChangeListener.setMasterChecker(this);
        this.config = config;
        this.eosListenerRegistration = Objects.requireNonNull(entityOwnershipService
                .registerListener(ASYNC_SERVICE_ENTITY_TYPE, this));

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

    @VisibleForTesting
    ContextChain createContextChain(final ConnectionContext connectionContext) {
        final DeviceInfo deviceInfo = connectionContext.getDeviceInfo();

        final DeviceContext deviceContext = deviceManager.createContext(connectionContext);
        deviceContext.registerMastershipWatcher(this);
        LOG.debug("Device" + CONTEXT_CREATED_FOR_CONNECTION, deviceInfo);

        final RpcContext rpcContext = rpcManager.createContext(deviceContext);
        rpcContext.registerMastershipWatcher(this);
        LOG.debug("RPC" + CONTEXT_CREATED_FOR_CONNECTION, deviceInfo);

        final StatisticsContext statisticsContext = statisticsManager.createContext(deviceContext);
        statisticsContext.registerMastershipWatcher(this);
        LOG.debug("Statistics" + CONTEXT_CREATED_FOR_CONNECTION, deviceInfo);

        final ContextChain contextChain = new ContextChainImpl(this, connectionContext,
                executorService, timer);
        contextChain.registerDeviceRemovedHandler(deviceManager);
        contextChain.registerDeviceRemovedHandler(rpcManager);
        contextChain.registerDeviceRemovedHandler(statisticsManager);
        contextChain.addContext(deviceContext);
        contextChain.addContext(rpcContext);
        contextChain.addContext(statisticsContext);
        contextChainMap.put(deviceInfo, contextChain);
        LOG.debug("Context chain" + CONTEXT_CREATED_FOR_CONNECTION, deviceInfo);

        deviceContext.onPublished();
        scheduler.add(deviceInfo, contextChain);
        scheduler.startIfNotRunning();
        LOG.info("Started timer for setting SLAVE role on node {} if no role will be set in {}s.",
                deviceInfo,
                CHECK_ROLE_MASTER_TIMEOUT / 1000L);

        contextChain.registerServices(singletonServiceProvider);
        return contextChain;
    }

    @Override
    public ConnectionStatus deviceConnected(final ConnectionContext connectionContext) throws Exception {
        final DeviceInfo deviceInfo = connectionContext.getDeviceInfo();
        final ContextChain contextChain = contextChainMap.get(deviceInfo);
        LOG.info("Device {} connected.", deviceInfo);

        if (Objects.nonNull(contextChain)) {
            if (contextChain.addAuxiliaryConnection(connectionContext)) {
                LOG.info("An auxiliary connection was added to device: {}", deviceInfo);
                return ConnectionStatus.MAY_CONTINUE;
            }

            LOG.warn("Device {} already connected. Closing all connection to the device.", deviceInfo);
            destroyContextChain(deviceInfo);
            return ConnectionStatus.ALREADY_CONNECTED;
        }

        LOG.debug("No context chain found for device: {}, creating new.", deviceInfo);
        final ContextChain newContextChain = createContextChain(connectionContext);
        LOG.debug("Successfully created context chain with identifier: {}", newContextChain.getIdentifier());
        return ConnectionStatus.MAY_CONTINUE;
    }

    @Override
    public void onNotAbleToStartMastership(@Nonnull final DeviceInfo deviceInfo,
                                           @Nonnull final String reason,
                                           final boolean mandatory) {
        LOG.warn("Not able to set MASTER role on device {}, reason: {}", deviceInfo, reason);

        if (!mandatory) {
            return;
        }

        Optional.ofNullable(contextChainMap.get(deviceInfo)).ifPresent(contextChain -> {
            LOG.warn("This mastering is mandatory, destroying context chain and closing connection for device {}.", deviceInfo);
            destroyContextChain(deviceInfo);
        });
    }

    @Override
    public void onMasterRoleAcquired(@Nonnull final DeviceInfo deviceInfo,
                                     @Nonnull final ContextChainMastershipState mastershipState) {
        scheduler.remove(deviceInfo);
        Optional.ofNullable(contextChainMap.get(deviceInfo)).ifPresent(contextChain -> {
            if (config.isUsingReconciliationFramework()) {
                if (mastershipState == ContextChainMastershipState.INITIAL_SUBMIT) {
                    LOG.error("Initial submit is not allowed here if using reconciliation framework.");
                } else {
                    contextChain.isMastered(mastershipState);
                    if (contextChain.isPrepared()) {
                        ownershipChangeListener.becomeMasterBeforeSubmittedDS(
                                deviceInfo,
                                reconciliationFrameworkCallback(deviceInfo, contextChain));
                    }
                }
            } else if (contextChain.isMastered(mastershipState)) {
                LOG.info("Role MASTER was granted to device {}", deviceInfo);
                ownershipChangeListener.becomeMaster(deviceInfo);
                deviceManager.sendNodeAddedNotification(deviceInfo.getNodeInstanceIdentifier());
            }
        });
    }

    @Override
    public void onSlaveRoleAcquired(final DeviceInfo deviceInfo) {
        scheduler.remove(deviceInfo);
        ownershipChangeListener.becomeSlaveOrDisconnect(deviceInfo);
        Optional.ofNullable(contextChainMap.get(deviceInfo)).ifPresent(ContextChain::makeContextChainStateSlave);
    }

    @Override
    public void onSlaveRoleNotAcquired(final DeviceInfo deviceInfo) {
        Optional.ofNullable(contextChainMap.get(deviceInfo)).ifPresent(contextChain -> destroyContextChain(deviceInfo));
    }

    @Override
    public void onDeviceDisconnected(final ConnectionContext connectionContext) {
        final DeviceInfo deviceInfo = connectionContext.getDeviceInfo();

        Optional.ofNullable(contextChainMap.get(deviceInfo)).ifPresent(contextChain -> {
            if (contextChain.auxiliaryConnectionDropped(connectionContext)) {
                LOG.info("Auxiliary connection from device {} disconnected.", deviceInfo);
            } else {
                LOG.info("Device {} disconnected.", deviceInfo);
                destroyContextChain(deviceInfo);
            }
        });
    }

    @VisibleForTesting
    boolean checkAllManagers() {
        return Objects.nonNull(deviceManager) && Objects.nonNull(rpcManager) && Objects.nonNull(statisticsManager);
    }

    @Override
    public void close() throws Exception {
        scheduler.close();
        contextChainMap.keySet().forEach(this::destroyContextChain);
        contextChainMap.clear();
        eosListenerRegistration.close();
    }

    @Override
    public void ownershipChanged(EntityOwnershipChange entityOwnershipChange) {
        if (entityOwnershipChange.hasOwner()) {
            return;
        }

        final String entityName = getEntityNameFromOwnershipChange(entityOwnershipChange);

        if (Objects.nonNull(entityName)) {
            LOG.debug("Entity {} has no owner", entityName);
            final NodeId nodeId = new NodeId(entityName);

            try {
                final KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier =
                        DeviceStateUtil.createNodeInstanceIdentifier(nodeId);

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

    private synchronized void destroyContextChain(final DeviceInfo deviceInfo) {
        scheduler.remove(deviceInfo);
        ownershipChangeListener.becomeSlaveOrDisconnect(deviceInfo);

        Optional.ofNullable(contextChainMap.remove(deviceInfo)).ifPresent(contextChain -> {
            deviceManager.sendNodeRemovedNotification(deviceInfo.getNodeInstanceIdentifier());
            contextChain.close();
        });
    }

    @Override
    public List<DeviceInfo> listOfMasteredDevices() {
        return contextChainMap
                .entrySet()
                .stream()
                .filter(deviceInfoContextChainEntry -> deviceInfoContextChainEntry
                        .getValue()
                        .isMastered(ContextChainMastershipState.CHECK))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAnyDeviceMastered() {
        return contextChainMap
                .entrySet()
                .stream()
                .findAny()
                .filter(deviceInfoContextChainEntry -> deviceInfoContextChainEntry.getValue()
                        .isMastered(ContextChainMastershipState.CHECK))
                .isPresent();
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

    private FutureCallback<ResultState> reconciliationFrameworkCallback(
            @Nonnull DeviceInfo deviceInfo,
            ContextChain contextChain) {
        return new FutureCallback<ResultState>() {
            @Override
            public void onSuccess(@Nullable ResultState result) {
                if (ResultState.DONOTHING == result) {
                    LOG.info("Device {} connection is enabled by reconciliation framework.", deviceInfo);
                    if (!contextChain.continueInitializationAfterReconciliation()) {
                        destroyContextChain(deviceInfo);
                    } else {
                        ownershipChangeListener.becomeMaster(deviceInfo);
                    }
                } else {
                    LOG.warn("Reconciliation framework failure.");
                    destroyContextChain(deviceInfo);
                }
            }

            @Override
            public void onFailure(@Nonnull Throwable t) {
                LOG.warn("Reconciliation framework failure.");
                destroyContextChain(deviceInfo);
            }
        };
    }
}
