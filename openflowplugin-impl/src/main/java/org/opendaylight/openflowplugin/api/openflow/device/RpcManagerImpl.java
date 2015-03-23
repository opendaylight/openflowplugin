/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.device;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;

public class RpcManagerImpl implements RpcManager {

    private DeviceContext deviceContext;

    // /**
    // * Collection of all rpc contexts which are available in rpc manager
    // */
    // final List<RpcContext> rpcContexts = new ArrayList<>();

    private final ProviderContext providerContext;

    public RpcManagerImpl(final ProviderContext providerContext) {
        this.providerContext = providerContext;
    }

    // /**
    // * @return number of rpc contexts
    // */
    // public int getNumberOfRpcContexts() {
    // return rpcContexts.size();
    // }

    /**
     * (non-Javadoc)
     * 
     * @see org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceContextReadyHandler#deviceConnected(org.opendaylight.openflowplugin.api.openflow.device.DeviceContext)
     */
    @Override
    public void deviceConnected(final DeviceContext deviceContext) {
        final RpcContext rpcContext = new RpcContextImpl(providerContext);
        rpcContext.setDeviceContext(deviceContext);
        MdSalRegistratorUtils.registerServices(rpcContext);
    }
}
