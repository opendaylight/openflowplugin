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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.impl.rpc.RpcContextImpl;
import org.opendaylight.openflowplugin.impl.rpc.RpcManagerImpl;
import org.opendaylight.openflowplugin.impl.services.SalFlowServiceImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class RpcManagerImplTest {

    private static final int AWAITED_NUM_OF_CALL_ADD_ROUTED_RPC = 12;

    private static final int AWAITED_NUM_OF_CALL_ADD_NEW_REQUEST = 1;

    final ProviderContext mockedProviderContext = mock(ProviderContext.class);
    final RpcManagerImpl rpcManager = new RpcManagerImpl(mockedProviderContext);
    final RpcContext mockedRpcContext = mock(RpcContext.class);
    final AddFlowInput mockedFlowInput = prepareTestingAddFlow();
    final DeviceContext mockedDeviceContext = mock(DeviceContext.class);

    @Ignore
    @Test
    public void deviceConnectedTest() {

        rpcManager.deviceConnected(mockedDeviceContext);

        verify(mockedProviderContext, times(AWAITED_NUM_OF_CALL_ADD_ROUTED_RPC)).addRoutedRpcImplementation(
                Matchers.any(Class.class), Matchers.any(RpcService.class));
    }


    /**
     * Tests behavior of RpcContextImpl when calling rpc from MD-SAL
     */
    @Ignore
    @Test
    public void invokeRpcTestExistsCapacityTest() throws InterruptedException, ExecutionException {
        final ConnectionContext mockedConnectionContext = mock(ConnectionContext.class);
        final FeaturesReply mockedFeatures = mock(FeaturesReply.class);
        final BigInteger dummyDatapathId = new BigInteger("1");
        final Short dummyVersion = 1;
        final ConnectionAdapter mockedConnectionAdapter = mock(ConnectionAdapter.class);

        when(mockedFeatures.getDatapathId()).thenReturn(dummyDatapathId);
        when(mockedFeatures.getVersion()).thenReturn(dummyVersion);
        when(mockedConnectionContext.getFeatures()).thenReturn(mockedFeatures);
        when(mockedConnectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);
        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedConnectionContext);
        final Xid mockedXid = mock(Xid.class);
        final Long dummyXid = 1l;
        when(mockedXid.getValue()).thenReturn(dummyXid);
        when(mockedDeviceContext.getNextXid()).thenReturn(mockedXid);

        invokeRpcTestExistsCapacity(10, true);
        invokeRpcTestExistsCapacity(0, false);
    }

    private void invokeRpcTestExistsCapacity(final int capacity, final boolean result) throws InterruptedException,
            ExecutionException {
        // TODO: how to invoke service remotely?
        final RpcContextImpl rpcContext = new RpcContextImpl(mockedProviderContext, mockedDeviceContext);
        when(mockedProviderContext.getRpcService(SalFlowService.class)).thenReturn(new SalFlowServiceImpl(rpcContext, mockedDeviceContext));
        rpcContext.setRequestContextQuota(capacity);

        final SalFlowService salFlowService = mockedProviderContext.getRpcService(SalFlowService.class);
        final Future<RpcResult<AddFlowOutput>> addedFlow = salFlowService.addFlow(prepareTestingAddFlow());
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
