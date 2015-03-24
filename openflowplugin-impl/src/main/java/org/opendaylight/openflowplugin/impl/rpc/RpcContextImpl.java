/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

public class RpcContextImpl implements RpcContext {

    final ProviderContext providerContext;

    // TODO: add private Sal salBroker
    private final List<RequestContext> requestContexts = new ArrayList<>();
    private DeviceContext deviceContext;
    private final List<RoutedRpcRegistration> rpcRegistrations = new ArrayList<>();
    private final List<RequestContext> synchronizedRequestsList = Collections.synchronizedList(new ArrayList<RequestContext>());

    private int maxRequestsPerDevice;

    public RpcContextImpl(final ProviderContext providerContext, final DeviceContext deviceContext) {
        this.providerContext = providerContext;
        this.deviceContext = deviceContext;
    }

    /**
     * @see org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext#registerRpcServiceImplementation(java.lang.Class,
     * org.opendaylight.yangtools.yang.binding.RpcService)
     */
    @Override
    public <S extends RpcService> void registerRpcServiceImplementation(final Class<S> serviceClass,
                                                                        final S serviceInstance) {
        rpcRegistrations.add(providerContext.addRoutedRpcImplementation(serviceClass, serviceInstance));
    }

    @Override
    public <T extends DataObject> Future<RpcResult<T>> addNewRequest(final DataObject data) {
        final Future<RpcResult<T>> rpcResultFuture;
        final RequestContext requestContext = new RequestContextImpl(this);

        if (synchronizedRequestsList.size() < maxRequestsPerDevice) {
            synchronizedRequestsList.add(requestContext);
            rpcResultFuture = requestContext.createRequestFuture(data);

            ListenableFuture<RpcResult<Void>> resultFutureFromDevice = sendRequestToDevice(data);
            Futures.addCallback(resultFutureFromDevice, new FutureCallback<Object>() {
                @Override
                public void onSuccess(final Object o) {
                    requestContext.requestSucceeded();
                }

                @Override
                public void onFailure(final Throwable throwable) {
                    requestContext.requestFailed(throwable.getCause().getMessage());
                }
            });
        } else {
            rpcResultFuture = Futures.immediateFuture(RpcResultBuilder.<T>failed().withError(RpcError.ErrorType.APPLICATION, "", "Request queue full.").build());
        }


        return rpcResultFuture;
    }

    private ListenableFuture<RpcResult<Void>> sendRequestToDevice(final DataObject data) {
        //TODO : send data to device
        return null;
    }

    /**
     * Unregisters all services.
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws Exception {
        for (final RoutedRpcRegistration rpcRegistration : rpcRegistrations) {
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
    public void forgetRequestContext(final RequestContext requestContext) {
        requestContexts.remove(requestContext);
    }

    public boolean isRequestContextCapacityEmpty() {
        return requestContexts.size() <= maxRequestsPerDevice;
    }

}
