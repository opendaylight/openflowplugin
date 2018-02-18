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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.eth.dst.grouping.EthDstValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.EthDstCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.EthDstCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfEthDstKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.eth.dst.grouping.NxmOfEthDstBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * Test for {@link EthDstConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class EthDstConvertorTest {

    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private static final MacAddress MAC_ADDRESS =  MacAddress.getDefaultInstance("01:23:45:67:89:AB");

    private EthDstConvertor ethDstConvertor;

    @Before
    public void setUp() throws Exception {

        final NxmOfEthDstBuilder nxmOfEthDstBuilder = new NxmOfEthDstBuilder()
                .setMacAddress(MAC_ADDRESS);
        final NxAugMatchNodesNodeTableFlowBuilder nxAugMatchNotifUpdateFlowStatsBuilder = new NxAugMatchNodesNodeTableFlowBuilder();
        nxAugMatchNotifUpdateFlowStatsBuilder.setNxmOfEthDst(nxmOfEthDstBuilder.build());

        final Augmentation<Extension> extensionAugmentation = nxAugMatchNotifUpdateFlowStatsBuilder.build();
        when(extension.getAugmentation(Matchers.<Class<Augmentation<Extension>>>any())).thenReturn(extensionAugmentation);

        ethDstConvertor = new EthDstConvertor();

    }

    @Test
    public void testConvert() throws Exception {
        final MatchEntry matchEntry = ethDstConvertor.convert(extension);
        Assert.assertEquals(MAC_ADDRESS, ((EthDstCaseValue)matchEntry.getMatchEntryValue()).getEthDstValues().getMacAddress());
    }

    @Test
    public void testConvert1() throws Exception {
        final EthDstValuesBuilder ethDstValuesBuilder = new EthDstValuesBuilder()
                .setMacAddress(MAC_ADDRESS);
        final EthDstCaseValueBuilder ethDstCaseValueBuilder = new EthDstCaseValueBuilder()
                .setEthDstValues(ethDstValuesBuilder.build());

        final EthDstCaseValue ethDstCaseValue = ethDstCaseValueBuilder.build();

        when(matchEntry.getMatchEntryValue()).thenReturn(ethDstCaseValue);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = ethDstConvertor.convert(matchEntry, MatchPath.PACKETRECEIVED_MATCH);
        Assert.assertEquals(MAC_ADDRESS, ((NxAugMatchNotifPacketIn)extensionAugment.getAugmentationObject()).getNxmOfEthDst().getMacAddress());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfEthDstKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = ethDstConvertor.convert(matchEntry, MatchPath.SWITCHFLOWREMOVED_MATCH);
        Assert.assertEquals(MAC_ADDRESS, ((NxAugMatchNotifSwitchFlowRemoved)extensionAugment1.getAugmentationObject()).getNxmOfEthDst().getMacAddress());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfEthDstKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = ethDstConvertor.convert(matchEntry, MatchPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(MAC_ADDRESS, ((NxAugMatchNodesNodeTableFlow)extensionAugment2.getAugmentationObject()).getNxmOfEthDst().getMacAddress());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfEthDstKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = ethDstConvertor.convert(matchEntry, MatchPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(MAC_ADDRESS, ((NxAugMatchRpcGetFlowStats)extensionAugment3.getAugmentationObject()).getNxmOfEthDst().getMacAddress());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfEthDstKey.class);



    }
}
