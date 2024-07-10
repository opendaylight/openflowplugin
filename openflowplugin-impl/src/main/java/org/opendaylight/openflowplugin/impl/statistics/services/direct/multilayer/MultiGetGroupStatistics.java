/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services.direct.multilayer;

import java.util.List;
import java.util.Optional;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.services.util.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractGetGroupStatistics;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStatsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.group._case.MultipartRequestGroupBuilder;
import org.opendaylight.yangtools.binding.util.BindingMap;

public final class MultiGetGroupStatistics extends AbstractGetGroupStatistics<MultipartReply> {
    private final VersionConvertorData data;

    public MultiGetGroupStatistics(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
            final ConvertorExecutor convertorExecutor, final MultipartWriterProvider statisticsWriterProvider) {
        super(requestContextStack, deviceContext, convertorExecutor, statisticsWriterProvider);
        data = new VersionConvertorData(getVersion());
    }

    @Override
    protected GetGroupStatisticsOutput buildReply(final List<MultipartReply> input, final boolean success) {
        if (!success) {
            return new GetGroupStatisticsOutputBuilder().build();
        }

        final var groupStats = BindingMap.<GroupStatsKey, GroupStats>orderedBuilder();
        for (var mpReply : input) {
            final var caseBody = (MultipartReplyGroupCase) mpReply.getMultipartReplyBody();
            final var replyBody = caseBody.getMultipartReplyGroup();
            final Optional<List<GroupStats>> groupStatsList = getConvertorExecutor().convert(
                replyBody.getGroupStats(), data);

            groupStatsList.ifPresent(groupStats::addAll);
        }

        return new GetGroupStatisticsOutputBuilder()
            .setGroupStats(groupStats.build())
            .build();
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetGroupStatisticsInput input) {
        final var mprGroupBuild = new MultipartRequestGroupBuilder();

        if (input.getGroupId() != null) {
            mprGroupBuild.setGroupId(new GroupId(input.getGroupId().getValue()));
        } else {
            mprGroupBuild.setGroupId(new GroupId(OFConstants.OFPG_ALL));
        }

        return RequestInputUtils.createMultipartHeader(getMultipartType(), xid.getValue(), getVersion())
            .setMultipartRequestBody(new MultipartRequestGroupCaseBuilder()
                .setMultipartRequestGroup(mprGroupBuild.build())
                .build())
            .build();
    }

}
