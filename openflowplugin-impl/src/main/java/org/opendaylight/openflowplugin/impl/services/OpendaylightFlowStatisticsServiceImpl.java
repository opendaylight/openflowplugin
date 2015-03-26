/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchReactor;
import org.opendaylight.openflowplugin.openflow.md.util.FlowCreatorUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.OpendaylightFlowStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.Future;

/**
 * @author joe
 */
// TODO: implement this
public class OpendaylightFlowStatisticsServiceImpl extends CommonService implements OpendaylightFlowStatisticsService {


    private static final Logger LOG = LoggerFactory.getLogger(OpendaylightFlowStatisticsServiceImpl.class);

    @Override
    public Future<RpcResult<GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput>> getAggregateFlowStatisticsFromFlowTableForAllFlows(
            final GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput input) {

        final RequestContext requestContext = rpcContext.createRequestContext();
        final SettableFuture<RpcResult<GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput>> result = rpcContext.storeOrFail(requestContext);
        if (!result.isDone()) {
            final Xid xid = deviceContext.getNextXid();

            // Create multipart request body for fetch all the group stats
            MultipartRequestAggregateCaseBuilder multipartRequestAggregateCaseBuilder = new MultipartRequestAggregateCaseBuilder();
            MultipartRequestAggregateBuilder mprAggregateRequestBuilder = new MultipartRequestAggregateBuilder();
            mprAggregateRequestBuilder.setTableId(input.getTableId().getValue());
            mprAggregateRequestBuilder.setOutPort(OFConstants.OFPP_ANY);
            mprAggregateRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
            mprAggregateRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
            mprAggregateRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);

            FlowCreatorUtil.setWildcardedFlowMatch(version, mprAggregateRequestBuilder);

            // Set request body to main multipart request
            multipartRequestAggregateCaseBuilder.setMultipartRequestAggregate(mprAggregateRequestBuilder.build());
            MultipartRequestInputBuilder mprInput =
                    createMultipartHeader(MultipartType.OFPMPAGGREGATE, xid.getValue());
            mprInput.setMultipartRequestBody(multipartRequestAggregateCaseBuilder.build());

            Future<RpcResult<Void>> resultFromOFLib = deviceContext.getPrimaryConnectionContext()
                    .getConnectionAdapter().multipartRequest(mprInput.build());

            ListenableFuture<RpcResult<Void>> futureResultFromOfLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

            convertRpcResultToRequestFuture(requestContext, futureResultFromOfLib);

        }

        return result;
    }

    @Override
    public Future<RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>> getAggregateFlowStatisticsFromFlowTableForGivenMatch
            (
                    final GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput input) {
        final RequestContext requestContext = rpcContext.createRequestContext();
        final SettableFuture<RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>> result = rpcContext.storeOrFail(requestContext);
        if (!result.isDone()) {
            final Xid xid = deviceContext.getNextXid();
            MultipartRequestAggregateCaseBuilder multipartRequestAggregateCaseBuilder = new MultipartRequestAggregateCaseBuilder();
            MultipartRequestAggregateBuilder mprAggregateRequestBuilder = new MultipartRequestAggregateBuilder();
            mprAggregateRequestBuilder.setTableId(input.getTableId());
            mprAggregateRequestBuilder.setOutPort(input.getOutPort().longValue());
            // TODO: repeating code
            if (version == OFConstants.OFP_VERSION_1_3) {
                mprAggregateRequestBuilder.setCookie(input.getCookie().getValue());
                mprAggregateRequestBuilder.setCookieMask(input.getCookieMask().getValue());
                mprAggregateRequestBuilder.setOutGroup(input.getOutGroup());
            } else {
                mprAggregateRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
                mprAggregateRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
                mprAggregateRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);
            }

            MatchReactor.getInstance().convert(input.getMatch(), version, mprAggregateRequestBuilder,
                    deviceContext.getPrimaryConnectionContext().getFeatures().getDatapathId());

            FlowCreatorUtil.setWildcardedFlowMatch(version, mprAggregateRequestBuilder);

            // Set request body to main multipart request
            multipartRequestAggregateCaseBuilder.setMultipartRequestAggregate(mprAggregateRequestBuilder.build());
            MultipartRequestInputBuilder mprInput = createMultipartHeader(MultipartType.OFPMPAGGREGATE, xid.getValue());
            mprInput.setMultipartRequestBody(multipartRequestAggregateCaseBuilder.build());
            Future<RpcResult<Void>> resultFromOFLib = deviceContext.getPrimaryConnectionContext().getConnectionAdapter().multipartRequest(mprInput.build());
            ListenableFuture<RpcResult<Void>> futureResultFromOfLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

            convertRpcResultToRequestFuture(requestContext, futureResultFromOfLib);

        }
        return result;
    }

    @Override
    public Future<RpcResult<GetAllFlowStatisticsFromFlowTableOutput>> getAllFlowStatisticsFromFlowTable(
            final GetAllFlowStatisticsFromFlowTableInput input) {

        final RequestContext requestContext = rpcContext.createRequestContext();
        final SettableFuture<RpcResult<GetAllFlowStatisticsFromFlowTableOutput>> result = rpcContext.storeOrFail(requestContext);
        if (!result.isDone()) {

            final Xid xid = deviceContext.getNextXid();

            MultipartRequestFlowBuilder mprFlowRequestBuilder = new MultipartRequestFlowBuilder();
            mprFlowRequestBuilder.setTableId(input.getTableId().getValue());
            mprFlowRequestBuilder.setOutPort(OFConstants.OFPP_ANY);
            mprFlowRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
            mprFlowRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
            mprFlowRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);
            FlowCreatorUtil.setWildcardedFlowMatch(version, mprFlowRequestBuilder);

            MultipartRequestFlowCaseBuilder multipartRequestFlowCaseBuilder = new MultipartRequestFlowCaseBuilder();
            multipartRequestFlowCaseBuilder.setMultipartRequestFlow(mprFlowRequestBuilder.build());

            MultipartRequestInputBuilder mprInput =
                    createMultipartHeader(MultipartType.OFPMPFLOW, xid.getValue());
            mprInput.setMultipartRequestBody(multipartRequestFlowCaseBuilder.build());
            Future<RpcResult<Void>> resultFromOFLib = deviceContext.getPrimaryConnectionContext().getConnectionAdapter().multipartRequest(mprInput.build());
            ListenableFuture<RpcResult<Void>> futureResultFromOfLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

            convertRpcResultToRequestFuture(requestContext, futureResultFromOfLib);

        }
        return result;
    }

    @Override
    public Future<RpcResult<GetAllFlowsStatisticsFromAllFlowTablesOutput>> getAllFlowsStatisticsFromAllFlowTables(
            final GetAllFlowsStatisticsFromAllFlowTablesInput input) {

        final RequestContext requestContext = rpcContext.createRequestContext();
        final SettableFuture<RpcResult<GetAllFlowsStatisticsFromAllFlowTablesOutput>> result = rpcContext.storeOrFail(requestContext);
        if (!result.isDone()) {

            final Xid xid = deviceContext.getNextXid();

            MultipartRequestFlowCaseBuilder multipartRequestFlowCaseBuilder =
                    new MultipartRequestFlowCaseBuilder();
            MultipartRequestFlowBuilder mprFlowRequestBuilder =
                    new MultipartRequestFlowBuilder();
            mprFlowRequestBuilder.setTableId(OFConstants.OFPTT_ALL);
            mprFlowRequestBuilder.setOutPort(OFConstants.OFPP_ANY);
            mprFlowRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
            mprFlowRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
            mprFlowRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);
            FlowCreatorUtil.setWildcardedFlowMatch(version, mprFlowRequestBuilder);

            MultipartRequestInputBuilder mprInput =
                    createMultipartHeader(MultipartType.OFPMPFLOW, xid.getValue());
            multipartRequestFlowCaseBuilder.setMultipartRequestFlow(mprFlowRequestBuilder.build());
            mprInput.setMultipartRequestBody(multipartRequestFlowCaseBuilder.build());
            Future<RpcResult<Void>> resultFromOFLib = deviceContext.getPrimaryConnectionContext().getConnectionAdapter().multipartRequest(mprInput.build());
            ListenableFuture<RpcResult<Void>> futureResultFromOfLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

            convertRpcResultToRequestFuture(requestContext, futureResultFromOfLib);
        }
        return result;
    }

    @Override
    public Future<RpcResult<GetFlowStatisticsFromFlowTableOutput>> getFlowStatisticsFromFlowTable(
            final GetFlowStatisticsFromFlowTableInput input) {
        final RequestContext requestContext = rpcContext.createRequestContext();
        final SettableFuture<RpcResult<GetFlowStatisticsFromFlowTableOutput>> result = rpcContext.storeOrFail(requestContext);
        if (!result.isDone()) {
            final Xid xid = deviceContext.getNextXid();

            MultipartRequestFlowCaseBuilder multipartRequestFlowCaseBuilder = new MultipartRequestFlowCaseBuilder();
            MultipartRequestFlowBuilder mprFlowRequestBuilder = new MultipartRequestFlowBuilder();
            mprFlowRequestBuilder.setTableId(input.getTableId());

            if (input.getOutPort() != null) {
                mprFlowRequestBuilder.setOutPort(input.getOutPort().longValue());
            } else {
                mprFlowRequestBuilder.setOutPort(OFConstants.OFPP_ANY);
            }

            if (input.getOutGroup() != null) {
                mprFlowRequestBuilder.setOutGroup(input.getOutGroup());
            } else {
                mprFlowRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
            }

            if (input.getCookie() != null) {
                mprFlowRequestBuilder.setCookie(input.getCookie().getValue());
            } else {
                mprFlowRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
            }

            if (input.getCookieMask() != null) {
                mprFlowRequestBuilder.setCookieMask(input.getCookieMask().getValue());
            } else {
                mprFlowRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);
            }

            // convert and inject match
            MatchReactor.getInstance().convert(input.getMatch(), version, mprFlowRequestBuilder,
                    deviceContext.getPrimaryConnectionContext().getFeatures().getDatapathId());

            // Set request body to main multipart request
            multipartRequestFlowCaseBuilder.setMultipartRequestFlow(mprFlowRequestBuilder.build());
            MultipartRequestInputBuilder mprInput =
                    createMultipartHeader(MultipartType.OFPMPFLOW, xid.getValue());
            mprInput.setMultipartRequestBody(multipartRequestFlowCaseBuilder.build());
            Future<RpcResult<Void>> resultFromOFLib = deviceContext.getPrimaryConnectionContext().getConnectionAdapter().multipartRequest(mprInput.build());
            ListenableFuture<RpcResult<Void>> futureResultFromOfLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

            convertRpcResultToRequestFuture(requestContext, futureResultFromOfLib);

        }
        return result;
    }

    private void convertRpcResultToRequestFuture(final RequestContext requestContext, final ListenableFuture<RpcResult<Void>> futureResultFromOfLib) {
        RpcResultConvertor<SetConfigOutput> rpcResultConvertor = new RpcResultConvertor<>(requestContext);
        rpcResultConvertor.processResultFromOfJava(futureResultFromOfLib, getWaitTime());
    }

    private MultipartRequestInputBuilder createMultipartHeader(MultipartType multipart,
                                                               Long xid) {
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(multipart);
        mprInput.setVersion(version);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));
        return mprInput;
    }


}
