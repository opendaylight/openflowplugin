/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.openflowjava.protocol.api.util.BinContent;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.util.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.AbstractCompatibleStatService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterConfigStatsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterConfigStatsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.config.stats.reply.MeterConfigStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.config.stats.reply.MeterConfigStatsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterConfigCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.MultipartReplyMeterConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterConfigCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterConfigCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter.config._case.MultipartRequestMeterConfigBuilder;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint32;

final class AllMeterConfigStatsService
        extends AbstractCompatibleStatService<GetAllMeterConfigStatisticsInput,
                                              GetAllMeterConfigStatisticsOutput,
                                              MeterConfigStatsUpdated> {
    private static final MultipartRequestMeterConfigCase METER_CONFIG_CASE =
        new MultipartRequestMeterConfigCaseBuilder()
            .setMultipartRequestMeterConfig(new MultipartRequestMeterConfigBuilder()
                .setMeterId(new MeterId(Uint32.valueOf(BinContent.intToUnsignedLong(Meter.OFPMALL.getIntValue()))))
                .build())
            .build();

    private final ConvertorExecutor convertorExecutor;
    private final VersionConvertorData data;

    AllMeterConfigStatsService(final RequestContextStack requestContextStack,
                                      final DeviceContext deviceContext,
                                      final AtomicLong compatibilityXidSeed,
                                      final ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext, compatibilityXidSeed);
        this.convertorExecutor = convertorExecutor;
        data = new VersionConvertorData(getVersion());
    }

    @Override
    protected OfHeader buildRequest(final Xid xid,
                                    final GetAllMeterConfigStatisticsInput input) {
        MultipartRequestInputBuilder mprInput = RequestInputUtils
                .createMultipartHeader(MultipartType.OFPMPMETERCONFIG, xid.getValue(), getVersion());
        return mprInput.setMultipartRequestBody(METER_CONFIG_CASE).build();
    }

    @Override
    public GetAllMeterConfigStatisticsOutput buildTxCapableResult(final TransactionId emulatedTxId) {
        return new GetAllMeterConfigStatisticsOutputBuilder().setTransactionId(emulatedTxId).build();
    }

    @Override
    public MeterConfigStatsUpdated transformToNotification(final List<MultipartReply> result,
            final TransactionId emulatedTxId) {
        final var stats = BindingMap.<MeterConfigStatsKey, MeterConfigStats>orderedBuilder();
        for (MultipartReply mpReply : result) {
            MultipartReplyMeterConfigCase caseBody = (MultipartReplyMeterConfigCase) mpReply.getMultipartReplyBody();
            MultipartReplyMeterConfig replyBody = caseBody.getMultipartReplyMeterConfig();

            final Optional<List<MeterConfigStats>> meterConfigStatsList =
                    convertorExecutor.convert(replyBody.getMeterConfig(), data);
            meterConfigStatsList.ifPresent(stats::addAll);
        }

        return new MeterConfigStatsUpdatedBuilder()
            .setId(getDeviceInfo().getNodeId())
            .setMoreReplies(Boolean.FALSE)
            .setTransactionId(emulatedTxId)
            .setMeterConfigStats(stats.build())
            .build();
    }
}
