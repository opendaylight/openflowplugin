/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class RpcContextImpl implements RpcContext {

    final ProviderContext providerContext;

    // TODO: add private Sal salBroker
    private final List<RequestContext> requestContexts = new ArrayList<>();
    private DeviceContext deviceContext;
    private final List<RoutedRpcRegistration> rpcRegistrations = new ArrayList<>();

    private int maxRequestsPerDevice;

    public RpcContextImpl(final ProviderContext providerContext) {
        this.providerContext = providerContext;
    }

    /**
     * 
     * @see org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext#registerRpcServiceImplementation(java.lang.Class,
     *      org.opendaylight.yangtools.yang.binding.RpcService)
     */
    @Override
    public <S extends RpcService> void registerRpcServiceImplementation(final Class<S> serviceClass,
            final S serviceInstance) {
        rpcRegistrations.add(providerContext.addRoutedRpcImplementation(serviceClass, serviceInstance));
    }

    /**
     * 
     * 
     * @see org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext#setDeviceContext(DeviceContext)
     *      api.openflow.device.DeviceContext)
     */
    @Override
    public void setDeviceContext(final DeviceContext deviceContext) {
        this.deviceContext = deviceContext;

    }

    @Override
    public Future<RpcResult<? extends DataObject>> addNewRequest(final DataObject data) {
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

    public boolean isRequestContextCapacityEmpty() {
        return requestContexts.size() <= maxRequestsPerDevice;
    }


}
