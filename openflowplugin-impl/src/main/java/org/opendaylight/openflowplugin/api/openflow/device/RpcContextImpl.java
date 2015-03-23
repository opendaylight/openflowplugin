/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.device;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.yangtools.yang.binding.RpcService;

public class RpcContextImpl implements RpcContext {

    final ProviderContext providerContext;

    // TODO: add private Sal salBroker
    private RequestContext requestContext;
    private DeviceContext deviceContext;
    private final List<RoutedRpcRegistration> rpcRegistrations = new ArrayList<>();

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
     * @see org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext#setDeviceContext(org.opendaylight.openflowplugin.
     *      api.openflow.device.DeviceContext)
     */
    @Override
    public void setDeviceContext(final DeviceContext deviceContext) {
        this.deviceContext = deviceContext;

    }

    /**
     * Unregisters all services.
     * 
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws Exception {

    }
}
