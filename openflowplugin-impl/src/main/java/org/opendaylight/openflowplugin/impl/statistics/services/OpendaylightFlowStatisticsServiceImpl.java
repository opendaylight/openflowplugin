/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.impl.services.CommonService;
import org.opendaylight.openflowplugin.impl.services.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.util.StatisticsServiceUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchReactor;
import org.opendaylight.openflowplugin.openflow.md.util.FlowCreatorUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.OpendaylightFlowStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.get.aggregate.flow.statistics.from.flow.table._for.given.match.output.AggregatedFlowStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author joe
 */
public class OpendaylightFlowStatisticsServiceImpl extends CommonService implements OpendaylightFlowStatisticsService {

    private static final Logger LOG = LoggerFactory.getLogger(OpendaylightFlowStatisticsServiceImpl.class);

    public OpendaylightFlowStatisticsServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    @Override
    public Future<RpcResult<GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput>> getAggregateFlowStatisticsFromFlowTableForAllFlows(
            final GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput input) {


        return handleServiceCall(
                new Function<RequestContext<GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput>, 
                ListenableFuture<RpcResult<GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput>>>() {

                    @Override
                    public ListenableFuture<RpcResult<GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput>> apply(final RequestContext<GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput> requestContext) {

                        // Create multipart request body for fetch all the group stats
                        final MultipartRequestAggregateCaseBuilder multipartRequestAggregateCaseBuilder = new MultipartRequestAggregateCaseBuilder();
                        final MultipartRequestAggregateBuilder mprAggregateRequestBuilder = new MultipartRequestAggregateBuilder();
                        mprAggregateRequestBuilder.setTableId(input.getTableId().getValue());
                        mprAggregateRequestBuilder.setOutPort(OFConstants.OFPP_ANY);
                        mprAggregateRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
                        mprAggregateRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
                        mprAggregateRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);
                        final short version = getVersion();
                        FlowCreatorUtil.setWildcardedFlowMatch(version, mprAggregateRequestBuilder);

                        // Set request body to main multipart request
                        multipartRequestAggregateCaseBuilder.setMultipartRequestAggregate(mprAggregateRequestBuilder
                                .build());
                        final Xid xid = requestContext.getXid();
                        final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                                MultipartType.OFPMPAGGREGATE, xid.getValue(), version);

                        mprInput.setMultipartRequestBody(multipartRequestAggregateCaseBuilder.build());

                        return StatisticsServiceUtil.getRpcResultListenableFuture(xid, mprInput.build(), getDeviceContext());
                    }
                });

    }

    @Override
    public Future<RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>> getAggregateFlowStatisticsFromFlowTableForGivenMatch(
            final GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput input) {


        ListenableFuture<RpcResult<List<MultipartReply>>> rpcResultListenableFuture = handleServiceCall(
                new Function<RequestContext<List<MultipartReply>>, ListenableFuture<RpcResult<List<MultipartReply>>>>() {

                    @Override
                    public ListenableFuture<RpcResult<List<MultipartReply>>> apply(final RequestContext<List<MultipartReply>> requestContext) {
                        final Xid xid = requestContext.getXid();
                        final DeviceContext deviceContext = getDeviceContext();
                        final MultipartRequestAggregateCaseBuilder multipartRequestAggregateCaseBuilder = new MultipartRequestAggregateCaseBuilder();
                        final MultipartRequestAggregateBuilder mprAggregateRequestBuilder = new MultipartRequestAggregateBuilder();
                        final short tableId = MoreObjects.firstNonNull(input.getTableId(), OFConstants.OFPTT_ALL).shortValue();
                        mprAggregateRequestBuilder.setTableId(tableId);
                        long outputPortValue = MoreObjects.firstNonNull(input.getOutPort(), OFConstants.OFPP_ANY).longValue();
                        mprAggregateRequestBuilder.setOutPort(outputPortValue);
                        // TODO: repeating code

                        final short version = getVersion();
                        if (version == OFConstants.OFP_VERSION_1_3) {

                            if (input.getCookie() == null) {
                                mprAggregateRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
                            } else {
                                mprAggregateRequestBuilder.setCookie(MoreObjects.firstNonNull(input.getCookie().getValue(), OFConstants.DEFAULT_COOKIE));
                            }

                            if (input.getCookieMask() == null) {
                                mprAggregateRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);
                            } else {
                                mprAggregateRequestBuilder.setCookieMask(MoreObjects.firstNonNull(input.getCookieMask().getValue(), OFConstants.DEFAULT_COOKIE_MASK));
                            }
                            long outGroup = MoreObjects.firstNonNull(input.getOutGroup(), OFConstants.OFPG_ANY).longValue();
                            mprAggregateRequestBuilder.setOutGroup(outGroup);
                        } else {
                            mprAggregateRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
                            mprAggregateRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
                            mprAggregateRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);
                        }

                        MatchReactor.getInstance().convert(input.getMatch(), version, mprAggregateRequestBuilder,
                                deviceContext.getPrimaryConnectionContext().getFeatures().getDatapathId());

                        FlowCreatorUtil.setWildcardedFlowMatch(version, mprAggregateRequestBuilder);

                        // Set request body to main multipart request
                        multipartRequestAggregateCaseBuilder.setMultipartRequestAggregate(mprAggregateRequestBuilder
                                .build());

                        final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                                MultipartType.OFPMPAGGREGATE, xid.getValue(), version);

                        mprInput.setMultipartRequestBody(multipartRequestAggregateCaseBuilder.build());
                        return StatisticsServiceUtil.getRpcResultListenableFuture(xid, mprInput.build(), getDeviceContext());
                    }
                });

        return Futures.transform(rpcResultListenableFuture, new Function<RpcResult<List<MultipartReply>>, RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>>() {
            @Nullable
            @Override
            public RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput> apply(final RpcResult<List<MultipartReply>> input) {
                final DeviceContext deviceContext = getDeviceContext();
                TranslatorLibrary translatorLibrary = deviceContext.oook();
                RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput> rpcResult;
                if (input.isSuccessful()) {
                    MultipartReply reply = input.getResult().get(0);
                    final TranslatorKey translatorKey = new TranslatorKey(reply.getVersion(), MultipartReplyAggregateCase.class.getName());
                    final MessageTranslator<MultipartReply, AggregatedFlowStatistics> messageTranslator = translatorLibrary.lookupTranslator(translatorKey);
                    List<AggregatedFlowStatistics> aggregStats = new ArrayList<AggregatedFlowStatistics>();

                    for (MultipartReply multipartReply : input.getResult()) {
                        aggregStats.add(messageTranslator.translate(multipartReply, deviceContext, null));
                    }

                    GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder getAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder =
                            new GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder();
                    getAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder.setAggregatedFlowStatistics(aggregStats);

                    rpcResult = RpcResultBuilder
                            .<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>success()
                            .withResult(getAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder.build())
                            .build();

                } else {
                    rpcResult = RpcResultBuilder
                            .<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>failed()
                            .withRpcErrors(input.getErrors())
                            .build();
                }
                return rpcResult;
            }
        });

    }

    @Override
    public Future<RpcResult<GetAllFlowStatisticsFromFlowTableOutput>> getAllFlowStatisticsFromFlowTable(
            final GetAllFlowStatisticsFromFlowTableInput input) {

        return handleServiceCall(new Function<RequestContext<GetAllFlowStatisticsFromFlowTableOutput>,
                ListenableFuture<RpcResult<GetAllFlowStatisticsFromFlowTableOutput>>>() {

            @Override
            public ListenableFuture<RpcResult<GetAllFlowStatisticsFromFlowTableOutput>> apply(final RequestContext<GetAllFlowStatisticsFromFlowTableOutput> requestContext) {

                final MultipartRequestFlowBuilder mprFlowRequestBuilder = new MultipartRequestFlowBuilder();
                mprFlowRequestBuilder.setTableId(input.getTableId().getValue());
                mprFlowRequestBuilder.setOutPort(OFConstants.OFPP_ANY);
                mprFlowRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
                mprFlowRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
                mprFlowRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);

                final short version = getVersion();
                FlowCreatorUtil.setWildcardedFlowMatch(version, mprFlowRequestBuilder);

                final MultipartRequestFlowCaseBuilder multipartRequestFlowCaseBuilder = new MultipartRequestFlowCaseBuilder();
                multipartRequestFlowCaseBuilder.setMultipartRequestFlow(mprFlowRequestBuilder.build());

                final Xid xid = requestContext.getXid();
                final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                        MultipartType.OFPMPFLOW, xid.getValue(), version);

                mprInput.setMultipartRequestBody(multipartRequestFlowCaseBuilder.build());
                return StatisticsServiceUtil.getRpcResultListenableFuture(xid, mprInput.build(), getDeviceContext());
            }
        });
    }

    @Override
    public Future<RpcResult<GetAllFlowsStatisticsFromAllFlowTablesOutput>> getAllFlowsStatisticsFromAllFlowTables(
            final GetAllFlowsStatisticsFromAllFlowTablesInput input) {


        return handleServiceCall(new Function<RequestContext<GetAllFlowsStatisticsFromAllFlowTablesOutput>,
                ListenableFuture<RpcResult<GetAllFlowsStatisticsFromAllFlowTablesOutput>>>() {

            @Override
            public ListenableFuture<RpcResult<GetAllFlowsStatisticsFromAllFlowTablesOutput>> apply(final RequestContext<GetAllFlowsStatisticsFromAllFlowTablesOutput> requestContext) {

                final MultipartRequestFlowCaseBuilder multipartRequestFlowCaseBuilder = new MultipartRequestFlowCaseBuilder();
                final MultipartRequestFlowBuilder mprFlowRequestBuilder = new MultipartRequestFlowBuilder();
                mprFlowRequestBuilder.setTableId(OFConstants.OFPTT_ALL);
                mprFlowRequestBuilder.setOutPort(OFConstants.OFPP_ANY);
                mprFlowRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
                mprFlowRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
                mprFlowRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);
                final short version = getVersion();
                FlowCreatorUtil.setWildcardedFlowMatch(version, mprFlowRequestBuilder);

                final Xid xid = requestContext.getXid();
                final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                        MultipartType.OFPMPFLOW, xid.getValue(), version);

                multipartRequestFlowCaseBuilder.setMultipartRequestFlow(mprFlowRequestBuilder.build());
                mprInput.setMultipartRequestBody(multipartRequestFlowCaseBuilder.build());
                return StatisticsServiceUtil.getRpcResultListenableFuture(xid, mprInput.build(), getDeviceContext());
            }
        });
    }

    @Override
    public Future<RpcResult<GetFlowStatisticsFromFlowTableOutput>> getFlowStatisticsFromFlowTable(
            final GetFlowStatisticsFromFlowTableInput input) {


        return handleServiceCall(new Function<RequestContext<GetFlowStatisticsFromFlowTableOutput>, ListenableFuture<RpcResult<GetFlowStatisticsFromFlowTableOutput>>>() {

            @Override
            public ListenableFuture<RpcResult<GetFlowStatisticsFromFlowTableOutput>> apply(final RequestContext<GetFlowStatisticsFromFlowTableOutput> requestContext) {

                final MultipartRequestFlowCaseBuilder multipartRequestFlowCaseBuilder = new MultipartRequestFlowCaseBuilder();
                final MultipartRequestFlowBuilder mprFlowRequestBuilder = new MultipartRequestFlowBuilder();
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
                final short version = getVersion();
                final DeviceContext deviceContext = getDeviceContext();
                MatchReactor.getInstance().convert(input.getMatch(), version, mprFlowRequestBuilder,
                        deviceContext.getPrimaryConnectionContext().getFeatures().getDatapathId());

                // Set request body to main multipart request
                final Xid xid = requestContext.getXid();
                multipartRequestFlowCaseBuilder.setMultipartRequestFlow(mprFlowRequestBuilder.build());
                final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                        MultipartType.OFPMPFLOW, xid.getValue(), version);
                mprInput.setMultipartRequestBody(multipartRequestFlowCaseBuilder.build());
                return StatisticsServiceUtil.getRpcResultListenableFuture(xid, mprInput.build(), getDeviceContext());
            }
        });
    }

}
