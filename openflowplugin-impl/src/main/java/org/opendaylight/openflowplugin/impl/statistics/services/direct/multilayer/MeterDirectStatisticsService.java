/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.direct.multilayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.services.util.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractMeterDirectStatisticsService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.MultipartReplyMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter._case.MultipartRequestMeterBuilder;

public class MeterDirectStatisticsService extends AbstractMeterDirectStatisticsService<MultipartReply> {

    private final VersionConvertorData data;

    public MeterDirectStatisticsService(final RequestContextStack requestContextStack,
                                        final DeviceContext deviceContext,
                                        final ConvertorExecutor convertorExecutor,
                                        final MultipartWriterProvider statisticsWriterProvider) {
        super(requestContextStack, deviceContext, convertorExecutor, statisticsWriterProvider);
        data = new VersionConvertorData(getVersion());
    }

    @Override
    protected GetMeterStatisticsOutput buildReply(List<MultipartReply> input, boolean success) {
        final List<MeterStats> meterStats = new ArrayList<>();

        if (success) {
            for (final MultipartReply mpReply : input) {
                final MultipartReplyMeterCase caseBody = (MultipartReplyMeterCase) mpReply.getMultipartReplyBody();
                final MultipartReplyMeter replyBody = caseBody.getMultipartReplyMeter();
                final Optional<List<MeterStats>> meterStatsList = getConvertorExecutor().convert(replyBody.getMeterStats(), data);
                meterStatsList.ifPresent(meterStats::addAll);
            }
        }

        return new GetMeterStatisticsOutputBuilder()
            .setMeterStats(meterStats)
            .build();
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetMeterStatisticsInput input) {
        final MultipartRequestMeterBuilder mprMeterBuild = new MultipartRequestMeterBuilder();

        if (input.getMeterId() != null) {
            mprMeterBuild.setMeterId(new MeterId(input.getMeterId().getValue()));
        } else {
            mprMeterBuild.setMeterId(new MeterId(OFConstants.OFPM_ALL));
        }

        return RequestInputUtils.createMultipartHeader(getMultipartType(), xid.getValue(), getVersion())
            .setMultipartRequestBody(new MultipartRequestMeterCaseBuilder()
                .setMultipartRequestMeter(mprMeterBuild.build())
                .build())
            .build();
    }

}
