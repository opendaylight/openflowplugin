/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.device;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.Matchers;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yangtools.yang.binding.RpcService;

public class RpcManagerImplTest {

    private static final int AWAITED_NUM_OF_CALL_ADD_ROUTED_RPC = 13;

    final ProviderContext mockedProviderContext = mock(ProviderContext.class);
    final RpcManagerImpl rpcManager = new RpcManagerImpl(mockedProviderContext);
    final DeviceContext mockedRequestContext = mock(DeviceContext.class);

    @Test
    public void deviceConnectedTest() {

        rpcManager.deviceConnected(mockedRequestContext);

        verify(mockedProviderContext, times(AWAITED_NUM_OF_CALL_ADD_ROUTED_RPC)).addRoutedRpcImplementation(
                Matchers.any(Class.class), Matchers.any(RpcService.class));
    }

    /**
     * Tests behavior of RpcContextImpl when calling rpc from MD-SAL
     */
    @Test
    public void invokeRpcTest() {
        
        rpcManager.deviceConnected(mockedRequestContext);
//        when(mockedProviderContext.getRpcService(SalFlowService.class)).thenReturn(value);
        final SalFlowService salFlowService = mockedProviderContext.getRpcService(SalFlowService.class);

    }
}
