/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.singlelayer;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.services.sal.AbstractUpdateFlow;
import org.opendaylight.openflowplugin.impl.util.FlowCreatorUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public final class SingleUpdateFlow extends AbstractUpdateFlow {
    private final SingleLayerFlowService<UpdateFlowOutput> service;

    public SingleUpdateFlow(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(deviceContext);
        service = new SingleLayerFlowService<>(requestContextStack, deviceContext, UpdateFlowOutput.class);
    }

    @Override
    protected ListenableFuture<RpcResult<UpdateFlowOutput>> invokeImpl(final UpdateFlowInput input) {
        final var updated = input.getUpdatedFlow();
        final var original = input.getOriginalFlow();

        if (FlowCreatorUtil.canModifyFlow(original, updated, version())) {
            return service.handleServiceCall(updated);
        }

        final var objectSettableFuture = SettableFuture.<RpcResult<UpdateFlowOutput>>create();
        final var listListenableFuture = Futures.successfulAsList(
            service.handleServiceCall(input.getOriginalFlow()),
            service.handleServiceCall(input.getUpdatedFlow()));

        Futures.addCallback(listListenableFuture, new FutureCallback<>() {
            @Override
            public void onSuccess(final List<RpcResult<UpdateFlowOutput>> results) {
                final var errors = new ArrayList<RpcError>();
                for (var flowModResult : results) {
                    if (flowModResult == null) {
                        errors.add(RpcResultBuilder.newError(
                            ErrorType.PROTOCOL, OFConstants.APPLICATION_TAG,
                            "unexpected flowMod result (null) occurred"));
                    } else if (!flowModResult.isSuccessful()) {
                        errors.addAll(flowModResult.getErrors());
                    }
                }

                final var rpcResultBuilder = errors.isEmpty() ? RpcResultBuilder.<UpdateFlowOutput>success()
                    : RpcResultBuilder.<UpdateFlowOutput>failed().withRpcErrors(errors);

                objectSettableFuture.set(rpcResultBuilder.build());
            }

            @Override
            public void onFailure(final Throwable throwable) {
                objectSettableFuture.set(RpcResultBuilder.<UpdateFlowOutput>failed().build());
            }
        }, MoreExecutors.directExecutor());

        return objectSettableFuture;
    }
}
