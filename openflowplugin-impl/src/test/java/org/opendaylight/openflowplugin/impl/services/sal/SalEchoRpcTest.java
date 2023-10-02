/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Future;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SendEchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SendEchoInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SendEchoOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

public class SalEchoRpcTest extends ServiceMocking {

    private static final byte[] DUMMY_DATA = "DUMMY DATA".getBytes();
    SalEchoRpc salEchoService;

    @Override
    protected void setup() {
        salEchoService = new SalEchoRpc(mockedRequestContextStack, mockedDeviceContext);
    }

    @Test
    public void testSendEcho() throws Exception {
        final EchoOutput echoOut = new EchoOutputBuilder()
                .setData(DUMMY_DATA)
                .build();
        final RpcResult<EchoOutput> replyRpcResult = RpcResultBuilder.success(echoOut).build();
        final ListenableFuture<RpcResult<EchoOutput>> replyFt = Futures.immediateFuture(replyRpcResult);
        Mockito.when(mockedRequestContext.getFuture()).thenReturn(replyFt);
        SendEchoInput sendEchoInput = new SendEchoInputBuilder()
                .setData(DUMMY_DATA)
                .build();

        final Future<RpcResult<SendEchoOutput>> echoOutput = salEchoService.sendEcho(sendEchoInput);

        Assert.assertNotNull(echoOutput);
        Assert.assertTrue(echoOutput.isDone());
        Assert.assertTrue(echoOutput.get().isSuccessful());
        verify(mockedRequestContextStack).createRequestContext();
        verify(mockedOutboundQueue).commitEntry(eq(Uint32.valueOf(2121)), any(), any());
    }
}