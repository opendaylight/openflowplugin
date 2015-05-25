/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import org.opendaylight.openflowjava.protocol.api.util.BinContent;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.AbstractSimpleService;
import org.opendaylight.openflowplugin.impl.services.RequestInputUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.group._case.MultipartRequestGroupBuilder;

final class AllGroupsStatsService extends AbstractSimpleService<GetAllGroupStatisticsInput, GetAllGroupStatisticsOutput> {
    private static final MultipartRequestGroupCase GROUP_CASE;

    static {
        final MultipartRequestGroupCaseBuilder caseBuilder = new MultipartRequestGroupCaseBuilder();
        final MultipartRequestGroupBuilder mprGroupBuild = new MultipartRequestGroupBuilder();
        mprGroupBuild.setGroupId(new GroupId(BinContent.intToUnsignedLong(Group.OFPGALL.getIntValue())));
        caseBuilder.setMultipartRequestGroup(mprGroupBuild.build());

        GROUP_CASE = caseBuilder.build();
    }

    AllGroupsStatsService(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext, GetAllGroupStatisticsOutput.class);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetAllGroupStatisticsInput input) {
        // Create multipart request header
        final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                MultipartType.OFPMPGROUP, xid.getValue(), getVersion());

        // Set request body to main multipart request
        mprInput.setMultipartRequestBody(GROUP_CASE);

        // Send the request, no cookies associated, use any connection
        return mprInput.build();
    }
}
