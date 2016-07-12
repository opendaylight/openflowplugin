/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.group.statistics.GroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.group.statistics.GroupStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.MultipartReplyGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.MultipartRequestBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.group._case.MultipartRequestGroupBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * The Group direct statistics service.
 */
public class GroupDirectStatisticsService extends AbstractDirectStatisticsService<GetGroupStatisticsInput, GetGroupStatisticsOutput> {
    private final VersionConvertorData data;

    /**
     * Instantiates a new Group direct statistics service.
     *  @param requestContextStack the request context stack
     * @param deviceContext       the device context
     * @param convertorManager
     */
    public GroupDirectStatisticsService(RequestContextStack requestContextStack, DeviceContext deviceContext, ConvertorManager convertorManager) {
        super(MultipartType.OFPMPGROUP, requestContextStack, deviceContext, convertorManager);
        data = new VersionConvertorData(getVersion());
    }

    @Override
    protected MultipartRequestBody buildRequestBody(GetGroupStatisticsInput input) {
        final MultipartRequestGroupBuilder mprGroupBuild = new MultipartRequestGroupBuilder();

        if (input.getGroupId() != null) {
            mprGroupBuild.setGroupId(new GroupId(input.getGroupId().getValue()));
        } else {
            mprGroupBuild.setGroupId(new GroupId(OFConstants.OFPG_ALL));
        }

        return new MultipartRequestGroupCaseBuilder()
                .setMultipartRequestGroup(mprGroupBuild.build())
                .build();
    }

    @Override
    protected GetGroupStatisticsOutput buildReply(List<MultipartReply> input, boolean success) {
        final List<GroupStats> groupStats = new ArrayList<>();

        if (success) {
            for (final MultipartReply mpReply : input) {
                final MultipartReplyGroupCase caseBody = (MultipartReplyGroupCase) mpReply.getMultipartReplyBody();
                final MultipartReplyGroup replyBody = caseBody.getMultipartReplyGroup();
                final Optional<List<GroupStats>> groupStatsList = getConvertorManager().convert(
                        replyBody.getGroupStats(), data);

                if (groupStatsList.isPresent()) {
                    groupStats.addAll(groupStatsList.get());
                }
            }
        }

        return new GetGroupStatisticsOutputBuilder()
                .setGroupStats(groupStats)
                .build();
    }

    @Override
    protected void storeStatistics(GetGroupStatisticsOutput output) throws Exception {
        final InstanceIdentifier<FlowCapableNode> nodePath = getDeviceInfo().getNodeInstanceIdentifier().augmentation(FlowCapableNode.class);

        for (final GroupStats groupStatistics : output.getGroupStats()) {
            final InstanceIdentifier<GroupStatistics> groupStatisticsPath = nodePath
                    .child(Group.class, new GroupKey(groupStatistics.getGroupId()))
                    .augmentation(NodeGroupStatistics.class)
                    .child(GroupStatistics.class);

            final GroupStatistics stats = new GroupStatisticsBuilder(groupStatistics).build();
            getTxFacade().writeToTransactionWithParentsSlow(LogicalDatastoreType.OPERATIONAL, groupStatisticsPath, stats);
        }
    }
}
