/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.device;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.Test;
import org.mockito.Matchers;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.impl.rpc.RpcContextImpl;
import org.opendaylight.openflowplugin.impl.rpc.RpcManagerImpl;
import org.opendaylight.openflowplugin.impl.services.SalFlowServiceImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class RpcManagerImplTest {

    private static final int AWAITED_NUM_OF_CALL_ADD_ROUTED_RPC = 12;

    private static final int AWAITED_NUM_OF_CALL_ADD_NEW_REQUEST = 1;

    final ProviderContext mockedProviderContext = mock(ProviderContext.class);
    final RpcManagerImpl rpcManager = new RpcManagerImpl(mockedProviderContext);
    final DeviceContext mockedRequestContext = mock(DeviceContext.class);

    @Test
    public void deviceConnectedTest() {

        rpcManager.deviceConnected(mockedRequestContext);

        verify(mockedProviderContext, times(AWAITED_NUM_OF_CALL_ADD_ROUTED_RPC)).addRoutedRpcImplementation(
                Matchers.any(Class.class), Matchers.any(RpcService.class));
    }

    final RpcContext mockedRpcContext = mock(RpcContext.class);
    final AddFlowInput mockedFlowInput = prepareTestingAddFlow();
    final DeviceContext mockedDeviceContext = mock(DeviceContext.class);

    /**
     * Tests behavior of RpcContextImpl when calling rpc from MD-SAL
     */
    @Test
    public void invokeRpcTestExistsCapacityTest() throws InterruptedException, ExecutionException {
        invokeRpcTestExistsCapacity(10, true);
        invokeRpcTestExistsCapacity(0, false);
    }

    private void invokeRpcTestExistsCapacity(final int capacity, final boolean result) throws InterruptedException,
            ExecutionException {
        // TODO: how to invoke service remotely?
        final RpcContextImpl rpcContext = new RpcContextImpl(mockedProviderContext, mockedDeviceContext);
        when(mockedProviderContext.getRpcService(SalFlowService.class)).thenReturn(new SalFlowServiceImpl(rpcContext));
        rpcContext.setRequestContextQuota(capacity);

        final SalFlowService salFlowService = mockedProviderContext.getRpcService(SalFlowService.class);
        final Future<RpcResult<AddFlowOutput>> addedFlow = salFlowService.addFlow(prepareTestingAddFlow());
        assertEquals(result, addedFlow.get().isSuccessful());
    }

    /**
     * @return
     */
    private AddFlowInput prepareTestingAddFlow() {
        final AddFlowInputBuilder builder = new AddFlowInputBuilder();
        builder.setFlowName("dummy flow");
        builder.setHardTimeout(10000);

        return builder.build();
    }
}
