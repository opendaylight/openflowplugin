/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.sal;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Future;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsyncInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsyncOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.SetAsyncInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.SetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncInput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link SalAsyncConfigServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SalAsyncConfigServiceImplTest extends ServiceMocking {

    private SalAsyncConfigServiceImpl salAsyncConfigService;

    @Override
    public void setup() {
        salAsyncConfigService = new SalAsyncConfigServiceImpl(
                mockedRequestContextStack, mockedDeviceContext);
    }

    @Test
    public void testSetAsync() throws Exception {
        final SetAsyncInput setAsyncInput = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol
                .rev130731.SetAsyncInputBuilder().build();
        final RpcResult<SetAsyncInput> replyRpcResult = RpcResultBuilder.success(setAsyncInput).build();
        final ListenableFuture<RpcResult<SetAsyncInput>> replyFuture = Futures.immediateFuture(replyRpcResult);
        Mockito.when(mockedRequestContext.getFuture()).thenReturn(replyFuture);

        final ListenableFuture<RpcResult<SetAsyncOutput>> setAsyncResult =
                salAsyncConfigService.setAsync(new SetAsyncInputBuilder().build());

        Assert.assertNotNull(setAsyncResult);
        Assert.assertTrue(setAsyncResult.isDone());
        Assert.assertTrue(setAsyncResult.get().isSuccessful());
        verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testGetAsyncTest() throws Exception {
        final GetAsyncOutput getAsyncOutput = new GetAsyncOutputBuilder().build();
        final RpcResult<GetAsyncOutput> replyRpcResult = RpcResultBuilder.success(getAsyncOutput).build();
        final ListenableFuture<RpcResult<GetAsyncOutput>> replyFuture = Futures.immediateFuture(replyRpcResult);
        Mockito.when(mockedRequestContext.getFuture()).thenReturn(replyFuture);

        final Future<RpcResult<GetAsyncOutput>> getAsyncResult =
                salAsyncConfigService.getAsync(new GetAsyncInputBuilder().build());

        Assert.assertNotNull(getAsyncResult);
        Assert.assertTrue(getAsyncResult.isDone());
        Assert.assertTrue(getAsyncResult.get().isSuccessful());
        verify(mockedRequestContextStack).createRequestContext();
        verify(mockedOutboundQueue).commitEntry(eq(ServiceMocking.DUMMY_XID_VALUE.toJava()),
                ArgumentMatchers.any(), ArgumentMatchers.any());
    }
}
