/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.MultipartRequestBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.port.stats._case.MultipartRequestPortStatsBuilder;

/**
 * The Node connector direct statistics service.
 */
public abstract class AbstractPortDirectStatisticsService<T extends OfHeader>
        extends AbstractDirectStatisticsService<GetNodeConnectorStatisticsInput, GetNodeConnectorStatisticsOutput, T> {

    public AbstractPortDirectStatisticsService(final RequestContextStack requestContextStack,
                                               final DeviceContext deviceContext,
                                               final ConvertorExecutor convertorExecutor,
                                               final MultipartWriterProvider statisticsWriterProvider) {
        super(MultipartType.OFPMPPORTSTATS, requestContextStack, deviceContext, convertorExecutor, statisticsWriterProvider);
    }

    @Override
    public MultipartRequestBody buildRequestBody(GetNodeConnectorStatisticsInput input) {
        final MultipartRequestPortStatsBuilder mprPortStatsBuilder = new MultipartRequestPortStatsBuilder();

        if (input.getNodeConnectorId() != null) {
            mprPortStatsBuilder.setPortNo(InventoryDataServiceUtil.portNumberfromNodeConnectorId(getOfVersion(), input.getNodeConnectorId()));
        } else {
            mprPortStatsBuilder.setPortNo(OFConstants.OFPP_ANY);
        }

        return new MultipartRequestPortStatsCaseBuilder()
                .setMultipartRequestPortStats(mprPortStatsBuilder.build())
                .build();
    }

}
