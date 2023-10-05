/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.services.singlelayer.SingleLayerGetAsyncConfigService;
import org.opendaylight.openflowplugin.impl.services.singlelayer.SingleLayerSetAsyncConfigService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsync;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsyncOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.SetAsync;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.SetAsyncOutput;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class SalAsyncConfigRpcs {

    private final SingleLayerSetAsyncConfigService setAsyncConfigService;
    private final SingleLayerGetAsyncConfigService getAsyncConfigService;

    public SalAsyncConfigRpcs(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        setAsyncConfigService = new SingleLayerSetAsyncConfigService(requestContextStack, deviceContext);
        getAsyncConfigService = new SingleLayerGetAsyncConfigService(requestContextStack, deviceContext);
    }

    @VisibleForTesting
    ListenableFuture<RpcResult<SetAsyncOutput>> setAsync(final SetAsyncInput input) {
        return setAsyncConfigService.handleServiceCall(input);
    }

    @VisibleForTesting
    ListenableFuture<RpcResult<GetAsyncOutput>> getAsync(final GetAsyncInput input) {
        return Futures.transform(getAsyncConfigService.handleServiceCall(input), result ->
                result != null && result.isSuccessful()
                        ? RpcResultBuilder.success(new GetAsyncOutputBuilder(result.getResult()).build()).build()
                        : RpcResultBuilder.<GetAsyncOutput>failed().build(),
                MoreExecutors.directExecutor());
    }

    public ClassToInstanceMap<Rpc<?,?>> getRpcClassToInstanceMap() {
        return ImmutableClassToInstanceMap.<Rpc<?, ?>>builder()
            .put(SetAsync.class, this::setAsync)
            .put(GetAsync.class, this::getAsync)
            .build();
    }
}
