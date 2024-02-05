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
import org.opendaylight.openflowplugin.impl.services.SendEchoImpl;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SendEchoInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

public class SalEchoServiceImplTest extends ServiceMocking {
    private static final byte[] DUMMY_DATA = "DUMMY DATA".getBytes();

    private SendEchoImpl salEchoService;

    @Override
    protected void setup() {
        salEchoService = new SendEchoImpl(mockedRequestContextStack, mockedDeviceContext);
    }

    @Test
    public void testSendEcho() throws Exception {
        final var echoOut = new EchoOutputBuilder().setData(DUMMY_DATA).build();
        final var replyRpcResult = RpcResultBuilder.success(echoOut).build();
        final var replyFt = Futures.immediateFuture(replyRpcResult);
        when(mockedRequestContext.getFuture()).thenReturn(replyFt);
        final var sendEchoInput = new SendEchoInputBuilder().setData(DUMMY_DATA).build();

        final var echoOutput = salEchoService.invoke(sendEchoInput);

        assertNotNull(echoOutput);
        assertTrue(echoOutput.isDone());
        assertTrue(echoOutput.get().isSuccessful());
        verify(mockedRequestContextStack).createRequestContext();
        verify(mockedOutboundQueue).commitEntry(eq(Uint32.valueOf(2121)), any(), any());
    }
}
