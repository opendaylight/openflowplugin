/**
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
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.udp.src.grouping.UdpSrcValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.UdpSrcCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.UdpSrcCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfUdpSrcKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.udp.src.grouping.NxmOfUdpSrcBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * Test for {@link UdpSrcConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class UdpSrcConvertorTest {
    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private static final PortNumber DEFAULT_PORT = new PortNumber(9999);

    private UdpSrcConvertor udpSrcConvertor;

    @Before
    public void setUp() throws Exception {
        final NxmOfUdpSrcBuilder nxmOfUdpSrcBuilder = new NxmOfUdpSrcBuilder()
                .setMask(1)
                .setPort(DEFAULT_PORT);
        final NxAugMatchNodesNodeTableFlowBuilder nxAugMatchNotifUpdateFlowStatsBuilder = new NxAugMatchNodesNodeTableFlowBuilder();
        nxAugMatchNotifUpdateFlowStatsBuilder.setNxmOfUdpSrc(nxmOfUdpSrcBuilder.build());

        final Augmentation<Extension> extensionAugmentation = nxAugMatchNotifUpdateFlowStatsBuilder.build();
        when(extension.getAugmentation(Matchers.<Class<Augmentation<Extension>>>any())).thenReturn(extensionAugmentation);

        udpSrcConvertor = new UdpSrcConvertor();
    }

    @Test
    public void testConvert() throws Exception {
        final MatchEntry matchEntry = udpSrcConvertor.convert(extension);
        Assert.assertEquals(Integer.valueOf(1), ((UdpSrcCaseValue)matchEntry.getMatchEntryValue()).getUdpSrcValues().getMask());
        Assert.assertEquals(DEFAULT_PORT, ((UdpSrcCaseValue)matchEntry.getMatchEntryValue()).getUdpSrcValues().getPort());
    }

    @Test
    public void testConvert1() throws Exception {
        final UdpSrcValuesBuilder udpSrcValuesBuilder = new UdpSrcValuesBuilder()
                .setMask(2)
                .setPort(DEFAULT_PORT);
        final UdpSrcCaseValueBuilder udpSrcCaseValueBuilder = new UdpSrcCaseValueBuilder()
                .setUdpSrcValues(udpSrcValuesBuilder.build());

        final UdpSrcCaseValue udpSrcCaseValue = udpSrcCaseValueBuilder.build();

        when(matchEntry.getMatchEntryValue()).thenReturn(udpSrcCaseValue);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = udpSrcConvertor.convert(matchEntry, MatchPath.PACKETRECEIVED_MATCH);
        Assert.assertEquals(Integer.valueOf(2), ((NxAugMatchNotifPacketIn)extensionAugment.getAugmentationObject()).getNxmOfUdpSrc().getMask());
        Assert.assertEquals(DEFAULT_PORT, ((NxAugMatchNotifPacketIn)extensionAugment.getAugmentationObject()).getNxmOfUdpSrc().getPort());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfUdpSrcKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = udpSrcConvertor.convert(matchEntry, MatchPath.SWITCHFLOWREMOVED_MATCH);
        Assert.assertEquals(Integer.valueOf(2), ((NxAugMatchNotifSwitchFlowRemoved)extensionAugment1.getAugmentationObject()).getNxmOfUdpSrc().getMask());
        Assert.assertEquals(DEFAULT_PORT, ((NxAugMatchNotifSwitchFlowRemoved)extensionAugment1.getAugmentationObject()).getNxmOfUdpSrc().getPort());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfUdpSrcKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = udpSrcConvertor.convert(matchEntry, MatchPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(Integer.valueOf(2), ((NxAugMatchNodesNodeTableFlow)extensionAugment2.getAugmentationObject()).getNxmOfUdpSrc().getMask());
        Assert.assertEquals(DEFAULT_PORT, ((NxAugMatchNodesNodeTableFlow)extensionAugment2.getAugmentationObject()).getNxmOfUdpSrc().getPort());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfUdpSrcKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = udpSrcConvertor.convert(matchEntry, MatchPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(Integer.valueOf(2), ((NxAugMatchRpcGetFlowStats)extensionAugment3.getAugmentationObject()).getNxmOfUdpSrc().getMask());
        Assert.assertEquals(DEFAULT_PORT, ((NxAugMatchRpcGetFlowStats)extensionAugment3.getAugmentationObject()).getNxmOfUdpSrc().getPort());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfUdpSrcKey.class);
    }

}
