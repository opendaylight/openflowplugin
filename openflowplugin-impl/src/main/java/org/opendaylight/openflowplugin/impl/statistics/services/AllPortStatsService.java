/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.util.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.AbstractCompatibleStatService;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.NodeConnectorStatisticsToNotificationTransformer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.port.stats._case.MultipartRequestPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.NodeConnectorStatisticsUpdate;

final class AllPortStatsService
        extends AbstractCompatibleStatService<GetAllNodeConnectorsStatisticsInput, GetAllNodeConnectorsStatisticsOutput, NodeConnectorStatisticsUpdate> {

    private static final MultipartRequestPortStatsCase PORT_STATS_CASE;

    static {
        MultipartRequestPortStatsCaseBuilder caseBuilder =
                new MultipartRequestPortStatsCaseBuilder();
        MultipartRequestPortStatsBuilder mprPortStatsBuilder =
                new MultipartRequestPortStatsBuilder();
        // Select all ports
        mprPortStatsBuilder.setPortNo(OFConstants.OFPP_ANY);
        caseBuilder.setMultipartRequestPortStats(mprPortStatsBuilder.build());

        PORT_STATS_CASE = caseBuilder.build();
    }

    public AllPortStatsService(RequestContextStack requestContextStack, DeviceContext deviceContext, AtomicLong compatibilityXidSeed) {
        super(requestContextStack, deviceContext, compatibilityXidSeed);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetAllNodeConnectorsStatisticsInput input) throws ServiceException {
        MultipartRequestInputBuilder mprInput = RequestInputUtils
                .createMultipartHeader(MultipartType.OFPMPPORTSTATS, xid.getValue(), getVersion());
        mprInput.setMultipartRequestBody(PORT_STATS_CASE);
        return mprInput.build();
    }

    @Override
    public GetAllNodeConnectorsStatisticsOutput buildTxCapableResult(TransactionId emulatedTxId) {
        return new GetAllNodeConnectorsStatisticsOutputBuilder().setTransactionId(emulatedTxId).build();
    }

    @Override
    public NodeConnectorStatisticsUpdate transformToNotification(List<MultipartReply> result, TransactionId emulatedTxId) {
        return NodeConnectorStatisticsToNotificationTransformer.transformToNotification(result, getDeviceInfo(), getOfVersion(), emulatedTxId);
    }
}
