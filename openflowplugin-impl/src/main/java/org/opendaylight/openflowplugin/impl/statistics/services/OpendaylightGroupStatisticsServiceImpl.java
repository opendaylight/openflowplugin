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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.OpendaylightGroupStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.group._case.MultipartRequestGroupBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * @author joe
 */
public class OpendaylightGroupStatisticsServiceImpl extends CommonService implements OpendaylightGroupStatisticsService {


    public OpendaylightGroupStatisticsServiceImpl(final RequestContextStack requestContextStack, DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    @Override
    public Future<RpcResult<GetAllGroupStatisticsOutput>> getAllGroupStatistics(final GetAllGroupStatisticsInput input) {


        return this.<GetAllGroupStatisticsOutput, Void>handleServiceCall(PRIMARY_CONNECTION,
                new Function<DataCrate<GetAllGroupStatisticsOutput>, ListenableFuture<RpcResult<Void>>>() {

                    @Override
                    public ListenableFuture<RpcResult<Void>> apply(final DataCrate<GetAllGroupStatisticsOutput> data) {

                        final MultipartRequestGroupCaseBuilder caseBuilder = new MultipartRequestGroupCaseBuilder();
                        final MultipartRequestGroupBuilder mprGroupBuild = new MultipartRequestGroupBuilder();
                        mprGroupBuild.setGroupId(new GroupId(
                                BinContent
                                        .intToUnsignedLong(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Group.OFPGALL
                                                .getIntValue())));
                        caseBuilder.setMultipartRequestGroup(mprGroupBuild.build());

                        // Create multipart request header
                        final Xid xid = data.getRequestContext().getXid();
                        final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                                MultipartType.OFPMPGROUP, xid.getValue(), version);

                        // Set request body to main multipart request
                        mprInput.setMultipartRequestBody(caseBuilder.build());

                        // Send the request, no cookies associated, use any connection

                        final Future<RpcResult<Void>> resultFromOFLib = deviceContext.getPrimaryConnectionContext()
                                .getConnectionAdapter().multipartRequest(mprInput.build());
                        return JdkFutureAdapters.listenInPoolThread(resultFromOFLib);
                    }
                });

    }

    @Override
    public Future<RpcResult<GetGroupDescriptionOutput>> getGroupDescription(final GetGroupDescriptionInput input) {
        return this.<GetGroupDescriptionOutput, Void>handleServiceCall(PRIMARY_CONNECTION,
                new Function<DataCrate<GetGroupDescriptionOutput>, ListenableFuture<RpcResult<Void>>>() {

                    @Override
                    public ListenableFuture<RpcResult<Void>> apply(final DataCrate<GetGroupDescriptionOutput> data) {
                        final MultipartRequestGroupDescCaseBuilder mprGroupDescCaseBuild = new MultipartRequestGroupDescCaseBuilder();

                        final Xid xid = data.getRequestContext().getXid();
                        final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                                MultipartType.OFPMPGROUPDESC, xid.getValue(), version);
                        mprInput.setMultipartRequestBody(mprGroupDescCaseBuild.build());
                        final Future<RpcResult<Void>> resultFromOFLib = deviceContext.getPrimaryConnectionContext()
                                .getConnectionAdapter().multipartRequest(mprInput.build());
                        return JdkFutureAdapters.listenInPoolThread(resultFromOFLib);
                    }
                });

    }

    @Override
    public Future<RpcResult<GetGroupFeaturesOutput>> getGroupFeatures(final GetGroupFeaturesInput input) {
        return this.<GetGroupFeaturesOutput, Void>handleServiceCall(PRIMARY_CONNECTION,
                new Function<DataCrate<GetGroupFeaturesOutput>, ListenableFuture<RpcResult<Void>>>() {

                    @Override
                    public ListenableFuture<RpcResult<Void>> apply(final DataCrate<GetGroupFeaturesOutput> data) {
                        final MultipartRequestGroupFeaturesCaseBuilder mprGroupFeaturesBuild = new MultipartRequestGroupFeaturesCaseBuilder();

                        final Xid xid = data.getRequestContext().getXid();
                        final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                                MultipartType.OFPMPGROUPFEATURES, xid.getValue(), version);
                        mprInput.setMultipartRequestBody(mprGroupFeaturesBuild.build());
                        final Future<RpcResult<Void>> resultFromOFLib = deviceContext.getPrimaryConnectionContext()
                                .getConnectionAdapter().multipartRequest(mprInput.build());
                        return JdkFutureAdapters.listenInPoolThread(resultFromOFLib);
                    }
                });

    }

    @Override
    public Future<RpcResult<GetGroupStatisticsOutput>> getGroupStatistics(final GetGroupStatisticsInput input) {
        return this.<GetGroupStatisticsOutput, Void>handleServiceCall(PRIMARY_CONNECTION,
                new Function<DataCrate<GetGroupStatisticsOutput>, ListenableFuture<RpcResult<Void>>>() {

                    @Override
                    public ListenableFuture<RpcResult<Void>> apply(final DataCrate<GetGroupStatisticsOutput> data) {

                        final MultipartRequestGroupCaseBuilder caseBuilder = new MultipartRequestGroupCaseBuilder();
                        final MultipartRequestGroupBuilder mprGroupBuild = new MultipartRequestGroupBuilder();
                        mprGroupBuild.setGroupId(new GroupId(input.getGroupId().getValue()));
                        caseBuilder.setMultipartRequestGroup(mprGroupBuild.build());

                        final Xid xid = data.getRequestContext().getXid();
                        final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                                MultipartType.OFPMPGROUP, xid.getValue(), version);

                        mprInput.setMultipartRequestBody(caseBuilder.build());
                        final Future<RpcResult<Void>> resultFromOFLib = deviceContext.getPrimaryConnectionContext()
                                .getConnectionAdapter().multipartRequest(mprInput.build());
                        return JdkFutureAdapters.listenInPoolThread(resultFromOFLib);
                    }
                });
    }

}
