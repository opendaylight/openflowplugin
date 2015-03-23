/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.device;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.openflowplugin.openflow.md.core.sal.AbstractModelDrivenSwitch;

public class RpcTests {

    private static final int AWAITED_NUM_OF_CALL_ADD_ROUTED_RPC = 10;

    @Test
    public void rpcContextCreationTest() {
        final RpcManagerImpl rpcManager = new RpcManagerImpl();
        final RequestContext mockedRequestContext = mock(RequestContext.class);
        final ProviderContext mockedProviderContext = mock(ProviderContext.class);

        rpcManager.onSessionInitiated(mockedProviderContext);
        rpcManager.deviceConnected(mockedRequestContext);

        verify(mockedProviderContext, times(AWAITED_NUM_OF_CALL_ADD_ROUTED_RPC)).addRoutedRpcImplementation(
                any(Class.class), any(AbstractModelDrivenSwitch.class));
    }
}
