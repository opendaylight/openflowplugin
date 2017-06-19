/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.sal;

import com.google.common.util.concurrent.Futures;
import java.util.Optional;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.AbstractSimpleService;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.AsyncConfigResponseConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class GetAsyncConfigService extends AbstractSimpleService<
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncInput,
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutput> {
    private final ConvertorExecutor convertorExecutor;
    private final VersionConvertorData data;

    public GetAsyncConfigService(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
                                 final ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext,
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutput.class);
        this.convertorExecutor = convertorExecutor;
        data = new VersionConvertorData(getVersion());
    }

    @Override
    protected OfHeader buildRequest(Xid xid, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731
            .GetAsyncInput input) throws ServiceException {
        return new GetAsyncInputBuilder()
                .setVersion(getVersion())
                .setXid(xid.getValue())
                .build();
    }

    public Future<RpcResult<GetAsyncOutput>> handleAndReply() {
        return Futures.transform(this.handleServiceCall(
                new GetAsyncInputBuilder().build()), (getAsyncReplyRpcResult) -> {
                if (getAsyncReplyRpcResult.isSuccessful()) {
                    final Optional<GetAsyncOutput> output =
                            convertorExecutor.convert(getAsyncReplyRpcResult.getResult(), data);

                    return RpcResultBuilder
                            .success(output.orElseGet(() -> AsyncConfigResponseConvertor.defaultResult()))
                            .build();
                }

                return RpcResultBuilder
                        .<GetAsyncOutput>failed()
                        .withRpcErrors(getAsyncReplyRpcResult.getErrors())
                        .build();
            });
    }
}