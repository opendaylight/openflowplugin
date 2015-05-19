/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import com.google.common.base.Function;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.impl.services.CommonService;
import org.opendaylight.openflowplugin.impl.services.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.util.StatisticsServiceUtil;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.port.stats._case.MultipartRequestPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.OpendaylightPortStatisticsService;
import org.opendaylight.yangtools.yang.common.RpcResult;

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
        return handleServiceCall(
                        new Function<RequestContext<GetAllNodeConnectorsStatisticsOutput>,
                        ListenableFuture<RpcResult<GetAllNodeConnectorsStatisticsOutput>>>() {

                            @Override
                            public ListenableFuture<RpcResult<GetAllNodeConnectorsStatisticsOutput>> apply(final RequestContext<GetAllNodeConnectorsStatisticsOutput> requestContext) {

                                MultipartRequestPortStatsCaseBuilder caseBuilder =
                                        new MultipartRequestPortStatsCaseBuilder();
                                MultipartRequestPortStatsBuilder mprPortStatsBuilder =
                                        new MultipartRequestPortStatsBuilder();
                                // Select all ports
                                mprPortStatsBuilder.setPortNo(OFConstants.OFPP_ANY);
                                caseBuilder.setMultipartRequestPortStats(mprPortStatsBuilder.build());

                                final Xid xid = requestContext.getXid();
                                MultipartRequestInputBuilder mprInput = RequestInputUtils
                                        .createMultipartHeader(MultipartType.OFPMPPORTSTATS, xid.getValue(), getVersion());
                                mprInput.setMultipartRequestBody(caseBuilder.build());
                                MultipartRequestInput multipartRequestInput = mprInput.build();
                                return StatisticsServiceUtil.getRpcResultListenableFuture(xid, multipartRequestInput, getDeviceContext());
                            }
                        });
    }

    @Override
    public Future<RpcResult<GetNodeConnectorStatisticsOutput>> getNodeConnectorStatistics(
            final GetNodeConnectorStatisticsInput input) {
        return handleServiceCall(
                        new Function<RequestContext<GetNodeConnectorStatisticsOutput>,
                        ListenableFuture<RpcResult<GetNodeConnectorStatisticsOutput>>>() {

                            @Override
                            public ListenableFuture<RpcResult<GetNodeConnectorStatisticsOutput>> apply(final RequestContext<GetNodeConnectorStatisticsOutput> requestContext) {

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

                                final Xid xid = requestContext.getXid();
                                MultipartRequestInputBuilder mprInput = RequestInputUtils
                                        .createMultipartHeader(MultipartType.OFPMPPORTSTATS, xid.getValue(), version);
                                mprInput.setMultipartRequestBody(caseBuilder.build());
                                MultipartRequestInput multipartRequestInput = mprInput.build();
                                return StatisticsServiceUtil.getRpcResultListenableFuture(xid, multipartRequestInput, getDeviceContext());
                            }
                        });

    }

}
