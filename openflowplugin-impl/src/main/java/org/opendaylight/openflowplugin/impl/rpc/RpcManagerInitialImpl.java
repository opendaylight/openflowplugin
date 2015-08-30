/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.impl.util.MdSalRegistratorUtils;

/**
 * This gets invoked after the device manager on init.
 *
 * Registers only the initial rpcs likes role. Hands it over to RoleManager
 */
public class RpcManagerInitialImpl implements RpcManager {

    private final RpcProviderRegistry rpcProviderRegistry;
    private DeviceInitializationPhaseHandler deviceInitPhaseHandler;
    private final int maxRequestsQuota;

    public RpcManagerInitialImpl(final RpcProviderRegistry rpcProviderRegistry,
                                 final int quotaValue) {
        this.rpcProviderRegistry = rpcProviderRegistry;
        maxRequestsQuota = quotaValue;
    }

    @Override
    public void setDeviceInitializationPhaseHandler(final DeviceInitializationPhaseHandler handler) {
        deviceInitPhaseHandler = handler;
    }

    @Override
    public void onDeviceContextLevelUp(final DeviceContext deviceContext) {
        final RpcContext rpcContext = new RpcContextImpl(deviceContext.getMessageSpy(), rpcProviderRegistry, deviceContext, maxRequestsQuota);
        deviceContext.setRpcContext(rpcContext);
        deviceContext.addDeviceContextClosedHandler(rpcContext);

        // register just the initial set of role services to configure the device
        MdSalRegistratorUtils.registerInitialServices(rpcContext, deviceContext);

        // go to statistics manager
        deviceInitPhaseHandler.onDeviceContextLevelUp(deviceContext);
    }

    @Override
    public void close() throws Exception {

    }
}
