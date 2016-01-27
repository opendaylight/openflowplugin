/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import com.google.common.base.Preconditions;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.ItemLifeCycleSource;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
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

    public RpcContextImpl(final MessageSpy messageSpy, final RpcProviderRegistry rpcProviderRegistry, final DeviceContext deviceContext, final int maxRequests) {
        this.messageSpy = messageSpy;
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.deviceContext = Preconditions.checkNotNull(deviceContext);
        tracker = new Semaphore(maxRequests, true);
    }

    /**
     * @see org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext#registerRpcServiceImplementation(java.lang.Class,
     * org.opendaylight.yangtools.yang.binding.RpcService)
     */
    @Override
    public <S extends RpcService> void registerRpcServiceImplementation(final Class<S> serviceClass,
                                                                        final S serviceInstance) {
        if (! rpcRegistrations.containsKey(serviceClass)) {
            final RoutedRpcRegistration<S> routedRpcReg = rpcProviderRegistry.addRoutedRpcImplementation(serviceClass, serviceInstance);
            routedRpcReg.registerPath(NodeContext.class, deviceContext.getDeviceState().getNodeInstanceIdentifier());
            rpcRegistrations.put(serviceClass, routedRpcReg);
        }
        LOG.debug("Registration of service {} for device {}.", serviceClass, deviceContext.getDeviceState().getNodeInstanceIdentifier());

        if (serviceInstance instanceof ItemLifeCycleSource) {
            // TODO: collect registration for selective unregistering in case of tearing down only one rpc
            deviceContext.getItemLifeCycleSourceRegistry().registerLifeCycleSource((ItemLifeCycleSource) serviceInstance);
        }
    }

    @Override
    public <S extends RpcService> S lookupRpcService(Class<S> serviceClass) {
        S service = null;
        for (RoutedRpcRegistration<?> rpcRegistration : rpcRegistrations) {
            final RpcService rpcService = rpcRegistration.getInstance();
            if (serviceClass.isInstance(rpcService)) {
                service = (S) rpcService;
                break;
            }
        }
        return service;
    }
    /**
     * Unregisters all services.
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() {
        for (final RoutedRpcRegistration<?> rpcRegistration : rpcRegistrations.values()) {
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
            LOG.info("Acquired semaphore for {}, available permits:{} ", deviceContext.getDeviceState().getNodeId(), tracker.availablePermits());
        }

        final Long xid = deviceContext.getReservedXid();
        if (xid == null) {
            LOG.error("Xid cannot be reserved for new RequestContext, node:{}", deviceContext.getDeviceState().getNodeId());
        }

        return new AbstractRequestContext<T>(deviceContext.getReservedXid()) {
            @Override
            public void close() {
                tracker.release();
                final long xid = getXid().getValue();
                LOG.info("Removed request context with xid {}", xid);
                messageSpy.spyMessage(RpcContextImpl.class, MessageSpy.STATISTIC_GROUP.REQUEST_STACK_FREED);
            }
        };
    }

    @Override
    public <S extends RpcService> void unregisterRpcServiceImplementation(final Class<S> serviceClass) {
        LOG.debug("Unregistration serviceClass {} for Node {}", serviceClass, deviceContext.getDeviceState().getNodeId());
        final RoutedRpcRegistration<?> rpcRegistration = rpcRegistrations.remove(serviceClass);
        if (rpcRegistration != null) {
            rpcRegistration.unregisterPath(NodeContext.class, deviceContext.getDeviceState().getNodeInstanceIdentifier());
            rpcRegistration.close();
        }
    }
}
