/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.base.MoreObjects;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.util.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchReactor;
import org.opendaylight.openflowplugin.openflow.md.util.FlowCreatorUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregateBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;

public abstract class AbstractAggregateFlowMultipartService<T extends OfHeader>
    extends AbstractMultipartService<GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput, T> {

    private final ConvertorExecutor convertorExecutor;

    public AbstractAggregateFlowMultipartService(final RequestContextStack requestContextStack, final DeviceContext deviceContext, final ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext);
        this.convertorExecutor = convertorExecutor;
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput input) throws ServiceException {
        final MultipartRequestAggregateCaseBuilder multipartRequestAggregateCaseBuilder = new MultipartRequestAggregateCaseBuilder();
        final MultipartRequestAggregateBuilder mprAggregateRequestBuilder = new MultipartRequestAggregateBuilder();
        final short tableId = MoreObjects.firstNonNull(input.getTableId(), OFConstants.OFPTT_ALL);
        mprAggregateRequestBuilder.setTableId(tableId);
        long outputPortValue = MoreObjects.firstNonNull(input.getOutPort(), OFConstants.OFPP_ANY).longValue();
        mprAggregateRequestBuilder.setOutPort(outputPortValue);

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
            long outGroup = MoreObjects.firstNonNull(input.getOutGroup(), OFConstants.OFPG_ANY);
            mprAggregateRequestBuilder.setOutGroup(outGroup);
        } else {
            mprAggregateRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
            mprAggregateRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
            mprAggregateRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);
        }

        MatchReactor.getInstance().convert(input.getMatch(), version, mprAggregateRequestBuilder, convertorExecutor);

        FlowCreatorUtil.setWildcardedFlowMatch(version, mprAggregateRequestBuilder);

        // Set request body to main multipart request
        multipartRequestAggregateCaseBuilder.setMultipartRequestAggregate(mprAggregateRequestBuilder
                .build());

        final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                MultipartType.OFPMPAGGREGATE, xid.getValue(), version);

        mprInput.setMultipartRequestBody(multipartRequestAggregateCaseBuilder.build());

        return mprInput.build();
    }

    /**
     * Process input and return reply
     * @param input input
     * @return reply
     */
    public abstract Future<RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>> handleAndReply(
        final GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput input);

}
