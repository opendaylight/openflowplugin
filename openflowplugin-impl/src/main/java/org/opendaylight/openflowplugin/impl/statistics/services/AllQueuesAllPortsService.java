/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.AbstractSimpleService;
import org.opendaylight.openflowplugin.impl.services.RequestInputUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.queue._case.MultipartRequestQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPortsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPortsOutput;

final class AllQueuesAllPortsService extends AbstractSimpleService<GetAllQueuesStatisticsFromAllPortsInput, GetAllQueuesStatisticsFromAllPortsOutput> {
    private static final MultipartRequestQueueCase QUEUE_CASE;

    static {
        MultipartRequestQueueCaseBuilder caseBuilder = new MultipartRequestQueueCaseBuilder();
        MultipartRequestQueueBuilder mprQueueBuilder = new MultipartRequestQueueBuilder();
        // Select all ports
        // Select all the ports
        mprQueueBuilder.setQueueId(OFConstants.OFPQ_ALL);
        mprQueueBuilder.setPortNo(OFConstants.OFPP_ANY);
        caseBuilder.setMultipartRequestQueue(mprQueueBuilder.build());

        QUEUE_CASE = caseBuilder.build();
    }

    AllQueuesAllPortsService(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext, GetAllQueuesStatisticsFromAllPortsOutput.class);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetAllQueuesStatisticsFromAllPortsInput input) {
        // Set request body to main multipart request
        MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                MultipartType.OFPMPQUEUE, xid.getValue(), getVersion());
        mprInput.setMultipartRequestBody(QUEUE_CASE);
        return mprInput.build();
    }
}
