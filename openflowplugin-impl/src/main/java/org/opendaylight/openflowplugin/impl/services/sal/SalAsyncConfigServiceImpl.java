/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.sal;

import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.SalAsyncConfigService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.SetAsyncInput;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class SalAsyncConfigServiceImpl implements SalAsyncConfigService {

    private final SetAsyncConfigService setAsyncConfigService;
    private final GetAsyncConfigService getAsyncConfigService;

    public SalAsyncConfigServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
                                     final ConvertorExecutor convertorExecutor) {
        setAsyncConfigService = new SetAsyncConfigService(requestContextStack, deviceContext, convertorExecutor);
        getAsyncConfigService = new GetAsyncConfigService(requestContextStack, deviceContext, convertorExecutor);
    }

    @Override
    public Future<RpcResult<Void>> setAsync(SetAsyncInput input) {
        return setAsyncConfigService.handleServiceCall(input);
    }

    @Override
    public Future<RpcResult<GetAsyncOutput>> getAsync(GetAsyncInput input) {
        return getAsyncConfigService.handleAndReply(input);
    }
}
