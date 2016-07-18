/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.concurrent.Future;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.exception.ConversionException;
import org.opendaylight.openflowplugin.impl.services.AbstractMultipartService;
import org.opendaylight.openflowplugin.impl.services.RequestInputUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.StoreStatsGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.MultipartRequestBody;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * The abstract direct statistics service.
 * This abstract service provides wrappers and tools for all other derived statistics services.
 *
 * @param <I> the input type parameter
 * @param <O> the output type parameter
 */
public abstract class AbstractDirectStatisticsService<I extends StoreStatsGrouping, O> extends AbstractMultipartService<I> {

    private final Function<RpcResult<List<MultipartReply>>, RpcResult<O>> resultTransformFunction =
            new Function<RpcResult<List<MultipartReply>>, RpcResult<O>>() {
                @Nullable
                @Override
                public RpcResult<O> apply(@Nullable RpcResult<List<MultipartReply>> input) {
                    Preconditions.checkNotNull(input);
                    final O reply = buildReply(input.getResult(), input.isSuccessful());
                    return RpcResultBuilder.success(reply).build();
                }
            };

    private final AsyncFunction<RpcResult<O>, RpcResult<O>> resultStoreFunction =
            new AsyncFunction<RpcResult<O>, RpcResult<O>>() {
                @Nullable
                @Override
                public ListenableFuture<RpcResult<O>> apply(@Nullable RpcResult<O> input) throws Exception {
                    Preconditions.checkNotNull(input);

                    if (input.isSuccessful()) {
                        storeStatistics(input.getResult());
                        getTxFacade().submitTransaction(); // TODO: If submitTransaction will ever return future, chain it
                    }

                    return Futures.immediateFuture(input);
                }
            };

    private final MultipartType multipartType;
    private final OpenflowVersion ofVersion = OpenflowVersion.get(getVersion());

    /**
     * Instantiates a new Abstract direct statistics service.
     *
     * @param multipartType       the multipart type
     * @param requestContextStack the request context stack
     * @param deviceContext       the device context
     */
    protected AbstractDirectStatisticsService(MultipartType multipartType, RequestContextStack requestContextStack, DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
        this.multipartType = multipartType;
    }

    /**
     * Handle input and reply future.
     *
     * @param input the input
     * @return the future
     */
    public Future<RpcResult<O>> handleAndReply(final I input) {
        final ListenableFuture<RpcResult<List<MultipartReply>>> rpcReply = handleServiceCall(input);
        ListenableFuture<RpcResult<O>> rpcResult = Futures.transform(rpcReply, resultTransformFunction);

        if (Boolean.TRUE.equals(input.isStoreStats())) {
            rpcResult = Futures.transform(rpcResult, resultStoreFunction);
        }

        return rpcResult;
    }

    @Override
    protected OfHeader buildRequest(Xid xid, I input) throws ConversionException {
        return RequestInputUtils.createMultipartHeader(multipartType, xid.getValue(), getVersion())
                .setMultipartRequestBody(buildRequestBody(input))
                .build();
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
    protected abstract O buildReply(List<MultipartReply> input, boolean success);

    /**
     * Store statistics.
     * TODO: Remove dependency on deviceContext from derived methods
     * TODO: Return future, so we will be able to chain it
     *
     * @param output the output
     * @throws Exception the exception
     */
    protected abstract void storeStatistics(O output) throws Exception;
}
