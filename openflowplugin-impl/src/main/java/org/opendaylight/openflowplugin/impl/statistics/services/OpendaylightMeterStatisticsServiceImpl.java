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
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Future;
import org.opendaylight.openflowjava.protocol.api.util.BinContent;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.CommonService;
import org.opendaylight.openflowplugin.impl.services.DataCrate;
import org.opendaylight.openflowplugin.impl.services.RequestInputUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.OpendaylightMeterStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterConfigCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter._case.MultipartRequestMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter.config._case.MultipartRequestMeterConfigBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * @author joe
 */
public class OpendaylightMeterStatisticsServiceImpl extends CommonService implements OpendaylightMeterStatisticsService {


    public OpendaylightMeterStatisticsServiceImpl(final RequestContextStack requestContextStack, DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    @Override
    public Future<RpcResult<GetAllMeterConfigStatisticsOutput>> getAllMeterConfigStatistics(
            final GetAllMeterConfigStatisticsInput input) {
        return this
                .<GetAllMeterConfigStatisticsOutput, Void>handleServiceCall(
                        PRIMARY_CONNECTION, new Function<DataCrate<GetAllMeterConfigStatisticsOutput>, ListenableFuture<RpcResult<Void>>>() {

                            @Override
                            public ListenableFuture<RpcResult<Void>> apply(final DataCrate<GetAllMeterConfigStatisticsOutput> data) {

                                MultipartRequestMeterConfigCaseBuilder caseBuilder =
                                        new MultipartRequestMeterConfigCaseBuilder();
                                MultipartRequestMeterConfigBuilder mprMeterConfigBuild =
                                        new MultipartRequestMeterConfigBuilder();
                                mprMeterConfigBuild.setMeterId(new MeterId(BinContent.intToUnsignedLong(
                                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common
                                                .types.rev130731.Meter.OFPMALL.getIntValue())));
                                caseBuilder.setMultipartRequestMeterConfig(mprMeterConfigBuild.build());

                                final Xid xid = data.getRequestContext().getXid();
                                MultipartRequestInputBuilder mprInput = RequestInputUtils
                                        .createMultipartHeader(MultipartType.OFPMPMETERCONFIG, xid.getValue(), version);
                                mprInput.setMultipartRequestBody(caseBuilder.build());
                                Future<RpcResult<Void>> resultFromOFLib = deviceContext
                                        .getPrimaryConnectionContext().getConnectionAdapter().multipartRequest(mprInput.build());
                                return JdkFutureAdapters
                                        .listenInPoolThread(resultFromOFLib);
                            }
                        });

    }

    @Override
    public Future<RpcResult<GetAllMeterStatisticsOutput>> getAllMeterStatistics(final GetAllMeterStatisticsInput input) {

        return this.<GetAllMeterStatisticsOutput, Void>handleServiceCall(
                PRIMARY_CONNECTION,
                new Function<DataCrate<GetAllMeterStatisticsOutput>, ListenableFuture<RpcResult<Void>>>() {
                    @Override
                    public ListenableFuture<RpcResult<Void>> apply(final DataCrate<GetAllMeterStatisticsOutput> data) {

                        MultipartRequestMeterCaseBuilder caseBuilder =
                                new MultipartRequestMeterCaseBuilder();
                        MultipartRequestMeterBuilder mprMeterBuild =
                                new MultipartRequestMeterBuilder();
                        mprMeterBuild.setMeterId(new MeterId(BinContent.intToUnsignedLong(
                                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common
                                        .types.rev130731.Meter.OFPMALL.getIntValue())));
                        caseBuilder.setMultipartRequestMeter(mprMeterBuild.build());

                        final Xid xid = data.getRequestContext().getXid();
                        MultipartRequestInputBuilder mprInput = RequestInputUtils
                                .createMultipartHeader(MultipartType.OFPMPMETER, xid.getValue(), version);
                        mprInput.setMultipartRequestBody(caseBuilder.build());
                        Future<RpcResult<Void>> resultFromOFLib = deviceContext.getPrimaryConnectionContext()
                                .getConnectionAdapter().multipartRequest(mprInput.build());

                        return JdkFutureAdapters.listenInPoolThread(resultFromOFLib);
                    }
                }
        );

    }

    @Override
    public Future<RpcResult<GetMeterFeaturesOutput>> getMeterFeatures(final GetMeterFeaturesInput input) {
        return this.<GetMeterFeaturesOutput, Void>handleServiceCall(
                PRIMARY_CONNECTION,
                new Function<DataCrate<GetMeterFeaturesOutput>, ListenableFuture<RpcResult<Void>>>() {
                    @Override
                    public ListenableFuture<RpcResult<Void>> apply(final DataCrate<GetMeterFeaturesOutput> data) {

                        MultipartRequestMeterFeaturesCaseBuilder mprMeterFeaturesBuild =
                                new MultipartRequestMeterFeaturesCaseBuilder();

                        final Xid xid = data.getRequestContext().getXid();
                        MultipartRequestInputBuilder mprInput =
                                RequestInputUtils.createMultipartHeader(MultipartType.OFPMPMETERFEATURES, xid.getValue(), version);
                        mprInput.setMultipartRequestBody(mprMeterFeaturesBuild.build());
                        Future<RpcResult<Void>> resultFromOFLib = deviceContext.getPrimaryConnectionContext().getConnectionAdapter().multipartRequest(mprInput.build());
                        return JdkFutureAdapters.listenInPoolThread(resultFromOFLib);
                    }
                });
    }

    @Override
    public Future<RpcResult<GetMeterStatisticsOutput>> getMeterStatistics(final GetMeterStatisticsInput input) {
        return this.<GetMeterStatisticsOutput, Void>handleServiceCall(
                PRIMARY_CONNECTION,
                new Function<DataCrate<GetMeterStatisticsOutput>, ListenableFuture<RpcResult<Void>>>() {
                    @Override
                    public ListenableFuture<RpcResult<Void>> apply(final DataCrate<GetMeterStatisticsOutput> data) {
                        MultipartRequestMeterCaseBuilder caseBuilder =
                                new MultipartRequestMeterCaseBuilder();
                        MultipartRequestMeterBuilder mprMeterBuild =
                                new MultipartRequestMeterBuilder();
                        mprMeterBuild.setMeterId(new MeterId(input.getMeterId().getValue()));
                        caseBuilder.setMultipartRequestMeter(mprMeterBuild.build());

                        final Xid xid = data.getRequestContext().getXid();
                        MultipartRequestInputBuilder mprInput =
                                RequestInputUtils.createMultipartHeader(MultipartType.OFPMPMETER, xid.getValue(), version);
                        mprInput.setMultipartRequestBody(caseBuilder.build());
                        Future<RpcResult<Void>> resultFromOFLib = deviceContext.getPrimaryConnectionContext().getConnectionAdapter().multipartRequest(mprInput.build());
                        return JdkFutureAdapters.listenInPoolThread(resultFromOFLib);
                    }
                });

    }

}
