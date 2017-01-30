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
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.impl.services.util.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.AbstractCompatibleStatService;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.NodeConnectorStatisticsToNotificationTransformer;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.port.stats._case.MultipartRequestPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.NodeConnectorStatisticsUpdate;

final class PortStatsService
        extends AbstractCompatibleStatService<GetNodeConnectorStatisticsInput, GetNodeConnectorStatisticsOutput, NodeConnectorStatisticsUpdate> {

    public PortStatsService(RequestContextStack requestContextStack, DeviceContext deviceContext, AtomicLong compatibilityXidSeed) {
        super(requestContextStack, deviceContext, compatibilityXidSeed);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetNodeConnectorStatisticsInput input) throws ServiceException {
        MultipartRequestPortStatsCaseBuilder caseBuilder =
                new MultipartRequestPortStatsCaseBuilder();
        MultipartRequestPortStatsBuilder mprPortStatsBuilder =
                new MultipartRequestPortStatsBuilder();
        // Set specific port
        final short version = getVersion();
        mprPortStatsBuilder
                .setPortNo(InventoryDataServiceUtil.portNumberfromNodeConnectorId(
                        OpenflowVersion.get(version),
                        input.getNodeConnectorId()));
        caseBuilder.setMultipartRequestPortStats(mprPortStatsBuilder.build());

        MultipartRequestInputBuilder mprInput = RequestInputUtils
                .createMultipartHeader(MultipartType.OFPMPPORTSTATS, xid.getValue(), version);
        mprInput.setMultipartRequestBody(caseBuilder.build());

        return mprInput.build();
    }

    @Override
    public GetNodeConnectorStatisticsOutput buildTxCapableResult(TransactionId emulatedTxId) {
        return new GetNodeConnectorStatisticsOutputBuilder().setTransactionId(emulatedTxId).build();
    }

    @Override
    public NodeConnectorStatisticsUpdate transformToNotification(List<MultipartReply> result, TransactionId emulatedTxId) {
        return NodeConnectorStatisticsToNotificationTransformer.transformToNotification(result, getDeviceInfo(), getOfVersion(), emulatedTxId);
    }
}
