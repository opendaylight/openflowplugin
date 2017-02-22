/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Future;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.services.EchoService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SalEchoService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SendEchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SendEchoOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SendEchoOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public final class SalEchoServiceImpl implements SalEchoService {

    private final EchoService echoService;

    public SalEchoServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        echoService = new EchoService(requestContextStack, deviceContext);
    }

    @Override
    public Future<RpcResult<SendEchoOutput>> sendEcho(final SendEchoInput sendEchoInput) {
        final EchoInputBuilder echoInputBld = new EchoInputBuilder()
                .setData(sendEchoInput.getData());
        return transform(echoService.handleServiceCall(echoInputBld));
    }

    private Future<RpcResult<SendEchoOutput>> transform(final ListenableFuture<RpcResult<EchoOutput>> rpcResultListenableFuture) {
        return Futures.transform(rpcResultListenableFuture, new Function<RpcResult<EchoOutput>, RpcResult<SendEchoOutput>>() {
            @Nullable
            @Override
            public RpcResult<SendEchoOutput> apply(@Nullable final RpcResult<EchoOutput> input) {
                Preconditions.checkNotNull(input, "echoOutput value is never expected to be NULL");
                final RpcResult<SendEchoOutput> rpcOutput;
                if (input.isSuccessful()) {
                    final SendEchoOutput sendEchoOutput = new SendEchoOutputBuilder()
                            .setData(input.getResult().getData())
                            .build();
                    rpcOutput = RpcResultBuilder.success(sendEchoOutput).build();
                } else {
                    rpcOutput = RpcResultBuilder.<SendEchoOutput>failed()
                            .withRpcErrors(input.getErrors())
                            .build();
                }
                return rpcOutput;
            }
        });
    }

}
