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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.udp.dst.grouping.UdpDstValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.UdpDstCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.UdpDstCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfUdpDstKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.udp.dst.grouping.NxmOfUdpDstBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.Uint16;

/**
 * Test for {@link UdpDstConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class UdpDstConvertorTest {
    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private static final PortNumber DEFAULT_PORT = new PortNumber(Uint16.valueOf(9999));

    private UdpDstConvertor udpDstConvertor;

    @Before
    public void setUp() {
        final NxmOfUdpDstBuilder nxmOfUdpDstBuilder = new NxmOfUdpDstBuilder()
                .setMask(Uint16.ONE)
                .setPort(DEFAULT_PORT);
        final NxAugMatchNodesNodeTableFlowBuilder nxAugMatchNotifUpdateFlowStatsBuilder =
                new NxAugMatchNodesNodeTableFlowBuilder();
        nxAugMatchNotifUpdateFlowStatsBuilder.setNxmOfUdpDst(nxmOfUdpDstBuilder.build());

        final Augmentation<Extension> extensionAugmentation = nxAugMatchNotifUpdateFlowStatsBuilder.build();
        when(extension.augmentation(ArgumentMatchers.any()))
            .thenReturn(extensionAugmentation);

        udpDstConvertor = new UdpDstConvertor();
    }

    @Test
    public void testConvert() {
        final MatchEntry converted = udpDstConvertor.convert(extension);
        Assert.assertEquals(Uint16.valueOf(1),
                ((UdpDstCaseValue) converted.getMatchEntryValue()).getUdpDstValues().getMask());
        Assert.assertEquals(DEFAULT_PORT,
                ((UdpDstCaseValue) converted.getMatchEntryValue()).getUdpDstValues().getPort());
    }

    @Test
    public void testConvert1() {
        final UdpDstValuesBuilder udpDstValuesBuilder = new UdpDstValuesBuilder()
                .setMask(Uint16.TWO)
                .setPort(DEFAULT_PORT);
        final UdpDstCaseValueBuilder udpDstCaseValueBuilder = new UdpDstCaseValueBuilder()
                .setUdpDstValues(udpDstValuesBuilder.build());

        final UdpDstCaseValue udpDstCaseValue = udpDstCaseValueBuilder.build();

        when(matchEntry.getMatchEntryValue()).thenReturn(udpDstCaseValue);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = udpDstConvertor.convert(matchEntry,
                MatchPath.PACKET_RECEIVED_MATCH);
        Assert.assertEquals(Uint16.valueOf(2),
                ((NxAugMatchNotifPacketIn) extensionAugment.getAugmentationObject()).getNxmOfUdpDst().getMask());
        Assert.assertEquals(DEFAULT_PORT,
                ((NxAugMatchNotifPacketIn) extensionAugment.getAugmentationObject()).getNxmOfUdpDst().getPort());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfUdpDstKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = udpDstConvertor
                .convert(matchEntry, MatchPath.SWITCH_FLOW_REMOVED_MATCH);
        Assert.assertEquals(Uint16.valueOf(2),
                ((NxAugMatchNotifSwitchFlowRemoved) extensionAugment1.getAugmentationObject()).getNxmOfUdpDst()
                        .getMask());
        Assert.assertEquals(DEFAULT_PORT, ((NxAugMatchNotifSwitchFlowRemoved) extensionAugment1.getAugmentationObject())
                .getNxmOfUdpDst().getPort());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfUdpDstKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = udpDstConvertor
                .convert(matchEntry, MatchPath.FLOWS_STATISTICS_UPDATE_MATCH);
        Assert.assertEquals(Uint16.valueOf(2),
                ((NxAugMatchNodesNodeTableFlow) extensionAugment2.getAugmentationObject()).getNxmOfUdpDst().getMask());
        Assert.assertEquals(DEFAULT_PORT,
                ((NxAugMatchNodesNodeTableFlow) extensionAugment2.getAugmentationObject()).getNxmOfUdpDst().getPort());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfUdpDstKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = udpDstConvertor
                .convert(matchEntry, MatchPath.FLOWS_STATISTICS_RPC_MATCH);
        Assert.assertEquals(Uint16.valueOf(2),
                ((NxAugMatchRpcGetFlowStats) extensionAugment3.getAugmentationObject()).getNxmOfUdpDst().getMask());
        Assert.assertEquals(DEFAULT_PORT,
                ((NxAugMatchRpcGetFlowStats) extensionAugment3.getAugmentationObject()).getNxmOfUdpDst().getPort());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfUdpDstKey.class);
    }
}
