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
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipChange;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListenerRegistration;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipService;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipState;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
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
    private static final String SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.ServiceEntityType";
    private static final String SEPARATOR = ":";
    private final Map<DeviceInfo, ContextChain> contextChainMap = new ConcurrentHashMap<>();
    private final Map<DeviceInfo, ? super ConnectionContext> connectingDevices = new ConcurrentHashMap<>();
    private final EntityOwnershipListenerRegistration eosListenerRegistration;
    private final ClusterSingletonServiceProvider singletonServiceProvider;
    private final Executor executor;
    private final OwnershipChangeListener ownershipChangeListener;
    private final ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(true)
            .setNameFormat("node-cleaner-%d").setUncaughtExceptionHandler((thread, throwable) -> {
                LOG.warn("Uncaught exception while removing node data from operational datastore.", throwable);
            }).build();
    private final ScheduledExecutorService nodeCleanerExecutor = Executors.newScheduledThreadPool(
            Runtime.getRuntime().availableProcessors() , threadFactory);
    private final EntityOwnershipService entityOwnershipService;
    private final OpenflowProviderConfig config;
    private DeviceManager deviceManager;
    private RpcManager rpcManager;
    private StatisticsManager statisticsManager;
    private RoleManager roleManager;

    public ContextChainHolderImpl(final Executor executor,
                                  final ClusterSingletonServiceProvider singletonServiceProvider,
                                  final EntityOwnershipService entityOwnershipService,
                                  final OwnershipChangeListener ownershipChangeListener,
                                  final OpenflowProviderConfig config) {
        this.singletonServiceProvider = singletonServiceProvider;
        this.executor = executor;
        this.ownershipChangeListener = ownershipChangeListener;
        this.ownershipChangeListener.setMasterChecker(this);
        this.entityOwnershipService = entityOwnershipService;
        this.config = config;
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

        final ContextChain contextChain = new ContextChainImpl(this, connectionContext, executor);
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
    public void onNotAbleToStartMastership(final DeviceInfo deviceInfo, final String reason, final boolean mandatory) {
        LOG.error("Not able to set MASTER role on device {}, reason: {}", deviceInfo, reason);

        if (!mandatory) {
            return;
        }
        if (contextChainMap.containsKey(deviceInfo)) {
            LOG.warn("This mastering is mandatory, destroying context chain and closing connection for device {}.",
                     deviceInfo);
            destroyContextChain(deviceInfo);
        }
    }

    @Override
    public void onMasterRoleAcquired(final DeviceInfo deviceInfo, final ContextChainMastershipState mastershipState) {
        final ContextChain contextChain = contextChainMap.get(deviceInfo);
        if (contextChain != null) {
            if (!ContextChainMastershipState.INITIAL_SUBMIT.equals(mastershipState)) {
                if (contextChain.isMastered(mastershipState, true)) {
                    Futures.addCallback(ownershipChangeListener.becomeMasterBeforeSubmittedDS(deviceInfo),
                                        reconciliationFrameworkCallback(deviceInfo, contextChain, mastershipState),
                                        MoreExecutors.directExecutor());
                }
            } else if (contextChain.isMastered(mastershipState, false)) {
                LOG.info("Role MASTER was granted to device {}", deviceInfo);
                OF_EVENT_LOG.debug("Master Elected, Node: {}", deviceInfo.getDatapathId());
                deviceManager.sendNodeAddedNotification(deviceInfo.getNodeInstanceIdentifier());
            }
        }
    }

    @Override
    public void onSlaveRoleAcquired(final DeviceInfo deviceInfo) {
        ownershipChangeListener.becomeSlaveOrDisconnect(deviceInfo);
        LOG.info("Role SLAVE was granted to device {}", deviceInfo);
        final ContextChain contextChain = contextChainMap.get(deviceInfo);
        if (contextChain != null) {
            contextChain.makeContextChainStateSlave();
        }
    }

    @Override
    public void onSlaveRoleNotAcquired(final DeviceInfo deviceInfo, final String reason) {
        LOG.error("Not able to set SLAVE role on device {}, reason: {}", deviceInfo, reason);
        if (contextChainMap.containsKey(deviceInfo)) {
            destroyContextChain(deviceInfo);
        }
    }

    @Override
    public void onDeviceDisconnected(final ConnectionContext connectionContext) {
        final DeviceInfo deviceInfo = connectionContext.getDeviceInfo();
        final ContextChain contextChain = contextChainMap.get(deviceInfo);
        if (contextChain != null) {
            if (contextChain.auxiliaryConnectionDropped(connectionContext)) {
                LOG.info("Auxiliary connection from device {} disconnected.", deviceInfo);
            } else {
                LOG.info("Device {} disconnected.", deviceInfo);
                destroyContextChain(deviceInfo);
            }
        }
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
        OF_EVENT_LOG.debug("EOS registration closed for all devices");
        eosListenerRegistration.close();
        OF_EVENT_LOG.debug("EOS registration closed for all devices");
        nodeCleanerExecutor.shutdownNow();
    }

    @Override
    @SuppressFBWarnings("BC_UNCONFIRMED_CAST_OF_RETURN_VALUE")
    public void ownershipChanged(final EntityOwnershipChange entityOwnershipChange) {
        LOG.info("Entity ownership change received for node : {}", entityOwnershipChange);
        if (entityOwnershipChange.inJeopardy()) {
            LOG.warn("Controller is in Jeopardy, ignore ownership change notification. {}", entityOwnershipChange);
            return;
        }
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

        if (entityName != null && entityName.startsWith("openflow:")) {
            if (nodeCleanerExecutor.isShutdown()) {
                LOG.warn("Node cleaner executor thread-pool is down.");
                return;
            }
            LOG.debug("Device {} will be removed from datastore in {} msec, if it's not transient notification.",
                    entityName, config.getDeviceDatastoreRemovalDelay().getValue());
            final String dpnId = getDpnIdFromNodeName(entityName);
            nodeCleanerExecutor.schedule(() -> {
                try {
                    EntityOwnershipState ownershipState = getCurrentOwnershipStatus(entityName);
                    if (ownershipState == null || EntityOwnershipState.NO_OWNER.equals(ownershipState)) {
                        LOG.debug("Entity {} has no owner", entityName);
                        final KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier =
                                DeviceStateUtil.createNodeInstanceIdentifier(new NodeId(entityName));
                        deviceManager.sendNodeRemovedNotification(nodeInstanceIdentifier);
                        LOG.info("Try to remove device {} from operational DS", entityName);
                        ListenableFuture<?> future =
                                deviceManager.removeDeviceFromOperationalDS(nodeInstanceIdentifier);
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
                    } else {
                        LOG.warn("Seems like device is still owned by other controller instance. Skip deleting {} "
                                + "node from operational datastore.", entityName);
                    }
                } catch (TimeoutException | ExecutionException | NullPointerException | InterruptedException e) {
                    LOG.warn("Not able to remove device {} from operational DS. ", entityName, e);
                }
            }, config.getDeviceDatastoreRemovalDelay().getValue().toJava(), TimeUnit.MILLISECONDS);
        }
    }

    private void destroyContextChain(final DeviceInfo deviceInfo) {
        OF_EVENT_LOG.debug("Destroying context chain for device {}", deviceInfo.getDatapathId());
        ownershipChangeListener.becomeSlaveOrDisconnect(deviceInfo);
        final ContextChain contextChain = contextChainMap.get(deviceInfo);
        if (contextChain != null) {
            deviceManager.sendNodeRemovedNotification(deviceInfo.getNodeInstanceIdentifier());
            contextChain.close();
            connectingDevices.remove(deviceInfo);
        }
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

    private FutureCallback<ResultState> reconciliationFrameworkCallback(@NonNull final DeviceInfo deviceInfo,
            final ContextChain contextChain, final ContextChainMastershipState mastershipState) {
        return new FutureCallback<>() {
            @Override
            public void onSuccess(final ResultState result) {
                if (ResultState.DONOTHING == result) {
                    OF_EVENT_LOG.debug("Device {} connection is enabled by reconciliation framework", deviceInfo);
                    LOG.info("Device {} connection is enabled by reconciliation framework.", deviceInfo);
                    if (mastershipState == ContextChainMastershipState.MASTER_ON_DEVICE) {
                        ownershipChangeListener.becomeMaster(deviceInfo);
                        contextChain.initializeDevice();
                    }
                    contextChain.continueInitializationAfterReconciliation();
                } else {
                    OF_EVENT_LOG.debug("Reconciliation framework failure for device {}", deviceInfo);
                    LOG.warn("Reconciliation framework failure for device {} with resultState {}", deviceInfo, result);
                    destroyContextChain(deviceInfo);
                }
            }

            @Override
            public void onFailure(final Throwable throwable) {
                OF_EVENT_LOG.debug("Reconciliation framework failure for device {} with error {}", deviceInfo,
                        throwable.getMessage());
                LOG.warn("Reconciliation framework failure for device {}", deviceInfo, throwable);
                destroyContextChain(deviceInfo);
            }
        };
    }

    private static String getDpnIdFromNodeName(final String nodeName) {
        return nodeName.substring(nodeName.lastIndexOf(SEPARATOR) + 1);
    }

    private @Nullable EntityOwnershipState getCurrentOwnershipStatus(final String nodeId) {
        org.opendaylight.mdsal.eos.binding.api.Entity entity = createNodeEntity(nodeId);
        Optional<EntityOwnershipState> ownershipStatus
                = entityOwnershipService.getOwnershipState(entity);

        if (ownershipStatus.isPresent()) {
            LOG.debug("Current ownership status for node {} is {}", nodeId, ownershipStatus.get());
            return ownershipStatus.get();
        }

        LOG.trace("Ownership status is not available for node {}", nodeId);
        return null;
    }

    private static org.opendaylight.mdsal.eos.binding.api.Entity createNodeEntity(final String nodeId) {
        return new org.opendaylight.mdsal.eos.binding.api.Entity(ASYNC_SERVICE_ENTITY_TYPE, nodeId);
    }
}
