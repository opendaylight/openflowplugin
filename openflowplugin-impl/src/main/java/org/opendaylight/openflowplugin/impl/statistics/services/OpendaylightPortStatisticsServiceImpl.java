/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.OpendaylightPortStatisticsService;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class OpendaylightPortStatisticsServiceImpl implements OpendaylightPortStatisticsService {
    private final AllPortStatsService allPortStats;
    private final PortStatsService portStats;

    public OpendaylightPortStatisticsServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        allPortStats = new AllPortStatsService(requestContextStack, deviceContext);
        portStats = new PortStatsService(requestContextStack, deviceContext);
    }

    @Override
    public Future<RpcResult<GetAllNodeConnectorsStatisticsOutput>> getAllNodeConnectorsStatistics(
            final GetAllNodeConnectorsStatisticsInput input) {
        return allPortStats.handleServiceCall(input);
    }

    @Override
    public Future<RpcResult<GetNodeConnectorStatisticsOutput>> getNodeConnectorStatistics(
            final GetNodeConnectorStatisticsInput input) {
        return portStats.handleServiceCall(input);
    }
}
