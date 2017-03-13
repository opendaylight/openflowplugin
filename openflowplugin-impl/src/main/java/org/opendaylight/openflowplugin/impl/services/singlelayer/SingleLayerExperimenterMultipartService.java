/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.singlelayer;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.services.AbstractExperimenterMultipartService;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SendExperimenterMpRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SendExperimenterMpRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SendExperimenterMpRequestOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.send.experimenter.mp.request.output.ExperimenterCoreMessageItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.multipart.reply.multipart.reply.body.MultipartReplyExperimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.multipart.request.multipart.request.body.MultipartRequestExperimenterBuilder;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;

public class SingleLayerExperimenterMultipartService extends AbstractExperimenterMultipartService<MultipartReply> {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SingleLayerExperimenterMultipartService.class);

    public SingleLayerExperimenterMultipartService(RequestContextStack requestContextStack, DeviceContext deviceContext,
                                                   ExtensionConverterProvider extensionConverterProvider) {
        super(requestContextStack, deviceContext, extensionConverterProvider);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final SendExperimenterMpRequestInput input) throws ServiceException {
        return new MultipartRequestBuilder()
            .setXid(xid.getValue())
            .setVersion(getVersion())
            .setRequestMore(false)
            .setMultipartRequestBody(new MultipartRequestExperimenterBuilder(input).build())
            .build();
    }

    @Override
    public Future<RpcResult<SendExperimenterMpRequestOutput>> handleAndReply(SendExperimenterMpRequestInput input) {
        final SettableFuture<RpcResult<SendExperimenterMpRequestOutput>> future = SettableFuture.create();

        Futures.addCallback(handleServiceCall(input), new FutureCallback<RpcResult<List<MultipartReply>>>() {
            @Override
            public void onSuccess(final RpcResult<List<MultipartReply>> result) {
                if (result.isSuccessful()) {
                    future.set(RpcResultBuilder
                        .success(new SendExperimenterMpRequestOutputBuilder()
                            .setExperimenterCoreMessageItem(result
                                .getResult()
                                .stream()
                                .map(MultipartReply::getMultipartReplyBody)
                                .filter(MultipartReplyExperimenter.class::isInstance)
                                .map(experimenter -> new ExperimenterCoreMessageItemBuilder()
                                    .setExperimenterMessageOfChoice(MultipartReplyExperimenter.class
                                        .cast(experimenter)
                                        .getExperimenterMessageOfChoice())
                                    .build())
                                .collect(Collectors.toList()))
                            .build())
                        .build());
                } else {
                    LOG.warn("OnSuccess, rpc result unsuccessful, multipart response for rpc sendExperimenterMpRequest was unsuccessful.");
                    future.set(RpcResultBuilder.<SendExperimenterMpRequestOutput>failed().withRpcErrors(result.getErrors()).build());
                }
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.warn("Failure multipart response for Experimenter-Mp request. Exception: {}", t);
                future.set(RpcResultBuilder.<SendExperimenterMpRequestOutput>failed().withError(ErrorType.RPC, "Future error", t).build());
            }
        });

        return future;
    }

}
