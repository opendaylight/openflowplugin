/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.AbstractCompatibleStatService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.OpendaylightFlowTableStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.MultipartReplyTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.multipart.reply.table.TableStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table._case.MultipartRequestTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId;
import org.opendaylight.yangtools.yang.common.RpcResult;

public final class OpendaylightFlowTableStatisticsServiceImpl extends
        AbstractCompatibleStatService<GetFlowTablesStatisticsInput, GetFlowTablesStatisticsOutput, FlowTableStatisticsUpdate> implements
        OpendaylightFlowTableStatisticsService {

    private final NotificationPublishService notificationPublishService;

    public OpendaylightFlowTableStatisticsServiceImpl(final RequestContextStack requestContextStack,
                                                      final DeviceContext deviceContext,
                                                      final AtomicLong compatibilityXidSeed,
                                                      final NotificationPublishService notificationPublishService) {
        super(requestContextStack, deviceContext, compatibilityXidSeed);
        this.notificationPublishService = notificationPublishService;
    }

    @Override
    public Future<RpcResult<GetFlowTablesStatisticsOutput>> getFlowTablesStatistics(
            final GetFlowTablesStatisticsInput input) {
        return handleAndNotify(input, notificationPublishService);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetFlowTablesStatisticsInput input) {
        // Create multipart request body for fetch all the group stats
        final MultipartRequestTableCaseBuilder multipartRequestTableCaseBuilder = new MultipartRequestTableCaseBuilder();
        final MultipartRequestTableBuilder multipartRequestTableBuilder = new MultipartRequestTableBuilder();
        multipartRequestTableBuilder.setEmpty(true);
        multipartRequestTableCaseBuilder.setMultipartRequestTable(multipartRequestTableBuilder.build());

        // Set request body to main multipart request
        final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                MultipartType.OFPMPTABLE, xid.getValue(), getVersion());

        mprInput.setMultipartRequestBody(multipartRequestTableCaseBuilder.build());

        return mprInput.build();
    }

    @Override
    public GetFlowTablesStatisticsOutput buildTxCapableResult(TransactionId emulatedTxId) {
        return new GetFlowTablesStatisticsOutputBuilder().setTransactionId(emulatedTxId).build();
    }

    @Override
    public FlowTableStatisticsUpdate transformToNotification(List<MultipartReply> mpReplyList, TransactionId emulatedTxId) {
        FlowTableStatisticsUpdateBuilder notification = new FlowTableStatisticsUpdateBuilder();
        notification.setId(getDeviceContext().getDeviceState().getNodeId());
        notification.setMoreReplies(Boolean.FALSE);
        notification.setTransactionId(emulatedTxId);

        final List<FlowTableAndStatisticsMap> salFlowStats = new ArrayList<>();
        notification.setFlowTableAndStatisticsMap(salFlowStats);
        for (MultipartReply mpReply : mpReplyList) {
            MultipartReplyTableCase caseBody = (MultipartReplyTableCase) mpReply.getMultipartReplyBody();
            MultipartReplyTable replyBody = caseBody.getMultipartReplyTable();
            List<TableStats> swTablesStats = replyBody.getTableStats();

            for (TableStats swTableStats : swTablesStats) {
                FlowTableAndStatisticsMapBuilder statisticsBuilder = new FlowTableAndStatisticsMapBuilder();
                statisticsBuilder.setActiveFlows(new Counter32(swTableStats.getActiveCount()));
                statisticsBuilder.setPacketsLookedUp(new Counter64(swTableStats.getLookupCount()));
                statisticsBuilder.setPacketsMatched(new Counter64(swTableStats.getMatchedCount()));
                statisticsBuilder.setTableId(new TableId(swTableStats.getTableId()));
                salFlowStats.add(statisticsBuilder.build());
            }
        }

        return notification.build();
    }
}
