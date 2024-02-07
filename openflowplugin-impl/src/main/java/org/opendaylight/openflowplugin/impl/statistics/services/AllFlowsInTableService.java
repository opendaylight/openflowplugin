/*
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
import org.opendaylight.openflowplugin.impl.services.util.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.AbstractCompatibleStatService;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.FlowStatisticsToNotificationTransformer;
import org.opendaylight.openflowplugin.impl.util.FlowCreatorUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;

public class AllFlowsInTableService extends AbstractCompatibleStatService<GetAllFlowStatisticsFromFlowTableInput,
        GetAllFlowStatisticsFromFlowTableOutput, FlowsStatisticsUpdate> {
    private final ConvertorExecutor convertorExecutor;

    public AllFlowsInTableService(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
            final AtomicLong compatibilityXidSeed, final ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext, compatibilityXidSeed);
        this.convertorExecutor = convertorExecutor;
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetAllFlowStatisticsFromFlowTableInput input) {
        final var mprFlowRequestBuilder = new MultipartRequestFlowBuilder()
            .setTableId(input.getTableId().getValue())
            .setOutPort(OFConstants.OFPP_ANY)
            .setOutGroup(OFConstants.OFPG_ANY)
            .setCookie(OFConstants.DEFAULT_COOKIE)
            .setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);

        final var version = getVersion();
        FlowCreatorUtil.forVersion(version).setWildcardedFlowMatch(mprFlowRequestBuilder);

        return RequestInputUtils.createMultipartHeader(MultipartType.OFPMPFLOW, xid.getValue(), version)
            .setMultipartRequestBody(new MultipartRequestFlowCaseBuilder()
                .setMultipartRequestFlow(mprFlowRequestBuilder.build())
                .build())
            .build();
    }

    @Override
    public GetAllFlowStatisticsFromFlowTableOutput buildTxCapableResult(final TransactionId emulatedTxId) {
        return new GetAllFlowStatisticsFromFlowTableOutputBuilder()
            .setTransactionId(emulatedTxId)
            .build();
    }

    @Override
    public FlowsStatisticsUpdate transformToNotification(final List<MultipartReply> mpResult,
            final TransactionId emulatedTxId) {
        return FlowStatisticsToNotificationTransformer.transformToNotification(mpResult, getDeviceInfo(),
            getOfVersion(), emulatedTxId, convertorExecutor);
    }
}
