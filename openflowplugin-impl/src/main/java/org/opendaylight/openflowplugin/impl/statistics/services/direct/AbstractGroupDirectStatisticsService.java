/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.group.statistics.GroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.group.statistics.GroupStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.MultipartRequestBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.group._case.MultipartRequestGroupBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * The Group direct statistics service.
 */
public abstract class AbstractGroupDirectStatisticsService<T extends OfHeader>
        extends AbstractDirectStatisticsService<GetGroupStatisticsInput, GetGroupStatisticsOutput, T> {

    protected AbstractGroupDirectStatisticsService(RequestContextStack requestContextStack, DeviceContext deviceContext, ConvertorExecutor convertorExecutor) {
        super(MultipartType.OFPMPGROUP, requestContextStack, deviceContext, convertorExecutor);
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
    protected void storeStatistics(GetGroupStatisticsOutput output) {
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
