/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.device;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.common.api.routing.RouteChangeListener;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.BindingAwareService;
import org.opendaylight.controller.sal.binding.api.rpc.RpcContextIdentifier;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.impl.rpc.RpcContextImpl;
import org.opendaylight.openflowplugin.impl.rpc.RpcManagerImpl;
import org.opendaylight.openflowplugin.impl.services.SalFlowServiceImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

@RunWith(MockitoJUnitRunner.class)
public class RpcManagerImplTest {
    private class ProviderContextImpl implements ProviderContext {
        private int routedRpcImplementations;

        public <T extends BindingAwareService> T getSALService(final Class<T> arg0) {
            return null;
        }

        @Override
        public <T extends RpcService> T getRpcService(final Class<T> arg0) {
            return null;
        }

        @Override
        public <T extends RpcService> RoutedRpcRegistration<T> addRoutedRpcImplementation(final Class<T> arg0,
                final T arg1) throws IllegalStateException {
            routedRpcImplementations++;
            return null;
        }

        @Override
        public <T extends RpcService> RpcRegistration<T> addRpcImplementation(final Class<T> arg0, final T arg1)
                throws IllegalStateException {
            return null;
        }

        @Override
        public <L extends RouteChangeListener<RpcContextIdentifier, InstanceIdentifier<?>>> ListenerRegistration<L> registerRouteChangeListener(
                final L arg0) {
            return null;
        }

        /**
         * @return the routedRpcImplementations
         */
        public int getRoutedRpcImplementations() {
            return routedRpcImplementations;
        }

    }

    private static final int AWAITED_NUM_OF_CALL_ADD_ROUTED_RPC = 12;

    @Mock
    ProviderContext mockedProviderContext;

    @Mock
    DeviceContext mockedDeviceContext;

    @Mock
    ConnectionAdapter mockedConnectionAdapter;

    @Before
    public void initialization() {
        final BigInteger dummyDatapathId = new BigInteger("1");
        final Short dummyVersion = 1;

        final FeaturesReply mockedFeatures = mock(FeaturesReply.class);
        when(mockedFeatures.getDatapathId()).thenReturn(dummyDatapathId);
        when(mockedFeatures.getVersion()).thenReturn(dummyVersion);

        final ConnectionContext mockedConnectionContext = mock(ConnectionContext.class);
        when(mockedConnectionContext.getFeatures()).thenReturn(mockedFeatures);
        when(mockedConnectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);

        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedConnectionContext);
    }

    @Test
    public void deviceConnectedTest() {
        final ProviderContextImpl providerContext = new ProviderContextImpl();
        final RpcManagerImpl rpcManager = new RpcManagerImpl(providerContext);

        rpcManager.deviceConnected(mockedDeviceContext);

        Assert.assertEquals(AWAITED_NUM_OF_CALL_ADD_ROUTED_RPC, providerContext.getRoutedRpcImplementations());
    }

    /**
     * Tests behavior of RpcContextImpl when calling rpc from MD-SAL
     */
    @Test
    public void invokeRpcTestExistsCapacityTest() throws InterruptedException, ExecutionException {
        when(mockedConnectionAdapter.flowMod(Matchers.any(FlowModInput.class))).thenReturn(
                Futures.immediateFuture(RpcResultBuilder.<Void> status(true).build()));

        final Xid mockedXid = mock(Xid.class);
        final Long dummyXid = 1l;
        when(mockedXid.getValue()).thenReturn(dummyXid);
        when(mockedDeviceContext.getNextXid()).thenReturn(mockedXid);

        invokeRpcTestExistsCapacity(10, true, mockedDeviceContext);
        invokeRpcTestExistsCapacity(0, false, mockedDeviceContext);
    }

    private void invokeRpcTestExistsCapacity(final int capacity, final boolean result, final DeviceContext deviceContext)
            throws InterruptedException, ExecutionException {

        // TODO: how to invoke service remotely?
        final RpcContextImpl rpcContext = new RpcContextImpl(mockedProviderContext, deviceContext);
        final SalFlowServiceImpl salFlowSrv = new SalFlowServiceImpl(rpcContext);
        when(mockedProviderContext.getRpcService(SalFlowService.class)).thenReturn(salFlowSrv);
        rpcContext.setRequestContextQuota(capacity);

        final SalFlowService salFlowService = mockedProviderContext.getRpcService(SalFlowService.class);
        final Future<RpcResult<AddFlowOutput>> addedFlow = salFlowService.addFlow(prepareTestingAddFlow());

        Assert.assertEquals(result, !addedFlow.isDone());

        if (addedFlow.isDone()) {
            final RpcResult<AddFlowOutput> rpcResult = addedFlow.get();
            Assert.assertFalse(rpcResult.isSuccessful());
        }
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
