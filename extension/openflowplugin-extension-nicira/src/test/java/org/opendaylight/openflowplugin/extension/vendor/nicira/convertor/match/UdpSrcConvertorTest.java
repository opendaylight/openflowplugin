/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.UdpSrcCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.UdpSrcCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.udp.src.grouping.UdpSrcValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfUdpSrcKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.udp.src.grouping.NxmOfUdpSrcBuilder;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.Uint16;

/**
 * Test for {@link UdpSrcConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class UdpSrcConvertorTest {
    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private static final PortNumber DEFAULT_PORT = new PortNumber(Uint16.valueOf(9999));

    private UdpSrcConvertor udpSrcConvertor;

    @Before
    public void setUp() {
        final NxmOfUdpSrcBuilder nxmOfUdpSrcBuilder = new NxmOfUdpSrcBuilder()
                .setMask(Uint16.ONE)
                .setPort(DEFAULT_PORT);
        final NxAugMatchNodesNodeTableFlowBuilder nxAugMatchNotifUpdateFlowStatsBuilder =
                new NxAugMatchNodesNodeTableFlowBuilder();
        nxAugMatchNotifUpdateFlowStatsBuilder.setNxmOfUdpSrc(nxmOfUdpSrcBuilder.build());

        final Augmentation<Extension> extensionAugmentation = nxAugMatchNotifUpdateFlowStatsBuilder.build();
        when(extension.augmentation(ArgumentMatchers.any()))
            .thenReturn(extensionAugmentation);

        udpSrcConvertor = new UdpSrcConvertor();
    }

    @Test
    public void testConvert() {
        final MatchEntry converted = udpSrcConvertor.convert(extension);
        Assert.assertEquals(Uint16.ONE,
                ((UdpSrcCaseValue) converted.getMatchEntryValue()).getUdpSrcValues().getMask());
        Assert.assertEquals(DEFAULT_PORT,
                ((UdpSrcCaseValue) converted.getMatchEntryValue()).getUdpSrcValues().getPort());
    }

    @Test
    public void testConvert1() {
        final UdpSrcValuesBuilder udpSrcValuesBuilder = new UdpSrcValuesBuilder()
                .setMask(Uint16.TWO)
                .setPort(DEFAULT_PORT);
        final UdpSrcCaseValueBuilder udpSrcCaseValueBuilder = new UdpSrcCaseValueBuilder()
                .setUdpSrcValues(udpSrcValuesBuilder.build());

        final UdpSrcCaseValue udpSrcCaseValue = udpSrcCaseValueBuilder.build();

        when(matchEntry.getMatchEntryValue()).thenReturn(udpSrcCaseValue);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = udpSrcConvertor.convert(matchEntry,
                MatchPath.PACKET_RECEIVED_MATCH);
        Assert.assertEquals(Uint16.TWO,
                ((NxAugMatchNotifPacketIn) extensionAugment.getAugmentationObject()).getNxmOfUdpSrc().getMask());
        Assert.assertEquals(DEFAULT_PORT,
                ((NxAugMatchNotifPacketIn) extensionAugment.getAugmentationObject()).getNxmOfUdpSrc().getPort());
        Assert.assertEquals(NxmOfUdpSrcKey.VALUE, extensionAugment.getKey());

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = udpSrcConvertor
                .convert(matchEntry, MatchPath.SWITCH_FLOW_REMOVED_MATCH);
        Assert.assertEquals(Uint16.TWO,
                ((NxAugMatchNotifSwitchFlowRemoved) extensionAugment1.getAugmentationObject()).getNxmOfUdpSrc()
                        .getMask());
        Assert.assertEquals(DEFAULT_PORT, ((NxAugMatchNotifSwitchFlowRemoved) extensionAugment1.getAugmentationObject())
                .getNxmOfUdpSrc().getPort());
        Assert.assertEquals(NxmOfUdpSrcKey.VALUE, extensionAugment.getKey());

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = udpSrcConvertor
                .convert(matchEntry, MatchPath.FLOWS_STATISTICS_UPDATE_MATCH);
        Assert.assertEquals(Uint16.TWO,
                ((NxAugMatchNodesNodeTableFlow) extensionAugment2.getAugmentationObject()).getNxmOfUdpSrc().getMask());
        Assert.assertEquals(DEFAULT_PORT,
                ((NxAugMatchNodesNodeTableFlow) extensionAugment2.getAugmentationObject()).getNxmOfUdpSrc().getPort());
        Assert.assertEquals(NxmOfUdpSrcKey.VALUE, extensionAugment.getKey());

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = udpSrcConvertor
                .convert(matchEntry, MatchPath.FLOWS_STATISTICS_RPC_MATCH);
        Assert.assertEquals(Uint16.TWO,
                ((NxAugMatchRpcGetFlowStats) extensionAugment3.getAugmentationObject()).getNxmOfUdpSrc().getMask());
        Assert.assertEquals(DEFAULT_PORT,
                ((NxAugMatchRpcGetFlowStats) extensionAugment3.getAugmentationObject()).getNxmOfUdpSrc().getPort());
        Assert.assertEquals(NxmOfUdpSrcKey.VALUE, extensionAugment.getKey());
    }
}
