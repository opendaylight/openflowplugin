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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.MultipartReplyGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.group._case.MultipartRequestGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStats;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GroupDirectStatisticsService extends AbstractDirectStatisticsService<GetGroupStatisticsInput, GetGroupStatisticsOutput> {
    private final GroupStatsResponseConvertor groupStatsConvertor = new GroupStatsResponseConvertor();

    public GroupDirectStatisticsService(RequestContextStack requestContextStack, DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    @Override
    protected OfHeader buildRequest(Xid xid, GetGroupStatisticsInput input) throws Exception {
        final MultipartRequestGroupCaseBuilder caseBuilder = new MultipartRequestGroupCaseBuilder();
        final MultipartRequestGroupBuilder mprGroupBuild = new MultipartRequestGroupBuilder();
        mprGroupBuild.setGroupId(new GroupId(input.getGroupId().getValue()));
        caseBuilder.setMultipartRequestGroup(mprGroupBuild.build());

        final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                MultipartType.OFPMPGROUP, xid.getValue(), getVersion());

        mprInput.setMultipartRequestBody(caseBuilder.build());
        return mprInput.build();
    }

    @Override
    protected Builder<GetGroupStatisticsOutput> buildReply(@Nullable List<MultipartReply> input) {
        final GetGroupStatisticsOutputBuilder builder = new GetGroupStatisticsOutputBuilder();
        final List<GroupStats> groupStats = new ArrayList<>();

        for (MultipartReply mpReply : input) {
            MultipartReplyGroupCase caseBody = (MultipartReplyGroupCase) mpReply.getMultipartReplyBody();
            MultipartReplyGroup replyBody = caseBody.getMultipartReplyGroup();
            groupStats.addAll(groupStatsConvertor.toSALGroupStatsList(replyBody.getGroupStats()));
        }

        builder.setGroupStats(groupStats);

        return builder;
    }

    @Override
    protected void storeStatistics(@Nullable GetGroupStatisticsOutput input) throws Exception {
        final InstanceIdentifier<FlowCapableNode> fNodeIdent = getDeviceContext()
                .getDeviceState().getNodeInstanceIdentifier().augmentation(FlowCapableNode.class);

        //TODO: Remove dependency on deviceContext
        for (final GroupStats groupStats : input.getGroupStats()) {
            final InstanceIdentifier<GroupStatistics> gsIdent = fNodeIdent
                    .child(Group.class, new GroupKey(groupStats.getGroupId()))
                    .augmentation(NodeGroupStatistics.class)
                    .child(GroupStatistics.class);

            final GroupStatistics stats = new GroupStatisticsBuilder(groupStats).build();

            getDeviceContext().writeToTransaction(LogicalDatastoreType.OPERATIONAL, gsIdent, stats);
        }
    }
}
