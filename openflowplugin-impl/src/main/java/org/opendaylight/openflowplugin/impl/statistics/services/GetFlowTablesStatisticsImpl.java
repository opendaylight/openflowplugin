/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.util.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.AbstractCompatibleStatService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table._case.MultipartRequestTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.RpcResult;

@Deprecated
public final class GetFlowTablesStatisticsImpl
        extends AbstractCompatibleStatService<GetFlowTablesStatisticsInput, GetFlowTablesStatisticsOutput,
                FlowTableStatisticsUpdate>
        implements GetFlowTablesStatistics {
    private final NotificationPublishService notificationPublishService;

    public GetFlowTablesStatisticsImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
            final AtomicLong compatibilityXidSeed, final NotificationPublishService notificationPublishService) {
        super(requestContextStack, deviceContext, compatibilityXidSeed);
        this.notificationPublishService = requireNonNull(notificationPublishService);
    }

    @Override
    public ListenableFuture<RpcResult<GetFlowTablesStatisticsOutput>> invoke(final GetFlowTablesStatisticsInput input) {
        return handleAndNotify(input, notificationPublishService);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetFlowTablesStatisticsInput input) {
        // Set request body to main multipart request
        return RequestInputUtils.createMultipartHeader(MultipartType.OFPMPTABLE, xid.getValue(), getVersion())
            // Create multipart request body for fetch all the group stats
            .setMultipartRequestBody(new MultipartRequestTableCaseBuilder()
                .setMultipartRequestTable(new MultipartRequestTableBuilder()
                    .setEmpty(Empty.value())
                    .build())
                .build())
            .build();
    }

    @Override
    public GetFlowTablesStatisticsOutput buildTxCapableResult(final TransactionId emulatedTxId) {
        return new GetFlowTablesStatisticsOutputBuilder().setTransactionId(emulatedTxId).build();
    }

    @Override
    public FlowTableStatisticsUpdate transformToNotification(final List<MultipartReply> mpReplyList,
                                                             final TransactionId emulatedTxId) {
        final var notification = new FlowTableStatisticsUpdateBuilder()
            .setId(getDeviceInfo().getNodeId())
            .setMoreReplies(Boolean.FALSE)
            .setTransactionId(emulatedTxId);

        final var salFlowStats = BindingMap.<FlowTableAndStatisticsMapKey, FlowTableAndStatisticsMap>orderedBuilder();
        for (var mpReply : mpReplyList) {
            final var caseBody = (MultipartReplyTableCase) mpReply.getMultipartReplyBody();

            //TODO: Duplicate code: look at MultiReplyTranslatorUtil method translateTable
            for (var swTableStats : caseBody.getMultipartReplyTable().nonnullTableStats()) {
                salFlowStats.add(new FlowTableAndStatisticsMapBuilder()
                    .setActiveFlows(new Counter32(swTableStats.getActiveCount()))
                    .setPacketsLookedUp(new Counter64(swTableStats.getLookupCount()))
                    .setPacketsMatched(new Counter64(swTableStats.getMatchedCount()))
                    .setTableId(new TableId(swTableStats.getTableId()))
                    .build());
            }
        }

        return notification.setFlowTableAndStatisticsMap(salFlowStats.build()).build();
    }
}
