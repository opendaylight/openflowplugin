/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.MeterStatsResponseConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.nodes.node.meter.MeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.nodes.node.meter.MeterStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.MultipartReplyMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.MultipartRequestBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter._case.MultipartRequestMeterBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * The Meter direct statistics service.
 */
public class MeterDirectStatisticsService extends AbstractDirectStatisticsService<GetMeterStatisticsInput, GetMeterStatisticsOutput> {
    private final MeterStatsResponseConvertor meterStatsConvertor = new MeterStatsResponseConvertor();

    /**
     * Instantiates a new Meter direct statistics service.
     *
     * @param requestContextStack the request context stack
     * @param deviceContext       the device context
     */
    public MeterDirectStatisticsService(RequestContextStack requestContextStack, DeviceContext deviceContext) {
        super(MultipartType.OFPMPMETER, requestContextStack, deviceContext);
    }

    @Override
    protected MultipartRequestBody buildRequestBody(GetMeterStatisticsInput input) {
        final MultipartRequestMeterBuilder mprMeterBuild = new MultipartRequestMeterBuilder();

        if (input.getMeterId() != null) {
            mprMeterBuild.setMeterId(new MeterId(input.getMeterId().getValue()));
        } else {
            mprMeterBuild.setMeterId(new MeterId(OFConstants.OFPM_ALL));
        }

        return new MultipartRequestMeterCaseBuilder()
                .setMultipartRequestMeter(mprMeterBuild.build())
                .build();
    }

    @Override
    protected GetMeterStatisticsOutput buildReply(List<MultipartReply> input, boolean success) {
        final List<MeterStats> meterStats = new ArrayList<>();

        if (success) {
            for (final MultipartReply mpReply : input) {
                final MultipartReplyMeterCase caseBody = (MultipartReplyMeterCase) mpReply.getMultipartReplyBody();
                final MultipartReplyMeter replyBody = caseBody.getMultipartReplyMeter();
                meterStats.addAll(meterStatsConvertor.toSALMeterStatsList(replyBody.getMeterStats()));
            }
        }

        return new GetMeterStatisticsOutputBuilder()
                .setMeterStats(meterStats)
                .build();
    }

    @Override
    protected void storeStatistics(GetMeterStatisticsOutput output) throws Exception {
        final InstanceIdentifier<FlowCapableNode> nodePath = getDeviceInfo().getNodeInstanceIdentifier().augmentation(FlowCapableNode.class);

        for (final MeterStats meterStatistics : output.getMeterStats()) {
            final InstanceIdentifier<MeterStatistics> meterPath = nodePath
                    .child(Meter.class, new MeterKey(meterStatistics.getMeterId()))
                    .augmentation(NodeMeterStatistics.class)
                    .child(MeterStatistics.class);

            final MeterStatistics stats = new MeterStatisticsBuilder(meterStatistics).build();
            getTxFacade().writeToTransactionWithParentsSlow(LogicalDatastoreType.OPERATIONAL, meterPath, stats);
        }
    }
}
