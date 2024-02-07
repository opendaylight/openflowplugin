/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;

public final class AllFlowsInAllTablesService extends
        AbstractCompatibleStatService<GetAllFlowsStatisticsFromAllFlowTablesInput,
                GetAllFlowsStatisticsFromAllFlowTablesOutput, FlowsStatisticsUpdate> {
    private final MultipartRequestFlowCase flowCase;
    private final ConvertorExecutor convertorExecutor;

    public AllFlowsInAllTablesService(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
                                      final AtomicLong compatibilityXidSeed,
                                      final ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext, compatibilityXidSeed);
        this.convertorExecutor = convertorExecutor;

        final var mprFlowRequestBuilder = new MultipartRequestFlowBuilder()
            .setTableId(OFConstants.OFPTT_ALL)
            .setOutPort(OFConstants.OFPP_ANY)
            .setOutGroup(OFConstants.OFPG_ANY)
            .setCookie(OFConstants.DEFAULT_COOKIE)
            .setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);
        FlowCreatorUtil.forVersion(getVersion()).setWildcardedFlowMatch(mprFlowRequestBuilder);
        flowCase = new MultipartRequestFlowCaseBuilder()
            .setMultipartRequestFlow(mprFlowRequestBuilder.build())
            .build();
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetAllFlowsStatisticsFromAllFlowTablesInput input) {
        return RequestInputUtils.createMultipartHeader(MultipartType.OFPMPFLOW, xid.getValue(), getVersion())
            .setMultipartRequestBody(flowCase)
            .build();
    }

    @Override
    public GetAllFlowsStatisticsFromAllFlowTablesOutput buildTxCapableResult(final TransactionId emulatedTxId) {
        return new GetAllFlowsStatisticsFromAllFlowTablesOutputBuilder().setTransactionId(emulatedTxId).build();
    }

    @Override
    public FlowsStatisticsUpdate transformToNotification(final List<MultipartReply> result,
            final TransactionId emulatedTxId) {
        return FlowStatisticsToNotificationTransformer.transformToNotification(result, getDeviceInfo(), getOfVersion(),
            emulatedTxId, convertorExecutor);
    }
}
