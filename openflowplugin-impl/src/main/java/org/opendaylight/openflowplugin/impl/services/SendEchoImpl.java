/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SendEcho;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SendEchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SendEchoOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SendEchoOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public final class SendEchoImpl extends AbstractSimpleService<EchoInputBuilder, EchoOutput> implements SendEcho {
    public SendEchoImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext, EchoOutput.class);
    }

    @Override
    public ListenableFuture<RpcResult<SendEchoOutput>> invoke(final SendEchoInput sendEchoInput) {
        return Futures.transform(handleServiceCall(new EchoInputBuilder().setData(sendEchoInput.getData())), input -> {
            requireNonNull(input, "echoOutput value is never expected to be NULL");
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
        }, MoreExecutors.directExecutor());
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final EchoInputBuilder input) {
        return input
                .setXid(xid.getValue())
                .setVersion(getVersion())
                .build();
    }
}
