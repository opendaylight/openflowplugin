/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.common;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.translator.TranslatorLibrarian;
import org.opendaylight.openflowplugin.impl.util.TranslatorLibraryUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandTypeBitmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterConfigCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.aggregate._case.MultipartReplyAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.MultipartReplyGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.MultipartReplyGroupDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.features._case.MultipartReplyGroupFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.MultipartReplyMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.MultipartReplyMeterConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.features._case.MultipartReplyMeterFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.MultipartReplyPortDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.multipart.reply.port.desc.PortsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.MultipartReplyPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.MultipartReplyQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.MultipartReplyTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.multipart.reply.table.TableStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.MultipartReplyTableFeaturesBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

@RunWith(MockitoJUnitRunner.class)
public class MultipartReplyTranslatorUtilTest {
    @Mock
    private DeviceInfo deviceInfo;
    private final ConvertorExecutor convertorExecutor = ConvertorManagerFactory.createDefaultManager();
    private TranslatorLibrarian translatorLibrarian;

    @Before
    public void setUp() {
        when(deviceInfo.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);

        translatorLibrarian = new TranslatorLibrarian() {
            private TranslatorLibrary translatorLibrary;

            @Override
            public TranslatorLibrary oook() {
                return translatorLibrary;
            }

            @Override
            public void setTranslatorLibrary(final TranslatorLibrary translatorLibrary) {
                this.translatorLibrary = translatorLibrary;
            }
        };

        TranslatorLibraryUtil.injectBasicTranslatorLibrary(translatorLibrarian, convertorExecutor);
    }

    private static MultipartReply buildReply(final MultipartType multipartType,
                                             final MultipartReplyBody multipartReplyBody) {
        return new MultipartReplyMessageBuilder()
                .setType(multipartType)
                .setMultipartReplyBody(multipartReplyBody)
                .build();
    }

    private void dummyAssertReply(final MultipartReply multipartReply) {
        final Optional<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart
                .reply.MultipartReplyBody> translatedReply = MultipartReplyTranslatorUtil
                .translate(multipartReply, deviceInfo, convertorExecutor, translatorLibrarian.oook());

        assertTrue(translatedReply.isPresent());
    }

    @Test
    public void translatePortDesc() {
        final MultipartReply multipartReply = buildReply(
                MultipartType.OFPMPPORTDESC,
                new MultipartReplyPortDescCaseBuilder()
                        .setMultipartReplyPortDesc(new MultipartReplyPortDescBuilder()
                                .setPorts(Collections.singletonList(new PortsBuilder()
                                        .build()))
                                .build())
                        .build());

        dummyAssertReply(multipartReply);
    }

    @Test
    public void translateTableFeatures() {
        final MultipartReply multipartReply = buildReply(
                MultipartType.OFPMPTABLEFEATURES,
                new MultipartReplyTableFeaturesCaseBuilder()
                        .setMultipartReplyTableFeatures(new MultipartReplyTableFeaturesBuilder()
                                .build())
                        .build());

        dummyAssertReply(multipartReply);
    }

    @Test
    public void translateDesc() {
        final MultipartReply multipartReply = buildReply(
                MultipartType.OFPMPDESC,
                new MultipartReplyDescCaseBuilder()
                        .setMultipartReplyDesc(new MultipartReplyDescBuilder()
                                .build())
                        .build());

        dummyAssertReply(multipartReply);
    }

    @Test
    public void translateFlow() {
        final MultipartReply multipartReply = buildReply(
                MultipartType.OFPMPFLOW,
                new MultipartReplyFlowCaseBuilder()
                        .setMultipartReplyFlow(new MultipartReplyFlowBuilder()
                                .build())
                        .build());

        dummyAssertReply(multipartReply);
    }

    @Test
    public void translateAggregate() {
        final MultipartReply multipartReply = buildReply(
                MultipartType.OFPMPAGGREGATE,
                new MultipartReplyAggregateCaseBuilder()
                        .setMultipartReplyAggregate(new MultipartReplyAggregateBuilder()
                                .setByteCount(Uint64.ONE)
                                .setFlowCount(Uint32.TEN)
                                .setPacketCount(Uint64.ONE)
                                .build())
                        .build());

        dummyAssertReply(multipartReply);
    }

    @Test
    public void translatePortStats() {
        final MultipartReply multipartReply = buildReply(
                MultipartType.OFPMPPORTSTATS,
                new MultipartReplyPortStatsCaseBuilder()
                        .setMultipartReplyPortStats(new MultipartReplyPortStatsBuilder()
                                .setPortStats(Collections.singletonList(new PortStatsBuilder()
                                        .build()))
                                .build())
                        .build());

        dummyAssertReply(multipartReply);
    }

    @Test
    public void translateGroup() {
        final MultipartReply multipartReply = buildReply(
                MultipartType.OFPMPGROUP,
                new MultipartReplyGroupCaseBuilder()
                        .setMultipartReplyGroup(new MultipartReplyGroupBuilder()
                                .build())
                        .build());

        dummyAssertReply(multipartReply);
    }

    @Test
    public void translateGroupDesc() {
        final MultipartReply multipartReply = buildReply(
                MultipartType.OFPMPGROUPDESC,
                new MultipartReplyGroupDescCaseBuilder()
                        .setMultipartReplyGroupDesc(new MultipartReplyGroupDescBuilder()
                                .build())
                        .build());

        dummyAssertReply(multipartReply);
    }

    @Test
    public void translateGroupFeatures() {
        final MultipartReply multipartReply = buildReply(
                MultipartType.OFPMPGROUPFEATURES,
                new MultipartReplyGroupFeaturesCaseBuilder()
                        .setMultipartReplyGroupFeatures(new MultipartReplyGroupFeaturesBuilder()
                                .setTypes(new GroupTypes(true, false, false, false))
                                .setCapabilities(new GroupCapabilities(false, true, true, false))
                                .setActionsBitmap(Collections.singletonList(new ActionType(
                                        true,
                                        false,
                                        false,
                                        false,
                                        false,
                                        false,
                                        false,
                                        false,
                                        false,
                                        false,
                                        false,
                                        false,
                                        false,
                                        false,
                                        false,
                                        false,
                                        false)))

                                .build())
                        .build());

        dummyAssertReply(multipartReply);
    }

    @Test
    public void translateMeter() {
        final MultipartReply multipartReply = buildReply(
                MultipartType.OFPMPMETER,
                new MultipartReplyMeterCaseBuilder()
                        .setMultipartReplyMeter(new MultipartReplyMeterBuilder()
                                .build())
                        .build());

        dummyAssertReply(multipartReply);
    }

    @Test
    public void translateMeterConfig() {
        final MultipartReply multipartReply = buildReply(
                MultipartType.OFPMPMETERCONFIG,
                new MultipartReplyMeterConfigCaseBuilder()
                        .setMultipartReplyMeterConfig(new MultipartReplyMeterConfigBuilder()
                                .build())
                        .build());

        dummyAssertReply(multipartReply);
    }

    @Test
    public void translateMeterFeatures() {
        final MultipartReply multipartReply = buildReply(
                MultipartType.OFPMPMETERFEATURES,
                new MultipartReplyMeterFeaturesCaseBuilder()
                        .setMultipartReplyMeterFeatures(new MultipartReplyMeterFeaturesBuilder()
                                .setMaxMeter(Uint32.TEN)
                                .setCapabilities(new MeterFlags(true, false, false, false))
                                .setBandTypes(new MeterBandTypeBitmap(true, false))
                                .build())
                        .build());

        dummyAssertReply(multipartReply);
    }

    @Test
    public void translateTable() {
        final MultipartReply multipartReply = buildReply(
                MultipartType.OFPMPTABLE,
                new MultipartReplyTableCaseBuilder()
                        .setMultipartReplyTable(new MultipartReplyTableBuilder()
                                .setTableStats(Collections.singletonList(new TableStatsBuilder()
                                        .setActiveCount(Uint32.TEN)
                                        .setLookupCount(Uint64.ONE)
                                        .setMatchedCount(Uint64.ONE)
                                        .setTableId(Uint8.TEN)
                                        .build()))
                                .build())
                        .build());

        dummyAssertReply(multipartReply);
    }

    @Test
    public void translateQueue() {
        final MultipartReply multipartReply = buildReply(
                MultipartType.OFPMPQUEUE,
                new MultipartReplyQueueCaseBuilder()
                        .setMultipartReplyQueue(new MultipartReplyQueueBuilder()
                                .setQueueStats(Collections.singletonList(new QueueStatsBuilder()
                                        .setTxErrors(Uint64.ONE)
                                        .setTxBytes(Uint64.ONE)
                                        .setTxPackets(Uint64.ONE)
                                        .setDurationNsec(Uint32.TEN)
                                        .setDurationSec(Uint32.TEN)
                                        .setQueueId(Uint32.TEN)
                                        .build()))
                                .build())
                        .build());

        dummyAssertReply(multipartReply);
    }
}