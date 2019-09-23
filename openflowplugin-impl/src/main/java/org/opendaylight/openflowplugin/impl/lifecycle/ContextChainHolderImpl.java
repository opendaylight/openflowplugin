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
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipChange;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListenerRegistration;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipService;
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
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.core.general.entity.rev150930.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextChainHolderImpl implements ContextChainHolder, MasterChecker {
    private static final Logger LOG = LoggerFactory.getLogger(ContextChainHolderImpl.class);
    private static final Logger OF_EVENT_LOG = LoggerFactory.getLogger("OfEventLog");

    private static final String CONTEXT_CREATED_FOR_CONNECTION = " context created for connection: {}";
    private static final long REMOVE_DEVICE_FROM_DS_TIMEOUT = 5000L;
    private static final String ASYNC_SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.AsyncServiceCloseEntityType";
    private static final String SEPARATOR = ":";
    private final Map<DeviceInfo, ContextChain> contextChainMap = new ConcurrentHashMap<>();
    private final Map<DeviceInfo, ? super ConnectionContext> connectingDevices = new ConcurrentHashMap<>();
    private final EntityOwnershipListenerRegistration eosListenerRegistration;
    private final ClusterSingletonServiceProvider singletonServiceProvider;
    private final ExecutorService executorService;
    private final OwnershipChangeListener ownershipChangeListener;
    private DeviceManager deviceManager;
    private RpcManager rpcManager;
    private StatisticsManager statisticsManager;
    private RoleManager roleManager;

    public ContextChainHolderImpl(final ExecutorService executorService,
                                  final ClusterSingletonServiceProvider singletonServiceProvider,
                                  final EntityOwnershipService entityOwnershipService,
                                  final OwnershipChangeListener ownershipChangeListener) {
        this.singletonServiceProvider = singletonServiceProvider;
        this.executorService = executorService;
        this.ownershipChangeListener = ownershipChangeListener;
        this.ownershipChangeListener.setMasterChecker(this);
        this.eosListenerRegistration = Objects
                .requireNonNull(entityOwnershipService.registerListener(ASYNC_SERVICE_ENTITY_TYPE, this));
    }

    @Override
    public <T extends OFPManager> void addManager(final T manager) {
        if (deviceManager == null && manager instanceof DeviceManager) {
            LOG.trace("Context chain holder: Device manager OK.");
            deviceManager = (DeviceManager) manager;
        } else if (rpcManager == null && manager instanceof RpcManager) {
            LOG.trace("Context chain holder: RPC manager OK.");
            rpcManager = (RpcManager) manager;
        } else if (statisticsManager == null && manager instanceof StatisticsManager) {
            LOG.trace("Context chain holder: Statistics manager OK.");
            statisticsManager = (StatisticsManager) manager;
        } else if (roleManager == null && manager instanceof RoleManager) {
            LOG.trace("Context chain holder: Role manager OK.");
            roleManager = (RoleManager) manager;
        }
    }

    @VisibleForTesting
    void createContextChain(final ConnectionContext connectionContext) {
        final DeviceInfo deviceInfo = connectionContext.getDeviceInfo();

        final DeviceContext deviceContext = deviceManager.createContext(connectionContext);
        deviceContext.registerMastershipWatcher(this);
        LOG.debug("Device" + CONTEXT_CREATED_FOR_CONNECTION, deviceInfo);

        final RpcContext rpcContext = rpcManager.createContext(deviceContext);
        rpcContext.registerMastershipWatcher(this);
        LOG.debug("RPC" + CONTEXT_CREATED_FOR_CONNECTION, deviceInfo);

        final StatisticsContext statisticsContext = statisticsManager
                .createContext(deviceContext, ownershipChangeListener.isReconciliationFrameworkRegistered());
        statisticsContext.registerMastershipWatcher(this);
        LOG.debug("Statistics" + CONTEXT_CREATED_FOR_CONNECTION, deviceInfo);

        final RoleContext roleContext = roleManager.createContext(deviceContext);
        roleContext.registerMastershipWatcher(this);
        LOG.debug("Role" + CONTEXT_CREATED_FOR_CONNECTION, deviceInfo);

        final ContextChain contextChain = new ContextChainImpl(this, connectionContext, executorService);
        contextChain.registerDeviceRemovedHandler(deviceManager);
        contextChain.registerDeviceRemovedHandler(rpcManager);
        contextChain.registerDeviceRemovedHandler(statisticsManager);
        contextChain.registerDeviceRemovedHandler(roleManager);
        contextChain.registerDeviceRemovedHandler(this);
        contextChain.addContext(deviceContext);
        contextChain.addContext(rpcContext);
        contextChain.addContext(statisticsContext);
        contextChain.addContext(roleContext);
        contextChainMap.put(deviceInfo, contextChain);
        connectingDevices.remove(deviceInfo);
        LOG.debug("Context chain" + CONTEXT_CREATED_FOR_CONNECTION, deviceInfo);

        deviceContext.onPublished();
        contextChain.registerServices(singletonServiceProvider);
    }

    @Override
    public ConnectionStatus deviceConnected(final ConnectionContext connectionContext) {
        final DeviceInfo deviceInfo = connectionContext.getDeviceInfo();
        final ContextChain contextChain = contextChainMap.get(deviceInfo);
        final FeaturesReply featuresReply = connectionContext.getFeatures();
        final Uint8 auxiliaryId = featuresReply != null ? featuresReply.getAuxiliaryId() : null;

        if (auxiliaryId != null && auxiliaryId.toJava() != 0) {
            if (contextChain == null) {
                LOG.warn("An auxiliary connection for device {}, but no primary connection. Refusing connection.",
                         deviceInfo);
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
            final boolean contextExists = contextChain != null;
            final boolean isClosing = contextExists && contextChain.isClosing();

            if (!isClosing && connectingDevices.putIfAbsent(deviceInfo, connectionContext) != null) {
                LOG.warn("Device {} is already trying to connect, wait until succeeded or disconnected.", deviceInfo);
                return ConnectionStatus.ALREADY_CONNECTED;
            }

            if (contextExists) {
                if (isClosing) {
                    LOG.warn("Device {} is already in termination state, closing all incoming connections.",
                             deviceInfo);
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
    public void onNotAbleToStartMastership(@Nonnull final DeviceInfo deviceInfo, @Nonnull final String reason,
                                           final boolean mandatory) {
        LOG.warn("Not able to set MASTER role on device {}, reason: {}", deviceInfo, reason);

        if (!mandatory) {
            return;
        }

        Optional.ofNullable(contextChainMap.get(deviceInfo)).ifPresent(contextChain -> {
            LOG.warn("This mastering is mandatory, destroying context chain and closing connection for device {}.",
                     deviceInfo);
            destroyContextChain(deviceInfo);
        });
    }

    @Override
    public void onMasterRoleAcquired(@Nonnull final DeviceInfo deviceInfo,
                                     @Nonnull final ContextChainMastershipState mastershipState) {
        Optional.ofNullable(contextChainMap.get(deviceInfo)).ifPresent(contextChain -> {
            if (ownershipChangeListener.isReconciliationFrameworkRegistered()
                    && !ContextChainMastershipState.INITIAL_SUBMIT.equals(mastershipState)) {
                if (contextChain.isMastered(mastershipState, true)) {
                    Futures.addCallback(ownershipChangeListener.becomeMasterBeforeSubmittedDS(deviceInfo),
                                        reconciliationFrameworkCallback(deviceInfo, contextChain, mastershipState),
                                        MoreExecutors.directExecutor());
                }
            } else if (contextChain.isMastered(mastershipState, false)) {
                LOG.info("Role MASTER was granted to device {}", deviceInfo);
                ownershipChangeListener.becomeMaster(deviceInfo);
                deviceManager.sendNodeAddedNotification(deviceInfo.getNodeInstanceIdentifier());
            }
        });
    }

    @Override
    public void onSlaveRoleAcquired(final DeviceInfo deviceInfo) {
        ownershipChangeListener.becomeSlaveOrDisconnect(deviceInfo);
        LOG.info("Role SLAVE was granted to device {}", deviceInfo);
        Optional.ofNullable(contextChainMap.get(deviceInfo)).ifPresent(ContextChain::makeContextChainStateSlave);
    }

    @Override
    public void onSlaveRoleNotAcquired(final DeviceInfo deviceInfo, final String reason) {
        LOG.warn("Not able to set SLAVE role on device {}, reason: {}", deviceInfo, reason);
        Optional.ofNullable(contextChainMap.get(deviceInfo)).ifPresent(contextChain -> destroyContextChain(deviceInfo));
    }

    @Override
    public void onDeviceDisconnected(final ConnectionContext connectionContext) {
        final DeviceInfo deviceInfo = connectionContext.getDeviceInfo();

        Optional.ofNullable(connectionContext.getDeviceInfo()).map(contextChainMap::get).ifPresent(contextChain -> {
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
        return deviceManager != null && rpcManager != null && statisticsManager != null && roleManager != null;
    }

    @Override
    public ContextChain getContextChain(final DeviceInfo deviceInfo) {
        return contextChainMap.get(deviceInfo);
    }

    @Override
    public void close() {
        Map<DeviceInfo, ContextChain> copyOfChains = new HashMap<>(contextChainMap);
        copyOfChains.keySet().forEach(this::destroyContextChain);
        copyOfChains.clear();
        contextChainMap.clear();
        eosListenerRegistration.close();
        OF_EVENT_LOG.debug("EOS registration closed for all devices");
    }

    @Override
    @SuppressFBWarnings("BC_UNCONFIRMED_CAST_OF_RETURN_VALUE")
    public void ownershipChanged(final EntityOwnershipChange entityOwnershipChange) {
        if (entityOwnershipChange.getState().hasOwner()) {
            return;
        }

        // Findbugs flags a false violation for "Unchecked/unconfirmed cast" from GenericEntity to Entity hence the
        // suppression above. The suppression is temporary until EntityOwnershipChange is modified to eliminate the
        // violation.
        final String entityName = entityOwnershipChange
                .getEntity()
                .getIdentifier()
                .firstKeyOf(Entity.class)
                .getName();

        if (Objects.nonNull(entityName) && entityName.contains("openflow:")) {
            LOG.debug("Entity {} has no owner", entityName);
            final String dpnId = getDpnIdFromNodeName(entityName);
            try {
                //TODO:Remove notifications
                final KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier =
                        DeviceStateUtil.createNodeInstanceIdentifier(new NodeId(entityName));
                deviceManager.sendNodeRemovedNotification(nodeInstanceIdentifier);
                LOG.info("Try to remove device {} from operational DS", entityName);
                ListenableFuture<?> future = deviceManager.removeDeviceFromOperationalDS(nodeInstanceIdentifier);
                Futures.addCallback(future, new FutureCallback<Object>() {
                    @Override
                    public void onSuccess(final Object result) {
                        LOG.debug("Node removed from Oper DS, Node: {}", dpnId);
                        OF_EVENT_LOG.debug("Node removed from Oper DS, Node: {}", dpnId);
                    }

                    @Override
                    public void onFailure(final Throwable throwable) {
                        LOG.error("Could not remove device {} from operational DS", dpnId, throwable);
                    }
                }, MoreExecutors.directExecutor());
                future.get(REMOVE_DEVICE_FROM_DS_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (TimeoutException | ExecutionException | NullPointerException | InterruptedException e) {
                LOG.warn("Not able to remove device {} from operational DS. ", entityName, e);
            }
            OF_EVENT_LOG.debug("Node removed, Node: {}", dpnId);
            LOG.info("Removing device from operational DS {} was successful", dpnId);
        }
    }

    private void destroyContextChain(final DeviceInfo deviceInfo) {
        OF_EVENT_LOG.debug("Destroying context chain for device {}", deviceInfo.getDatapathId());
        ownershipChangeListener.becomeSlaveOrDisconnect(deviceInfo);
        Optional.ofNullable(contextChainMap.get(deviceInfo)).ifPresent(contextChain -> {
            deviceManager.sendNodeRemovedNotification(deviceInfo.getNodeInstanceIdentifier());
            contextChain.close();
            connectingDevices.remove(deviceInfo);
        });
    }

    @Override
    public List<DeviceInfo> listOfMasteredDevices() {
        return contextChainMap.entrySet().stream()
                .filter(deviceInfoContextChainEntry -> deviceInfoContextChainEntry.getValue()
                        .isMastered(ContextChainMastershipState.CHECK, false)).map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAnyDeviceMastered() {
        return contextChainMap.entrySet().stream().findAny()
                .filter(deviceInfoContextChainEntry -> deviceInfoContextChainEntry.getValue()
                        .isMastered(ContextChainMastershipState.CHECK, false)).isPresent();
    }

    @Override
    public void onDeviceRemoved(final DeviceInfo deviceInfo) {
        contextChainMap.remove(deviceInfo);
        LOG.debug("Context chain removed for node {}", deviceInfo);
    }

    private FutureCallback<ResultState> reconciliationFrameworkCallback(@Nonnull final DeviceInfo deviceInfo,
            final ContextChain contextChain, final ContextChainMastershipState mastershipState) {
        return new FutureCallback<>() {
            @Override
            public void onSuccess(final ResultState result) {
                if (ResultState.DONOTHING == result) {
                    OF_EVENT_LOG.debug("Device {} connection is enabled by reconciliation framework", deviceInfo);
                    LOG.info("Device {} connection is enabled by reconciliation framework.", deviceInfo);
                    if (mastershipState == ContextChainMastershipState.MASTER_ON_DEVICE) {
                        contextChain.initializeDevice();
                    }
                    contextChain.continueInitializationAfterReconciliation();
                } else {
                    OF_EVENT_LOG.debug("Reconciliation framework failure for device {}", deviceInfo);
                    LOG.warn("Reconciliation framework failure for device {}", deviceInfo);
                    destroyContextChain(deviceInfo);
                }
            }

            @Override
            public void onFailure(final Throwable throwable) {
                OF_EVENT_LOG.debug("Reconciliation framework failure for device {} with error {}", deviceInfo,
                        throwable.getMessage());
                LOG.warn("Reconciliation framework failure.");
                destroyContextChain(deviceInfo);
            }
        };
    }

    private String getDpnIdFromNodeName(final String nodeName) {
        return nodeName.substring(nodeName.lastIndexOf(SEPARATOR) + 1);
    }
}
