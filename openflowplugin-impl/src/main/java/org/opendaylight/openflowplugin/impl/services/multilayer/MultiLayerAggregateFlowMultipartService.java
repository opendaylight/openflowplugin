/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.multilayer;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Optional;
import java.util.stream.Collectors;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.impl.services.AbstractAggregateFlowMultipartService;
import org.opendaylight.openflowplugin.impl.services.util.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.util.FlowCreatorUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchInjector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.get.aggregate.flow.statistics.from.flow.table._for.given.match.output.AggregatedFlowStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregateBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

public class MultiLayerAggregateFlowMultipartService extends AbstractAggregateFlowMultipartService<MultipartReply> {

    private final TranslatorLibrary translatorLibrary;
    private final ConvertorExecutor convertorExecutor;
    private final VersionConvertorData data;

    public MultiLayerAggregateFlowMultipartService(final RequestContextStack requestContextStack,
                                                   final DeviceContext deviceContext,
                                                   final ConvertorExecutor convertorExecutor,
                                                   final TranslatorLibrary translatorLibrary) {
        super(requestContextStack, deviceContext);
        this.convertorExecutor = convertorExecutor;
        this.translatorLibrary = translatorLibrary;
        this.data = new VersionConvertorData(getVersion());
    }


    @Override
    protected OfHeader buildRequest(final Xid xid,
                                    final GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput input) {
        final MultipartRequestAggregateCaseBuilder multipartRequestAggregateCaseBuilder
                = new MultipartRequestAggregateCaseBuilder();
        final MultipartRequestAggregateBuilder mprAggregateRequestBuilder = new MultipartRequestAggregateBuilder();
        final Uint8 tableId = MoreObjects.firstNonNull(input.getTableId(), OFConstants.OFPTT_ALL);
        mprAggregateRequestBuilder.setTableId(tableId);
        long outputPortValue = MoreObjects.firstNonNull(input.getOutPort(), OFConstants.OFPP_ANY).longValue();
        mprAggregateRequestBuilder.setOutPort(outputPortValue);

        final short version = getVersion();
        if (version == OFConstants.OFP_VERSION_1_3) {

            if (input.getCookie() == null) {
                mprAggregateRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
            } else {
                mprAggregateRequestBuilder
                        .setCookie(MoreObjects.firstNonNull(input.getCookie().getValue(), OFConstants.DEFAULT_COOKIE));
            }

            if (input.getCookieMask() == null) {
                mprAggregateRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);
            } else {
                mprAggregateRequestBuilder.setCookieMask(
                        MoreObjects.firstNonNull(input.getCookieMask().getValue(), OFConstants.DEFAULT_COOKIE_MASK));
            }
            Uint32 outGroup = MoreObjects.firstNonNull(input.getOutGroup(), OFConstants.OFPG_ANY);
            mprAggregateRequestBuilder.setOutGroup(outGroup);
        } else {
            mprAggregateRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
            mprAggregateRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
            mprAggregateRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);
        }

        // convert and inject match
        final Optional<Object> conversionMatch = convertorExecutor.convert(input.getMatch(), data);
        MatchInjector.inject(conversionMatch, mprAggregateRequestBuilder, data.getVersion());

        FlowCreatorUtil.setWildcardedFlowMatch(version, mprAggregateRequestBuilder);

        // Set request body to main multipart request
        multipartRequestAggregateCaseBuilder.setMultipartRequestAggregate(mprAggregateRequestBuilder.build());

        final MultipartRequestInputBuilder mprInput = RequestInputUtils
                .createMultipartHeader(MultipartType.OFPMPAGGREGATE, xid.getValue(), version);

        mprInput.setMultipartRequestBody(multipartRequestAggregateCaseBuilder.build());

        return mprInput.build();
    }

    @Override
    public ListenableFuture<RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>> handleAndReply(
            final GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput input) {
        return Futures.transform(handleServiceCall(input),
            result -> {
                if (Preconditions.checkNotNull(result).isSuccessful()) {
                    final MessageTranslator<MultipartReply, AggregatedFlowStatistics>
                             messageTranslator = translatorLibrary.lookupTranslator(
                                 new TranslatorKey(getVersion(),
                                               MultipartReplyAggregateCase.class.getName()));

                    return RpcResultBuilder.success(
                             new GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder()
                                     .setAggregatedFlowStatistics(result.getResult().stream()
                                                                          .map(multipartReply ->
                                                                                       messageTranslator
                                                                                  .translate(
                                                                                          multipartReply,
                                                                                          getDeviceInfo(),
                                                                                          null))
                                                                          .collect(Collectors
                                                                                           .toList())))
                             .build();
                }

                return RpcResultBuilder
                             .<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>failed()
                             .withRpcErrors(result.getErrors()).build();
            }, MoreExecutors.directExecutor());
    }
}
