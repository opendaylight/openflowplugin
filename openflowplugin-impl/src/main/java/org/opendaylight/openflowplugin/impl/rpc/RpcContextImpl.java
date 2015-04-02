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
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class RpcContextImpl implements RpcContext {

    final ProviderContext providerContext;

    // TODO: add private Sal salBroker
    private final List<RequestContext<? extends DataObject>> requestContexts = new ArrayList<>();
    private final DeviceContext deviceContext;
    private final List<RoutedRpcRegistration> rpcRegistrations = new ArrayList<>();
    private final List<RequestContext<?>> synchronizedRequestsList = Collections
            .<RequestContext<?>>synchronizedList(new ArrayList<RequestContext<?>>());

    private int maxRequestsPerDevice;

    public RpcContextImpl(final ProviderContext providerContext, final DeviceContext deviceContext) {
        this.providerContext = providerContext;
        this.deviceContext = deviceContext;
    }

    /**
     * @see org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext#registerRpcServiceImplementation(java.lang.Class,
     *      org.opendaylight.yangtools.yang.binding.RpcService)
     */
    @Override
    public <S extends RpcService> void registerRpcServiceImplementation(final Class<S> serviceClass,
            final S serviceInstance) {
        rpcRegistrations.add(providerContext.addRoutedRpcImplementation(serviceClass, serviceInstance));
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
        requestContexts.remove(requestContext);
    }

    @Override
    public DeviceContext getDeviceContext() {
        return deviceContext;
    }

    @Override
    public <T> RequestContext<T> createRequestContext() {
        return new RequestContextImpl<T>(this);
    }

    public boolean isRequestContextCapacityEmpty() {
        return requestContexts.size() <= maxRequestsPerDevice;
    }

}
