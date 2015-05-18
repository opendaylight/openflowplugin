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
import org.opendaylight.openflowjava.protocol.api.util.BinContent;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.CommonService;
import org.opendaylight.openflowplugin.impl.services.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.util.StatisticsServiceUtil;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
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


        return this.<GetAllGroupStatisticsOutput, Void>handleServiceCall(new Function<RequestContext<GetAllGroupStatisticsOutput>, ListenableFuture<RpcResult<Void>>>() {

            @Override
            public ListenableFuture<RpcResult<Void>> apply(final RequestContext<GetAllGroupStatisticsOutput> requestContext) {

                final MultipartRequestGroupCaseBuilder caseBuilder = new MultipartRequestGroupCaseBuilder();
                final MultipartRequestGroupBuilder mprGroupBuild = new MultipartRequestGroupBuilder();
                mprGroupBuild.setGroupId(new GroupId(
                        BinContent
                                .intToUnsignedLong(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Group.OFPGALL
                                        .getIntValue())));
                caseBuilder.setMultipartRequestGroup(mprGroupBuild.build());

                // Create multipart request header
                final Xid xid = requestContext.getXid();
                final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                        MultipartType.OFPMPGROUP, xid.getValue(), getVersion());

                // Set request body to main multipart request
                mprInput.setMultipartRequestBody(caseBuilder.build());

                // Send the request, no cookies associated, use any connection

                MultipartRequestInput multipartRequestInput = mprInput.build();

                return StatisticsServiceUtil.getRpcResultListenableFuture(xid, multipartRequestInput, getDeviceContext());
            }
        });

    }


    @Override
    public Future<RpcResult<GetGroupDescriptionOutput>> getGroupDescription(final GetGroupDescriptionInput input) {
        return this.<GetGroupDescriptionOutput, Void>handleServiceCall(
                new Function<RequestContext<GetGroupDescriptionOutput>, ListenableFuture<RpcResult<Void>>>() {

                    @Override
                    public ListenableFuture<RpcResult<Void>> apply(final RequestContext<GetGroupDescriptionOutput> requestContext) {
                        final MultipartRequestGroupDescCaseBuilder mprGroupDescCaseBuild = new MultipartRequestGroupDescCaseBuilder();

                        final Xid xid = requestContext.getXid();
                        final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                                MultipartType.OFPMPGROUPDESC, xid.getValue(), getVersion());
                        mprInput.setMultipartRequestBody(mprGroupDescCaseBuild.build());
                        MultipartRequestInput multipartRequestInput = mprInput.build();
                        return StatisticsServiceUtil.getRpcResultListenableFuture(xid, multipartRequestInput, getDeviceContext());
                    }
                });

    }

    @Override
    public Future<RpcResult<GetGroupFeaturesOutput>> getGroupFeatures(final GetGroupFeaturesInput input) {
        return this.<GetGroupFeaturesOutput, Void>handleServiceCall(
                new Function<RequestContext<GetGroupFeaturesOutput>, ListenableFuture<RpcResult<Void>>>() {

                    @Override
                    public ListenableFuture<RpcResult<Void>> apply(final RequestContext<GetGroupFeaturesOutput> requestContext) {
                        final MultipartRequestGroupFeaturesCaseBuilder mprGroupFeaturesBuild = new MultipartRequestGroupFeaturesCaseBuilder();

                        final Xid xid = requestContext.getXid();
                        final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                                MultipartType.OFPMPGROUPFEATURES, xid.getValue(), getVersion());
                        mprInput.setMultipartRequestBody(mprGroupFeaturesBuild.build());
                        MultipartRequestInput multipartRequestInput = mprInput.build();
                        return StatisticsServiceUtil.getRpcResultListenableFuture(xid, multipartRequestInput, getDeviceContext());
                    }
                });

    }

    @Override
    public Future<RpcResult<GetGroupStatisticsOutput>> getGroupStatistics(final GetGroupStatisticsInput input) {
        return this.<GetGroupStatisticsOutput, Void>handleServiceCall(
                new Function<RequestContext<GetGroupStatisticsOutput>, ListenableFuture<RpcResult<Void>>>() {

                    @Override
                    public ListenableFuture<RpcResult<Void>> apply(final RequestContext<GetGroupStatisticsOutput> requestContext) {

                        final MultipartRequestGroupCaseBuilder caseBuilder = new MultipartRequestGroupCaseBuilder();
                        final MultipartRequestGroupBuilder mprGroupBuild = new MultipartRequestGroupBuilder();
                        mprGroupBuild.setGroupId(new GroupId(input.getGroupId().getValue()));
                        caseBuilder.setMultipartRequestGroup(mprGroupBuild.build());

                        final Xid xid = requestContext.getXid();
                        final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                                MultipartType.OFPMPGROUP, xid.getValue(), getVersion());

                        mprInput.setMultipartRequestBody(caseBuilder.build());
                        MultipartRequestInput multipartRequestInput = mprInput.build();
                        return StatisticsServiceUtil.getRpcResultListenableFuture(xid, multipartRequestInput, getDeviceContext());
                    }
                });
    }

}
