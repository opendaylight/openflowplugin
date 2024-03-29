/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.openflowjava.protocol.api.util.BinContent;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.util.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.AbstractCompatibleStatService;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.GroupStatisticsToNotificationTransformer;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupStatisticsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.group._case.MultipartRequestGroupBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

final class AllGroupsStatsService extends
        AbstractCompatibleStatService<GetAllGroupStatisticsInput, GetAllGroupStatisticsOutput, GroupStatisticsUpdated> {
    private static final MultipartRequestGroupCase GROUP_CASE = new MultipartRequestGroupCaseBuilder()
        .setMultipartRequestGroup(new MultipartRequestGroupBuilder()
            .setGroupId(new GroupId(Uint32.valueOf(BinContent.intToUnsignedLong(Group.OFPGALL.getIntValue()))))
            .build())
        .build();

    private final ConvertorExecutor convertorExecutor;

    AllGroupsStatsService(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
                          final AtomicLong compatibilityXidSeed, final ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext, compatibilityXidSeed);
        this.convertorExecutor = convertorExecutor;
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

    @Override
    public GetAllGroupStatisticsOutput buildTxCapableResult(final TransactionId emulatedTxId) {
        return new GetAllGroupStatisticsOutputBuilder().setTransactionId(emulatedTxId).build();
    }

    @Override
    public GroupStatisticsUpdated transformToNotification(final List<MultipartReply> result,
            final TransactionId emulatedTxId) {
        return GroupStatisticsToNotificationTransformer.transformToNotification(result, getDeviceInfo(), emulatedTxId,
            convertorExecutor);
    }
}
