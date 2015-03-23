/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.device;

import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.yangtools.yang.binding.RpcService;

public class RpcContextImpl implements RpcContext {

    /**
     * 
     * 
     * @see org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext#registerRpcServiceImplementation(java.lang.Class,
     *      org.opendaylight.yangtools.yang.binding.RpcService)
     */
    @Override
    public <S extends RpcService> void registerRpcServiceImplementation(final Class<S> serviceClass,
            final S serviceInstance) {
        // TODO Auto-generated method stub

    }

    /**
     * 
     * 
     * @see org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext#setRequestContext(org.opendaylight.openflowplugin
     *      .api.openflow.device.RequestContext)
     */
    @Override
    public void setRequestContext(final RequestContext requestContext) {
        // TODO Auto-generated method stub

    }

    /**
     * 
     * 
     * @see org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext#setDeviceContext(org.opendaylight.openflowplugin.
     *      api.openflow.device.DeviceContext)
     */
    @Override
    public void setDeviceContext(final DeviceContext deviceContext) {
        // TODO Auto-generated method stub

    }

}
