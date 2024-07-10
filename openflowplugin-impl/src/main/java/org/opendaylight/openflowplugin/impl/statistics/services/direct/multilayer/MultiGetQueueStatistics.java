/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.direct.multilayer;

import java.util.List;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.services.util.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractGetQueueStatistics;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.QueueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.duration.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.queue._case.MultipartRequestQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMapKey;
import org.opendaylight.yangtools.binding.util.BindingMap;

public final class MultiGetQueueStatistics extends AbstractGetQueueStatistics<MultipartReply> {
    public MultiGetQueueStatistics(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
            final ConvertorExecutor convertorExecutor, final MultipartWriterProvider statisticsWriterProvider) {
        super(requestContextStack, deviceContext, convertorExecutor, statisticsWriterProvider);
    }

    @Override
    protected GetQueueStatisticsOutput buildReply(final List<MultipartReply> input, final boolean success) {
        if (!success) {
            return new GetQueueStatisticsOutputBuilder().build();
        }

        final var queueIdAndStatisticsMap =
            BindingMap.<QueueIdAndStatisticsMapKey, QueueIdAndStatisticsMap>orderedBuilder();

        for (var mpReply : input) {
            final var caseBody = (MultipartReplyQueueCase) mpReply.getMultipartReplyBody();
            final var replyBody = caseBody.getMultipartReplyQueue();

            for (var queueStats : replyBody.nonnullQueueStats()) {
                queueIdAndStatisticsMap.add(new QueueIdAndStatisticsMapBuilder()
                    .setNodeConnectorId(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(
                        getDatapathId(), queueStats.getPortNo(), getOfVersion()))
                    .setQueueId(new QueueId(queueStats.getQueueId()))
                    .setTransmissionErrors(new Counter64(queueStats.getTxErrors()))
                    .setTransmittedBytes(new Counter64(queueStats.getTxBytes()))
                    .setTransmittedPackets(new Counter64(queueStats.getTxPackets()))
                    .setDuration(new DurationBuilder()
                        .setSecond(new Counter32(queueStats.getDurationSec()))
                        .setNanosecond(new Counter32(queueStats.getDurationNsec()))
                        .build())
                    .build());
            }
        }

        return new GetQueueStatisticsOutputBuilder()
            .setQueueIdAndStatisticsMap(queueIdAndStatisticsMap.build())
            .build();
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetQueueStatisticsInput input) {
        final var mprQueueBuilder = new MultipartRequestQueueBuilder();
        if (input.getQueueId() != null) {
            mprQueueBuilder.setQueueId(input.getQueueId().getValue());
        } else {
            mprQueueBuilder.setQueueId(OFConstants.OFPQ_ALL);
        }

        if (input.getNodeConnectorId() != null) {
            mprQueueBuilder.setPortNo(InventoryDataServiceUtil.portNumberfromNodeConnectorId(getOfVersion(),
                    input.getNodeConnectorId()));
        } else {
            mprQueueBuilder.setPortNo(OFConstants.OFPP_ANY);
        }

        return RequestInputUtils.createMultipartHeader(getMultipartType(), xid.getValue(), getVersion())
            .setMultipartRequestBody(new MultipartRequestQueueCaseBuilder()
                .setMultipartRequestQueue(mprQueueBuilder.build())
                .build())
            .build();
    }

}
