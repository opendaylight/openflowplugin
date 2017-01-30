/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.impl.services.AbstractMultipartService;
import org.opendaylight.openflowplugin.impl.services.util.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.openflowplugin.impl.statistics.datastore.StatisticsWriterProvider;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.StoreStatsGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.MultipartRequestBody;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * The abstract direct statistics service.
 * This abstract service provides wrappers and tools for all other derived statistics services.
 *
 * @param <I> the input type parameter
 * @param <O> the output type parameter
 */
abstract class AbstractDirectStatisticsService<I extends StoreStatsGrouping, O extends DataContainer, T extends OfHeader>
        extends AbstractMultipartService<I, T> {

    private final Function<RpcResult<List<T>>, RpcResult<O>> resultTransformFunction =
            new Function<RpcResult<List<T>>, RpcResult<O>>() {
                @Nullable
                @Override
                public RpcResult<O> apply(@Nullable RpcResult<List<T>> input) {
                    return Preconditions.checkNotNull(input).isSuccessful()
                            ? RpcResultBuilder.success(buildReply(input.getResult(), input.isSuccessful())).build()
                            : RpcResultBuilder.<O>failed().build();
                }
            };

    private final Function<RpcResult<O>, RpcResult<O>> resultStoreFunction =
            new Function<RpcResult<O>, RpcResult<O>>() {
                @Nullable
                @Override
                public RpcResult<O> apply(@Nullable RpcResult<O> input)  {
                    Preconditions.checkNotNull(input);

                    if (input.isSuccessful()) {
                        statisticsWriterProvider
                            .lookup(multipartType)
                            .ifPresent(writer -> {
                                writer.write(input.getResult(), true);
                                getTxFacade().submitTransaction();
                            });
                    }

                    return input;
                }
            };

    private final MultipartType multipartType;
    private final OpenflowVersion ofVersion = OpenflowVersion.get(getVersion());
    private final ConvertorExecutor convertorExecutor;
    private final StatisticsWriterProvider statisticsWriterProvider;

    /**
     * Instantiates a new Abstract direct statistics service.
     *  @param multipartType           the multipart type
     * @param requestContextStack      the request context stack
     * @param deviceContext            the device context
     * @param convertorExecutor        convertor executor
     * @param statisticsWriterProvider statistics writer provider
     */
    AbstractDirectStatisticsService(final MultipartType multipartType,
                                    final RequestContextStack requestContextStack,
                                    final DeviceContext deviceContext,
                                    final ConvertorExecutor convertorExecutor,
                                    final StatisticsWriterProvider statisticsWriterProvider) {
        super(requestContextStack, deviceContext);
        this.multipartType = multipartType;
        this.convertorExecutor = convertorExecutor;
        this.statisticsWriterProvider = statisticsWriterProvider;
    }

    /**
     * Handle input and reply future.
     *
     * @param input the input
     * @return the future
     */
    Future<RpcResult<O>> handleAndReply(final I input) {
        final ListenableFuture<RpcResult<List<T>>> rpcReply = handleServiceCall(input);
        ListenableFuture<RpcResult<O>> rpcResult = Futures.transform(rpcReply, resultTransformFunction::apply);

        if (Boolean.TRUE.equals(input.isStoreStats())) {
            rpcResult = Futures.transform(rpcResult, resultStoreFunction::apply);
        }

        return rpcResult;
    }

    @Override
    protected OfHeader buildRequest(Xid xid, I input) throws ServiceException {
        return RequestInputUtils.createMultipartHeader(multipartType, xid.getValue(), getVersion())
                .setMultipartRequestBody(buildRequestBody(input))
                .build();
    }

    /**
     * Get convertor executor
     * @return convertor executor
     */
    protected ConvertorExecutor getConvertorExecutor() {
        return convertorExecutor;
    }

    /**
     * Gets openflow version.
     *
     * @return the openflow version
     */
    protected OpenflowVersion getOfVersion() {
        return ofVersion;
    }

    /**
     * Build multipart request body.
     *
     * @param input the input
     * @return the multipart request body
     */
    protected abstract MultipartRequestBody buildRequestBody(I input);

    /**
     * Build output from multipart reply input.
     *
     * @param input the input
     * @return the output
     */
    protected abstract O buildReply(List<T> input, boolean success);

}
