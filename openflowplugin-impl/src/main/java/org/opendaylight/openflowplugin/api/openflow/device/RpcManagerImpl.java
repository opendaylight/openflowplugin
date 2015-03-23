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
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;

public class RpcManagerImpl implements RpcManager {

    private DeviceContext deviceContext;

    /**
     * Collection of all rpc contexts which are available in rpc manager
     */
    private final List<RpcContext> rpcContexts = new ArrayList<>();

    private ProviderContext providerContext;

    public RpcManagerImpl() {
    }

    /**
     * @return number of rpc contexts
     */
    public int getNumberOfRpcContexts() {
        return rpcContexts.size();
    }

    @Override
    public void deviceConnected(final RequestContext requestContext) {
        final RpcContext rpcContext = new RpcContextImpl();
        rpcContext.setRequestContext(requestContext);
        rpcContext.setDeviceContext(deviceContext);
        rpcContext.registerServices(providerContext);
        rpcContexts.add(rpcContext);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opendaylight.controller.sal.binding.api.BindingAwareProvider#onSessionInitiated(org.opendaylight.controller
     * .sal.binding.api.BindingAwareBroker.ProviderContext)
     */
    @Override
    public void onSessionInitiated(final ProviderContext providerContext) {
        this.providerContext = providerContext;
    }

}
