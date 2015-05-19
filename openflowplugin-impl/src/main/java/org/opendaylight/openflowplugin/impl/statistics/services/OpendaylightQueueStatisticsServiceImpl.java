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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.queue._case.MultipartRequestQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPortsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPortsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromGivenPortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromGivenPortOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetQueueStatisticsFromGivenPortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetQueueStatisticsFromGivenPortOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.OpendaylightQueueStatisticsService;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * @author joe
 */
public class OpendaylightQueueStatisticsServiceImpl extends CommonService implements OpendaylightQueueStatisticsService {

    public OpendaylightQueueStatisticsServiceImpl(final RequestContextStack requestContextStack, DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    @Override
    public Future<RpcResult<GetAllQueuesStatisticsFromAllPortsOutput>> getAllQueuesStatisticsFromAllPorts(
            final GetAllQueuesStatisticsFromAllPortsInput input) {
        return handleServiceCallNew(
                new Function<RequestContext<GetAllQueuesStatisticsFromAllPortsOutput>,
                ListenableFuture<RpcResult<GetAllQueuesStatisticsFromAllPortsOutput>>>() {

                    @Override
                    public ListenableFuture<RpcResult<GetAllQueuesStatisticsFromAllPortsOutput>> apply(final RequestContext<GetAllQueuesStatisticsFromAllPortsOutput> requestContext) {


                        MultipartRequestQueueCaseBuilder caseBuilder = new MultipartRequestQueueCaseBuilder();
                        MultipartRequestQueueBuilder mprQueueBuilder = new MultipartRequestQueueBuilder();
                        // Select all ports
                        // Select all the ports
                        mprQueueBuilder.setQueueId(OFConstants.OFPQ_ALL);
                        mprQueueBuilder.setPortNo(OFConstants.OFPP_ANY);
                        caseBuilder.setMultipartRequestQueue(mprQueueBuilder.build());

                        // Set request body to main multipart request
                        final Xid xid = requestContext.getXid();

                        MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                                MultipartType.OFPMPQUEUE, xid.getValue(), getVersion());
                        mprInput.setMultipartRequestBody(caseBuilder.build());
                        MultipartRequestInput multipartRequestInput = mprInput.build();

                        return StatisticsServiceUtil.getRpcResultListenableFuture(xid, multipartRequestInput, getDeviceContext());
                    }
                });

    }

    @Override
    public Future<RpcResult<GetAllQueuesStatisticsFromGivenPortOutput>> getAllQueuesStatisticsFromGivenPort(
            final GetAllQueuesStatisticsFromGivenPortInput input) {
        return handleServiceCallNew(
                new Function<RequestContext<GetAllQueuesStatisticsFromGivenPortOutput>, ListenableFuture<RpcResult<GetAllQueuesStatisticsFromGivenPortOutput>>>() {

                    @Override
                    public ListenableFuture<RpcResult<GetAllQueuesStatisticsFromGivenPortOutput>> apply(final RequestContext<GetAllQueuesStatisticsFromGivenPortOutput> requestContext) {

                        MultipartRequestQueueCaseBuilder caseBuilder = new MultipartRequestQueueCaseBuilder();
                        MultipartRequestQueueBuilder mprQueueBuilder = new MultipartRequestQueueBuilder();
                        // Select all queues
                        // Select specific port
                        final short version = getVersion();
                        mprQueueBuilder.setPortNo(InventoryDataServiceUtil.portNumberfromNodeConnectorId(
                                OpenflowVersion.get(version), input.getNodeConnectorId()));

                        mprQueueBuilder.setQueueId(OFConstants.OFPQ_ALL);
                        caseBuilder.setMultipartRequestQueue(mprQueueBuilder.build());

                        // Set request body to main multipart request
                        final Xid xid = requestContext.getXid();
                        MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                                MultipartType.OFPMPQUEUE, xid.getValue(), version);
                        mprInput.setMultipartRequestBody(caseBuilder.build());
                        MultipartRequestInput multipartRequestInput = mprInput.build();
                        return StatisticsServiceUtil.getRpcResultListenableFuture(xid, multipartRequestInput, getDeviceContext());
                    }
                });
    }

    @Override
    public Future<RpcResult<GetQueueStatisticsFromGivenPortOutput>> getQueueStatisticsFromGivenPort(
            final GetQueueStatisticsFromGivenPortInput input) {
        return handleServiceCallNew(
                new Function<RequestContext<GetQueueStatisticsFromGivenPortOutput>, ListenableFuture<RpcResult<GetQueueStatisticsFromGivenPortOutput>>>() {

                    @Override
                    public ListenableFuture<RpcResult<GetQueueStatisticsFromGivenPortOutput>> apply(final RequestContext<GetQueueStatisticsFromGivenPortOutput> requestContext) {

                        MultipartRequestQueueCaseBuilder caseBuilder = new MultipartRequestQueueCaseBuilder();
                        MultipartRequestQueueBuilder mprQueueBuilder = new MultipartRequestQueueBuilder();
                        // Select specific queue
                        mprQueueBuilder.setQueueId(input.getQueueId().getValue());
                        // Select specific port
                        final short version = getVersion();
                        mprQueueBuilder.setPortNo(InventoryDataServiceUtil.portNumberfromNodeConnectorId(
                                OpenflowVersion.get(version), input.getNodeConnectorId()));
                        caseBuilder.setMultipartRequestQueue(mprQueueBuilder.build());

                        // Set request body to main multipart request
                        final Xid xid = requestContext.getXid();
                        MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                                MultipartType.OFPMPQUEUE, xid.getValue(), version);
                        mprInput.setMultipartRequestBody(caseBuilder.build());
                        MultipartRequestInput multipartRequestInput = mprInput.build();
                        return StatisticsServiceUtil.getRpcResultListenableFuture(xid, multipartRequestInput, getDeviceContext());
                    }
                });
    }

}
