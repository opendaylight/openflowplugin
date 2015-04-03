/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import org.opendaylight.openflowplugin.api.openflow.device.Xid;

import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.TableFeaturesConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.MultipartRequestTableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.multipart.request.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author joe
 */
public class SalTableServiceImpl extends CommonService implements SalTableService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SalTableServiceImpl.class);

    @Override
    public Future<RpcResult<UpdateTableOutput>> updateTable(final UpdateTableInput input) {
        class FunctionImpl implements Function<DataCrate<UpdateTableOutput>,ListenableFuture<RpcResult<UpdateTableOutput>>> {

            @Override
            public ListenableFuture<RpcResult<UpdateTableOutput>> apply(final DataCrate<UpdateTableOutput> data) {

                final SettableFuture<RpcResult<UpdateTableOutput>> result = SettableFuture.create();


                final MultipartRequestTableFeaturesCaseBuilder caseBuilder = new MultipartRequestTableFeaturesCaseBuilder();
                final MultipartRequestTableFeaturesBuilder requestBuilder = new MultipartRequestTableFeaturesBuilder();
                final List<TableFeatures> ofTableFeatureList = TableFeaturesConvertor.toTableFeaturesRequest(input
                        .getUpdatedTable());
                requestBuilder.setTableFeatures(ofTableFeatureList);
                caseBuilder.setMultipartRequestTableFeatures(requestBuilder.build());

                // Set request body to main multipart request
                final Xid xid = deviceContext.getNextXid();
                data.getRequestContext().setXid(xid);
                final MultipartRequestInputBuilder mprInput = createMultipartHeader(MultipartType.OFPMPTABLEFEATURES,
                        xid.getValue());
                mprInput.setMultipartRequestBody(caseBuilder.build());

                final Future<RpcResult<Void>> resultFromOFLib = provideConnectionAdapter(PRIMARY_CONNECTION)
                        .multipartRequest(mprInput.build());
                final ListenableFuture<RpcResult<Void>> resultLib = JdkFutureAdapters
                        .listenInPoolThread(resultFromOFLib);

                Futures.addCallback(resultLib, new ResultCallback<UpdateTableOutput>(result) {
                    @Override
                    public UpdateTableOutput createResult() {
                        final UpdateTableOutputBuilder queueStatsFromPortBuilder = new UpdateTableOutputBuilder()
                                .setTransactionId(new TransactionId(BigInteger.valueOf(xid.getValue())));
                        return queueStatsFromPortBuilder.build();
                    }
                });

                return result;
            }
        }

        return this.<UpdateTableOutput, UpdateTableOutput>handleServiceCall( PRIMARY_CONNECTION,
                 new FunctionImpl());
    }

    private MultipartRequestInputBuilder createMultipartHeader(final MultipartType multipart, final Long xid) {
        final MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(multipart);
        mprInput.setVersion(version);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));
        return mprInput;
    }

    private abstract static class ResultCallback<T> implements FutureCallback<RpcResult<Void>> {

        private final SettableFuture<RpcResult<T>> result;

        /**
         * @param result
         */
        public ResultCallback(final SettableFuture<RpcResult<T>> result) {
            this.result = result;
        }

        public abstract T createResult();

        @Override
        public void onSuccess(final RpcResult<Void> resultArg) {
            result.set(RpcResultBuilder.success(createResult()).build());
        }

        @Override
        public void onFailure(final Throwable t) {
            result.set(RpcResultBuilder
                    .<T>failed()
                    .withWarning(ErrorType.RPC, OFConstants.ERROR_TAG_TIMEOUT, "something wrong happened",
                            OFConstants.APPLICATION_TAG, "", t).build());
        }
    }

}
