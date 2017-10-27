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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.eth.src.grouping.EthSrcValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.EthSrcCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.EthSrcCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfEthSrcKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.eth.src.grouping.NxmOfEthSrcBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * Test for {@link EthSrcConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class EthSrcConvertorTest {

    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private static final MacAddress MAC_ADDRESS =  MacAddress.getDefaultInstance("01:23:45:67:89:AB");

    private EthSrcConvertor ethSrcConvertor;

    @Before
    public void setUp() throws Exception {
        final NxmOfEthSrcBuilder nxmOfEthSrcBuilder = new NxmOfEthSrcBuilder()
                .setMacAddress(MAC_ADDRESS);
        final NxAugMatchNodesNodeTableFlowBuilder nxAugMatchNotifUpdateFlowStatsBuilder = new NxAugMatchNodesNodeTableFlowBuilder();
        nxAugMatchNotifUpdateFlowStatsBuilder.setNxmOfEthSrc(nxmOfEthSrcBuilder.build());

        final Augmentation<Extension> extensionAugmentation = nxAugMatchNotifUpdateFlowStatsBuilder.build();
        when(extension.getAugmentation(Matchers.<Class<Augmentation<Extension>>>any())).thenReturn(extensionAugmentation);

        ethSrcConvertor = new EthSrcConvertor();
    }

    @Test
    public void testConvert() throws Exception {
        final MatchEntry matchEntry = ethSrcConvertor.convert(extension);
        Assert.assertEquals(MAC_ADDRESS, ((EthSrcCaseValue)matchEntry.getMatchEntryValue()).getEthSrcValues().getMacAddress());
    }

    @Test
    public void testConvert1() throws Exception {
        final EthSrcValuesBuilder ethSrcValuesBuilder = new EthSrcValuesBuilder()
                .setMacAddress(MAC_ADDRESS);
        final EthSrcCaseValueBuilder ethSrcCaseValueBuilder = new EthSrcCaseValueBuilder()
                .setEthSrcValues(ethSrcValuesBuilder.build());

        final EthSrcCaseValue ethSrcCaseValue = ethSrcCaseValueBuilder.build();

        when(matchEntry.getMatchEntryValue()).thenReturn(ethSrcCaseValue);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = ethSrcConvertor.convert(matchEntry, MatchPath.PACKETRECEIVED_MATCH);
        Assert.assertEquals(MAC_ADDRESS, ((NxAugMatchNotifPacketIn)extensionAugment.getAugmentationObject()).getNxmOfEthSrc().getMacAddress());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfEthSrcKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = ethSrcConvertor.convert(matchEntry, MatchPath.SWITCHFLOWREMOVED_MATCH);
        Assert.assertEquals(MAC_ADDRESS, ((NxAugMatchNotifSwitchFlowRemoved)extensionAugment1.getAugmentationObject()).getNxmOfEthSrc().getMacAddress());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfEthSrcKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = ethSrcConvertor.convert(matchEntry, MatchPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(MAC_ADDRESS, ((NxAugMatchNodesNodeTableFlow)extensionAugment2.getAugmentationObject()).getNxmOfEthSrc().getMacAddress());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfEthSrcKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = ethSrcConvertor.convert(matchEntry, MatchPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(MAC_ADDRESS, ((NxAugMatchRpcGetFlowStats)extensionAugment3.getAugmentationObject()).getNxmOfEthSrc().getMacAddress());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfEthSrcKey.class);
    }
}
