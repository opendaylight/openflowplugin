/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.openflowplugin.impl.services.singlelayer.GetAsyncImpl;
import org.opendaylight.openflowplugin.impl.services.singlelayer.SetAsyncImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsyncInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsyncOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.SetAsyncInputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link SalAsyncConfigServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SalAsyncConfigServiceImplTest extends ServiceMocking {
    @Test
    public void testSetAsync() throws Exception {
        final var setAsync = new SetAsyncImpl(mockedRequestContextStack, mockedDeviceContext);

        final var setAsyncInput = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol
                .rev130731.SetAsyncInputBuilder().build();
        final var replyRpcResult = RpcResultBuilder.success(setAsyncInput).build();
        final var replyFuture = Futures.immediateFuture(replyRpcResult);
        when(mockedRequestContext.getFuture()).thenReturn(replyFuture);

        final var setAsyncResult = setAsync.invoke(new SetAsyncInputBuilder().build());

        assertNotNull(setAsyncResult);
        assertTrue(setAsyncResult.isDone());
        assertTrue(setAsyncResult.get().isSuccessful());
        verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testGetAsyncTest() throws Exception {
        final var getAsync = new GetAsyncImpl(mockedRequestContextStack, mockedDeviceContext);

        final var getAsyncOutput = new GetAsyncOutputBuilder().build();
        final var replyRpcResult = RpcResultBuilder.success(getAsyncOutput).build();
        final var replyFuture = Futures.immediateFuture(replyRpcResult);
        when(mockedRequestContext.getFuture()).thenReturn(replyFuture);

        final var getAsyncResult = getAsync.invoke(new GetAsyncInputBuilder().build());

        assertNotNull(getAsyncResult);
        assertTrue(getAsyncResult.isDone());
        assertTrue(getAsyncResult.get().isSuccessful());
        verify(mockedRequestContextStack).createRequestContext();
        verify(mockedOutboundQueue).commitEntry(eq(ServiceMocking.DUMMY_XID_VALUE), any(), any());
    }
}
