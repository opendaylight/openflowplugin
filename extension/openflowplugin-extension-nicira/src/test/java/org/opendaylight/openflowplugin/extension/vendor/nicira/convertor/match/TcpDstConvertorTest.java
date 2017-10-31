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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.tcp.dst.grouping.TcpDstValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TcpDstCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TcpDstCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfTcpDstKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.tcp.dst.grouping.NxmOfTcpDstBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * Test for {@link TcpDstConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TcpDstConvertorTest {
    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private static final PortNumber DEFAULT_PORT = new PortNumber(9999);

    private TcpDstConvertor tcpDstConvertor;

    @Before
    public void setUp() throws Exception {
        final NxmOfTcpDstBuilder nxmOfTcpDstBuilder = new NxmOfTcpDstBuilder()
                .setMask(1)
                .setPort(DEFAULT_PORT);
        final NxAugMatchNodesNodeTableFlowBuilder nxAugMatchNotifUpdateFlowStatsBuilder = new NxAugMatchNodesNodeTableFlowBuilder();
        nxAugMatchNotifUpdateFlowStatsBuilder.setNxmOfTcpDst(nxmOfTcpDstBuilder.build());

        final Augmentation<Extension> extensionAugmentation = nxAugMatchNotifUpdateFlowStatsBuilder.build();
        when(extension.getAugmentation(Matchers.<Class<Augmentation<Extension>>>any())).thenReturn(extensionAugmentation);

        tcpDstConvertor = new TcpDstConvertor();
    }

    @Test
    public void testConvert() throws Exception {
        final MatchEntry matchEntry = tcpDstConvertor.convert(extension);
        Assert.assertEquals(1, ((TcpDstCaseValue)matchEntry.getMatchEntryValue()).getTcpDstValues().getMask().intValue());
        Assert.assertEquals(DEFAULT_PORT, ((TcpDstCaseValue)matchEntry.getMatchEntryValue()).getTcpDstValues().getPort());
    }

    @Test
    public void testConvert1() throws Exception {
        final TcpDstValuesBuilder tcpDstValuesBuilder = new TcpDstValuesBuilder()
                .setMask(2)
                .setPort(DEFAULT_PORT);
        final TcpDstCaseValueBuilder tcpDstCaseValueBuilder = new TcpDstCaseValueBuilder()
                .setTcpDstValues(tcpDstValuesBuilder.build());

        final TcpDstCaseValue tcpDstCaseValue = tcpDstCaseValueBuilder.build();

        when(matchEntry.getMatchEntryValue()).thenReturn(tcpDstCaseValue);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = tcpDstConvertor.convert(matchEntry, MatchPath.PACKETRECEIVED_MATCH);
        Assert.assertEquals(2, ((NxAugMatchNotifPacketIn)extensionAugment.getAugmentationObject()).getNxmOfTcpDst().getMask().intValue());
        Assert.assertEquals(DEFAULT_PORT, ((NxAugMatchNotifPacketIn)extensionAugment.getAugmentationObject()).getNxmOfTcpDst().getPort());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfTcpDstKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = tcpDstConvertor.convert(matchEntry, MatchPath.SWITCHFLOWREMOVED_MATCH);
        Assert.assertEquals(2, ((NxAugMatchNotifSwitchFlowRemoved)extensionAugment1.getAugmentationObject()).getNxmOfTcpDst().getMask().intValue());
        Assert.assertEquals(DEFAULT_PORT, ((NxAugMatchNotifSwitchFlowRemoved)extensionAugment1.getAugmentationObject()).getNxmOfTcpDst().getPort());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfTcpDstKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = tcpDstConvertor.convert(matchEntry, MatchPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(2, ((NxAugMatchNodesNodeTableFlow)extensionAugment2.getAugmentationObject()).getNxmOfTcpDst().getMask().intValue());
        Assert.assertEquals(DEFAULT_PORT, ((NxAugMatchNodesNodeTableFlow)extensionAugment2.getAugmentationObject()).getNxmOfTcpDst().getPort());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfTcpDstKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = tcpDstConvertor.convert(matchEntry, MatchPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(2, ((NxAugMatchRpcGetFlowStats)extensionAugment3.getAugmentationObject()).getNxmOfTcpDst().getMask().intValue());
        Assert.assertEquals(DEFAULT_PORT, ((NxAugMatchRpcGetFlowStats)extensionAugment3.getAugmentationObject()).getNxmOfTcpDst().getPort());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfTcpDstKey.class);
    }
}
