/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import com.google.common.base.Function;
import com.google.common.util.concurrent.JdkFutureAdapters;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.impl.services.CommonService;
import org.opendaylight.openflowplugin.impl.services.DataCrate;
import org.opendaylight.openflowplugin.impl.services.RequestInputUtils;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.port.stats._case.MultipartRequestPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.OpendaylightPortStatisticsService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import java.util.concurrent.Future;

/**
 * @author joe
 */
public class OpendaylightPortStatisticsServiceImpl extends CommonService implements OpendaylightPortStatisticsService {

    public OpendaylightPortStatisticsServiceImpl(final RequestContextStack requestContextStack, DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    @Override
    public Future<RpcResult<GetAllNodeConnectorsStatisticsOutput>> getAllNodeConnectorsStatistics(
            final GetAllNodeConnectorsStatisticsInput input) {
        return this
                .<GetAllNodeConnectorsStatisticsOutput, Void>handleServiceCall(
                        PRIMARY_CONNECTION, new Function<DataCrate<GetAllNodeConnectorsStatisticsOutput>, Future<RpcResult<Void>>>() {

                            @Override
                            public Future<RpcResult<Void>> apply(final DataCrate<GetAllNodeConnectorsStatisticsOutput> data) {

                                MultipartRequestPortStatsCaseBuilder caseBuilder =
                                        new MultipartRequestPortStatsCaseBuilder();
                                MultipartRequestPortStatsBuilder mprPortStatsBuilder =
                                        new MultipartRequestPortStatsBuilder();
                                // Select all ports
                                mprPortStatsBuilder.setPortNo(OFConstants.OFPP_ANY);
                                caseBuilder.setMultipartRequestPortStats(mprPortStatsBuilder.build());

                                final Xid xid = deviceContext.getNextXid();
                                data.getRequestContext().setXid(xid);
                                MultipartRequestInputBuilder mprInput = RequestInputUtils
                                        .createMultipartHeader(MultipartType.OFPMPPORTSTATS, xid.getValue(), version);
                                mprInput.setMultipartRequestBody(caseBuilder.build());
                                Future<RpcResult<Void>> resultFromOFLib = deviceContext
                                        .getPrimaryConnectionContext().getConnectionAdapter().multipartRequest(mprInput.build());
                                return JdkFutureAdapters.listenInPoolThread(resultFromOFLib);
                            }
                        });
    }

    @Override
    public Future<RpcResult<GetNodeConnectorStatisticsOutput>> getNodeConnectorStatistics(
            final GetNodeConnectorStatisticsInput input) {
        return this
                .<GetNodeConnectorStatisticsOutput, Void>handleServiceCall(
                        PRIMARY_CONNECTION, new Function<DataCrate<GetNodeConnectorStatisticsOutput>, Future<RpcResult<Void>>>() {

                            @Override
                            public Future<RpcResult<Void>> apply(final DataCrate<GetNodeConnectorStatisticsOutput> data) {

                                MultipartRequestPortStatsCaseBuilder caseBuilder =
                                        new MultipartRequestPortStatsCaseBuilder();
                                MultipartRequestPortStatsBuilder mprPortStatsBuilder =
                                        new MultipartRequestPortStatsBuilder();
                                // Set specific port
                                mprPortStatsBuilder
                                        .setPortNo(InventoryDataServiceUtil.portNumberfromNodeConnectorId(
                                                OpenflowVersion.get(version),
                                                input.getNodeConnectorId()));
                                caseBuilder.setMultipartRequestPortStats(mprPortStatsBuilder.build());

                                final Xid xid = deviceContext.getNextXid();
                                data.getRequestContext().setXid(xid);
                                MultipartRequestInputBuilder mprInput = RequestInputUtils
                                        .createMultipartHeader(MultipartType.OFPMPPORTSTATS, xid.getValue(), version);
                                mprInput.setMultipartRequestBody(caseBuilder.build());
                                Future<RpcResult<Void>> resultFromOFLib = deviceContext.getPrimaryConnectionContext()
                                        .getConnectionAdapter().multipartRequest(mprInput.build());
                                return JdkFutureAdapters.listenInPoolThread(resultFromOFLib);
                            }
                        });

    }

}
