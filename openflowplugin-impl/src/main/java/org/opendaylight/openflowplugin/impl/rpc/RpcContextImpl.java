/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipWatcher;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.util.MdSalRegistrationUtils;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RpcContextImpl implements RpcContext {
    private static final Logger LOG = LoggerFactory.getLogger(RpcContextImpl.class);
    private final RpcProviderService rpcProviderRegistry;
    private final MessageSpy messageSpy;
    private final Semaphore tracker;
    private final boolean isStatisticsRpcEnabled;

    // TODO: add private Sal salBroker
    private final ConcurrentMap<Class, Registration> rpcRegistrationsMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class, Object> rpcInstancesMap = new ConcurrentHashMap<>();
    private final KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier;
    private final DeviceInfo deviceInfo;
    private final DeviceContext deviceContext;
    private final ExtensionConverterProvider extensionConverterProvider;
    private final ConvertorExecutor convertorExecutor;
    private final NotificationPublishService notificationPublishService;

    private ContextChainMastershipWatcher contextChainMastershipWatcher = null;

    RpcContextImpl(@NonNull final RpcProviderService rpcProviderRegistry,
                   final int maxRequests,
                   @NonNull final DeviceContext deviceContext,
                   @NonNull final ExtensionConverterProvider extensionConverterProvider,
                   @NonNull final ConvertorExecutor convertorExecutor,
                   @NonNull final NotificationPublishService notificationPublishService,
                   final boolean statisticsRpcEnabled) {
        this.deviceContext = deviceContext;
        deviceInfo = deviceContext.getDeviceInfo();
        nodeInstanceIdentifier = deviceContext.getDeviceInfo().getNodeInstanceIdentifier();
        messageSpy = deviceContext.getMessageSpy();
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.extensionConverterProvider = extensionConverterProvider;
        this.notificationPublishService = notificationPublishService;
        this.convertorExecutor = convertorExecutor;
        isStatisticsRpcEnabled = statisticsRpcEnabled;
        tracker = new Semaphore(maxRequests, true);
    }

    @Override
    public void close() {
        unregisterRPCs();
    }

    private void unregisterRPCs() {
        for (final var iterator =
                Iterators.consumingIterator(rpcRegistrationsMap.entrySet().iterator()); iterator.hasNext(); ) {
            final var nextIteration = iterator.next();
            final var rpcRegistration = nextIteration.getValue();
            rpcRegistration.close();
            rpcInstancesMap.remove(nextIteration.getKey());

            if (LOG.isDebugEnabled()) {
                LOG.debug("Closing RPC Registration of service {} for device {}.",
                    nextIteration.getKey().getSimpleName(),
                    nodeInstanceIdentifier.getKey().getId().getValue());
            }
        }
    }

    @Override
    public <T> RequestContext<T> createRequestContext() {
        if (!tracker.tryAcquire()) {
            LOG.trace("Device queue {} at capacity", this);
            return null;
        } else {
            LOG.trace("Acquired semaphore for {}, available permits:{} ",
                    nodeInstanceIdentifier.getKey().getId().getValue(), tracker.availablePermits());
        }

        final Uint32 xid = deviceInfo.reserveXidForDeviceMessage();
        if (xid == null) {
            LOG.warn("Xid cannot be reserved for new RequestContext, node:{}",
                    nodeInstanceIdentifier.getKey().getId().getValue());
            tracker.release();
            return null;
        }

        return new AbstractRequestContext<>(xid) {
            @Override
            public void close() {
                tracker.release();
                LOG.trace("Removed request context with xid {}", getXid().getValue());
                messageSpy.spyMessage(RpcContextImpl.class, MessageSpy.StatisticsGroup.REQUEST_STACK_FREED);
            }
        };
    }

    @Override
    public void unregisterRpcServiceImplementations(final Class serviceClass) {
        LOG.trace("Try to unregister serviceClass {} for Node {}",
            serviceClass, nodeInstanceIdentifier.getKey().getId());
        final var rpcRegistration = rpcRegistrationsMap.remove(serviceClass);
        rpcInstancesMap.remove(serviceClass);
        if (rpcRegistration != null) {
            rpcRegistration.close();
            LOG.debug("Un-registration serviceClass {} for Node {}", serviceClass.getSimpleName(),
                    nodeInstanceIdentifier.getKey().getId().getValue());
        }
    }

    @Override
    public void registerRpcServiceImplementations(final Object rpcsInstance,
        final ClassToInstanceMap<Rpc<?, ?>> rpcMap) {
        if (!rpcRegistrationsMap.containsKey(rpcsInstance.getClass())) {
            final var routedRpcReg = rpcProviderRegistry.registerRpcImplementations(rpcMap,
                ImmutableSet.of(nodeInstanceIdentifier));
            rpcRegistrationsMap.put(rpcsInstance.getClass(), routedRpcReg);
            rpcInstancesMap.put(rpcsInstance.getClass(), rpcsInstance);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Registration of service {} for device {}.",
                    rpcsInstance.getClass().getName(),
                    nodeInstanceIdentifier.getKey().getId().getValue());
            }
        }
    }

    @Override
    public <S> S lookupRpcServices(Class<S> serviceClass) {
        for (Map.Entry<Class, ?> c: rpcInstancesMap.entrySet()) {
            if (serviceClass.isAssignableFrom(c.getKey())) {
                return (S) c.getValue();
            }
        }
        return null;
    }

    @VisibleForTesting
    boolean isEmptyRpcRegistrations() {
        return rpcRegistrationsMap.isEmpty();
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    @Override
    public void registerMastershipWatcher(@NonNull final ContextChainMastershipWatcher newWatcher) {
        contextChainMastershipWatcher = newWatcher;
    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
        return Futures.transform(Futures.immediateFuture(null), input -> {
            unregisterRPCs();
            return null;
        }, MoreExecutors.directExecutor());
    }

    @Override
    public void instantiateServiceInstance() {
        MdSalRegistrationUtils.registerServices(this, deviceContext, extensionConverterProvider, convertorExecutor);

        if (isStatisticsRpcEnabled && !deviceContext.canUseSingleLayerSerialization()) {
            MdSalRegistrationUtils.registerStatCompatibilityServices(
                    this,
                    deviceContext,
                    notificationPublishService,
                    convertorExecutor);
        }

        contextChainMastershipWatcher.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.RPC_REGISTRATION);
    }

    @NonNull
    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return deviceInfo.getServiceIdentifier();
    }
}
