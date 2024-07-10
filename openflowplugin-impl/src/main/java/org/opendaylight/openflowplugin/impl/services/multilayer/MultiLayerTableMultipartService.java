/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.multilayer;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.services.AbstractTableMultipartService;
import org.opendaylight.openflowplugin.impl.services.util.RequestInputUtils;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.MultipartReplyTableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.MultipartRequestTableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiLayerTableMultipartService extends AbstractTableMultipartService<MultipartReply> {

    private static final Logger LOG = LoggerFactory.getLogger(MultiLayerTableMultipartService.class);
    private final VersionConvertorData data;
    private final ConvertorExecutor convertorExecutor;

    public MultiLayerTableMultipartService(final RequestContextStack requestContextStack,
                                           final DeviceContext deviceContext,
                                           final ConvertorExecutor convertorExecutor,
                                           final MultipartWriterProvider multipartWriterProvider) {
        super(requestContextStack, deviceContext, multipartWriterProvider);
        this.convertorExecutor = convertorExecutor;
        data = new VersionConvertorData(getVersion());
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final UpdateTableInput input) {
        final Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart
                .request.multipart.request.body.multipart.request.table.features._case.multipart.request
                .table.features.TableFeatures>> tableFeatures =
                convertorExecutor.convert(input.getUpdatedTable(), data);

        return RequestInputUtils.createMultipartHeader(MultipartType.OFPMPTABLEFEATURES, xid.getValue(), getVersion())
                .setMultipartRequestBody(new MultipartRequestTableFeaturesCaseBuilder()
                        .setMultipartRequestTableFeatures(new MultipartRequestTableFeaturesBuilder()
                                .setTableFeatures(tableFeatures
                                        .orElseGet(Collections::emptyList))
                                .build())
                        .build())
                .build();
    }

    @Override
    public ListenableFuture<RpcResult<UpdateTableOutput>> handleAndReply(final UpdateTableInput input) {
        final ListenableFuture<RpcResult<List<MultipartReply>>> multipartFuture = handleServiceCall(input);
        final SettableFuture<RpcResult<UpdateTableOutput>> finalFuture = SettableFuture.create();

        class CallBackImpl implements FutureCallback<RpcResult<List<MultipartReply>>> {
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
                        final Uint32 xid = multipartReplies.get(0).getXid();
                        LOG.debug(
                            "OnSuccess, rpc result successful,"
                                    + " multipart response for rpc update-table with xid {} obtained.",
                            xid);
                        final UpdateTableOutputBuilder updateTableOutputBuilder = new UpdateTableOutputBuilder();
                        updateTableOutputBuilder.setTransactionId(new TransactionId(Uint64.valueOf(xid)));
                        finalFuture.set(RpcResultBuilder.success(updateTableOutputBuilder.build()).build());
                        try {
                            storeStatistics(convertToSalTableFeatures(multipartReplies));
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
        }

        Futures.addCallback(multipartFuture, new CallBackImpl(), MoreExecutors.directExecutor());

        return finalFuture;
    }

    protected Map<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesKey,
                  org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures>
                        convertToSalTableFeatures(final List<MultipartReply> multipartReplies) {
        final var salTableFeaturesAll = BindingMap.<
            org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesKey,
            org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures>
                orderedBuilder();
        for (final MultipartReply multipartReply : multipartReplies) {
            if (multipartReply.getType().equals(MultipartType.OFPMPTABLEFEATURES)) {
                final MultipartReplyBody multipartReplyBody = multipartReply.getMultipartReplyBody();
                if (multipartReplyBody instanceof MultipartReplyTableFeaturesCase) {
                    final MultipartReplyTableFeaturesCase tableFeaturesCase =
                            (MultipartReplyTableFeaturesCase) multipartReplyBody;
                    final MultipartReplyTableFeatures salTableFeatures = tableFeaturesCase
                            .getMultipartReplyTableFeatures();

                    final Optional<List<TableFeatures>> salTableFeaturesPartial =
                            convertorExecutor.convert(salTableFeatures, data);

                    salTableFeaturesPartial.ifPresent(salTableFeaturesAll::addAll);

                    LOG.debug("TableFeature {} for xid {}.", salTableFeatures, multipartReply.getXid());
                }
            }
        }

        return salTableFeaturesAll.build();
    }
}
