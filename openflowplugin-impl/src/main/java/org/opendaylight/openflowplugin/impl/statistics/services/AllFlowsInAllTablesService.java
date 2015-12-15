/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.AbstractCompatibleStatService;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.FlowStatisticsToNotificationTransformer;
import org.opendaylight.openflowplugin.openflow.md.util.FlowCreatorUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;

public final class AllFlowsInAllTablesService extends AbstractCompatibleStatService<GetAllFlowsStatisticsFromAllFlowTablesInput,
        GetAllFlowsStatisticsFromAllFlowTablesOutput, FlowsStatisticsUpdate> {
    private final MultipartRequestFlowCase flowCase;

    public AllFlowsInAllTablesService(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
                                      final AtomicLong compatibilityXidSeed) {
        super(requestContextStack, deviceContext, compatibilityXidSeed);

        final MultipartRequestFlowCaseBuilder multipartRequestFlowCaseBuilder = new MultipartRequestFlowCaseBuilder();
        final MultipartRequestFlowBuilder mprFlowRequestBuilder = new MultipartRequestFlowBuilder();
        mprFlowRequestBuilder.setTableId(OFConstants.OFPTT_ALL);
        mprFlowRequestBuilder.setOutPort(OFConstants.OFPP_ANY);
        mprFlowRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
        mprFlowRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
        mprFlowRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);
        FlowCreatorUtil.setWildcardedFlowMatch(getVersion(), mprFlowRequestBuilder);
        multipartRequestFlowCaseBuilder.setMultipartRequestFlow(mprFlowRequestBuilder.build());

        flowCase = multipartRequestFlowCaseBuilder.build();
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetAllFlowsStatisticsFromAllFlowTablesInput input) {
        final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                MultipartType.OFPMPFLOW, xid.getValue(), getVersion());
        mprInput.setMultipartRequestBody(flowCase);

        return mprInput.build();
    }

    @Override
    public GetAllFlowsStatisticsFromAllFlowTablesOutput buildTxCapableResult(TransactionId emulatedTxId) {
        return new GetAllFlowsStatisticsFromAllFlowTablesOutputBuilder().setTransactionId(emulatedTxId).build();
    }

    @Override
    public FlowsStatisticsUpdate transformToNotification(List<MultipartReply> result, TransactionId emulatedTxId) {
        return FlowStatisticsToNotificationTransformer.transformToNotification(result, getDeviceContext(), getOfVersion(), emulatedTxId);
    }
}
