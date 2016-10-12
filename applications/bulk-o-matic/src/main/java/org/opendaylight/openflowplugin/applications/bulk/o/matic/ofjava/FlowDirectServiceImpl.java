/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.bulk.o.matic.ofjava;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class FlowDirectServiceImpl<O extends DataObject> extends AbstractDirectService<FlowModInputBuilder, O> {
    FlowDirectServiceImpl(OutboundQueue outboundQueue) {
        super(outboundQueue);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final FlowModInputBuilder input) {
        input.setXid(xid.getValue());
        return input.build();
    }

    @Override
    public ListenableFuture<RpcResult<O>> handleServiceCall(@Nonnull final FlowModInputBuilder input) {
        final SettableFuture<RpcResult<O>> finalFuture = SettableFuture.create();

        Futures.addCallback(super.handleServiceCall(input), new FutureCallback<RpcResult<O>>() {
            @Override
            public void onSuccess(final RpcResult<O> result) {
                final ArrayList<RpcError> errors = new ArrayList<>();

                if (result == null) {
                    errors.add(RpcResultBuilder.newError(
                            RpcError.ErrorType.PROTOCOL, OFConstants.APPLICATION_TAG,
                            "unexpected flowMod result (null) occurred"));
                } else if (!result.isSuccessful()) {
                    errors.addAll(result.getErrors());
                }

                final RpcResultBuilder<O> rpcResultBuilder;

                if (errors.isEmpty()) {
                    rpcResultBuilder = RpcResultBuilder.success();
                } else {
                    rpcResultBuilder = RpcResultBuilder.<O>failed().withRpcErrors(errors);
                }

                finalFuture.set(rpcResultBuilder.build());
            }

            @Override
            public void onFailure(final Throwable t) {
                RpcResultBuilder<O> rpcResultBuilder = RpcResultBuilder.failed();
                finalFuture.set(rpcResultBuilder.build());
            }
        });

        return finalFuture;
    }
}
