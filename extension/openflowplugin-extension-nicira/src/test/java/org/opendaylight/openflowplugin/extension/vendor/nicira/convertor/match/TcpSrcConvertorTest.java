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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.tcp.src.grouping.TcpSrcValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TcpSrcCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TcpSrcCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfTcpSrcKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.tcp.src.grouping.NxmOfTcpSrcBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * Test for {@link TcpSrcConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TcpSrcConvertorTest {
    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private static final PortNumber DEFAULT_PORT = new PortNumber(9999);

    private TcpSrcConvertor tcpSrcConvertor;

    @Before
    public void setUp() throws Exception {
        final NxmOfTcpSrcBuilder nxmOfTcpSrcBuilder = new NxmOfTcpSrcBuilder()
                .setMask(1)
                .setPort(DEFAULT_PORT);
        final NxAugMatchNodesNodeTableFlowBuilder nxAugMatchNotifUpdateFlowStatsBuilder = new NxAugMatchNodesNodeTableFlowBuilder();
        nxAugMatchNotifUpdateFlowStatsBuilder.setNxmOfTcpSrc(nxmOfTcpSrcBuilder.build());

        final Augmentation<Extension> extensionAugmentation = nxAugMatchNotifUpdateFlowStatsBuilder.build();
        when(extension.getAugmentation(Matchers.<Class<Augmentation<Extension>>>any())).thenReturn(extensionAugmentation);

        tcpSrcConvertor = new TcpSrcConvertor();
    }

    @Test
    public void testConvert() throws Exception {
        final MatchEntry matchEntry = tcpSrcConvertor.convert(extension);
        Assert.assertEquals(1, ((TcpSrcCaseValue)matchEntry.getMatchEntryValue()).getTcpSrcValues().getMask().intValue());
        Assert.assertEquals(DEFAULT_PORT, ((TcpSrcCaseValue)matchEntry.getMatchEntryValue()).getTcpSrcValues().getPort());
    }

    @Test
    public void testConvert1() throws Exception {
        final TcpSrcValuesBuilder tcpSrcValuesBuilder = new TcpSrcValuesBuilder()
                .setMask(2)
                .setPort(DEFAULT_PORT);
        final TcpSrcCaseValueBuilder tcpSrcCaseValueBuilder = new TcpSrcCaseValueBuilder()
                .setTcpSrcValues(tcpSrcValuesBuilder.build());

        final TcpSrcCaseValue tcpSrcCaseValue = tcpSrcCaseValueBuilder.build();

        when(matchEntry.getMatchEntryValue()).thenReturn(tcpSrcCaseValue);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = tcpSrcConvertor.convert(matchEntry, MatchPath.PACKETRECEIVED_MATCH);
        Assert.assertEquals(2, ((NxAugMatchNotifPacketIn)extensionAugment.getAugmentationObject()).getNxmOfTcpSrc().getMask().intValue());
        Assert.assertEquals(DEFAULT_PORT, ((NxAugMatchNotifPacketIn)extensionAugment.getAugmentationObject()).getNxmOfTcpSrc().getPort());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfTcpSrcKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = tcpSrcConvertor.convert(matchEntry, MatchPath.SWITCHFLOWREMOVED_MATCH);
        Assert.assertEquals(2, ((NxAugMatchNotifSwitchFlowRemoved)extensionAugment1.getAugmentationObject()).getNxmOfTcpSrc().getMask().intValue());
        Assert.assertEquals(DEFAULT_PORT, ((NxAugMatchNotifSwitchFlowRemoved)extensionAugment1.getAugmentationObject()).getNxmOfTcpSrc().getPort());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfTcpSrcKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = tcpSrcConvertor.convert(matchEntry, MatchPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(2, ((NxAugMatchNodesNodeTableFlow)extensionAugment2.getAugmentationObject()).getNxmOfTcpSrc().getMask().intValue());
        Assert.assertEquals(DEFAULT_PORT, ((NxAugMatchNodesNodeTableFlow)extensionAugment2.getAugmentationObject()).getNxmOfTcpSrc().getPort());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfTcpSrcKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = tcpSrcConvertor.convert(matchEntry, MatchPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(2, ((NxAugMatchRpcGetFlowStats)extensionAugment3.getAugmentationObject()).getNxmOfTcpSrc().getMask().intValue());
        Assert.assertEquals(DEFAULT_PORT, ((NxAugMatchRpcGetFlowStats)extensionAugment3.getAugmentationObject()).getNxmOfTcpSrc().getPort());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfTcpSrcKey.class);
    }
}
