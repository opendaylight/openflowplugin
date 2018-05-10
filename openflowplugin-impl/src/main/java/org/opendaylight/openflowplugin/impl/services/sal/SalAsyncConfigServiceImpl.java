/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.sal;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Objects;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.services.singlelayer.SingleLayerGetAsyncConfigService;
import org.opendaylight.openflowplugin.impl.services.singlelayer.SingleLayerSetAsyncConfigService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsyncOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.SalAsyncConfigService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.SetAsyncOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class SalAsyncConfigServiceImpl implements SalAsyncConfigService {

    private final SingleLayerSetAsyncConfigService setAsyncConfigService;
    private final SingleLayerGetAsyncConfigService getAsyncConfigService;

    public SalAsyncConfigServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        setAsyncConfigService = new SingleLayerSetAsyncConfigService(requestContextStack, deviceContext);
        this.getAsyncConfigService = new SingleLayerGetAsyncConfigService(requestContextStack, deviceContext);
    }

    @Override
    public ListenableFuture<RpcResult<SetAsyncOutput>> setAsync(SetAsyncInput input) {
        return setAsyncConfigService.handleServiceCall(input);
    }

    @Override
    public ListenableFuture<RpcResult<GetAsyncOutput>> getAsync(GetAsyncInput input) {
        return Futures.transform(getAsyncConfigService.handleServiceCall(input), result ->
                Objects.nonNull(result) && result.isSuccessful()
                        ? RpcResultBuilder.success(new GetAsyncOutputBuilder(result.getResult())).build()
                        : RpcResultBuilder.<GetAsyncOutput>failed().build(),
                MoreExecutors.directExecutor());
    }
}
