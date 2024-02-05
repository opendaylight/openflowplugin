/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.singlelayer;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.AbstractSimpleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.AsyncConfigMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsync;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsyncOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public final class GetAsyncImpl extends AbstractSimpleService<GetAsyncInput, AsyncConfigMessage> implements GetAsync{
    public GetAsyncImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext, AsyncConfigMessage.class);
    }

    @Override
    public ListenableFuture<RpcResult<GetAsyncOutput>> invoke(final GetAsyncInput input) {
        return Futures.transform(handleServiceCall(input), result ->
                result != null && result.isSuccessful()
                        ? RpcResultBuilder.success(new GetAsyncOutputBuilder(result.getResult()).build()).build()
                        : RpcResultBuilder.<GetAsyncOutput>failed().build(),
                MoreExecutors.directExecutor());
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetAsyncInput input) {
        return new GetAsyncInputBuilder().setVersion(getVersion()).setXid(xid.getValue()).build();
    }
}