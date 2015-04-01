/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.base.Function;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.openflowjava.protocol.api.util.BinContent;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
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
import java.util.concurrent.Future;

/**
 * @author joe
 */
public class OpendaylightMeterStatisticsServiceImpl extends CommonService implements OpendaylightMeterStatisticsService {


    @Override
    public Future<RpcResult<GetAllMeterConfigStatisticsOutput>> getAllMeterConfigStatistics(
            final GetAllMeterConfigStatisticsInput input) {
        return this
                .<GetAllMeterConfigStatisticsOutput, Void> handleServiceCall(
                        PRIMARY_CONNECTION,  new Function<DataCrate<GetAllMeterConfigStatisticsOutput>, Future<RpcResult<Void>>>() {

                    @Override
                    public Future<RpcResult<Void>> apply(final DataCrate<GetAllMeterConfigStatisticsOutput> data) {

                        MultipartRequestMeterConfigCaseBuilder caseBuilder =
                                new MultipartRequestMeterConfigCaseBuilder();
                        MultipartRequestMeterConfigBuilder mprMeterConfigBuild =
                                new MultipartRequestMeterConfigBuilder();
                        mprMeterConfigBuild.setMeterId(new MeterId(BinContent.intToUnsignedLong(
                                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common
                                .types.rev130731.Meter.OFPMALL.getIntValue())));
                        caseBuilder.setMultipartRequestMeterConfig(mprMeterConfigBuild.build());

                        final Xid xid = deviceContext.getNextXid();
                        data.getRequestContext().setXid(xid);
                        MultipartRequestInputBuilder mprInput = RequestInputUtils
                                .createMultipartHeader(MultipartType.OFPMPMETERCONFIG, xid.getValue(), version);
                        mprInput.setMultipartRequestBody(caseBuilder.build());
                        Future<RpcResult<Void>> resultFromOFLib = deviceContext
                                .getPrimaryConnectionContext().getConnectionAdapter().multipartRequest(mprInput.build());
                        return JdkFutureAdapters
                                .listenInPoolThread(resultFromOFLib);
                    }});

    }

    @Override
    public Future<RpcResult<GetAllMeterStatisticsOutput>> getAllMeterStatistics(final GetAllMeterStatisticsInput input) {
        final RequestContext<GetAllMeterStatisticsOutput> requestContext = requestContextStack.createRequestContext();
        final SettableFuture<RpcResult<GetAllMeterStatisticsOutput>> result = requestContextStack.storeOrFail(requestContext);

        if (!result.isDone()) {

            final Xid xid = deviceContext.getNextXid();

            MultipartRequestMeterCaseBuilder caseBuilder =
                    new MultipartRequestMeterCaseBuilder();
            MultipartRequestMeterBuilder mprMeterBuild =
                    new MultipartRequestMeterBuilder();
            mprMeterBuild.setMeterId(new MeterId(BinContent.intToUnsignedLong(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common
                            .types.rev130731.Meter.OFPMALL.getIntValue())));
            caseBuilder.setMultipartRequestMeter(mprMeterBuild.build());

            MultipartRequestInputBuilder mprInput = RequestInputUtils
                    .createMultipartHeader(MultipartType.OFPMPMETER, xid.getValue(), version);
            mprInput.setMultipartRequestBody(caseBuilder.build());
            Future<RpcResult<Void>> resultFromOFLib = deviceContext.getPrimaryConnectionContext()
                    .getConnectionAdapter().multipartRequest(mprInput.build());

            ListenableFuture<RpcResult<Void>> futureResultFromOfLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

            final RpcResultConvertor<GetAllMeterStatisticsOutput> rpcResultConvertor = new RpcResultConvertor<>(requestContext, deviceContext);
            rpcResultConvertor.processResultFromOfJava(futureResultFromOfLib);
        } else {
            RequestContextUtil.closeRequstContext(requestContext);
        }
        return result;
    }

    @Override
    public Future<RpcResult<GetMeterFeaturesOutput>> getMeterFeatures(final GetMeterFeaturesInput input) {
        final RequestContext requestContext = requestContextStack.createRequestContext();
        final SettableFuture<RpcResult<GetMeterFeaturesOutput>> result = requestContextStack.storeOrFail(requestContext);

        if (!result.isDone()) {

            final Xid xid = deviceContext.getNextXid();

            MultipartRequestMeterFeaturesCaseBuilder mprMeterFeaturesBuild =
                    new MultipartRequestMeterFeaturesCaseBuilder();

            MultipartRequestInputBuilder mprInput =
                    RequestInputUtils.createMultipartHeader(MultipartType.OFPMPMETERFEATURES, xid.getValue(), version);
            mprInput.setMultipartRequestBody(mprMeterFeaturesBuild.build());
            Future<RpcResult<Void>> resultFromOFLib = deviceContext.getPrimaryConnectionContext().getConnectionAdapter().multipartRequest(mprInput.build());
            ListenableFuture<RpcResult<Void>> futureResultFromOfLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

            final RpcResultConvertor<GetMeterFeaturesOutput> rpcResultConvertor = new RpcResultConvertor<>(requestContext, deviceContext);
            rpcResultConvertor.processResultFromOfJava(futureResultFromOfLib);
        } else {
            RequestContextUtil.closeRequstContext(requestContext);
        }
        return result;
    }

    @Override
    public Future<RpcResult<GetMeterStatisticsOutput>> getMeterStatistics(final GetMeterStatisticsInput input) {
        final RequestContext requestContext = requestContextStack.createRequestContext();
        final SettableFuture<RpcResult<GetMeterStatisticsOutput>> result = requestContextStack.storeOrFail(requestContext);

        if (!result.isDone()) {

            final Xid xid = deviceContext.getNextXid();

            MultipartRequestMeterCaseBuilder caseBuilder =
                    new MultipartRequestMeterCaseBuilder();
            MultipartRequestMeterBuilder mprMeterBuild =
                    new MultipartRequestMeterBuilder();
            mprMeterBuild.setMeterId(new MeterId(input.getMeterId().getValue()));
            caseBuilder.setMultipartRequestMeter(mprMeterBuild.build());

            MultipartRequestInputBuilder mprInput =
                    RequestInputUtils.createMultipartHeader(MultipartType.OFPMPMETER, xid.getValue(), version);
            mprInput.setMultipartRequestBody(caseBuilder.build());
            Future<RpcResult<Void>> resultFromOFLib = deviceContext.getPrimaryConnectionContext().getConnectionAdapter().multipartRequest(mprInput.build());
            ListenableFuture<RpcResult<Void>> futureResultFromOfLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

            final RpcResultConvertor<GetMeterStatisticsOutput> rpcResultConvertor = new RpcResultConvertor<>(requestContext, deviceContext);
            rpcResultConvertor.processResultFromOfJava(futureResultFromOfLib);

        } else {
            RequestContextUtil.closeRequstContext(requestContext);
        }
        return result;
    }

}
