/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.FlowConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

final class FlowService<O extends DataObject> extends AbstractSimpleService<FlowModInputBuilder, O> {

    protected FlowService(final RequestContextStack requestContextStack, final DeviceContext deviceContext, final Class<O> clazz) {
        super(requestContextStack, deviceContext, clazz);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final FlowModInputBuilder input) {
        input.setXid(xid.getValue());
        return input.build();
    }

    List<FlowModInputBuilder> toFlowModInputs(final Flow input) {
        return FlowConvertor.toFlowModInputs(input, getVersion(), getDatapathId());
    }

    ListenableFuture<RpcResult<O>> processFlowModInputBuilders(final List<FlowModInputBuilder> ofFlowModInputs) {
        final List<ListenableFuture<RpcResult<O>>> partialFutures = new ArrayList<>(ofFlowModInputs.size());

        for (final FlowModInputBuilder flowModInputBuilder : ofFlowModInputs) {
            partialFutures.add(handleServiceCall(flowModInputBuilder));
        }

        final ListenableFuture<List<RpcResult<O>>> allFutures = Futures.successfulAsList(partialFutures);
        final SettableFuture<RpcResult<O>> finalFuture = SettableFuture.create();
        Futures.addCallback(allFutures, new FutureCallback<List<RpcResult<O>>>() {
            @Override
            public void onSuccess(final List<RpcResult<O>> results) {
                final ArrayList<RpcError> errors = new ArrayList<>();
                for (RpcResult<O> flowModResult : results) {
                    if (flowModResult == null) {
                        errors.add(RpcResultBuilder.newError(
                                RpcError.ErrorType.PROTOCOL, OFConstants.APPLICATION_TAG,
                                "unexpected flowMod result (null) occurred"));
                    } else if (!flowModResult.isSuccessful()) {
                        errors.addAll(flowModResult.getErrors());
                    }
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
