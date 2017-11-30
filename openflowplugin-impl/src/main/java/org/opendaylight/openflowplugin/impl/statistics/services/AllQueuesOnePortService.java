/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.impl.services.util.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.AbstractCompatibleStatService;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.QueueStatisticsToNotificationTransformer;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.queue._case.MultipartRequestQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromGivenPortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromGivenPortOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromGivenPortOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.QueueStatisticsUpdate;

final class AllQueuesOnePortService
        extends AbstractCompatibleStatService<GetAllQueuesStatisticsFromGivenPortInput, GetAllQueuesStatisticsFromGivenPortOutput, QueueStatisticsUpdate> {

    public AllQueuesOnePortService(RequestContextStack requestContextStack, DeviceContext deviceContext, AtomicLong compatibilityXidSeed) {
        super(requestContextStack, deviceContext, compatibilityXidSeed);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetAllQueuesStatisticsFromGivenPortInput input) throws ServiceException {
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
        MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                MultipartType.OFPMPQUEUE, xid.getValue(), version);
        mprInput.setMultipartRequestBody(caseBuilder.build());

        return mprInput.build();
    }

    @Override
    public GetAllQueuesStatisticsFromGivenPortOutput buildTxCapableResult(TransactionId emulatedTxId) {
        return new GetAllQueuesStatisticsFromGivenPortOutputBuilder().setTransactionId(emulatedTxId).build();
    }

    @Override
    public QueueStatisticsUpdate transformToNotification(List<MultipartReply> result, TransactionId emulatedTxId) {
        return QueueStatisticsToNotificationTransformer.transformToNotification(result, getDeviceInfo(), getOfVersion(), emulatedTxId);
    }
}
