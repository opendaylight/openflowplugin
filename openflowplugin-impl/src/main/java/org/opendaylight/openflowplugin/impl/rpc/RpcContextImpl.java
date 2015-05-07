/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;

public class RpcContextImpl implements RpcContext {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(RpcContextImpl.class);
    final RpcProviderRegistry rpcProviderRegistry;

    // TODO: add private Sal salBroker
    private final KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier;
    private final Collection<RoutedRpcRegistration<?>> rpcRegistrations = new ArrayList<>();

    @GuardedBy("requestsList")
    private final Collection<RequestContext<?>> requestsList = new ArrayList<RequestContext<?>>();

    private int maxRequestsPerDevice;

    public RpcContextImpl(final RpcProviderRegistry rpcProviderRegistry, final KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier) {
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.nodeInstanceIdentifier = nodeInstanceIdentifier;
    }

    /**
     * @see org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext#registerRpcServiceImplementation(java.lang.Class,
     * org.opendaylight.yangtools.yang.binding.RpcService)
     */
    @Override
    public <S extends RpcService> void registerRpcServiceImplementation(final Class<S> serviceClass,
                                                                        final S serviceInstance) {
        final RoutedRpcRegistration<S> routedRpcReg = rpcProviderRegistry.addRoutedRpcImplementation(serviceClass, serviceInstance);
        routedRpcReg.registerPath(NodeContext.class, nodeInstanceIdentifier);
        rpcRegistrations.add(routedRpcReg);
        LOG.debug("Registration of service {} for device {}.",serviceClass, nodeInstanceIdentifier);
    }

    @Override
    public <T> SettableFuture<RpcResult<T>> storeOrFail(final RequestContext<T> requestContext) {
        final SettableFuture<RpcResult<T>> rpcResultFuture = requestContext.getFuture();

        final boolean success;
        // FIXME: use a fixed-size collection, with lockless reserve/set queue
        synchronized (requestsList) {
            if (requestsList.size() >= maxRequestsPerDevice) {
                requestsList.add(requestContext);
                success = true;
            } else {
                success = false;
            }
        }

        if (!success) {
            final RpcResult<T> rpcResult = RpcResultBuilder.<T>failed()
                    .withError(RpcError.ErrorType.APPLICATION, "", "Device's request queue is full.").build();
            rpcResultFuture.set(rpcResult);
        }

        return rpcResultFuture;
    }

    /**
     * Unregisters all services.
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws Exception {
        for (final RoutedRpcRegistration<?> rpcRegistration : rpcRegistrations) {
            rpcRegistration.unregisterPath(NodeContext.class, nodeInstanceIdentifier);
            rpcRegistration.close();
        }
    }

    /**
     * @see org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext#setRequestContextQuota(int)
     */
    @Override
    public void setRequestContextQuota(final int maxRequestsPerDevice) {
        this.maxRequestsPerDevice = maxRequestsPerDevice;
    }

    @Override
    public <T> void forgetRequestContext(final RequestContext<T> requestContext) {
        synchronized (requestsList) {
            requestsList.remove(requestContext);
            LOG.trace("Removed request context with xid {}. Context request in list {}.",
                requestContext.getXid().getValue(), requestsList.size());
        }
    }

    @Override
    public <T> RequestContext<T> createRequestContext() {
        return new RequestContextImpl<T>(this);
    }

    @Override
    public void onDeviceDisconnected(final ConnectionContext connectionContext) {
        for (RoutedRpcRegistration<?> registration : rpcRegistrations) {
            registration.close();
        }

        synchronized (requestsList) {
            requestsList.clear();
        }
    }
}
