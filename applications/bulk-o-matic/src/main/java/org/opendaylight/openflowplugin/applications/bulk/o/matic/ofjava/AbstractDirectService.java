/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.bulk.o.matic.ofjava;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.openflowjava.protocol.api.connection.DeviceRequestFailedException;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Error;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractDirectService<I, O> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDirectService.class);

    private final OutboundQueue outboundQueue;

    AbstractDirectService(final OutboundQueue outboundQueue) {
        this.outboundQueue = outboundQueue;
    }

    protected abstract OfHeader buildRequest(Xid xid, I input);

    public ListenableFuture<RpcResult<O>> handleServiceCall(@Nonnull final I input) {
        Preconditions.checkNotNull(input);

        final Class<?> requestType;
        if (input instanceof DataContainer) {
            requestType = ((DataContainer) input).getImplementedInterface();
        } else {
            requestType = input.getClass();
        }

        LOG.trace("Handling general service call");
        final Xid xid = new Xid(outboundQueue.reserveEntry());

        final SettableFuture<RpcResult<O>> future = SettableFuture.create();

        if (Objects.isNull(xid.getValue())) {
            future.set(fail("Outbound queue wasn't able to reserve XID."));
            return future;
        }

        OfHeader request = null;

        try {
            request = buildRequest(xid, input);
            Verify.verify(xid.getValue().equals(request.getXid()), "Expected XID %s got %s", xid.getValue(), request.getXid());
        } catch (Exception e) {
            LOG.error("Failed to build request for {}, forfeiting request {}", input, xid.getValue(), e);
            future.set(fail("failed to build request input: " + e.getMessage()));
        } finally {
            outboundQueue.commitEntry(xid.getValue(), request, new FutureCallback<OfHeader>() {
                @Override
                public void onSuccess(@Nullable OfHeader result) {
                    if (result == null) {
                        future.set(RpcResultBuilder.<O>success().build());
                        return;
                    }

                    if (!requestType.isInstance(result)) {
                        LOG.info("Expected response type {}, got {}, result is empty", requestType, result.getClass());
                        future.set(RpcResultBuilder.<O>success().build());
                    } else{
                        future.set(RpcResultBuilder.success((O) requestType.cast(result)).build());
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    final RpcResultBuilder<O> builder;

                    if (t instanceof DeviceRequestFailedException) {
                        final Error err = ((DeviceRequestFailedException) t).getError();
                        final String errorString = String.format("Device reported error type %s code %s", err.getTypeString(), err.getCodeString());

                        builder = RpcResultBuilder.<O>failed().withError(RpcError.ErrorType.APPLICATION, errorString, t);
                    } else {
                        builder = RpcResultBuilder.<O>failed().withError(RpcError.ErrorType.APPLICATION, t.getMessage(), t);
                    }

                    future.set(builder.build());
                }
            });
        }

        return future;
    }

    private RpcResult<O> fail(String s) {
        return  RpcResultBuilder.<O>failed().withError(RpcError.ErrorType.APPLICATION, "", s).build();
    }
}
