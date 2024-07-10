/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.util.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.AbstractCompatibleStatService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupDescStatsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupDescStatsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.desc.stats.reply.GroupDescStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.desc.stats.reply.GroupDescStatsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.MultipartReplyGroupDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupDescCaseBuilder;
import org.opendaylight.yangtools.binding.util.BindingMap;

final class GroupDescriptionService
        extends AbstractCompatibleStatService<GetGroupDescriptionInput,
                                              GetGroupDescriptionOutput,
                                              GroupDescStatsUpdated> {
    private static final MultipartRequestGroupDescCase GROUP_DESC_CASE =
            new MultipartRequestGroupDescCaseBuilder().build();
    private final ConvertorExecutor convertorExecutor;

    GroupDescriptionService(final RequestContextStack requestContextStack,
                            final DeviceContext deviceContext,
                            final AtomicLong compatibilityXidSeed,
                            final ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext, compatibilityXidSeed);
        this.convertorExecutor = convertorExecutor;
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetGroupDescriptionInput input) {
        final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                MultipartType.OFPMPGROUPDESC, xid.getValue(), getVersion());
        mprInput.setMultipartRequestBody(GROUP_DESC_CASE);
        return mprInput.build();
    }

    @Override
    public GetGroupDescriptionOutput buildTxCapableResult(final TransactionId emulatedTxId) {
        return new GetGroupDescriptionOutputBuilder().setTransactionId(emulatedTxId).build();
    }

    @Override
    public GroupDescStatsUpdated transformToNotification(final List<MultipartReply> result,
            final TransactionId emulatedTxId) {
        final VersionConvertorData data = new VersionConvertorData(getVersion());

        final var stats = BindingMap.<GroupDescStatsKey, GroupDescStats>orderedBuilder();
        for (MultipartReply mpReply : result) {
            MultipartReplyGroupDescCase caseBody = (MultipartReplyGroupDescCase) mpReply.getMultipartReplyBody();
            MultipartReplyGroupDesc replyBody = caseBody.getMultipartReplyGroupDesc();
            final Optional<List<GroupDescStats>> groupDescStatsList = convertorExecutor.convert(
                    replyBody.getGroupDesc(), data);

            groupDescStatsList.ifPresent(stats::addAll);
        }

        return new GroupDescStatsUpdatedBuilder()
            .setId(getDeviceInfo().getNodeId())
            .setMoreReplies(Boolean.FALSE)
            .setTransactionId(emulatedTxId)
            .setGroupDescStats(stats.build())
            .build();
    }
}
