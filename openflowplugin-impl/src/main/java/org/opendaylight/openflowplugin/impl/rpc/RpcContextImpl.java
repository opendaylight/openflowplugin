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
import java.util.Collections;
import java.util.List;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeContext;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;

public class RpcContextImpl implements RpcContext {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(RpcContextImpl.class);
    final RpcProviderRegistry rpcProviderRegistry;

    // TODO: add private Sal salBroker
    private final DeviceContext deviceContext;
    private final List<RoutedRpcRegistration> rpcRegistrations = new ArrayList<>();
    private final List<RequestContext<?>> synchronizedRequestsList = Collections
            .<RequestContext<?>>synchronizedList(new ArrayList<RequestContext<?>>());

    private int maxRequestsPerDevice;

    public RpcContextImpl(final RpcProviderRegistry rpcProviderRegistry, final DeviceContext deviceContext) {
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.deviceContext = deviceContext;
    }

    /**
     * @see org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext#registerRpcServiceImplementation(java.lang.Class,
     * org.opendaylight.yangtools.yang.binding.RpcService)
     */
    @Override
    public <S extends RpcService> void registerRpcServiceImplementation(final Class<S> serviceClass,
                                                                        final S serviceInstance) {
        final RoutedRpcRegistration<S> routedRpcReg = rpcProviderRegistry.addRoutedRpcImplementation(serviceClass, serviceInstance);
        routedRpcReg.registerPath(NodeContext.class, deviceContext.getDeviceState().getNodeInstanceIdentifier());
        rpcRegistrations.add(routedRpcReg);
        LOG.debug("Registration of service {} for device {}.",serviceClass, deviceContext.getDeviceState().getNodeInstanceIdentifier());
    }

    @Override
    public <T> SettableFuture<RpcResult<T>> storeOrFail(final RequestContext<T> requestContext) {
        final SettableFuture<RpcResult<T>> rpcResultFuture = requestContext.getFuture();

        if (synchronizedRequestsList.size() < maxRequestsPerDevice) {
            synchronizedRequestsList.add(requestContext);
        } else {
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
            rpcRegistration.unregisterPath(NodeContext.class, deviceContext.getDeviceState().getNodeInstanceIdentifier());
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
        synchronizedRequestsList.remove(requestContext);
        LOG.trace("Removed request context with xid {}. Context request in list {}.",
                requestContext.getXid().getValue(), synchronizedRequestsList.size());
    }


    @Override
    public <T> RequestContext<T> createRequestContext() {
        return new RequestContextImpl<T>(this);
    }

    public boolean isRequestContextCapacityEmpty() {
        return synchronizedRequestsList.size() <= maxRequestsPerDevice;
    }

    @Override
    public void onDeviceDisconnected(final ConnectionContext connectionContext) {
        for (RoutedRpcRegistration registration : rpcRegistrations) {
            registration.close();
        }

        synchronizedRequestsList.clear();
    }
}
