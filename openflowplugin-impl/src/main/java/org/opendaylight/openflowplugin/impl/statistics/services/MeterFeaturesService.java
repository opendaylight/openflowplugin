/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.util.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.AbstractCompatibleStatService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterFeaturesUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterFeaturesUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandDrop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandDscpRemark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBurst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterKbps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterPktps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandTypeBitmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.features._case.MultipartReplyMeterFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterFeaturesCaseBuilder;

final class MeterFeaturesService
        extends AbstractCompatibleStatService<GetMeterFeaturesInput, GetMeterFeaturesOutput, MeterFeaturesUpdated> {
    private static final MultipartRequestMeterFeaturesCase METER_FEATURES_CASE =
            new MultipartRequestMeterFeaturesCaseBuilder().build();

    MeterFeaturesService(final RequestContextStack requestContextStack,
                                final DeviceContext deviceContext,
                                final AtomicLong compatibilityXidSeed) {
        super(requestContextStack, deviceContext, compatibilityXidSeed);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetMeterFeaturesInput input) {
        MultipartRequestInputBuilder mprInput =
                RequestInputUtils.createMultipartHeader(MultipartType.OFPMPMETERFEATURES, xid.getValue(), getVersion());
        mprInput.setMultipartRequestBody(METER_FEATURES_CASE);
        return mprInput.build();
    }

    @Override
    public GetMeterFeaturesOutput buildTxCapableResult(final TransactionId emulatedTxId) {
        return new GetMeterFeaturesOutputBuilder().setTransactionId(emulatedTxId).build();
    }

    @Override
    public MeterFeaturesUpdated transformToNotification(final List<MultipartReply> result,
            final TransactionId emulatedTxId) {
        final int mpSize = result.size();
        Preconditions.checkArgument(mpSize == 1, "unexpected (!=1) mp-reply size received: %s", mpSize);

        MeterFeaturesUpdatedBuilder notification = new MeterFeaturesUpdatedBuilder();
        notification.setId(getDeviceInfo().getNodeId());
        notification.setMoreReplies(Boolean.FALSE);
        notification.setTransactionId(emulatedTxId);

        MultipartReplyMeterFeaturesCase caseBody =
                (MultipartReplyMeterFeaturesCase) result.get(0).getMultipartReplyBody();
        MultipartReplyMeterFeatures replyBody = caseBody.getMultipartReplyMeterFeatures();
        notification.setMaxBands(replyBody.getMaxBands());
        notification.setMaxColor(replyBody.getMaxColor());
        notification.setMaxMeter(new Counter32(replyBody.getMaxMeter()));
        notification.setMeterCapabilitiesSupported(extractMeterCapabilities(replyBody.getCapabilities()));
        notification.setMeterBandSupported(extractSupportedMeterBand(replyBody, replyBody.getBandTypes()));

        return notification.build();
    }

    @VisibleForTesting
    protected static Set<MeterBand> extractSupportedMeterBand(
            final MultipartReplyMeterFeatures replyBody, final MeterBandTypeBitmap bandTypes) {
        final var supportedMeterBand = ImmutableSet.<MeterBand>builder();
        if (bandTypes.getOFPMBTDROP()) {
            supportedMeterBand.add(MeterBandDrop.VALUE);
        }
        if (replyBody.getBandTypes().getOFPMBTDSCPREMARK()) {
            supportedMeterBand.add(MeterBandDscpRemark.VALUE);
        }
        return supportedMeterBand.build();
    }

    @VisibleForTesting
    protected static Set<MeterCapability> extractMeterCapabilities(final MeterFlags capabilities) {
        final var supportedCapabilities = ImmutableSet.<MeterCapability>builder();
        if (capabilities.getOFPMFBURST()) {
            supportedCapabilities.add(MeterBurst.VALUE);
        }
        if (capabilities.getOFPMFKBPS()) {
            supportedCapabilities.add(MeterKbps.VALUE);
        }
        if (capabilities.getOFPMFPKTPS()) {
            supportedCapabilities.add(MeterPktps.VALUE);
        }
        if (capabilities.getOFPMFSTATS()) {
            supportedCapabilities.add(MeterStats.VALUE);
        }
        return supportedCapabilities.build();
    }
}
