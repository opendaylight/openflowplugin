/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import java.math.BigInteger;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutputBuilder;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.TableFeaturesConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.MultipartRequestTableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.multipart.request.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;

public class SalTableServiceImpl extends CommonService implements SalTableService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SalTableServiceImpl.class);

    public SalTableServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    @Override
    public Future<RpcResult<UpdateTableOutput>> updateTable(final UpdateTableInput input) {
        class FunctionImpl implements Function<DataCrate<List<MultipartReply>>,ListenableFuture<RpcResult<List<MultipartReply>>>> {

            @Override
            public ListenableFuture<RpcResult<List<MultipartReply>>> apply(final DataCrate<List<MultipartReply>> data) {
                messageSpy.spyMessage(input.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMITTED_SUCCESS);

                final SettableFuture<RpcResult<List<MultipartReply>>> result = SettableFuture.create();


                final MultipartRequestTableFeaturesCaseBuilder caseBuilder = new MultipartRequestTableFeaturesCaseBuilder();
                final MultipartRequestTableFeaturesBuilder requestBuilder = new MultipartRequestTableFeaturesBuilder();
                final List<TableFeatures> ofTableFeatureList = TableFeaturesConvertor.toTableFeaturesRequest(input
                        .getUpdatedTable());
                requestBuilder.setTableFeatures(ofTableFeatureList);
                caseBuilder.setMultipartRequestTableFeatures(requestBuilder.build());

                // Set request body to main multipart request
                final Xid xid = data.getRequestContext().getXid();
                deviceContext.getOpenflowMessageListenerFacade().registerMultipartXid(xid.getValue());
                final MultipartRequestInputBuilder mprInput = createMultipartHeader(MultipartType.OFPMPTABLEFEATURES,
                        xid.getValue());
                mprInput.setMultipartRequestBody(caseBuilder.build());

                final Future<RpcResult<Void>> resultFromOFLib = provideConnectionAdapter(PRIMARY_CONNECTION)
                        .multipartRequest(mprInput.build());
                final ListenableFuture<RpcResult<Void>> resultLib = JdkFutureAdapters
                        .listenInPoolThread(resultFromOFLib);

                return result;
            }
        }

        final ListenableFuture<RpcResult<List<MultipartReply>>> multipartFuture = handleServiceCall( PRIMARY_CONNECTION, new FunctionImpl());
        final SettableFuture<RpcResult<UpdateTableOutput>> finalFuture = SettableFuture.create();

        class CallBackImpl implements FutureCallback<RpcResult<List<MultipartReply>>> {
            private final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CallBackImpl.class);

            @Override
            public void onSuccess(RpcResult<List<MultipartReply>> result) {

                if (result.isSuccessful()) {
                    final List<MultipartReply> multipartReplies = result.getResult();
                    if (multipartReplies.isEmpty()) {
                        LOGGER.debug("Multipart reply to table features request shouldn't be empty list.");
                        finalFuture.set(RpcResultBuilder.<UpdateTableOutput>failed().withError(ErrorType.RPC, "Multipart reply list is empty.").build());
                    } else {
                        final Long xid = multipartReplies.get(0).getXid();
                        LOGGER.debug("OnSuccess, rpc result successful, multipart response for rpc update-table with xid {} obtained.",xid);
                        final UpdateTableOutputBuilder updateTableOutputBuilder = new UpdateTableOutputBuilder();
                        updateTableOutputBuilder.setTransactionId(new TransactionId(BigInteger.valueOf(xid)));
                        finalFuture.set(RpcResultBuilder.success(updateTableOutputBuilder.build()).build());
                    }
                    //TODO: output could contain more interesting things then only xid.
                    //(According to rfc output for table-update it is only xid)
//                    for (MultipartReply multipartReply : result.getResult()) {
//                        if (multipartReply.getType().equals(MultipartType.OFPMPTABLEFEATURES)) {
//                        }
//                    }
                } else {
                    LOGGER.debug("OnSuccess, rpc result unsuccessful, multipart response for rpc update-table was unsuccessful.");
                    finalFuture.set(RpcResultBuilder.<UpdateTableOutput>failed().withRpcErrors(result.getErrors()).build());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                LOGGER.debug("Failure multipart response for table features request. Exception: {}", t);
                finalFuture.set(RpcResultBuilder.<UpdateTableOutput>failed().withError(ErrorType.RPC, "Future error", t).build());
            }
        }

        Futures.addCallback(multipartFuture, new CallBackImpl());

        return finalFuture;
    }


    private MultipartRequestInputBuilder createMultipartHeader(final MultipartType multipart, final Long xid) {
        final MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(multipart);
        mprInput.setVersion(version);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));
        return mprInput;
    }

}
