/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import com.google.common.annotations.VisibleForTesting;
import io.netty.util.HashedWheelTimer;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;
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
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextChainHolderImpl implements ContextChainHolder {
    private static final Logger LOG = LoggerFactory.getLogger(ContextChainHolderImpl.class);

    private static final String CONTEXT_CREATED_FOR_CONNECTION = " context created for connection: {}";
    private static final long REMOVE_DEVICE_FROM_DS_TIMEOUT = 5000L;
    private static final String ASYNC_SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.AsyncServiceCloseEntityType";

    private final Map<DeviceInfo, ContextChain> contextChainMap = new ConcurrentHashMap<>();
    private final Map<DeviceInfo, ? super ConnectionContext> connectingDevices = new ConcurrentHashMap<>();
    private DeviceManager deviceManager;
    private RpcManager rpcManager;
    private StatisticsManager statisticsManager;
    private EntityOwnershipListenerRegistration eosListenerRegistration;
    private ClusterSingletonServiceProvider singletonServiceProvider;
    private final ExecutorService executorService;
    private final HashedWheelTimer timer;

    public ContextChainHolderImpl(final HashedWheelTimer timer,
                                  final ExecutorService executorService,
                                  final ClusterSingletonServiceProvider singletonServiceProvider,
                                  final EntityOwnershipService entityOwnershipService) {
        this.timer = timer;
        this.singletonServiceProvider = singletonServiceProvider;
        this.executorService = executorService;
        this.eosListenerRegistration = Objects.requireNonNull(entityOwnershipService
                .registerListener(ASYNC_SERVICE_ENTITY_TYPE, this));
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
        deviceContext.registerMastershipChangeListener(this);
        LOG.debug("Device" + CONTEXT_CREATED_FOR_CONNECTION, deviceInfo);

        final RpcContext rpcContext = rpcManager.createContext(deviceContext);
        rpcContext.registerMastershipChangeListener(this);
        LOG.debug("RPC" + CONTEXT_CREATED_FOR_CONNECTION, deviceInfo);

        final StatisticsContext statisticsContext = statisticsManager.createContext(deviceContext);
        statisticsContext.registerMastershipChangeListener(this);
        LOG.debug("Statistics" + CONTEXT_CREATED_FOR_CONNECTION, deviceInfo);

        final ContextChain contextChain = new ContextChainImpl(this, connectionContext,
                executorService);
        contextChain.registerDeviceRemovedHandler(deviceManager);
        contextChain.registerDeviceRemovedHandler(rpcManager);
        contextChain.registerDeviceRemovedHandler(statisticsManager);
        contextChain.registerDeviceRemovedHandler(this);
        contextChain.addContext(deviceContext);
        contextChain.addContext(rpcContext);
        contextChain.addContext(statisticsContext);
        contextChainMap.put(deviceInfo, contextChain);
        connectingDevices.remove(deviceInfo);
        LOG.debug("Context chain" + CONTEXT_CREATED_FOR_CONNECTION, deviceInfo);
        deviceContext.onPublished();
        contextChain.registerServices(singletonServiceProvider);
        return contextChain;
    }

    @Override
    public ConnectionStatus deviceConnected(final ConnectionContext connectionContext) throws Exception {

        final DeviceInfo deviceInfo = connectionContext.getDeviceInfo();
        final ContextChain contextChain = contextChainMap.get(deviceInfo);
        final FeaturesReply featuresReply = connectionContext.getFeatures();
        final Short auxiliaryId = featuresReply != null ? featuresReply.getAuxiliaryId() : null;

        if (auxiliaryId != null && auxiliaryId != 0) {
            if (contextChain == null) {
                LOG.warn("An auxiliary connection for device {}, but no primary connection. Refusing connection.", deviceInfo);
                return ConnectionStatus.REFUSING_AUXILIARY_CONNECTION;
            } else {
                if (contextChain.addAuxiliaryConnection(connectionContext)) {
                    LOG.info("An auxiliary connection was added to device: {}", deviceInfo);
                    return ConnectionStatus.MAY_CONTINUE;
                } else {
                    LOG.warn("Not able to add auxiliary connection to the device {}", deviceInfo);
                    return ConnectionStatus.REFUSING_AUXILIARY_CONNECTION;
                }
            }
        } else {
            LOG.info("Device {} connected.", deviceInfo);
            if (connectingDevices.putIfAbsent(deviceInfo, connectionContext) != null) {
                LOG.warn("Device {} is already trying to connect, wait until succeeded or disconnected.", deviceInfo);
                return ConnectionStatus.ALREADY_CONNECTED;
            }
            if (contextChain != null) {
                if (contextChain.isClosing()) {
                    LOG.warn("Device {} is already in termination state, closing all incoming connections.", deviceInfo);
                    return ConnectionStatus.CLOSING;
                }
                LOG.warn("Device {} already connected. Closing previous connection", deviceInfo);
                destroyContextChain(deviceInfo);
                LOG.info("Old connection dropped, creating new context chain for device {}", deviceInfo);
                createContextChain(connectionContext);
            } else {
                LOG.info("No context chain found for device: {}, creating new.", deviceInfo);
                createContextChain(connectionContext);
            }
            return ConnectionStatus.MAY_CONTINUE;
        }

    }

    @Override
    public void onNotAbleToStartMastership(final DeviceInfo deviceInfo, @Nonnull final String reason, final boolean mandatory) {
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
    public void onMasterRoleAcquired(final DeviceInfo deviceInfo, @Nonnull final ContextChainMastershipState mastershipState) {
        Optional.ofNullable(contextChainMap.get(deviceInfo)).ifPresent(contextChain -> {
            if (contextChain.isMastered(mastershipState)) {
                LOG.info("Role MASTER was granted to device {}", deviceInfo);
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

        Optional.ofNullable(deviceInfo).map(contextChainMap::get).ifPresent(contextChain -> {
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
        contextChainMap.keySet().forEach(this::destroyContextChain);
        contextChainMap.clear();


        if (Objects.nonNull(eosListenerRegistration)) {
            eosListenerRegistration.close();
            eosListenerRegistration = null;
        }
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
        Optional.ofNullable(contextChainMap.get(deviceInfo)).ifPresent(contextChain -> {
            deviceManager.sendNodeRemovedNotification(deviceInfo.getNodeInstanceIdentifier());
            contextChain.close();
        });
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

    @Override
    public void onDeviceRemoved(final DeviceInfo deviceInfo) {
        contextChainMap.remove(deviceInfo);
        LOG.debug("Context chain removed for node {}", deviceInfo);
    }
}
