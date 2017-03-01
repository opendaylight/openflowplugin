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
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.services.AbstractTableMultipartService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.multipart.reply.multipart.reply.body.MultipartReplyTableFeatures;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleLayerTableMultipartService extends AbstractTableMultipartService<MultipartReply> {

    private static final Logger LOG = LoggerFactory.getLogger(SingleLayerTableMultipartService.class);

    public SingleLayerTableMultipartService(RequestContextStack requestContextStack,
                                            DeviceContext deviceContext,
                                            ConvertorExecutor convertorExecutor,
                                            MultipartWriterProvider multipartWriterProvider) {
        super(requestContextStack, deviceContext, convertorExecutor, multipartWriterProvider);
    }

    @Override
    public Future<RpcResult<UpdateTableOutput>> handleAndReply(UpdateTableInput input) {
        final SettableFuture<RpcResult<UpdateTableOutput>> finalFuture = SettableFuture.create();

        Futures.addCallback(handleServiceCall(input), new FutureCallback<RpcResult<List<MultipartReply>>>() {
            @Override
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
                                .setTransactionId(new TransactionId(BigInteger.valueOf(multipartReplies.get(0).getXid())))
                                .build())
                            .build());

                        try {
                            storeStatistics(multipartReplies
                                .stream()
                                .map(MultipartReply::getMultipartReplyBody)
                                .filter(MultipartReplyTableFeatures.class::isInstance)
                                .flatMap(multipartReplyBody -> MultipartReplyTableFeatures.class
                                    .cast(multipartReplyBody)
                                    .getTableFeatures()
                                    .stream())
                                .collect(Collectors.toList()));
                        } catch (Exception e) {
                            LOG.warn("Not able to write to operational datastore: {}", e.getMessage());
                        }
                    }
                } else {
                    LOG.debug("OnSuccess, rpc result unsuccessful, multipart response for rpc update-table was unsuccessful.");
                    finalFuture.set(RpcResultBuilder.<UpdateTableOutput>failed().withRpcErrors(result.getErrors())
                        .build());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                LOG.error("Failure multipart response for table features request. Exception: {}", t);
                finalFuture.set(RpcResultBuilder.<UpdateTableOutput>failed()
                    .withError(ErrorType.RPC, "Future error", t).build());
            }
        });

        return finalFuture;
    }

}
