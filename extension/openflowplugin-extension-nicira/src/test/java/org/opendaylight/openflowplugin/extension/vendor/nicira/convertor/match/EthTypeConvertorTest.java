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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.eth.type.grouping.EthTypeValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.EthTypeCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.EthTypeCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfEthTypeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.eth.type.grouping.NxmOfEthTypeBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * Test for {@link EthTypeConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class EthTypeConvertorTest {

    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private EthTypeConvertor ethTypeConvertor;

    @Before
    public void setUp() throws Exception {

        final NxmOfEthTypeBuilder nxmOfEthTypeBuilder = new NxmOfEthTypeBuilder()
                .setValue(1);
        final NxAugMatchNodesNodeTableFlowBuilder nxAugMatchNotifUpdateFlowStatsBuilder = new NxAugMatchNodesNodeTableFlowBuilder();
        nxAugMatchNotifUpdateFlowStatsBuilder.setNxmOfEthType(nxmOfEthTypeBuilder.build());

        final Augmentation<Extension> extensionAugmentation = nxAugMatchNotifUpdateFlowStatsBuilder.build();
        when(extension.getAugmentation(Matchers.<Class<Augmentation<Extension>>>any())).thenReturn(extensionAugmentation);

        ethTypeConvertor = new EthTypeConvertor();

    }

    @Test
    public void testConvert() throws Exception {
        final MatchEntry matchEntry = ethTypeConvertor.convert(extension);
        Assert.assertEquals(1, ((EthTypeCaseValue)matchEntry.getMatchEntryValue()).getEthTypeValues().getValue().intValue());
    }

    @Test
    public void testConvert1() throws Exception {
        final EthTypeValuesBuilder ethTypeValuesBuilder = new EthTypeValuesBuilder()
                .setValue(Integer.valueOf(1));
        final EthTypeCaseValueBuilder ethTypeCaseValueBuilder = new EthTypeCaseValueBuilder()
                .setEthTypeValues(ethTypeValuesBuilder.build());

        final EthTypeCaseValue ethTypeCaseValue = ethTypeCaseValueBuilder.build();

        when(matchEntry.getMatchEntryValue()).thenReturn(ethTypeCaseValue);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = ethTypeConvertor.convert(matchEntry, MatchPath.PACKETRECEIVED_MATCH);
        Assert.assertEquals(1, ((NxAugMatchNotifPacketIn)extensionAugment.getAugmentationObject()).getNxmOfEthType().getValue().intValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfEthTypeKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = ethTypeConvertor.convert(matchEntry, MatchPath.SWITCHFLOWREMOVED_MATCH);
        Assert.assertEquals(1, ((NxAugMatchNotifSwitchFlowRemoved)extensionAugment1.getAugmentationObject()).getNxmOfEthType().getValue().intValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfEthTypeKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = ethTypeConvertor.convert(matchEntry, MatchPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(1, ((NxAugMatchNodesNodeTableFlow)extensionAugment2.getAugmentationObject()).getNxmOfEthType().getValue().intValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfEthTypeKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = ethTypeConvertor.convert(matchEntry, MatchPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(1, ((NxAugMatchRpcGetFlowStats)extensionAugment3.getAugmentationObject()).getNxmOfEthType().getValue().intValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfEthTypeKey.class);
    }
}
