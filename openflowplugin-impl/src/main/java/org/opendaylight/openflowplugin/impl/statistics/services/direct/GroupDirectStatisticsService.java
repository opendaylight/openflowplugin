package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.RequestInputUtils;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.GroupStatsResponseConvertor;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.MultipartReplyGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.group._case.MultipartRequestGroupBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.ArrayList;
import java.util.List;

public class GroupDirectStatisticsService extends AbstractDirectStatisticsService<GetGroupStatisticsInput, GetGroupStatisticsOutput> {
    private final GroupStatsResponseConvertor groupStatsConvertor = new GroupStatsResponseConvertor();

    public GroupDirectStatisticsService(RequestContextStack requestContextStack, DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    @Override
    protected OfHeader buildRequest(Xid xid, GetGroupStatisticsInput input) throws Exception {
        final MultipartRequestGroupBuilder mprGroupBuild = new MultipartRequestGroupBuilder()
                .setGroupId(new GroupId(input.getGroupId().getValue()));

        final MultipartRequestGroupCaseBuilder caseBuilder = new MultipartRequestGroupCaseBuilder()
                .setMultipartRequestGroup(mprGroupBuild.build());

        return RequestInputUtils.createMultipartHeader(MultipartType.OFPMPGROUP, xid.getValue(), getVersion())
                .setMultipartRequestBody(caseBuilder.build())
                .build();
    }

    @Override
    protected GetGroupStatisticsOutput buildReply(List<MultipartReply> input) {
        final List<GroupStats> groupStats = new ArrayList<>();

        for (MultipartReply mpReply : input) {
            MultipartReplyGroupCase caseBody = (MultipartReplyGroupCase) mpReply.getMultipartReplyBody();
            MultipartReplyGroup replyBody = caseBody.getMultipartReplyGroup();
            groupStats.addAll(groupStatsConvertor.toSALGroupStatsList(replyBody.getGroupStats()));
        }

        return new GetGroupStatisticsOutputBuilder()
                .setGroupStats(groupStats)
                .build();
    }

    @Override
    protected void storeStatistics(GetGroupStatisticsOutput output) throws Exception {
        final InstanceIdentifier<FlowCapableNode> nodePath = getDeviceContext()
                .getDeviceState().getNodeInstanceIdentifier().augmentation(FlowCapableNode.class);

        //TODO: Remove dependency on deviceContext
        for (final GroupStats groupStatistics : output.getGroupStats()) {
            final InstanceIdentifier<GroupStatistics> groupStatisticsPath = nodePath
                    .child(Group.class, new GroupKey(groupStatistics.getGroupId()))
                    .augmentation(NodeGroupStatistics.class)
                    .child(GroupStatistics.class);

            final GroupStatistics stats = new GroupStatisticsBuilder(groupStatistics).build();
            getDeviceContext().writeToTransactionWithParentsSlow(LogicalDatastoreType.OPERATIONAL, groupStatisticsPath, stats);
        }
    }
}
