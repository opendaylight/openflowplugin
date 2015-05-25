/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.AbstractSimpleService;
import org.opendaylight.openflowplugin.impl.services.RequestInputUtils;
import org.opendaylight.openflowplugin.openflow.md.util.FlowCreatorUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregateBuilder;

final class AggregateFlowsInTableService extends AbstractSimpleService<GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput, GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput> {
    public AggregateFlowsInTableService(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext, GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput.class);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput input) {
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
        final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                MultipartType.OFPMPAGGREGATE, xid.getValue(), version);

        mprInput.setMultipartRequestBody(multipartRequestAggregateCaseBuilder.build());

        return mprInput.build();
    }
}
