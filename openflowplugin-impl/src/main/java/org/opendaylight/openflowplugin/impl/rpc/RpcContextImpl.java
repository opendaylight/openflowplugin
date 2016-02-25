/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.util.MdSalRegistratorUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeContext;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcContextImpl implements RpcContext {
    private static final Logger LOG = LoggerFactory.getLogger(RpcContextImpl.class);
    private final RpcProviderRegistry rpcProviderRegistry;
    private final DeviceContext deviceContext;
    private final MessageSpy messageSpy;
    private final Semaphore tracker;

    // TODO: add private Sal salBroker
    private final ConcurrentMap<Class<?>, RoutedRpcRegistration<?>> rpcRegistrations = new ConcurrentHashMap<>();

    public RpcContextImpl(final MessageSpy messageSpy, final RpcProviderRegistry rpcProviderRegistry, final DeviceContext deviceContext,
 final int maxRequests) {
        this.deviceContext = Preconditions.checkNotNull(deviceContext);
        this.messageSpy = Preconditions.checkNotNull(messageSpy);
        this.rpcProviderRegistry = Preconditions.checkNotNull(rpcProviderRegistry);
        tracker = new Semaphore(maxRequests, true);
        deviceContext.setRpcContext(RpcContextImpl.this);
    }

    /**
     * @see org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext#registerRpcServiceImplementation(java.lang.Class,
     * org.opendaylight.yangtools.yang.binding.RpcService)
     */
    @Override
    public <S extends RpcService> void registerRpcServiceImplementation(final Class<S> serviceClass,
                                                                        final S serviceInstance) {
        LOG.trace("Try to register service {} for device {}.", serviceClass, deviceContext.getDeviceState().getNodeInstanceIdentifier());
        if (! rpcRegistrations.containsKey(serviceClass)) {
            final RoutedRpcRegistration<S> routedRpcReg = rpcProviderRegistry.addRoutedRpcImplementation(serviceClass, serviceInstance);
            routedRpcReg.registerPath(NodeContext.class, deviceContext.getDeviceState().getNodeInstanceIdentifier());
            rpcRegistrations.put(serviceClass, routedRpcReg);
            LOG.debug("Registration of service {} for device {}.", serviceClass, deviceContext.getDeviceState().getNodeInstanceIdentifier());
        }
    }

    @Override
    public <S extends RpcService> S lookupRpcService(final Class<S> serviceClass) {
        final RpcService rpcService = rpcRegistrations.get(serviceClass).getInstance();
        return (S) rpcService;
    }
    /**
     * Unregisters all services.
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() {
        for (final Iterator<Entry<Class<?>, RoutedRpcRegistration<?>>> iterator = Iterators
                .consumingIterator(rpcRegistrations.entrySet().iterator()); iterator.hasNext();) {
            final RoutedRpcRegistration<?> rpcRegistration = iterator.next().getValue();
            rpcRegistration.unregisterPath(NodeContext.class, deviceContext.getDeviceState().getNodeInstanceIdentifier());
            rpcRegistration.close();
            LOG.debug("Closing RPC Registration of service {} for device {}.", rpcRegistration.getServiceType(),
                    deviceContext.getDeviceState().getNodeInstanceIdentifier());
        }
    }

    @Override
    public <T> RequestContext<T> createRequestContext() {
        if (!tracker.tryAcquire()) {
            LOG.trace("Device queue {} at capacity", this);
            return null;
        } else {
            LOG.trace("Acquired semaphore for {}, available permits:{} ", deviceContext.getDeviceState().getNodeId(), tracker.availablePermits());
        }

        final Long xid = deviceContext.reservedXidForDeviceMessage();
        if (xid == null) {
            LOG.warn("Xid cannot be reserved for new RequestContext, node:{}", deviceContext.getDeviceState().getNodeId());
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
        LOG.trace("Try to unregister serviceClass {} for Node {}", serviceClass, deviceContext.getDeviceState().getNodeId());
        final RoutedRpcRegistration<?> rpcRegistration = rpcRegistrations.remove(serviceClass);
        if (rpcRegistration != null) {
            rpcRegistration.unregisterPath(NodeContext.class, deviceContext.getDeviceState().getNodeInstanceIdentifier());
            rpcRegistration.close();
            LOG.debug("Unregistration serviceClass {} for Node {}", serviceClass, deviceContext.getDeviceState().getNodeId());
        }
    }
}
