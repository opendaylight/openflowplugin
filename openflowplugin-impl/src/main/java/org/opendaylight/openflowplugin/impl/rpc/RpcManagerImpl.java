/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.impl.util.MdSalRegistratorUtils;

public class RpcManagerImpl implements RpcManager {

    private final ProviderContext providerContext;
    private DeviceInitializationPhaseHandler deviceInitPhaseHandler;

    public RpcManagerImpl(final ProviderContext providerContext) {
        this.providerContext = providerContext;
    }

    @Override
    public void setDeviceInitializationPhaseHandler(final DeviceInitializationPhaseHandler handler) {
        deviceInitPhaseHandler = handler;
    }

    @Override
    public void onDeviceContextLevelUp(final DeviceContext deviceContext) {
        final RpcContext rpcContext = new RpcContextImpl(providerContext, deviceContext);
        MdSalRegistratorUtils.registerServices(rpcContext, deviceContext);
        // finish device initialization cycle back to DeviceManager
        deviceInitPhaseHandler.onDeviceContextLevelUp(deviceContext);
    }
}
