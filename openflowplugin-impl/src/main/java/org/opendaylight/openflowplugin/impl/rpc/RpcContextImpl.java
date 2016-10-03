/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.ClusterInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.util.MdSalRegistrationUtils;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RpcContextImpl implements RpcContext {
    private static final Logger LOG = LoggerFactory.getLogger(RpcContextImpl.class);
    private final RpcProviderRegistry rpcProviderRegistry;
    private final MessageSpy messageSpy;
    private final Semaphore tracker;
    private boolean isStatisticsRpcEnabled;

    // TODO: add private Sal salBroker
    private final ConcurrentMap<Class<?>, RoutedRpcRegistration<?>> rpcRegistrations = new ConcurrentHashMap<>();
    private final KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier;
    private volatile CONTEXT_STATE state = CONTEXT_STATE.INITIALIZATION;
    private final DeviceInfo deviceInfo;
    private final DeviceContext deviceContext;
    private final ExtensionConverterProvider extensionConverterProvider;
    private final ConvertorExecutor convertorExecutor;
    private final NotificationPublishService notificationPublishService;
    private ClusterInitializationPhaseHandler clusterInitializationPhaseHandler;

    RpcContextImpl(final DeviceInfo deviceInfo,
                   final RpcProviderRegistry rpcProviderRegistry,
                   final MessageSpy messageSpy,
                   final int maxRequests,
                   final KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier,
                   final DeviceContext deviceContext,
                   final ExtensionConverterProvider extensionConverterProvider,
                   final ConvertorExecutor convertorExecutor,
                   final NotificationPublishService notificationPublishService) {
        this.messageSpy = Preconditions.checkNotNull(messageSpy);
        this.rpcProviderRegistry = Preconditions.checkNotNull(rpcProviderRegistry);
        this.nodeInstanceIdentifier = nodeInstanceIdentifier;
        this.tracker = new Semaphore(maxRequests, true);
        this.extensionConverterProvider = extensionConverterProvider;
        this.notificationPublishService = notificationPublishService;
        this.deviceInfo = deviceInfo;
        this.deviceContext = deviceContext;
        this.convertorExecutor = convertorExecutor;
    }

    /**
     * @see org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext#registerRpcServiceImplementation(java.lang.Class,
     * org.opendaylight.yangtools.yang.binding.RpcService)
     */
    @Override
    public <S extends RpcService> void registerRpcServiceImplementation(final Class<S> serviceClass,
                                                                        final S serviceInstance) {
        LOG.trace("Try to register service {} for device {}.", serviceClass, nodeInstanceIdentifier);
        if (! rpcRegistrations.containsKey(serviceClass)) {
            final RoutedRpcRegistration<S> routedRpcReg = rpcProviderRegistry.addRoutedRpcImplementation(serviceClass, serviceInstance);
            routedRpcReg.registerPath(NodeContext.class, nodeInstanceIdentifier);
            rpcRegistrations.put(serviceClass, routedRpcReg);
            LOG.debug("Registration of service {} for device {}.", serviceClass.getSimpleName(), nodeInstanceIdentifier.getKey().getId().getValue());
        }
    }

    @Override
    public <S extends RpcService> S lookupRpcService(final Class<S> serviceClass) {
        RoutedRpcRegistration<?> registration = rpcRegistrations.get(serviceClass);
        final RpcService rpcService = registration.getInstance();
        return (S) rpcService;
    }

    /**
     * Unregisters all services.
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() {
        if (CONTEXT_STATE.TERMINATION.equals(getState())){
            if (LOG.isDebugEnabled()) {
                LOG.debug("RpcContext for node {} is already in TERMINATION state.", getDeviceInfo().getLOGValue());
            }
        } else {
            try {
                stopClusterServices(true).get();
            } catch (Exception e) {
                LOG.debug("Failed to close RpcContext for node {} with exception: ", getDeviceInfo().getLOGValue(), e);
            }

            this.state = CONTEXT_STATE.TERMINATION;
        }
    }

    @Override
    public <T> RequestContext<T> createRequestContext() {
        if (!tracker.tryAcquire()) {
            LOG.trace("Device queue {} at capacity", this);
            return null;
        } else {
            LOG.trace("Acquired semaphore for {}, available permits:{} ", nodeInstanceIdentifier.getKey().getId().getValue(), tracker.availablePermits());
        }

        final Long xid = deviceInfo.reserveXidForDeviceMessage();
        if (xid == null) {
            LOG.warn("Xid cannot be reserved for new RequestContext, node:{}", nodeInstanceIdentifier.getKey().getId().getValue());
            tracker.release();
            return null;
        }

        return new AbstractRequestContext<T>(xid) {
            @Override
            public void close() {
                tracker.release();
                final long xid = getXid().getValue();
                LOG.trace("Removed request context with xid {}", xid);
                messageSpy.spyMessage(RpcContextImpl.class, MessageSpy.STATISTIC_GROUP.REQUEST_STACK_FREED);
            }
        };
    }

    @Override
    public <S extends RpcService> void unregisterRpcServiceImplementation(final Class<S> serviceClass) {
        LOG.trace("Try to unregister serviceClass {} for Node {}", serviceClass, nodeInstanceIdentifier.getKey().getId());
        final RoutedRpcRegistration<?> rpcRegistration = rpcRegistrations.remove(serviceClass);
        if (rpcRegistration != null) {
            rpcRegistration.unregisterPath(NodeContext.class, nodeInstanceIdentifier);
            rpcRegistration.close();
            LOG.debug("Un-registration serviceClass {} for Node {}", serviceClass.getSimpleName(), nodeInstanceIdentifier.getKey().getId().getValue());
        }
    }

    @VisibleForTesting
    boolean isEmptyRpcRegistrations() {
        return this.rpcRegistrations.isEmpty();
    }

    @Override
    public void setStatisticsRpcEnabled(boolean isStatisticsRpcEnabled) {
        this.isStatisticsRpcEnabled = isStatisticsRpcEnabled;
    }

    @Override
    public CONTEXT_STATE getState() {
        return this.state;
    }

    @Override
    public ServiceGroupIdentifier getServiceIdentifier() {
        return this.deviceInfo.getServiceIdentifier();
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return this.deviceInfo;
    }

    @Override
    public ListenableFuture<Void> stopClusterServices(boolean connectionInterrupted) {
        if (CONTEXT_STATE.TERMINATION.equals(getState())) {
            return Futures.immediateCancelledFuture();
        }

        return Futures.transform(Futures.immediateFuture(null), new Function<Object, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable Object input) {
                for (final Iterator<Entry<Class<?>, RoutedRpcRegistration<?>>> iterator = Iterators
                        .consumingIterator(rpcRegistrations.entrySet().iterator()); iterator.hasNext(); ) {
                    final RoutedRpcRegistration<?> rpcRegistration = iterator.next().getValue();
                    rpcRegistration.unregisterPath(NodeContext.class, nodeInstanceIdentifier);
                    rpcRegistration.close();

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Closing RPC Registration of service {} for device {}.", rpcRegistration.getServiceType().getSimpleName(),
                                nodeInstanceIdentifier.getKey().getId().getValue());
                    }
                }

                return null;
            }
        });
    }

    @Override
    public void setLifecycleInitializationPhaseHandler(final ClusterInitializationPhaseHandler handler) {
        this.clusterInitializationPhaseHandler = handler;
    }

    @Override
    public boolean onContextInstantiateService(final ConnectionContext connectionContext) {
        if (connectionContext.getConnectionState().equals(ConnectionContext.CONNECTION_STATE.RIP)) {
            LOG.warn("Connection on device {} was interrupted, will stop starting master services.", deviceInfo.getLOGValue());
            return false;
        }

        MdSalRegistrationUtils.registerServices(this, deviceContext, extensionConverterProvider, convertorExecutor);

        if (isStatisticsRpcEnabled) {
            MdSalRegistrationUtils.registerStatCompatibilityServices(
                    this,
                    deviceContext,
                    notificationPublishService,
                    convertorExecutor);
        }

        return this.clusterInitializationPhaseHandler.onContextInstantiateService(connectionContext);
    }
}
