/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.impl.services.AbstractSimpleService;
import org.opendaylight.openflowplugin.impl.services.RequestInputUtils;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.port.stats._case.MultipartRequestPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatisticsOutput;

final class PortStatsService extends AbstractSimpleService<GetNodeConnectorStatisticsInput, GetNodeConnectorStatisticsOutput> {

    protected PortStatsService(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext, GetNodeConnectorStatisticsOutput.class);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetNodeConnectorStatisticsInput input) {
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
}
