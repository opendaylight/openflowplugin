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
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.services.AbstractTableMultipartService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.multipart.reply.multipart.reply.body.MultipartReplyTableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.multipart.request.multipart.request.body.MultipartRequestTableFeaturesBuilder;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleLayerTableMultipartService extends AbstractTableMultipartService<MultipartReply> {

    private static final Logger LOG = LoggerFactory.getLogger(SingleLayerTableMultipartService.class);

    public SingleLayerTableMultipartService(final RequestContextStack requestContextStack,
                                            final DeviceContext deviceContext,
                                            final MultipartWriterProvider multipartWriterProvider) {
        super(requestContextStack, deviceContext, multipartWriterProvider);
    }


    @Override
    protected OfHeader buildRequest(final Xid xid, final UpdateTableInput input) {
        return new MultipartRequestBuilder()
                .setXid(xid.getValue())
                .setVersion(getVersion())
                .setRequestMore(false)
                .setMultipartRequestBody(new MultipartRequestTableFeaturesBuilder(input.getUpdatedTable())
                        .build())
                .build();
    }

    @Override
    public ListenableFuture<RpcResult<UpdateTableOutput>> handleAndReply(final UpdateTableInput input) {
        final SettableFuture<RpcResult<UpdateTableOutput>> finalFuture = SettableFuture.create();

        Futures.addCallback(handleServiceCall(input), new FutureCallback<RpcResult<List<MultipartReply>>>() {
            @Override
            @SuppressWarnings("checkstyle:IllegalCatch")
            public void onSuccess(final RpcResult<List<MultipartReply>> result) {
                if (result.isSuccessful()) {
                    final List<MultipartReply> multipartReplies = result.getResult();
                    if (multipartReplies.isEmpty()) {
                        LOG.debug("Multipart reply to table features request shouldn't be empty list.");
                        finalFuture.set(RpcResultBuilder.<UpdateTableOutput>failed()
                            .withError(ErrorType.RPC, "Multipart reply list is empty.").build());
                    } else {
                        finalFuture.set(RpcResultBuilder
                            .success(new UpdateTableOutputBuilder()
                                .setTransactionId(
                                        new TransactionId(Uint64.valueOf(multipartReplies.get(0).getXid())))
                                .build())
                            .build());

                        try {
                            storeStatistics(multipartReplies
                                .stream()
                                .map(MultipartReply::getMultipartReplyBody)
                                .filter(MultipartReplyTableFeatures.class::isInstance)
                                .flatMap(multipartReplyBody -> ((MultipartReplyTableFeatures) multipartReplyBody)
                                    .nonnullTableFeatures().values()
                                    .stream())
                                .collect(BindingMap.toOrderedMap()));
                        } catch (Exception e) {
                            LOG.warn("Not able to write to operational datastore: {}", e.getMessage());
                        }
                    }
                } else {
                    LOG.debug("OnSuccess, rpc result unsuccessful,"
                            + " multipart response for rpc update-table was unsuccessful.");
                    finalFuture.set(RpcResultBuilder.<UpdateTableOutput>failed().withRpcErrors(result.getErrors())
                        .build());
                }
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.error("Failure multipart response for table features request", throwable);
                finalFuture.set(RpcResultBuilder.<UpdateTableOutput>failed()
                    .withError(ErrorType.RPC, "Future error", throwable).build());
            }
        }, MoreExecutors.directExecutor());

        return finalFuture;
    }
}
